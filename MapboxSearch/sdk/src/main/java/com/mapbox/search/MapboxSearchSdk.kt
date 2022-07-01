package com.mapbox.search

import android.app.Application
import com.mapbox.search.analytics.AnalyticsEventJsonParser
import com.mapbox.search.analytics.AnalyticsServiceImpl
import com.mapbox.search.analytics.CrashEventsFactory
import com.mapbox.search.analytics.InternalAnalyticsService
import com.mapbox.search.analytics.SearchEventsService
import com.mapbox.search.analytics.SearchFeedbackEventsFactory
import com.mapbox.search.common.BuildConfig
import com.mapbox.search.common.concurrent.CommonMainThreadChecker
import com.mapbox.search.common.logger.logd
import com.mapbox.search.core.CoreEngineOptions
import com.mapbox.search.core.CoreSearchEngine
import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.record.DataProviderEngineRegistrationServiceImpl
import com.mapbox.search.record.FavoritesDataProviderImpl
import com.mapbox.search.record.HistoryDataProviderImpl
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.RecordsFileStorage
import com.mapbox.search.result.SearchResultFactory
import com.mapbox.search.utils.AndroidKeyboardLocaleProvider
import com.mapbox.search.utils.AppInfoProviderImpl
import com.mapbox.search.utils.CompoundCompletionCallback
import com.mapbox.search.utils.FormattedTimeProvider
import com.mapbox.search.utils.FormattedTimeProviderImpl
import com.mapbox.search.utils.KeyboardLocaleProvider
import com.mapbox.search.utils.LocalTimeProvider
import com.mapbox.search.utils.TimeProvider
import com.mapbox.search.utils.UUIDProvider
import com.mapbox.search.utils.UUIDProviderImpl
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.utils.file.InternalFileSystem
import com.mapbox.search.utils.loader.DataLoader
import com.mapbox.search.utils.loader.InternalDataLoader
import com.mapbox.search.utils.orientation.AndroidScreenOrientationProvider
import com.mapbox.search.utils.orientation.ScreenOrientationProvider
import java.util.concurrent.Executor

/**
 * The entry point to initialize Search SDK.
 */
@Suppress("LargeClass")
public object MapboxSearchSdk {

    private val userAgent = if (BuildConfig.DEBUG) {
        "search-sdk-android-internal/${BuildConfig.VERSION_NAME}"
    } else {
        "search-sdk-android/${BuildConfig.VERSION_NAME}"
    }

    private var isInitialized = false

    private lateinit var searchRequestContextProvider: SearchRequestContextProvider
    private lateinit var searchResultFactory: SearchResultFactory
    private lateinit var timeProvider: TimeProvider
    private lateinit var formattedTimeProvider: FormattedTimeProvider
    private lateinit var uuidProvider: UUIDProvider

    private lateinit var indexableDataProvidersRegistry: IndexableDataProvidersRegistryImpl

    @get:JvmSynthetic
    internal lateinit var internalServiceProvider: InternalServiceProvider

    private lateinit var searchEngineSettings: SearchEngineSettings
    private lateinit var offlineSearchEngineSettings: OfflineSearchEngineSettings

    private lateinit var sbsCoreSearchEngine: CoreSearchEngineInterface
    private lateinit var geocodingCoreSearchEngine: CoreSearchEngineInterface

    private lateinit var offlineSearchEngineShared: OfflineSearchEngine

    /**
     * Shared [ServiceProvider] instance.
     *
     * @throws IllegalStateException if [MapboxSearchSdk] is not initialized.
     */
    @JvmStatic
    public val serviceProvider: ServiceProvider
        get() {
            checkInitialized()
            return internalServiceProvider
        }

