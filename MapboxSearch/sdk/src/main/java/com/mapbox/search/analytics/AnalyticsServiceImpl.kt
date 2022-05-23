package com.mapbox.search.analytics

import android.content.Context
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.geojson.Point
import com.mapbox.search.CompletionCallback
import com.mapbox.search.ResponseInfo
import com.mapbox.search.analytics.events.SearchFeedbackEvent
import com.mapbox.search.common.assertDebug
import com.mapbox.search.common.extension.lastKnownLocationOrNull
import com.mapbox.search.common.logger.logd
import com.mapbox.search.common.logger.loge
import com.mapbox.search.common.throwDebug
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.BaseSearchResult
import com.mapbox.search.result.BaseSearchSuggestion
import com.mapbox.search.result.GeocodingCompatSearchSuggestion
import com.mapbox.search.result.IndexableRecordSearchResult
import com.mapbox.search.result.IndexableRecordSearchResultImpl
import com.mapbox.search.result.IndexableRecordSearchSuggestion
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.ServerSearchResultImpl
import com.mapbox.search.result.ServerSearchSuggestion
import java.util.concurrent.Executor

internal class AnalyticsServiceImpl(
    private val context: Context,
    private val eventsService: SearchEventsService,
    private val eventsJsonParser: AnalyticsEventJsonParser,
    private val feedbackEventsFactory: SearchFeedbackEventsFactory,
    private val crashEventsFactory: CrashEventsFactory,
    private val locationEngine: LocationEngine
) : InternalAnalyticsService, ErrorsReporter {

    override fun createRawFeedbackEvent(
        searchResult: SearchResult,
        responseInfo: ResponseInfo,
        executor: Executor,
        callback: CompletionCallback<String>
    ) {
        createFeedbackEvent(
            searchResult = searchResult,
            responseInfo = responseInfo,
            // Location is null because it's not needed for template events.
            // See SearchFeedbackEventsFactory.createSearchFeedbackEvent
            currentLocation = null,
            asTemplate = true,
            callback = object : CompletionCallback<SearchFeedbackEvent> {
                override fun onComplete(result: SearchFeedbackEvent) {
                    executor.execute {
                        callback.onComplete(eventsJsonParser.serialize(result))
                    }
                }

                override fun onError(e: Exception) {
                    executor.execute {
                        callback.onError(e)
                    }
                }
            }
        )
    }

    override fun createRawFeedbackEvent(
        searchSuggestion: SearchSuggestion,
        responseInfo: ResponseInfo,
        executor: Executor,
        callback: CompletionCallback<String>
    ) {
        createFeedbackEvent(
            searchSuggestion = searchSuggestion,
            responseInfo = responseInfo,
            // Location is null because it's not needed for template events.
            // See SearchFeedbackEventsFactory.createSearchFeedbackEvent
            currentLocation = null,
            asTemplate = true,
            callback = object : CompletionCallback<SearchFeedbackEvent> {
                override fun onComplete(result: SearchFeedbackEvent) {
                    executor.execute {
                        callback.onComplete(eventsJsonParser.serialize(result))
                    }
                }

                override fun onError(e: Exception) {
                    executor.execute {
                        callback.onError(e)
                    }
                }
            }
        )
    }

    override fun sendFeedback(
        searchResult: SearchResult,
        responseInfo: ResponseInfo,
        event: FeedbackEvent
    ) {
        locationEngine.lastKnownLocationOrNull(context) {
            createFeedbackEvent(
                searchResult = searchResult,
                responseInfo = responseInfo,
                currentLocation = it,
                event = event,
                callback = object : CompletionCallback<SearchFeedbackEvent> {
                    override fun onComplete(result: SearchFeedbackEvent) {
                        sendFeedbackInternal(result)
                    }

                    override fun onError(e: Exception) {
                        loge(e, "Unable to send event: $event")
                    }
                }
            )
        }
    }

    override fun sendFeedback(
        searchSuggestion: SearchSuggestion,
        responseInfo: ResponseInfo,
        event: FeedbackEvent
    ) {
        locationEngine.lastKnownLocationOrNull(context) {
            createFeedbackEvent(
                searchSuggestion = searchSuggestion,
                responseInfo = responseInfo,
                currentLocation = it,
                event = event,
                callback = object : CompletionCallback<SearchFeedbackEvent> {
                    override fun onComplete(result: SearchFeedbackEvent) {
                        sendFeedbackInternal(result)
                    }

                    override fun onError(e: Exception) {
                        loge(e, "Unable to send event: $event")
                    }
                }
            )
        }
    }

    override fun sendFeedback(historyRecord: HistoryRecord, event: FeedbackEvent) {
        locationEngine.lastKnownLocationOrNull(context) {
            val feedbackEvent = feedbackEventsFactory.createSearchFeedbackEvent(historyRecord, event, currentLocation = it)
            sendFeedbackInternal(feedbackEvent)
        }
    }

    override fun sendFeedback(favoriteRecord: FavoriteRecord, event: FeedbackEvent) {
        locationEngine.lastKnownLocationOrNull(context) {
            val feedbackEvent = feedbackEventsFactory.createSearchFeedbackEvent(favoriteRecord, event, currentLocation = it)
            sendFeedbackInternal(feedbackEvent)
        }
    }

    override fun sendMissingResultFeedback(event: MissingResultFeedbackEvent) {
        locationEngine.lastKnownLocationOrNull(context) {
            feedbackEventsFactory.createSearchFeedbackEvent(
                event,
                currentLocation = it,
                object : CompletionCallback<SearchFeedbackEvent> {
                    override fun onComplete(result: SearchFeedbackEvent) {
                        sendFeedbackInternal(result)
                    }

                    override fun onError(e: Exception) {
                        loge(e, "Unable to send event: $event")
                    }
                }
            )
        }
    }

    private fun sendFeedbackInternal(feedbackEvent: SearchFeedbackEvent) {
        try {
            check(feedbackEvent.isValid) {
                "Event is not valid $feedbackEvent"
            }
            val jsonEvent = eventsJsonParser.serialize(feedbackEvent)
            eventsService.sendEventJson(jsonEvent)
            logd("Feedback event: $feedbackEvent")
        } catch (e: Exception) {
            loge(e, "Unable to send event: $feedbackEvent")
            throwDebug(e)
        }
    }

    private fun createFeedbackEvent(
        searchResult: SearchResult,
        responseInfo: ResponseInfo?,
        currentLocation: Point?,
        event: FeedbackEvent? = null,
        asTemplate: Boolean = false,
        callback: CompletionCallback<SearchFeedbackEvent>
    ) {
        assertDebug(searchResult is ServerSearchResultImpl ||
                    searchResult is IndexableRecordSearchResultImpl
        ) {
            "searchResult of unsupported type (${searchResult.javaClass.simpleName}) was provided. " +
                    "Please, do not use custom types. If it's not the case, contact Search SDK team."
        }
        require(searchResult is BaseSearchResult) { "Parameter searchResult must provide original response." }

        return feedbackEventsFactory.createSearchFeedbackEvent(
            originalSearchResult = searchResult.originalSearchResult,
            requestOptions = searchResult.requestOptions,
            searchResponse = responseInfo?.coreSearchResponse,
            currentLocation = currentLocation,
            isReproducible = responseInfo?.isReproducible,
            event = event,
            isCached = searchResult is IndexableRecordSearchResult,
            asTemplate = asTemplate,
            callback = callback,
        )
    }

    private fun createFeedbackEvent(
        searchSuggestion: SearchSuggestion,
        responseInfo: ResponseInfo?,
        currentLocation: Point?,
        event: FeedbackEvent? = null,
        asTemplate: Boolean = false,
        callback: CompletionCallback<SearchFeedbackEvent>
    ) {
        assertDebug(searchSuggestion is ServerSearchSuggestion ||
                    searchSuggestion is IndexableRecordSearchSuggestion ||
                    searchSuggestion is GeocodingCompatSearchSuggestion
        ) {
            "searchSuggestion of unsupported type (${searchSuggestion.javaClass.simpleName}) was provided. " +
                    "Please, do not use custom types. If it's not the case, contact Search SDK team."
        }
        require(searchSuggestion is BaseSearchSuggestion) { "Parameter searchSuggestion must provide original response." }

        feedbackEventsFactory.createSearchFeedbackEvent(
            originalSearchResult = searchSuggestion.originalSearchResult,
            requestOptions = searchSuggestion.requestOptions,
            searchResponse = responseInfo?.coreSearchResponse,
            currentLocation = currentLocation,
            isReproducible = responseInfo?.isReproducible,
            event = event,
            isCached = searchSuggestion is IndexableRecordSearchSuggestion,
            asTemplate = asTemplate,
            callback = callback,
        )
    }

    override fun setAccessToken(accessToken: String) {
        eventsService.updateAccessToken(accessToken)
    }

    override fun reportError(throwable: Throwable) {
        if (!crashEventsFactory.isAllowedForAnalytics(throwable)) {
            logd("$throwable is not Search SDK related error. Skip it.")
            return
        }

        val event = crashEventsFactory.createEvent(throwable, isSilent = true, customData = null)
        eventsService.sendEventJson(event)
    }
}
