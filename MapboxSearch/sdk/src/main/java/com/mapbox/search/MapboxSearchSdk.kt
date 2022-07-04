package com.mapbox.search

import android.app.Application
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.bindgen.Value
import com.mapbox.common.EventsService
import com.mapbox.common.EventsServiceOptions
import com.mapbox.common.TileDataDomain
import com.mapbox.common.TileStoreOptions
import com.mapbox.search.analytics.AnalyticsEventJsonParser
import com.mapbox.search.analytics.AnalyticsServiceImpl
import com.mapbox.search.analytics.CrashEventsFactory
import com.mapbox.search.analytics.InternalAnalyticsService
import com.mapbox.search.analytics.SearchFeedbackEventsFactory
import com.mapbox.search.common.BuildConfig
import com.mapbox.search.common.concurrent.CommonMainThreadChecker
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

    @JvmSynthetic
    internal lateinit var internalServiceProvider: InternalServiceProvider

    /**
     * [ServiceProvider] instance.
     *
     * @throws IllegalStateException if [MapboxSearchSdk] is not initialized.
     */
    @JvmStatic
    public val serviceProvider: ServiceProvider
        get() {
            checkInitialized()
            return internalServiceProvider
        }

    @JvmSynthetic
    internal fun initializeInternal(
        application: Application,
        timeProvider: TimeProvider = LocalTimeProvider(),
        formattedTimeProvider: FormattedTimeProvider = FormattedTimeProviderImpl(timeProvider),
        uuidProvider: UUIDProvider = UUIDProviderImpl(),
        keyboardLocaleProvider: KeyboardLocaleProvider = AndroidKeyboardLocaleProvider(application),
        orientationProvider: ScreenOrientationProvider = AndroidScreenOrientationProvider(application),
        dataLoader: DataLoader<ByteArray> = InternalDataLoader(application, InternalFileSystem()),
    ) {
        this.timeProvider = timeProvider
        this.formattedTimeProvider = formattedTimeProvider
        this.uuidProvider = uuidProvider

        CommonMainThreadChecker.isOnMainLooper = {
            SearchSdkMainThreadWorker.isMainThread
        }

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

        isInitialized = true
    }

    private fun createAnalyticsService(
        settings: OfflineSearchEngineSettings,
        coreSearchEngine: CoreSearchEngineInterface,
    ) = createAnalyticsService(
        application = settings.application,
        accessToken = settings.accessToken,
        coreSearchEngine = coreSearchEngine,
        locationEngine = settings.locationEngine,
        viewportProvider = settings.viewportProvider
    )

    private fun createAnalyticsService(
        settings: SearchEngineSettings,
        coreSearchEngine: CoreSearchEngineInterface,
    ) = createAnalyticsService(
        application = settings.application,
        accessToken = settings.accessToken,
        coreSearchEngine = coreSearchEngine,
        locationEngine = settings.locationEngine,
        viewportProvider = settings.viewportProvider
    )

    private fun createAnalyticsService(
        application: Application,
        accessToken: String,
        coreSearchEngine: CoreSearchEngineInterface,
        locationEngine: LocationEngine,
        viewportProvider: ViewportProvider?,
    ): AnalyticsServiceImpl {
        val eventJsonParser = AnalyticsEventJsonParser()

        val searchFeedbackEventsFactory = SearchFeedbackEventsFactory(
            providedUserAgent = userAgent,
            viewportProvider = viewportProvider,
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

        val eventsService = EventsService(EventsServiceOptions(accessToken, userAgent, null))

        return AnalyticsServiceImpl(
            context = application,
            eventsService = eventsService,
            eventsJsonParser = eventJsonParser,
            feedbackEventsFactory = searchFeedbackEventsFactory,
            crashEventsFactory = crashEventsFactory,
            locationEngine = locationEngine,
        )
    }

//    TODO preregister history and favorite data providers so that consecutive calls will be faster
//    private fun registerDefaultDataProviders() {
//        indexableDataProvidersRegistry.register(historyDataProvider)
//        indexableDataProvidersRegistry.register(favoritesDataProvider)
//    }

    /**
     * Creates a new instance of the [SearchEngine].
     * A new instance doesn't have any [IndexableDataProvider] registered by default.
     *
     * @param settings [SearchEngine] settings.
     *
     * @return a new instance instance of [SearchEngine].
     * @see createSearchEngineWithBuiltInDataProviders
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

        val searchEngine = createSearchEngine(
            apiType,
            settings,
            coreEngine,
            analyticsService ?: createAnalyticsService(settings, coreEngine)
        )

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
        analyticsService: InternalAnalyticsService = createAnalyticsService(settings, coreEngine),
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
     * Creates a new instance of [OfflineSearchEngine].
     *
     * @param settings [OfflineSearchEngine] settings.
     *
     * @return a new instance of [OfflineSearchEngine].
     */
    @JvmStatic
    public fun createOfflineSearchEngine(settings: OfflineSearchEngineSettings): OfflineSearchEngine {
        checkInitialized()

        with(settings) {
            tileStore.setOption(
                TileStoreOptions.MAPBOX_APIURL,
                TileDataDomain.SEARCH,
                Value.valueOf(tilesBaseUri.toString())
            )

            tileStore.setOption(
                TileStoreOptions.MAPBOX_ACCESS_TOKEN,
                TileDataDomain.SEARCH,
                Value.valueOf(accessToken)
            )
        }

        val coreEngine = CoreSearchEngine(
            CoreEngineOptions(
                settings.accessToken,
                SearchEngineSettings.DEFAULT_ENDPOINT_GEOCODING,
                ApiType.SBS.mapToCore(),
                userAgent,
                null
            ),
            settings.wrapperLocationProvider,
        )

        return OfflineSearchEngineImpl(
            analyticsService = createAnalyticsService(settings, coreEngine),
            settings = settings,
            coreEngine = coreEngine,
            requestContextProvider = searchRequestContextProvider,
            searchResultFactory = searchResultFactory,
        )
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