    /**
     * Experimental API, can be changed or removed in the next SDK releases.
     * Initialize Search SDK.
     *
     * @param [searchEngineSettings] optional [SearchEngine] settings.
     * @param [offlineSearchEngineSettings] optional offline search settings.
     *
     * @throws IllegalStateException if [MapboxSearchSdk] has already been initialized.
     */
    @JvmStatic
    public fun initialize(
        searchEngineSettings: SearchEngineSettings,
        offlineSearchEngineSettings: OfflineSearchEngineSettings,
    ) {
        initializeInternal(
            searchEngineSettings = searchEngineSettings,
            offlineSearchEngineSettings = offlineSearchEngineSettings,
            allowReinitialization = false
        )
    }

    internal fun reinitializeInternal(
        application: Application,
        timeProvider: TimeProvider = LocalTimeProvider(),
        formattedTimeProvider: FormattedTimeProvider = FormattedTimeProviderImpl(timeProvider),
        uuidProvider: UUIDProvider = UUIDProviderImpl(),
        keyboardLocaleProvider: KeyboardLocaleProvider = AndroidKeyboardLocaleProvider(application),
        orientationProvider: ScreenOrientationProvider = AndroidScreenOrientationProvider(application),
        dataLoader: DataLoader<ByteArray> = InternalDataLoader(application, InternalFileSystem()),
    ) {
        initializeInternal(
            SearchEngineSettings(application, ""),
            OfflineSearchEngineSettings(application, ""),
            true,
            timeProvider, formattedTimeProvider, uuidProvider, keyboardLocaleProvider, orientationProvider, dataLoader
        )
    }

    @Suppress("LongParameterList")
    internal fun initializeInternal(
        searchEngineSettings: SearchEngineSettings,
        offlineSearchEngineSettings: OfflineSearchEngineSettings,
        allowReinitialization: Boolean = false,
        timeProvider: TimeProvider = LocalTimeProvider(),
        formattedTimeProvider: FormattedTimeProvider = FormattedTimeProviderImpl(timeProvider),
        uuidProvider: UUIDProvider = UUIDProviderImpl(),
        keyboardLocaleProvider: KeyboardLocaleProvider = AndroidKeyboardLocaleProvider(searchEngineSettings.application),
        orientationProvider: ScreenOrientationProvider = AndroidScreenOrientationProvider(searchEngineSettings.application),
        dataLoader: DataLoader<ByteArray> = InternalDataLoader(searchEngineSettings.application, InternalFileSystem()),
    ) {
        check(allowReinitialization || !isInitialized) {
            "Already initialized"
        }

        this.searchEngineSettings = searchEngineSettings
        this.offlineSearchEngineSettings = offlineSearchEngineSettings
        this.timeProvider = timeProvider
        this.formattedTimeProvider = formattedTimeProvider
        this.uuidProvider = uuidProvider

        CommonMainThreadChecker.isOnMainLooper = {
            SearchSdkMainThreadWorker.isMainThread
        }

        geocodingCoreSearchEngine = createCoreEngineByApiType(ApiType.GEOCODING, searchEngineSettings)
        sbsCoreSearchEngine = createCoreEngineByApiType(ApiType.SBS, searchEngineSettings)

        searchRequestContextProvider = SearchRequestContextProvider(
            keyboardLocaleProvider,
            orientationProvider
        )

        indexableDataProvidersRegistry = IndexableDataProvidersRegistryImpl(
            dataProviderEngineRegistrationService = DataProviderEngineRegistrationServiceImpl()
        )

        val historyDataProvider = HistoryDataProviderImpl(
            recordsStorage = RecordsFileStorage.History(dataLoader),
            timeProvider = timeProvider,
        )

        val favoritesDataProvider = FavoritesDataProviderImpl(
            recordsStorage = RecordsFileStorage.Favorite(dataLoader),
        )

        internalServiceProvider = ServiceProviderImpl(
            historyDataProvider = historyDataProvider,
            favoritesDataProvider = favoritesDataProvider,
        )

        searchResultFactory = SearchResultFactory(indexableDataProvidersRegistry)

        registerDefaultDataProviders()

        isInitialized = true

        offlineSearchEngineShared = createOfflineSearchEngine(offlineSearchEngineSettings, sbsCoreSearchEngine)
    }

