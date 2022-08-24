package com.mapbox.search

import android.app.Application
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.common.EventsServerOptions
import com.mapbox.common.EventsService
import com.mapbox.search.analytics.AnalyticsEventJsonParser
import com.mapbox.search.analytics.AnalyticsService
import com.mapbox.search.analytics.AnalyticsServiceImpl
import com.mapbox.search.analytics.SearchFeedbackEventsFactory
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreEngineOptions
import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.base.location.LocationEngineAdapter
import com.mapbox.search.base.location.WrapperLocationProvider
import com.mapbox.search.base.logger.logw
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.utils.AndroidKeyboardLocaleProvider
import com.mapbox.search.base.utils.FormattedTimeProvider
import com.mapbox.search.base.utils.FormattedTimeProviderImpl
import com.mapbox.search.base.utils.KeyboardLocaleProvider
import com.mapbox.search.base.utils.LocalTimeProvider
import com.mapbox.search.base.utils.TimeProvider
import com.mapbox.search.base.utils.UUIDProvider
import com.mapbox.search.base.utils.UUIDProviderImpl
import com.mapbox.search.base.utils.UserAgentProvider
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.base.utils.orientation.AndroidScreenOrientationProvider
import com.mapbox.search.base.utils.orientation.ScreenOrientationProvider
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.record.DataProviderEngineRegistrationServiceImpl
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.FavoritesDataProviderImpl
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.record.HistoryDataProviderImpl
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.RecordsFileStorage
import com.mapbox.search.utils.CompoundCompletionCallback
import com.mapbox.search.utils.LoggingCompletionCallback
import com.mapbox.search.utils.file.InternalFileSystem
import com.mapbox.search.utils.loader.DataLoader
import com.mapbox.search.utils.loader.InternalDataLoader
import java.util.concurrent.Executor

/**
 * The entry point to initialize Search SDK.
 */
@Suppress("LargeClass")
public object MapboxSearchSdk {

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
            providedUserAgent = UserAgentProvider.userAgent,
            viewportProvider = viewportProvider,
            uuidProvider = uuidProvider,
            coreSearchEngine = coreSearchEngine,
            eventJsonParser = eventJsonParser,
            formattedTimeProvider = formattedTimeProvider,
        )

        val eventsService = EventsService.getOrCreate(EventsServerOptions(accessToken, UserAgentProvider.userAgent))

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
            CoreEngineOptions(settings.accessToken, endpoint, apiType.mapToCore(), UserAgentProvider.userAgent, null),
            WrapperLocationProvider(LocationEngineAdapter(application, settings.locationEngine)) {
                settings.viewportProvider?.getViewport()?.mapToCore()
            }
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
