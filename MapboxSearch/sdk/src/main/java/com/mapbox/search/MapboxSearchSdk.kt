package com.mapbox.search

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
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.FavoritesDataProviderImpl
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.record.HistoryDataProviderImpl
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableRecord
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
import java.util.WeakHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.LazyThreadSafetyMode.NONE

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

    private var dataProviderInitializationCallback = CompoundDataProviderInitializationCallback()

    private var isInitialized = false

    // Strong references for bindgen interfaces
    private lateinit var platformClient: PlatformClient
    private lateinit var coreLocationProvider: CoreLocationProvider

    private lateinit var searchRequestContextProvider: SearchRequestContextProvider

    private lateinit var httpErrorsCache: HttpErrorsCache
    private lateinit var searchResultFactory: SearchResultFactory

    private lateinit var sbsCoreSearchEngine: CoreSearchEngineInterface
    private lateinit var geocodingCoreSearchEngine: CoreSearchEngineInterface

    private lateinit var tileStore: TileStore
    private lateinit var offlineSearchEngine: OfflineSearchEngine

    private lateinit var searchEnginesExecutor: ExecutorService
    private lateinit var offlineSearchEngineExecutor: ExecutorService
    private lateinit var globalDataProvidersRegistryExecutor: ExecutorService

    @get:JvmSynthetic
    internal lateinit var internalServiceProvider: InternalServiceProvider

    private val coreSearchEngines = WeakHashMap<CoreSearchEngineInterface, Unit>()
    private lateinit var accessToken: String
    private lateinit var searchSdkSettings: SearchSdkSettings

    // TODO(#753): allow users to create engines
    private val sbsSearchEngine: SearchEngine by lazy(NONE) { createSearchEngine(ApiType.SBS) }
    private val geocodingSearchEngine: SearchEngine by lazy(NONE) { createSearchEngine(ApiType.GEOCODING) }
    private val sbsCategorySearchEngine: CategorySearchEngine by lazy(NONE) { createCategorySearchEngine(ApiType.SBS) }
    private val geocodingCategorySearchEngine: CategorySearchEngine by lazy(NONE) { createCategorySearchEngine(ApiType.GEOCODING) }
    private val sbsReverseSearchEngine: ReverseGeocodingSearchEngine by lazy(NONE) { createReverseGeocodingSearchEngine(ApiType.SBS) }
    private val geocodingReverseSearchEngine: ReverseGeocodingSearchEngine by lazy(NONE) { createReverseGeocodingSearchEngine(ApiType.GEOCODING) }

    /**
     * Default [HistoryDataProvider] priority.
     */
    public const val LAYER_PRIORITY_HISTORY: Int = 100

    /**
     * Default [FavoritesDataProvider] priority.
     */
    public const val LAYER_PRIORITY_FAVORITES: Int = 101

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
     * Default [LocationEngine] is retrieved from [LocationEngineProvider.getBestLocationEngine].
     * @param [viewportProvider] optional viewport provider.
     * @param [searchSdkSettings] optional Search SDK settings.
     * @param [offlineSearchSettings] optional offline search settings.
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
        searchSdkSettings: SearchSdkSettings = SearchSdkSettings(),
        offlineSearchSettings: OfflineSearchSettings = OfflineSearchSettings(),
    ) {
        initializeInternal(
            application = application,
            accessToken = accessToken,
            locationEngine = locationEngine,
            viewportProvider = viewportProvider,
            searchSdkSettings = searchSdkSettings,
            offlineSearchSettings = offlineSearchSettings,
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
        offlineSearchSettings: OfflineSearchSettings = OfflineSearchSettings(),
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
            application, accessToken, userAgent, locationEngine, viewportProvider, uuidProvider, ::getCoreEngineByApiType, formattedTimeProvider
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

        geocodingCoreSearchEngine = createCoreEngineByApiType(ApiType.GEOCODING, withCoreLayers = false)
        sbsCoreSearchEngine = createCoreEngineByApiType(ApiType.SBS, withCoreLayers = false)

        searchRequestContextProvider = SearchRequestContextProvider(
            keyboardLocaleProvider,
            orientationProvider
        )

        val globalDataProvidersRegistry = DefaultIndexableDataProvidersRegistry(
            registryExecutor = globalDataProvidersRegistryExecutor,
            syncLocker = globalDataProvidersLock,
        )

        val historyDataProvider = HistoryDataProviderImpl(
            recordsStorage = RecordsFileStorage.History(dataLoader),
            timeProvider = timeProvider,
            maxRecordsAmount = searchSdkSettings.maxHistoryRecordsAmount,
        )

        val favoritesDataProvider = FavoritesDataProviderImpl(
            recordsStorage = RecordsFileStorage.Favorite(dataLoader),
        )

        with(globalDataProvidersRegistry) {
            register(
                historyDataProvider,
                LAYER_PRIORITY_HISTORY,
                dataProviderInitializationCallback,
            )
            register(
                favoritesDataProvider,
                LAYER_PRIORITY_FAVORITES,
                dataProviderInitializationCallback,
            )
        }

        internalServiceProvider = ServiceProviderImpl(
            analyticsSender = analyticsService,
            locationEngine = locationEngine,
            historyDataProvider = historyDataProvider,
            favoritesDataProvider = favoritesDataProvider,
            errorsReporter = errorsReporter ?: analyticsService,
            globalDataProvidersRegistry = globalDataProvidersRegistry,
        )

        CommonErrorsReporter.reporter = {
            internalServiceProvider.errorsReporter().reportError(it)
        }

        searchResultFactory = SearchResultFactory(globalDataProvidersRegistry)

        tileStore = offlineSearchSettings.tileStore ?: TileStore.create()

        tileStore.setOption(
            TileStoreOptions.MAPBOX_APIURL,
            TileDataDomain.SEARCH,
            Value.valueOf(offlineSearchSettings.tilesBaseUriOrDefault().toString())
        )

        tileStore.setOption(
            TileStoreOptions.MAPBOX_ACCESS_TOKEN,
            TileDataDomain.SEARCH,
            Value.valueOf(accessToken)
        )

        offlineSearchEngine = OfflineSearchEngineImpl(
            coreEngine = sbsCoreSearchEngine,
            httpErrorsCache = httpErrorsCache,
            historyService = internalServiceProvider.historyService(),
            requestContextProvider = searchRequestContextProvider,
            searchResultFactory = searchResultFactory,
            engineExecutorService = offlineSearchEngineExecutor,
            tileStore = tileStore,
        )

        isInitialized = true
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
     * Experimental API, can be changed or removed in the next SDK releases.
     *
     * Adds a callback, that will be invoked when default data providers initialization process has been completed.
     * If some data providers are already ready to use, callback will be triggered immediately.
     *
     * @param [callback] the callback to be invoked on the main thread when default data provider initialization process has been completed.
     */
    @JvmStatic
    public fun addDataProviderInitializationCallback(callback: DataProviderInitializationCallback) {
        dataProviderInitializationCallback.addCallback(callback)
    }

    /**
     * Experimental API, can be changed or removed in the next SDK releases.
     *
     * Removes an callback for default data providers initialization process.
     *
     * @param [callback] the callback to be invoked on the main thread when default data provider initialization process has been completed.
     *
     * @see addDataProviderInitializationCallback
     */
    @JvmStatic
    public fun removeDataProviderInitializationCallback(callback: DataProviderInitializationCallback) {
        dataProviderInitializationCallback.removeCallback(callback)
    }

    @JvmSynthetic
    internal fun resetDataProviderInitializationCallbacks() {
        dataProviderInitializationCallback.removeAllCallbacks()
        dataProviderInitializationCallback = CompoundDataProviderInitializationCallback()
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
        coreSearchEngines.forEach { (coreEngine, _) ->
            coreEngine.setAccessToken(accessToken)
        }
        geocodingCoreSearchEngine.setAccessToken(accessToken)
        sbsCoreSearchEngine.setAccessToken(accessToken)
        internalServiceProvider.internalAnalyticsService().setAccessToken(accessToken)
        tileStore.setOption(
            TileStoreOptions.MAPBOX_ACCESS_TOKEN,
            TileDataDomain.SEARCH,
            Value.valueOf(accessToken)
        )
    }

    /**
     * Get [SearchEngine] instance for forward geocoding.
     * @return instance of [SearchEngine].
     * @throws IllegalStateException if [MapboxSearchSdk] is not initialized.
     */
    @JvmStatic
    public fun getSearchEngine(): SearchEngine {
        return when (getDefaultApiType()) {
            ApiType.GEOCODING -> geocodingSearchEngine
            ApiType.SBS -> sbsSearchEngine
        }
    }

    internal fun createSearchEngine(apiType: ApiType): SearchEngine {
        checkInitialized()
        return SearchEngineImpl(
            apiType,
            createCoreEngineByApiType(apiType, withCoreLayers = true),
            httpErrorsCache,
            internalServiceProvider.historyService(),
            searchRequestContextProvider,
            searchResultFactory,
            searchEnginesExecutor,
        )
    }

    /**
     * Get [CategorySearchEngine] instance to search by category.
     * @return instance of [CategorySearchEngine].
     * @throws IllegalStateException if [MapboxSearchSdk] is not initialized.
     */
    @JvmStatic
    public fun getCategorySearchEngine(): CategorySearchEngine {
        return when (getDefaultApiType()) {
            ApiType.GEOCODING -> geocodingCategorySearchEngine
            ApiType.SBS -> sbsCategorySearchEngine
        }
    }

    internal fun createCategorySearchEngine(apiType: ApiType): CategorySearchEngine {
        checkInitialized()
        return CategorySearchEngineImpl(
            apiType,
            createCoreEngineByApiType(apiType, withCoreLayers = true),
            httpErrorsCache,
            searchRequestContextProvider,
            searchResultFactory,
            searchEnginesExecutor,
        )
    }

    /**
     * Get [ReverseGeocodingSearchEngine] instance for reverse geocoding.
     * @return instance of [ReverseGeocodingSearchEngine].
     * @throws IllegalStateException if [MapboxSearchSdk] is not initialized.
     */
    @JvmStatic
    public fun getReverseGeocodingSearchEngine(): ReverseGeocodingSearchEngine {
        return when (getDefaultApiType()) {
            ApiType.GEOCODING -> geocodingReverseSearchEngine
            ApiType.SBS -> sbsReverseSearchEngine
        }
    }

    /**
     * Gets existing instance of the [OfflineSearchEngine].
     * @return existing instance of [ReverseGeocodingSearchEngine].
     * @throws IllegalStateException if [MapboxSearchSdk] is not initialized.
     */
    @JvmStatic
    public fun getOfflineSearchEngine(): OfflineSearchEngine {
        checkInitialized()
        return offlineSearchEngine
    }

    internal fun createReverseGeocodingSearchEngine(apiType: ApiType): ReverseGeocodingSearchEngine {
        checkInitialized()
        return ReverseGeocodingSearchEngineImpl(
            apiType,
            createCoreEngineByApiType(apiType, withCoreLayers = false),
            httpErrorsCache,
            searchRequestContextProvider,
            searchResultFactory,
            searchEnginesExecutor,
        )
    }

    private fun <R : IndexableRecord> IndexableDataProvidersRegistry.register(
        dataProvider: IndexableDataProvider<R>,
        priority: Int,
        callback: DataProviderInitializationCallback
    ) {
        register(
            dataProvider,
            priority,
            object : IndexableDataProvidersRegistry.Callback {
                override fun onSuccess() {
                    callback.onInitialized(dataProvider)
                }

                override fun onError(e: Exception) {
                    callback.onError(dataProvider, e)
                }
            }
        )
    }

    private fun createCoreEngineByApiType(apiType: ApiType, withCoreLayers: Boolean): CoreSearchEngineInterface {
        val coreEngine = CoreSearchEngine(
            CoreEngineOptions(accessToken, getEndpointByApiType(apiType), apiType.mapToCore(), userAgent),
            platformClient,
            coreLocationProvider,
        )
        coreSearchEngines[coreEngine] = Unit
        if (withCoreLayers) {
            internalServiceProvider.globalDataProvidersRegistry().addCoreSearchEngine(coreEngine)
        }
        return coreEngine
    }

    private fun getEndpointByApiType(apiType: ApiType): String? {
        return when (apiType) {
            ApiType.GEOCODING -> searchSdkSettings.geocodingEndpointBaseUrl
            ApiType.SBS -> searchSdkSettings.singleBoxSearchBaseUrl
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