    private fun createAnalyticsService(
        application: Application,
        accessToken: String,
        coreSearchEngine: CoreSearchEngineInterface
    ): AnalyticsServiceImpl {
        val eventJsonParser = AnalyticsEventJsonParser()

        val searchFeedbackEventsFactory = SearchFeedbackEventsFactory(
            providedUserAgent = userAgent,
            viewportProvider = searchEngineSettings.viewportProvider,
            uuidProvider = uuidProvider,
            coreSearchEngine = coreSearchEngine,
            eventJsonParser = eventJsonParser,
            formattedTimeProvider = formattedTimeProvider,
        )

        val crashEventsFactory = CrashEventsFactory(
            timeProvider = timeProvider,
            appInfoProvider = AppInfoProviderImpl(
                context = application,
                searchSdkPackageName = com.mapbox.search.BuildConfig.LIBRARY_PACKAGE_NAME,
                searchSdkVersionName = BuildConfig.VERSION_NAME
            )
        )

        return AnalyticsServiceImpl(
            context = application,
            eventsService = SearchEventsService(accessToken, userAgent),
            eventsJsonParser = eventJsonParser,
            feedbackEventsFactory = searchFeedbackEventsFactory,
            crashEventsFactory = crashEventsFactory,
            locationEngine = searchEngineSettings.locationEngine,
        )
    }

    private fun registerDefaultDataProviders() {
        class DataProviderInitializationCallback(
            private val apiType: ApiType,
            private val provider: IndexableDataProvider<*>,
        ) : CompletionCallback<Unit> {
            override fun onComplete(result: Unit) {
                logd("Data provider ${provider.dataProviderName} was registered for $apiType")
            }

            override fun onError(e: Exception) {
                logd("Unable to register ${provider.dataProviderName} data provider for $apiType: ${e.message}")
            }
        }

        val historyDataProvider = internalServiceProvider.historyDataProvider()
        val favoritesDataProvider = internalServiceProvider.favoritesDataProvider()

        indexableDataProvidersRegistry.register(
            dataProvider = historyDataProvider,
            searchEngine = sbsCoreSearchEngine,
            executor = SearchSdkMainThreadWorker.mainExecutor,
            callback = DataProviderInitializationCallback(ApiType.SBS, historyDataProvider)
        )

        indexableDataProvidersRegistry.register(
            dataProvider = historyDataProvider,
            searchEngine = geocodingCoreSearchEngine,
            executor = SearchSdkMainThreadWorker.mainExecutor,
            callback = DataProviderInitializationCallback(ApiType.GEOCODING, historyDataProvider)
        )

        indexableDataProvidersRegistry.register(
            dataProvider = favoritesDataProvider,
            searchEngine = sbsCoreSearchEngine,
            executor = SearchSdkMainThreadWorker.mainExecutor,
            callback = DataProviderInitializationCallback(ApiType.SBS, favoritesDataProvider)
        )

        indexableDataProvidersRegistry.register(
            dataProvider = favoritesDataProvider,
            searchEngine = geocodingCoreSearchEngine,
            executor = SearchSdkMainThreadWorker.mainExecutor,
            callback = DataProviderInitializationCallback(ApiType.GEOCODING, favoritesDataProvider)
        )
    }

    /**
     * Creates a new instance of the [SearchEngine].
     * A new instance doesn't have any [IndexableDataProvider] registered by default.
     *
     * @param settings [SearchEngine] settings.
     *
     * @return a new instance instance of [SearchEngine].
     */
    @JvmStatic
    public fun createSearchEngine(settings: SearchEngineSettings): SearchEngine {
        return createSearchEngine(getDefaultApiType(), settings)
    }

    /**
     * Creates a new instance of the [SearchEngine] with
     * [com.mapbox.search.record.HistoryDataProvider] and [com.mapbox.search.record.FavoritesDataProvider]
     * registered by default.
     *
     * @param settings [SearchEngine] settings.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle result.
     *
     * @return a new instance of [SearchEngine].
     * @see createSearchEngine
     */
    @JvmOverloads
    @JvmStatic
    public fun createSearchEngineWithBuiltInDataProviders(
        settings: SearchEngineSettings,
        executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
        callback: CompletionCallback<Unit> = StubCompletionCallback()
    ): SearchEngine {
        return createSearchEngineWithBuiltInDataProviders(getDefaultApiType(), settings, null, executor, callback)
    }

