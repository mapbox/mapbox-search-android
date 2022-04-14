package com.mapbox.search

import android.Manifest
import android.app.Application
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.annotation.module.MapboxModuleType
import com.mapbox.bindgen.Value
import com.mapbox.common.TileDataDomain
import com.mapbox.common.TileStore
import com.mapbox.common.TileStoreOptions
import com.mapbox.common.module.LibraryLoader
import com.mapbox.common.module.provider.MapboxModuleProvider
import com.mapbox.common.module.provider.ModuleProviderArgument
import com.mapbox.search.analytics.ErrorsReporter
import com.mapbox.search.analytics.TelemetryService
import com.mapbox.search.common.BuildConfig
import com.mapbox.search.common.CommonErrorsReporter
import com.mapbox.search.common.concurrent.CommonMainThreadChecker
import com.mapbox.search.common.logger.logd
import com.mapbox.search.common.logger.searchSdkLogger
import com.mapbox.search.core.CoreEngineOptions
import com.mapbox.search.core.CoreLocationProvider
import com.mapbox.search.core.CoreSearchEngine
import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.core.PlatformClientImpl
import com.mapbox.search.core.http.AsyncHttpCallbackDecorator
import com.mapbox.search.core.http.HttpClientImpl
import com.mapbox.search.core.http.HttpErrorsCache
import com.mapbox.search.core.http.HttpErrorsCacheImpl
import com.mapbox.search.core.http.OkHttpHelper
import com.mapbox.search.core.http.UserAgentProviderImpl
import com.mapbox.search.internal.bindgen.PlatformClient
import com.mapbox.search.location.LocationEngineAdapter
import com.mapbox.search.location.WrapperLocationProvider
import com.mapbox.search.record.DataProviderEngineRegistrationServiceImpl
import com.mapbox.search.record.FavoritesDataProviderImpl
import com.mapbox.search.record.HistoryDataProviderImpl
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.RecordsFileStorage
import com.mapbox.search.result.SearchResultFactory
import com.mapbox.search.utils.AndroidKeyboardLocaleProvider
import com.mapbox.search.utils.FormattedTimeProvider
import com.mapbox.search.utils.FormattedTimeProviderImpl
import com.mapbox.search.utils.KeyboardLocaleProvider
import com.mapbox.search.utils.LocalTimeProvider
import com.mapbox.search.utils.SyncLocker
import com.mapbox.search.utils.SyncLockerImpl
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

    private const val SEARCH_SDK_NATIVE_LIBRARY_NAME = "SearchCore"

    private val userAgent = if (BuildConfig.DEBUG) {
        "search-sdk-android-internal/${BuildConfig.VERSION_NAME}"
    } else {
        "search-sdk-android/${BuildConfig.VERSION_NAME}"
    }
    private val globalDataProvidersLock: SyncLocker = SyncLockerImpl()

    private var isInitialized = false

    // Strong references for bindgen interfaces
    private lateinit var platformClient: PlatformClient
    private lateinit var coreLocationProvider: CoreLocationProvider

    private lateinit var searchRequestContextProvider: SearchRequestContextProvider

    private lateinit var httpErrorsCache: HttpErrorsCache
    private lateinit var searchResultFactory: SearchResultFactory

    private lateinit var tileStore: TileStore

    private lateinit var searchEnginesExecutor: ExecutorService
    private lateinit var offlineSearchEngineExecutor: ExecutorService
    private lateinit var globalDataProvidersRegistryExecutor: ExecutorService

    private lateinit var indexableDataProvidersRegistry: IndexableDataProvidersRegistryImpl

    @get:JvmSynthetic
    internal lateinit var internalServiceProvider: InternalServiceProvider

    private val coreSearchEngines: MutableSet<CoreSearchEngineInterface> = Collections.newSetFromMap(WeakHashMap())
    private lateinit var accessToken: String
    private lateinit var searchSdkSettings: SearchSdkSettings
    private lateinit var searchEngineSettings: SearchEngineSettings

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
     * @param [application] android application instance.
     * @param [accessToken] mapbox access token.
     * @param [locationEngine] optional location engine instance.
     * Default [LocationEngine] is retrieved from [LocationEngineProvider.getBestLocationEngine]. Note that this class requires
     * [Manifest.permission.ACCESS_COARSE_LOCATION] or [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
     * @param [viewportProvider] optional viewport provider.
     * @param [searchSdkSettings] optional Search SDK settings.
     * @param [searchEngineSettings] optional [SearchEngine] settings.
     * @param [offlineSearchEngineSettings] optional offline search settings.
     *
     * @throws IllegalStateException if [MapboxSearchSdk] has already been initialized.
     */
    @JvmOverloads
    @JvmStatic
    public fun initialize(
        application: Application,
        accessToken: String,
        locationEngine: LocationEngine = LocationEngineProvider.getBestLocationEngine(application),
        viewportProvider: ViewportProvider? = null,
        searchEngineSettings: SearchEngineSettings = SearchEngineSettings(),
        searchSdkSettings: SearchSdkSettings = SearchSdkSettings(),
        offlineSearchEngineSettings: OfflineSearchEngineSettings = OfflineSearchEngineSettings(),
    ) {
        initializeInternal(
            application = application,
            accessToken = accessToken,
            locationEngine = locationEngine,
            viewportProvider = viewportProvider,
            searchSdkSettings = searchSdkSettings,
            searchEngineSettings = searchEngineSettings,
            offlineSearchEngineSettings = offlineSearchEngineSettings,
            allowReinitialization = false
        )
    }

    @Suppress("LongParameterList")
    internal fun initializeInternal(
        application: Application,
        accessToken: String,
        locationEngine: LocationEngine,
        viewportProvider: ViewportProvider? = null,
        searchSdkSettings: SearchSdkSettings = SearchSdkSettings(),
        searchEngineSettings: SearchEngineSettings = SearchEngineSettings(),
        offlineSearchEngineSettings: OfflineSearchEngineSettings = OfflineSearchEngineSettings(),
        allowReinitialization: Boolean = false,
        timeProvider: TimeProvider = LocalTimeProvider(),
        formattedTimeProvider: FormattedTimeProvider = FormattedTimeProviderImpl(timeProvider),
        uuidProvider: UUIDProvider = UUIDProviderImpl(),
        keyboardLocaleProvider: KeyboardLocaleProvider = AndroidKeyboardLocaleProvider(application),
        orientationProvider: ScreenOrientationProvider = AndroidScreenOrientationProvider(application),
        errorsReporter: ErrorsReporter? = null,
        dataLoader: DataLoader<ByteArray> = InternalDataLoader(application, InternalFileSystem()),
    ) {
        check(allowReinitialization || !isInitialized) {
            "Already initialized"
        }

        val locationProvider = LocationEngineAdapter(application, locationEngine)

        this.accessToken = accessToken
        this.searchSdkSettings = searchSdkSettings
        this.searchEngineSettings = searchEngineSettings

        CommonMainThreadChecker.isOnMainLooper = {
            SearchSdkMainThreadWorker.isMainThread
        }

        searchSdkLogger = MapboxModuleProvider.createModule(
            MapboxModuleType.CommonLogger, ::mapboxModuleParamsProvider
        )

        MapboxModuleProvider.createModule<LibraryLoader>(
            MapboxModuleType.CommonLibraryLoader, ::mapboxModuleParamsProvider
        ).run {
            load(SEARCH_SDK_NATIVE_LIBRARY_NAME)
        }

        val analyticsService = TelemetryService(
            context = application,
            accessToken = accessToken,
            userAgent = userAgent,
            locationEngine = locationEngine,
            viewportProvider = viewportProvider,
            uuidProvider = uuidProvider,
            coreEngineProvider = ::getCoreEngineByApiType,
            formattedTimeProvider = formattedTimeProvider,
        )

        httpErrorsCache = HttpErrorsCacheImpl()

        val debugLogsEnabled = System.getProperty("com.mapbox.mapboxsearch.enableDebugLogs")?.toBoolean() == true

        val httpClient = HttpClientImpl(
            client = OkHttpHelper(debugLogsEnabled).getClient(),
            errorsCache = httpErrorsCache,
            uuidProvider = uuidProvider,
            userAgentProvider = UserAgentProviderImpl(application)
        )

        searchEnginesExecutor = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "SearchEngine executor")
        }
        offlineSearchEngineExecutor = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "OfflineSearchEngine executor")
        }
        globalDataProvidersRegistryExecutor = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "Global DataProviderRegistry executor")
        }

        platformClient = PlatformClientImpl(
            httpClient = httpClient,
            analyticsService = analyticsService,
            uuidProvider = uuidProvider,
            callbackDecorator = {
                AsyncHttpCallbackDecorator(
                    executor = searchEnginesExecutor,
                    syncLocker = globalDataProvidersLock,
                    originalCallback = it,
                )
            },
        )

        coreLocationProvider = WrapperLocationProvider(locationProvider, viewportProvider)

        geocodingCoreSearchEngine = createCoreEngineByApiType(ApiType.GEOCODING, searchEngineSettings)
        sbsCoreSearchEngine = createCoreEngineByApiType(ApiType.SBS, searchEngineSettings)

        searchRequestContextProvider = SearchRequestContextProvider(
            keyboardLocaleProvider,
            orientationProvider
        )

        indexableDataProvidersRegistry = IndexableDataProvidersRegistryImpl(
            syncLocker = globalDataProvidersLock,
            dataProviderEngineRegistrationService = DataProviderEngineRegistrationServiceImpl(
                registryExecutor = globalDataProvidersRegistryExecutor,
                syncLocker = globalDataProvidersLock,
            )
        )

        val historyDataProvider = HistoryDataProviderImpl(
            recordsStorage = RecordsFileStorage.History(dataLoader),
            timeProvider = timeProvider,
            maxRecordsAmount = searchSdkSettings.maxHistoryRecordsAmount,
        )

        val favoritesDataProvider = FavoritesDataProviderImpl(
            recordsStorage = RecordsFileStorage.Favorite(dataLoader),
        )

        internalServiceProvider = ServiceProviderImpl(
            analyticsSender = analyticsService,
            locationEngine = locationEngine,
            historyDataProvider = historyDataProvider,
            favoritesDataProvider = favoritesDataProvider,
            errorsReporter = errorsReporter ?: analyticsService,
        )

        CommonErrorsReporter.reporter = {
            internalServiceProvider.errorsReporter().reportError(it)
        }

        searchResultFactory = SearchResultFactory(indexableDataProvidersRegistry)

        tileStore = offlineSearchEngineSettings.tileStore ?: TileStore.create()

        tileStore.setOption(
            TileStoreOptions.MAPBOX_APIURL,
            TileDataDomain.SEARCH,
            Value.valueOf(offlineSearchEngineSettings.tilesBaseUriOrDefault().toString())
        )

        tileStore.setOption(
            TileStoreOptions.MAPBOX_ACCESS_TOKEN,
            TileDataDomain.SEARCH,
            Value.valueOf(accessToken)
        )

        registerDefaultDataProviders()

        isInitialized = true

        sbsSearchEngineShared = createSearchEngine(ApiType.SBS, searchEngineSettings, useSharedCoreEngine = true)
        geocodingSearchEngineShared = createSearchEngine(ApiType.GEOCODING, searchEngineSettings, useSharedCoreEngine = true)
        offlineSearchEngineShared = createOfflineSearchEngine(sbsCoreSearchEngine)
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
                logd(e, "Unable to register ${provider.dataProviderName} data provider for $apiType")
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

    private fun mapboxModuleParamsProvider(type: MapboxModuleType): Array<ModuleProviderArgument> {
        return when (type) {
            MapboxModuleType.CommonLibraryLoader -> arrayOf()
            MapboxModuleType.CommonLogger -> arrayOf()
            MapboxModuleType.CommonHttpClient, // TODO support common Http service
            MapboxModuleType.NavigationRouter,
            MapboxModuleType.NavigationTripNotification,
            MapboxModuleType.MapTelemetry -> throw IllegalArgumentException("not supported: $type")
        }
    }

    /**
     * Change current SDK access token in runtime.
     * @param [accessToken] new access token.
     * @throws IllegalStateException if [MapboxSearchSdk] is not initialized.
     */
    @JvmStatic
    public fun setAccessToken(accessToken: String) {
        checkInitialized()
        this.accessToken = accessToken
        coreSearchEngines.forEach { it.setAccessToken(accessToken) }
        internalServiceProvider.internalAnalyticsService().setAccessToken(accessToken)
        tileStore.setOption(
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
            getCoreEngineByApiType(apiType)
        } else {
            createCoreEngineByApiType(apiType, searchEngineSettings)
        }

        return SearchEngineImpl(
            apiType,
            coreEngine,
            httpErrorsCache,
            internalServiceProvider.historyService(),
            searchRequestContextProvider,
            searchResultFactory,
            searchEnginesExecutor,
            indexableDataProvidersRegistry
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

    private fun createOfflineSearchEngine(coreEngine: CoreSearchEngineInterface): OfflineSearchEngine {
        checkInitialized()
        return OfflineSearchEngineImpl(
            coreEngine = coreEngine,
            httpErrorsCache = httpErrorsCache,
            historyService = internalServiceProvider.historyService(),
            requestContextProvider = searchRequestContextProvider,
            searchResultFactory = searchResultFactory,
            engineExecutorService = offlineSearchEngineExecutor,
            tileStore = tileStore,
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
            CoreEngineOptions(accessToken, endpoint, apiType.mapToCore(), userAgent),
            platformClient,
            coreLocationProvider,
        ).apply {
            coreSearchEngines.add(this)
        }
    }

    private fun getCoreEngineByApiType(apiType: ApiType): CoreSearchEngineInterface {
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
