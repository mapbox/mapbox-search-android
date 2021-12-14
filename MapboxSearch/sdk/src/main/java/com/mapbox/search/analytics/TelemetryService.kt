package com.mapbox.search.analytics

import android.content.Context
import com.google.gson.Gson
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.telemetry.MapboxCrashReporter
import com.mapbox.android.telemetry.MapboxTelemetry
import com.mapbox.geojson.Point
import com.mapbox.search.ApiType
import com.mapbox.search.BuildConfig
import com.mapbox.search.ResponseInfo
import com.mapbox.search.ViewportProvider
import com.mapbox.search.analytics.events.SearchFeedbackEvent
import com.mapbox.search.common.assertDebug
import com.mapbox.search.common.extension.lastKnownLocationOrNull
import com.mapbox.search.common.logger.logd
import com.mapbox.search.common.logger.loge
import com.mapbox.search.common.throwDebug
import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.CoreResponseProvider
import com.mapbox.search.result.GeocodingCompatSearchSuggestion
import com.mapbox.search.result.IndexableRecordSearchResult
import com.mapbox.search.result.IndexableRecordSearchResultImpl
import com.mapbox.search.result.IndexableRecordSearchSuggestion
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.ServerSearchResultImpl
import com.mapbox.search.result.ServerSearchSuggestion
import com.mapbox.search.utils.FormattedTimeProvider
import com.mapbox.search.utils.UUIDProvider
import java.lang.Exception

internal class TelemetryService : InternalAnalyticsService, ErrorsReporter {

    private val context: Context
    private val mapBoxTelemetry: MapboxTelemetry
    private val eventsJsonParser: AnalyticsEventJsonParser
    private val eventsFactory: TelemetrySearchEventsFactory
    private val errorsReporter: MapboxCrashReporter
    private val locationEngine: LocationEngine

    private val gson = Gson()

    internal constructor(
        context: Context,
        mapBoxTelemetry: MapboxTelemetry,
        locationEngine: LocationEngine,
        eventsJsonParser: AnalyticsEventJsonParser,
        eventsFactory: TelemetrySearchEventsFactory,
        errorsReporter: MapboxCrashReporter,
    ) {
        this.context = context
        this.mapBoxTelemetry = mapBoxTelemetry
        this.locationEngine = locationEngine
        this.eventsJsonParser = eventsJsonParser
        this.eventsFactory = eventsFactory
        this.errorsReporter = errorsReporter
    }

    internal constructor(
        context: Context,
        accessToken: String,
        userAgent: String,
        locationEngine: LocationEngine,
        viewportProvider: ViewportProvider?,
        uuidProvider: UUIDProvider,
        coreEngineProvider: (ApiType) -> CoreSearchEngineInterface,
        formattedTimeProvider: FormattedTimeProvider,
        eventsJsonParser: AnalyticsEventJsonParser = AnalyticsEventJsonParser(),
    ) {
        this.context = context.applicationContext
        this.mapBoxTelemetry = MapboxTelemetry(context.applicationContext, accessToken, userAgent).apply {
            enable()
            updateDebugLoggingEnabled(BuildConfig.DEBUG)
        }
        this.eventsJsonParser = eventsJsonParser
        this.eventsFactory = TelemetrySearchEventsFactory(
            userAgent, viewportProvider, uuidProvider, coreEngineProvider,
            eventsJsonParser, formattedTimeProvider, gson::toJson
        )
        this.errorsReporter = MapboxCrashReporter(
            mapBoxTelemetry,
            BuildConfig.LIBRARY_PACKAGE_NAME,
            BuildConfig.VERSION_NAME,
            emptySet()
        )
        this.locationEngine = locationEngine

        logd("Initialize TelemetryAnalyticsService with $userAgent agent", tag = LOG_TAG)
    }

    override fun postJsonEvent(event: String) {
        try {
            logd("postJsonEvent: $event", tag = LOG_TAG)
            val parsedEvent = eventsJsonParser.parse(event)
            check(parsedEvent.isValid) {
                "Broken telemetry event $event"
            }
            mapBoxTelemetry.push(parsedEvent)
            logd("Parsed event: $parsedEvent", tag = LOG_TAG)
        } catch (e: Exception) {
            loge(e, "Unable to send event: $event", tag = LOG_TAG)
            throwDebug(e)
        }
    }

    override fun createRawFeedbackEvent(
        searchResult: SearchResult,
        responseInfo: ResponseInfo
    ): String {
        val feedbackEvent = createFeedbackEvent(
            searchResult = searchResult,
            responseInfo = responseInfo,
            // Location is null because it's not needed for template events.
            // See TelemetrySearchEventsFactory.createSearchFeedbackEvent
            currentLocation = null,
            asTemplate = true,
        )
        return eventsJsonParser.serialize(feedbackEvent)
    }

    override fun createRawFeedbackEvent(
        searchSuggestion: SearchSuggestion,
        responseInfo: ResponseInfo
    ): String {
        val feedbackEvent = createFeedbackEvent(
            searchSuggestion = searchSuggestion,
            responseInfo = responseInfo,
            // Location is null because it's not needed for template events.
            // See TelemetrySearchEventsFactory.createSearchFeedbackEvent
            currentLocation = null,
            asTemplate = true,
        )
        return eventsJsonParser.serialize(feedbackEvent)
    }

    override fun sendFeedback(
        searchResult: SearchResult,
        responseInfo: ResponseInfo,
        event: FeedbackEvent
    ) {
        locationEngine.lastKnownLocationOrNull(context) {
            val feedbackEvent = createFeedbackEvent(
                searchResult = searchResult,
                responseInfo = responseInfo,
                currentLocation = it,
                event = event
            )
            sendFeedbackInternal(feedbackEvent)
        }
    }