    internal fun createSearchEngineWithBuiltInDataProviders(
        apiType: ApiType,
        settings: SearchEngineSettings,
        analyticsService: InternalAnalyticsService? = null,
        executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
        callback: CompletionCallback<Unit> = StubCompletionCallback()
    ): SearchEngine {
        val coreEngine = createCoreEngineByApiType(apiType, settings)

        val searchEngine = with(settings) {
            createSearchEngine(
                apiType,
                settings,
                coreEngine,
                analyticsService ?: createAnalyticsService(application, accessToken, coreEngine)
            )
        }

        val compoundCallback = CompoundCompletionCallback(2, executor, callback)
        compoundCallback.addInnerTask(
            searchEngine.registerDataProvider(serviceProvider.historyDataProvider(), executor, compoundCallback)
        )
        compoundCallback.addInnerTask(
            searchEngine.registerDataProvider(serviceProvider.favoritesDataProvider(), executor, compoundCallback)
        )

        return searchEngine
    }

    internal fun createSearchEngine(
        apiType: ApiType,
        settings: SearchEngineSettings,
        coreEngine: CoreSearchEngineInterface = createCoreEngineByApiType(apiType, settings),
        analyticsService: InternalAnalyticsService = createAnalyticsService(settings.application, settings.accessToken, coreEngine),
    ): SearchEngine {
        checkInitialized()

        return SearchEngineImpl(
            apiType,
            settings,
            analyticsService,
            coreEngine,
            internalServiceProvider.historyService(),
            searchRequestContextProvider,
            searchResultFactory,
            indexableDataProvidersRegistry = indexableDataProvidersRegistry
        )
    }

    /**
     * Gets a shared instance of the [OfflineSearchEngine].
     * @return shared instance of the [OfflineSearchEngine].
     * @throws IllegalStateException if [MapboxSearchSdk] is not initialized.
     */
    @JvmStatic
    public fun getOfflineSearchEngine(): OfflineSearchEngine {
        checkInitialized()
        return offlineSearchEngineShared
    }

    private fun createOfflineSearchEngine(
        settings: OfflineSearchEngineSettings,
        coreEngine: CoreSearchEngineInterface
    ): OfflineSearchEngine {
        checkInitialized()
        return with(searchEngineSettings) {
            OfflineSearchEngineImpl(
                settings = settings,
                analyticsService = createAnalyticsService(application, accessToken, coreEngine),
                coreEngine = coreEngine,
                requestContextProvider = searchRequestContextProvider,
                searchResultFactory = searchResultFactory,
                tileStore = offlineSearchEngineSettings.tileStore,
            )
        }
    }

    private fun createCoreEngineByApiType(
        apiType: ApiType,
        searchEngineSettings: SearchEngineSettings
    ): CoreSearchEngineInterface {
        val endpoint = when (apiType) {
            ApiType.GEOCODING -> searchEngineSettings.geocodingEndpointBaseUrl
            ApiType.SBS -> searchEngineSettings.singleBoxSearchBaseUrl
        }

        return CoreSearchEngine(
            // TODO allow customer to customize events url
            CoreEngineOptions(searchEngineSettings.accessToken, endpoint, apiType.mapToCore(), userAgent, null),
            searchEngineSettings.wrapperLocationProvider,
        )
    }

    private fun getDefaultApiType(): ApiType {
        return if (System.getProperty("com.mapbox.mapboxsearch.enableSBS")?.toBoolean() == true) {
            ApiType.SBS
        } else {
            ApiType.GEOCODING
        }
    }

    private fun checkInitialized() {
        check(isInitialized) {
            "Initialize MapboxSearchSdk first"
        }
    }
}
