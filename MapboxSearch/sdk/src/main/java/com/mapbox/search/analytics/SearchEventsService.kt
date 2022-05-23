package com.mapbox.search.analytics

import com.mapbox.bindgen.Value
import com.mapbox.common.Event
import com.mapbox.common.EventsService
import com.mapbox.common.EventsServiceOptions
import com.mapbox.search.common.logger.loge

internal class SearchEventsService(
    token: String,
    private val userAgent: String,
) {

    private var eventsService: EventsService

    init {
        val options = EventsServiceOptions(token, userAgent, null)
        eventsService = EventsService(options)
    }

    @Synchronized
    fun sendEventJson(eventJson: String) {
        val eventValue = Value.fromJson(eventJson)
        if (eventValue.isValue) {
            val event = Event(eventValue.value!!)
            eventsService.sendEvent(event) { error ->
                if (error != null) {
                    loge("Unable to send event: $error")
                }
            }
        } else {
            loge("Unable to create event from json event: ${eventValue.error}")
        }
    }

    @Synchronized
    fun updateAccessToken(newToken: String) {
        val options = EventsServiceOptions(newToken, userAgent, null)
        eventsService = EventsService(options)
    }
}