    override fun sendFeedback(
        searchSuggestion: SearchSuggestion,
        responseInfo: ResponseInfo,
        event: FeedbackEvent
    ) {
        locationEngine.lastKnownLocationOrNull(context) {
            val feedbackEvent = createFeedbackEvent(
                searchSuggestion = searchSuggestion,
                responseInfo = responseInfo,
                currentLocation = it,
                event = event,
            )
            sendFeedbackInternal(feedbackEvent)
        }
    }

    override fun sendFeedback(historyRecord: HistoryRecord, event: FeedbackEvent) {
        locationEngine.lastKnownLocationOrNull(context) {
            val feedbackEvent = eventsFactory.createSearchFeedbackEvent(historyRecord, event, currentLocation = it)
            sendFeedbackInternal(feedbackEvent)
        }
    }

    override fun sendFeedback(favoriteRecord: FavoriteRecord, event: FeedbackEvent) {
        locationEngine.lastKnownLocationOrNull(context) {
            val feedbackEvent = eventsFactory.createSearchFeedbackEvent(favoriteRecord, event, currentLocation = it)
            sendFeedbackInternal(feedbackEvent)
        }
    }

    override fun sendMissingResultFeedback(event: MissingResultFeedbackEvent) {
        locationEngine.lastKnownLocationOrNull(context) {
            val feedbackEvent = eventsFactory.createSearchFeedbackEvent(event, currentLocation = it)
            sendFeedbackInternal(feedbackEvent)
        }
    }

    override fun sendRawFeedbackEvent(rawFeedbackEvent: String, event: FeedbackEvent) {
        val cachedFeedbackEvent = try {
            eventsJsonParser.parse(rawFeedbackEvent) as SearchFeedbackEvent
        } catch (e: Exception) {
            loge(e, "Could not parse cached event template as SearchFeedbackEvent. Feedback won't be sent.")
            return
        }

        locationEngine.lastKnownLocationOrNull(context) {
            eventsFactory.updateCachedSearchFeedbackEvent(cachedFeedbackEvent, event, currentLocation = it)
            sendFeedbackInternal(cachedFeedbackEvent)
        }
    }

    private fun sendFeedbackInternal(feedbackEvent: SearchFeedbackEvent) {
        try {
            check(feedbackEvent.isValid) {
                "Broken telemetry event $feedbackEvent"
            }
            mapBoxTelemetry.push(feedbackEvent)
            logd("Feedback event: $feedbackEvent", tag = LOG_TAG)
        } catch (e: Exception) {
            loge(e, "Unable to send event: $feedbackEvent", tag = LOG_TAG)
            throwDebug(e)
        }
    }

    private fun createFeedbackEvent(
        searchResult: SearchResult,
        responseInfo: ResponseInfo?,
        currentLocation: Point?,
        event: FeedbackEvent? = null,
        asTemplate: Boolean = false
    ): SearchFeedbackEvent {
        assertDebug(searchResult is ServerSearchResultImpl ||
                    searchResult is IndexableRecordSearchResultImpl
        ) {
            "searchResult of unsupported type (${searchResult.javaClass.simpleName}) was provided. " +
                    "Please, do not use custom types. If it's not the case, contact Search SDK team."
        }
        require(searchResult is CoreResponseProvider) { "Parameter searchResult must provide original response." }

        return eventsFactory.createSearchFeedbackEvent(
            originalSearchResult = searchResult.originalSearchResult,
            requestOptions = searchResult.requestOptions,
            coreSearchResponse = responseInfo?.coreSearchResponse,
            currentLocation = currentLocation,
            isReproducible = responseInfo?.isReproducible,
            event = event,
            isCached = searchResult is IndexableRecordSearchResult,
            asTemplate = asTemplate
        )
    }

    private fun createFeedbackEvent(
        searchSuggestion: SearchSuggestion,
        responseInfo: ResponseInfo?,
        currentLocation: Point?,
        event: FeedbackEvent? = null,
        asTemplate: Boolean = false
    ): SearchFeedbackEvent {
        assertDebug(searchSuggestion is ServerSearchSuggestion ||
                    searchSuggestion is IndexableRecordSearchSuggestion ||
                    searchSuggestion is GeocodingCompatSearchSuggestion
        ) {
            "searchSuggestion of unsupported type (${searchSuggestion.javaClass.simpleName}) was provided. " +
                    "Please, do not use custom types. If it's not the case, contact Search SDK team."
        }
        require(searchSuggestion is CoreResponseProvider) { "Parameter searchSuggestion must provide original response." }

        return eventsFactory.createSearchFeedbackEvent(
            originalSearchResult = searchSuggestion.originalSearchResult,
            requestOptions = searchSuggestion.requestOptions,
            coreSearchResponse = responseInfo?.coreSearchResponse,
            currentLocation = currentLocation,
            isReproducible = responseInfo?.isReproducible,
            event = event,
            isCached = searchSuggestion is IndexableRecordSearchSuggestion,
            asTemplate = asTemplate
        )
    }

    override fun setAccessToken(accessToken: String) {
        mapBoxTelemetry.updateAccessToken(accessToken)
    }

    override fun reportError(throwable: Throwable) {
        // TODO(#692): remove workaround
        errorsReporter.reportError(throwable, mapOf("source" to "Search SDK"))
    }

    private companion object {
        const val LOG_TAG = "TelemetryAnalyticsService"
    }
}
