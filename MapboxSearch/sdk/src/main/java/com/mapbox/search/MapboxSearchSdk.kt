package com.mapbox.search

import android.app.Application
import androidx.annotation.VisibleForTesting
import com.mapbox.bindgen.Value
import com.mapbox.common.TileDataDomain
import com.mapbox.common.TileStore
import com.mapbox.common.TileStoreOptions
import com.mapbox.search.analytics.AnalyticsEventJsonParser
import com.mapbox.search.analytics.AnalyticsServiceImpl
import com.mapbox.search.analytics.CrashEventsFactory
import com.mapbox.search.analytics.InternalAnalyticsService
import com.mapbox.search.analytics.SearchEventsService
import com.mapbox.search.analytics.SearchFeedbackEventsFactory
import com.mapbox.search.common.BuildConfig
import com.mapbox.search.common.logger.logd
import com.mapbox.search.core.CoreEngineOptions
import com.mapbox.search.core.CoreLocationProvider
import com.mapbox.search.core.CoreSearchEngine
import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.location.LocationEngineAdapter
import com.mapbox.search.location.WrapperLocationProvider
import com.mapbox.search.record.DataProviderEngineRegistrationServiceImpl
import com.mapbox.search.record.FavoritesDataProviderImpl
import com.mapbox.search.record.HistoryDataProviderImpl
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.RecordsFileStorage
import com.mapbox.search.result.SearchResultFactory
import com.mapbox.search.utils.AndroidKeyboardLocaleProvider
import com.mapbox.search.utils.AppInfoProviderImpl
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
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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

    // Strong references for bindgen interfaces
    private lateinit var coreLocationProvider: CoreLocationProvider

    private lateinit var searchRequestContextProvider: SearchRequestContextProvider
    private lateinit var searchResultFactory: SearchResultFactory
    private lateinit var timeProvider: TimeProvider
    private lateinit var formattedTimeProvider: FormattedTimeProvider
    private lateinit var uuidProvider: UUIDProvider

    private lateinit var indexableDataProvidersRegistry: IndexableDataProvidersRegistryImpl

    @get:JvmSynthetic
    internal lateinit var internalServiceProvider: InternalServiceProvider

    private val coreSearchEngines: MutableSet<CoreSearchEngineInterface> = Collections.newSetFromMap(WeakHashMap())
    private val analyticsServices: MutableSet<AnalyticsServiceImpl> = Collections.newSetFromMap(WeakHashMap())

    private lateinit var searchEngineSettings: SearchEngineSettings
    private lateinit var offlineSearchEngineSettings: OfflineSearchEngineSettings

    private lateinit var sbsCoreSearchEngine: CoreSearchEngineInterface
    private lateinit var geocodingCoreSearchEngine: CoreSearchEngineInterface

    private lateinit var sbsSearchEngineShared: SearchEngine
    private lateinit var geocodingSearchEngineShared: SearchEngine
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

    private val SearchEngineSettings.application: Application
        get() = applicationContext.applicationContext as Application

    private val SearchEngineSettings.locationProvider: CoreLocationProvider
        get() = LocationEngineAdapter(application, locationEngine)

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

        val locationProvider = searchEngineSettings.locationProvider

        this.searchEngineSettings = searchEngineSettings
        this.offlineSearchEngineSettings = offlineSearchEngineSettings
        this.timeProvider = timeProvider
        this.formattedTimeProvider = formattedTimeProvider
        this.uuidProvider = uuidProvider

        coreLocationProvider = WrapperLocationProvider(locationProvider, searchEngineSettings.viewportProvider)

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
            locationEngine = searchEngineSettings.locationEngine,
            historyDataProvider = historyDataProvider,
            favoritesDataProvider = favoritesDataProvider,
        )

        searchResultFactory = SearchResultFactory(indexableDataProvidersRegistry)

        registerDefaultDataProviders()

        isInitialized = true

        sbsSearchEngineShared = createSearchEngine(ApiType.SBS, searchEngineSettings, useSharedCoreEngine = true)
        geocodingSearchEngineShared = createSearchEngine(ApiType.GEOCODING, searchEngineSettings, useSharedCoreEngine = true)
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

        val analyticsService = AnalyticsServiceImpl(
            context = application,
            eventsService = SearchEventsService(accessToken, userAgent),
            eventsJsonParser = eventJsonParser,
            feedbackEventsFactory = searchFeedbackEventsFactory,
            crashEventsFactory = crashEventsFactory,
            locationEngine = searchEngineSettings.locationEngine,
        )
        analyticsServices.add(analyticsService)
        return analyticsService
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
     * Change current SDK access token in runtime.
     * @param [accessToken] new access token.
     * @throws IllegalStateException if [MapboxSearchSdk] is not initialized.
     *
     * TODO FIXME remove
     */
    @JvmStatic
    public fun setAccessToken(accessToken: String) {
        checkInitialized()
        //this.accessToken = accessToken
        coreSearchEngines.forEach { it.setAccessToken(accessToken) }
        analyticsServices.forEach { it.setAccessToken(accessToken) }
        offlineSearchEngineSettings.tileStore.setOption(
            TileStoreOptions.MAPBOX_ACCESS_TOKEN,
            TileDataDomain.SEARCH,
            Value.valueOf(accessToken)
        )
    }

    /**
     * Get a shared instance of the [SearchEngine].
     * Shared [SearchEngine] has [com.mapbox.search.record.HistoryDataProvider] and [com.mapbox.search.record.FavoritesDataProvider]
     * registered by default.
     *
     * @return shared instance instance of [SearchEngine].
     * @see createSearchEngine
     * @throws IllegalStateException if [MapboxSearchSdk] is not initialized.
     */
    @JvmStatic
    public fun getSearchEngine(): SearchEngine {
        return when (getDefaultApiType()) {
            ApiType.GEOCODING -> geocodingSearchEngineShared
            ApiType.SBS -> sbsSearchEngineShared
        }
    }

    /**
     * Creates a new instance of the [SearchEngine].
     * Unlike the shared instance of the [SearchEngine] acquired from [getSearchEngine],
     * a new instance doesn't have any [IndexableDataProvider] registered.
     *
     * @param searchEngineSettings Optional [SearchEngine] settings.
     * By default [SearchEngineSettings] passed to [MapboxSearchSdk.initialize] will be used.
     *
     * @return a new instance instance of [SearchEngine].
     * @see [getSearchEngine]
     * @throws IllegalStateException if [MapboxSearchSdk] is not initialized.
     */
    @JvmOverloads
    public fun createSearchEngine(
        searchEngineSettings: SearchEngineSettings = this.searchEngineSettings
    ): SearchEngine {
        return createSearchEngine(getDefaultApiType(), searchEngineSettings, useSharedCoreEngine = false)
    }

    internal fun createSearchEngine(
        apiType: ApiType,
        searchEngineSettings: SearchEngineSettings,
        useSharedCoreEngine: Boolean
    ): SearchEngine {
        checkInitialized()

        val coreEngine = if (useSharedCoreEngine) {
            getSharedCoreEngineByApiType(apiType)
        } else {
            createCoreEngineByApiType(apiType, searchEngineSettings)
        }

        return with(searchEngineSettings) {
            createSearchEngine(apiType, searchEngineSettings, coreEngine, createAnalyticsService(application, accessToken, coreEngine))
        }
    }

    internal fun createSearchEngine(
        apiType: ApiType,
        searchEngineSettings: SearchEngineSettings,
        coreEngine: CoreSearchEngineInterface,
        analyticsService: InternalAnalyticsService,
    ): SearchEngine {
        checkInitialized()

        return SearchEngineImpl(
            apiType,
            searchEngineSettings,
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

    @VisibleForTesting
    internal fun createCoreEngineByApiType(
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
            coreLocationProvider,
        ).apply {
            coreSearchEngines.add(this)
        }
    }

    @VisibleForTesting
    internal fun getSharedCoreEngineByApiType(apiType: ApiType): CoreSearchEngineInterface {
        return when (apiType) {
            ApiType.GEOCODING -> geocodingCoreSearchEngine
            ApiType.SBS -> sbsCoreSearchEngine
        }
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
