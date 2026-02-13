package com.mapbox.search

import android.app.Application
import com.mapbox.common.EventsServerOptions
import com.mapbox.common.EventsService
import com.mapbox.common.location.LocationProvider
import com.mapbox.search.analytics.AnalyticsEventJsonParser
import com.mapbox.search.analytics.AnalyticsServiceImpl
import com.mapbox.search.analytics.SearchFeedbackEventsFactory
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreSearchEngineInterface
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
import com.mapbox.search.base.utils.orientation.AndroidScreenOrientationProvider
import com.mapbox.search.base.utils.orientation.ScreenOrientationProvider
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.record.DataProviderEngineRegistrationServiceImpl
import com.mapbox.search.record.FavoritesDataProviderImpl
import com.mapbox.search.record.HistoryDataProviderImpl
import com.mapbox.search.record.RecordsFileStorage
import com.mapbox.search.utils.LoggingCompletionCallback
import com.mapbox.search.utils.file.InternalFileSystem
import com.mapbox.search.utils.loader.DataLoader
import com.mapbox.search.utils.loader.InternalDataLoader

internal object MapboxSearchSdk {

    lateinit var searchRequestContextProvider: SearchRequestContextProvider
    lateinit var searchResultFactory: SearchResultFactory
    private lateinit var timeProvider: TimeProvider
    private lateinit var uuidProvider: UUIDProvider

    lateinit var indexableDataProvidersRegistry: IndexableDataProvidersRegistryImpl

    private val formattedTimeProvider: FormattedTimeProvider by lazy {
        FormattedTimeProviderImpl(timeProvider)
    }

    private lateinit var application: Application

    fun initialize(
        application: Application,
        timeProvider: TimeProvider = LocalTimeProvider(),
        uuidProvider: UUIDProvider = UUIDProviderImpl(),
        keyboardLocaleProvider: KeyboardLocaleProvider = AndroidKeyboardLocaleProvider(application),
        orientationProvider: ScreenOrientationProvider = AndroidScreenOrientationProvider(application),
        dataLoader: DataLoader<ByteArray> = InternalDataLoader(application, InternalFileSystem()),
    ) {
        this.application = application
        this.timeProvider = timeProvider
        this.uuidProvider = uuidProvider

        searchRequestContextProvider = SearchRequestContextProvider(
            keyboardLocaleProvider,
            orientationProvider
        )

        indexableDataProvidersRegistry = IndexableDataProvidersRegistryImpl(
            dataProviderEngineRegistrationService = DataProviderEngineRegistrationServiceImpl()
        )

        ServiceProvider.INTERNAL_INSTANCE = ServiceProviderImpl(
            historyDataProviderInitializer = {
                val provider = HistoryDataProviderImpl(
                    recordsStorage = RecordsFileStorage.History(dataLoader),
                    timeProvider = timeProvider,
                )

                indexableDataProvidersRegistry.preregister(
                    provider,
                    SearchSdkMainThreadWorker.mainExecutor,
                    LoggingCompletionCallback("HistoryDataProvider register")
                )

                provider
            },
            favoritesDataProviderInitializer = {
                val provider = FavoritesDataProviderImpl(
                    recordsStorage = RecordsFileStorage.Favorite(dataLoader),
                )

                indexableDataProvidersRegistry.preregister(
                    provider,
                    SearchSdkMainThreadWorker.mainExecutor,
                    LoggingCompletionCallback("FavoritesDataProvider register")
                )

                provider
            },
        )

        searchResultFactory = SearchResultFactory(indexableDataProvidersRegistry)
    }

    fun createAnalyticsService(
        settings: SearchEngineSettings,
        coreSearchEngine: CoreSearchEngineInterface,
    ) = createAnalyticsService(
        coreSearchEngine = coreSearchEngine,
        locationProvider = settings.locationProvider,
        viewportProvider = settings.viewportProvider
    )

    private fun createAnalyticsService(
        coreSearchEngine: CoreSearchEngineInterface,
        locationProvider: LocationProvider?,
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

        val eventsService = EventsService.getOrCreate(EventsServerOptions(UserAgentProvider.sdkInformation(), null))

        return AnalyticsServiceImpl(
            eventsService = eventsService,
            eventsJsonParser = eventJsonParser,
            feedbackEventsFactory = searchFeedbackEventsFactory,
            locationProvider = locationProvider,
        )
    }
}
