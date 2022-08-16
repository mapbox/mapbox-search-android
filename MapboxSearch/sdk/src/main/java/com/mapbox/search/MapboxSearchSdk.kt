package com.mapbox.search

import android.app.Application
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.bindgen.Value
import com.mapbox.common.EventsServerOptions
import com.mapbox.common.EventsService
import com.mapbox.common.TileDataDomain
import com.mapbox.common.TileStoreOptions
import com.mapbox.search.analytics.AnalyticsEventJsonParser
import com.mapbox.search.analytics.AnalyticsService
import com.mapbox.search.analytics.AnalyticsServiceImpl
import com.mapbox.search.analytics.SearchFeedbackEventsFactory
import com.mapbox.search.common.BuildConfig
import com.mapbox.search.common.concurrent.CommonMainThreadChecker
import com.mapbox.search.common.logger.logw
import com.mapbox.search.core.CoreEngineOptions
import com.mapbox.search.core.CoreSearchEngine
import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.location.LocationEngineAdapter
import com.mapbox.search.location.WrapperLocationProvider
import com.mapbox.search.record.DataProviderEngineRegistrationServiceImpl
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.FavoritesDataProviderImpl
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.record.HistoryDataProviderImpl
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.RecordsFileStorage
import com.mapbox.search.result.SearchResultFactory
import com.mapbox.search.utils.AndroidKeyboardLocaleProvider
import com.mapbox.search.utils.CompoundCompletionCallback
import com.mapbox.search.utils.FormattedTimeProvider
import com.mapbox.search.utils.FormattedTimeProviderImpl
import com.mapbox.search.utils.KeyboardLocaleProvider
import com.mapbox.search.utils.LocalTimeProvider
import com.mapbox.search.utils.LoggingCompletionCallback
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
    internal lateinit var application: Application

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
        this.application = application
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

        preregisterDefaultDataProviders(
            historyDataProvider, favoritesDataProvider
        )

        isInitialized = true
    }

    private fun preregisterDefaultDataProviders(
        historyDataProvider: HistoryDataProvider,
        favoritesDataProvider: FavoritesDataProvider
    ) {
        indexableDataProvidersRegistry.preregister(
            historyDataProvider,
            SearchSdkMainThreadWorker.mainExecutor,
            LoggingCompletionCallback("HistoryDataProvider register")
        )
        indexableDataProvidersRegistry.preregister(
            favoritesDataProvider,
            SearchSdkMainThreadWorker.mainExecutor,
            LoggingCompletionCallback("FavoritesDataProvider register")
        )
    }

    private fun createAnalyticsService(
        settings: OfflineSearchEngineSettings,
        coreSearchEngine: CoreSearchEngineInterface,
    ) = createAnalyticsService(
        application = application,
        accessToken = settings.accessToken,
        coreSearchEngine = coreSearchEngine,
        locationEngine = settings.locationEngine,
        viewportProvider = settings.viewportProvider
    )

    private fun createAnalyticsService(
        settings: SearchEngineSettings,
        coreSearchEngine: CoreSearchEngineInterface,
    ) = createAnalyticsService(
        application = application,
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

        val eventsService = EventsService.getOrCreate(EventsServerOptions(accessToken, userAgent))

        return AnalyticsServiceImpl(
            context = application,
            eventsService = eventsService,
            eventsJsonParser = eventJsonParser,
            feedbackEventsFactory = searchFeedbackEventsFactory,
            locationEngine = locationEngine,
        )
    }

    /**
     * Creates a new instance of the [SearchEngine] with a default [ApiType].
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
     * Experimental API, can be changed or removed in the next SDK releases.
     *
     * Creates a new instance of the [SearchEngine].
     * A new instance doesn't have any [IndexableDataProvider] registered by default.
     *
     * @param settings [SearchEngine] settings.
     * @param apiType The type of the API used by the Search Engines.
     * Note that [ApiType.GEOCODING] is the only available publicly.
     * You might need to [contact sales](https://www.mapbox.com/contact/sales/) to enable access for other API types.
     *
     * @return a new instance instance of [SearchEngine].
     * @see createSearchEngineWithBuiltInDataProviders
     */
    @JvmStatic
    public fun createSearchEngine(apiType: ApiType, settings: SearchEngineSettings): SearchEngine {
        return internalCreateSearchEngine(apiType, settings)
    }

    /**
     * Creates a new instance of the [SearchEngine] with a default [ApiType] and default data providers (
     * [com.mapbox.search.record.HistoryDataProvider] and [com.mapbox.search.record.FavoritesDataProvider])
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
        return createSearchEngineWithBuiltInDataProviders(getDefaultApiType(), settings, executor, callback)
    }

    /**
     * Experimental API, can be changed or removed in the next SDK releases.
     *
     * Creates a new instance of the [SearchEngine] with default data providers (
     * [com.mapbox.search.record.HistoryDataProvider] and [com.mapbox.search.record.FavoritesDataProvider])
     * registered by default.
     *
     * @param settings [SearchEngine] settings.
     * @param apiType The type of the API used by the Search Engines. By default [ApiType.GEOCODING] will be used.
     * Note that [ApiType.GEOCODING] is the only available publicly.
     * You might need to [contact sales](https://www.mapbox.com/contact/sales/) to enable access for other API types.
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param callback Callback to handle result.
     *
     * @return a new instance of [SearchEngine].
     * @see createSearchEngine
     */
    @JvmOverloads
    @JvmStatic
    public fun createSearchEngineWithBuiltInDataProviders(
        apiType: ApiType,
        settings: SearchEngineSettings,
        executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
        callback: CompletionCallback<Unit> = StubCompletionCallback()
    ): SearchEngine {
        val coreEngine = createCoreEngineByApiType(apiType, settings)

        val searchEngine = internalCreateSearchEngine(
            apiType,
            settings,
            coreEngine,
            createAnalyticsService(settings, coreEngine)
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

    @JvmSynthetic
    internal fun internalCreateSearchEngine(
        apiType: ApiType,
        settings: SearchEngineSettings,
        coreEngine: CoreSearchEngineInterface = createCoreEngineByApiType(apiType, settings),
        analyticsService: AnalyticsService = createAnalyticsService(settings, coreEngine),
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
            WrapperLocationProvider(
                LocationEngineAdapter(application, settings.locationEngine),
                settings.viewportProvider
            ),
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
        settings: SearchEngineSettings
    ): CoreSearchEngineInterface {
        val endpoint = when (apiType) {
            ApiType.GEOCODING -> settings.geocodingEndpointBaseUrl
            ApiType.SBS -> settings.singleBoxSearchBaseUrl
            ApiType.AUTOFILL -> null
        }

        return CoreSearchEngine(
            // TODO allow customer to customize events url
            CoreEngineOptions(settings.accessToken, endpoint, apiType.mapToCore(), userAgent, null),
            WrapperLocationProvider(
                LocationEngineAdapter(application, settings.locationEngine),
                settings.viewportProvider
            ),
        )
    }

    private fun getDefaultApiType(): ApiType {
        return if (System.getProperty("com.mapbox.mapboxsearch.enableSBS")?.toBoolean() == true) {
            logw("\"com.mapbox.mapboxsearch.enableSBS\" flag is DEPRECATED. Specify ApiType explicitly")
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
