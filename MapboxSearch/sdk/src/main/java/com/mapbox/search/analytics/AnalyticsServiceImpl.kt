package com.mapbox.search.analytics

import com.mapbox.bindgen.Value
import com.mapbox.common.Event
import com.mapbox.common.EventsServiceInterface
import com.mapbox.common.location.LocationProvider
import com.mapbox.geojson.Point
import com.mapbox.search.ResponseInfo
import com.mapbox.search.analytics.events.SearchFeedbackEvent
import com.mapbox.search.base.logger.logd
import com.mapbox.search.base.logger.loge
import com.mapbox.search.base.throwDebug
import com.mapbox.search.base.utils.extension.toPoint
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.isIndexableRecordSuggestion
import java.util.concurrent.Executor

internal class AnalyticsServiceImpl(
    private val eventsService: EventsServiceInterface,
    private val eventsJsonParser: AnalyticsEventJsonParser,
    private val feedbackEventsFactory: SearchFeedbackEventsFactory,
    private val locationProvider: LocationProvider?
) : AnalyticsService {

    fun createRawFeedbackEvent(
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

    fun createRawFeedbackEvent(
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
        event: FeedbackEvent,
        callback: CompletionCallback<Unit>?
    ) {
        locationProvider?.getLastLocation {
            createFeedbackEvent(
                searchResult = searchResult,
                responseInfo = responseInfo,
                currentLocation = it?.toPoint(),
                event = event,
                callback = object : CompletionCallback<SearchFeedbackEvent> {
                    override fun onComplete(result: SearchFeedbackEvent) {
                        sendFeedbackInternal(result, callback)
                    }

                    override fun onError(e: Exception) {
                        loge("Unable to send event $event: ${e.message}")
                        callback?.onError(e)
                    }
                }
            )
        }
    }

    override fun sendFeedback(
        searchSuggestion: SearchSuggestion,
        responseInfo: ResponseInfo,
        event: FeedbackEvent,
        callback: CompletionCallback<Unit>?
    ) {
        locationProvider?.getLastLocation {
            createFeedbackEvent(
                searchSuggestion = searchSuggestion,
                responseInfo = responseInfo,
                currentLocation = it?.toPoint(),
                event = event,
                callback = object : CompletionCallback<SearchFeedbackEvent> {
                    override fun onComplete(result: SearchFeedbackEvent) {
                        sendFeedbackInternal(result, callback)
                    }

                    override fun onError(e: Exception) {
                        loge("Unable to send event $event: ${e.message}")
                        callback?.onError(e)
                    }
                }
            )
        }
    }

    override fun sendFeedback(historyRecord: HistoryRecord, event: FeedbackEvent, callback: CompletionCallback<Unit>?) {
        locationProvider?.getLastLocation {
            val feedbackEvent = feedbackEventsFactory.createSearchFeedbackEvent(historyRecord, event, currentLocation = it?.toPoint())
            sendFeedbackInternal(feedbackEvent, callback)
        }
    }

    override fun sendFeedback(favoriteRecord: FavoriteRecord, event: FeedbackEvent, callback: CompletionCallback<Unit>?) {
        locationProvider?.getLastLocation {
            val feedbackEvent = feedbackEventsFactory.createSearchFeedbackEvent(favoriteRecord, event, currentLocation = it?.toPoint())
            sendFeedbackInternal(feedbackEvent, callback)
        }
    }

    override fun sendMissingResultFeedback(event: MissingResultFeedbackEvent, callback: CompletionCallback<Unit>?) {
        locationProvider?.getLastLocation {
            feedbackEventsFactory.createSearchFeedbackEvent(
                event,
                currentLocation = it?.toPoint(),
                object : CompletionCallback<SearchFeedbackEvent> {
                    override fun onComplete(result: SearchFeedbackEvent) {
                        sendFeedbackInternal(result, callback)
                    }

                    override fun onError(e: Exception) {
                        loge("Unable to send event $event: ${e.message}")
                        callback?.onError(e)
                    }
                }
            )
        }
    }

    private fun sendFeedbackInternal(feedbackEvent: SearchFeedbackEvent, callback: CompletionCallback<Unit>?) {
        try {
            check(feedbackEvent.isValid) {
                "Event is not valid $feedbackEvent"
            }
            val jsonEvent = eventsJsonParser.serialize(feedbackEvent)
            sendEventJson(jsonEvent)
            logd("Feedback event: $feedbackEvent")
            callback?.onComplete(Unit)
        } catch (e: Exception) {
            callback?.onError(e)
            throwDebug(e) { "Unable to send event: $feedbackEvent: ${e.message}" }
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
        return feedbackEventsFactory.createSearchFeedbackEvent(
            baseRawSearchResult = searchResult.base.rawSearchResult,
            requestOptions = searchResult.requestOptions,
            searchResponse = responseInfo?.coreSearchResponse,
            currentLocation = currentLocation,
            isReproducible = responseInfo?.isReproducible,
            event = event,
            isCached = searchResult.indexableRecord != null,
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
        feedbackEventsFactory.createSearchFeedbackEvent(
            baseRawSearchResult = searchSuggestion.base.rawSearchResult,
            requestOptions = searchSuggestion.requestOptions,
            searchResponse = responseInfo?.coreSearchResponse,
            currentLocation = currentLocation,
            isReproducible = responseInfo?.isReproducible,
            event = event,
            isCached = searchSuggestion.isIndexableRecordSuggestion,
            asTemplate = asTemplate,
            callback = callback,
        )
    }

    private fun sendEventJson(eventJson: String) {
        val eventValue = Value.fromJson(eventJson)
        if (eventValue.isValue) {
            val event = Event(eventValue.value!!, null)
            eventsService.sendEvent(event) { error ->
                loge("Unable to send event: $error")
            }
        } else {
            loge("Unable to create event from json event: ${eventValue.error}")
        }
    }
}
