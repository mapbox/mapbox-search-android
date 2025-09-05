package com.mapbox.search.analytics

import com.google.gson.Gson
import com.mapbox.search.analytics.events.SearchFeedbackEvent
import org.json.JSONObject

internal class AnalyticsEventJsonParser {

    private val gson: Gson = Gson()

    /**
     * @throws org.json.JSONException if passed [jsonEvent] doesn't have "event" attribute.
     * @throws com.google.gson.JsonSyntaxException if [jsonEvent] is not a valid representation for an object of subtype of BaseSearchEvent
     * @throws IllegalArgumentException if [jsonEvent] has unknown "event" type
     */
    fun parse(jsonEvent: String): SearchFeedbackEvent {
        val jsonObject = JSONObject(jsonEvent)
        return when (val event = jsonObject.getString("event")) {
            SearchFeedbackEvent.EVENT_NAME -> gson.fromJson(jsonEvent, SearchFeedbackEvent::class.java)
            else -> throw IllegalArgumentException("Unknown event type $event")
        }
    }

    fun serialize(event: SearchFeedbackEvent): String {
        require(event.event == SearchFeedbackEvent.EVENT_NAME) {
            "$event is not valid"
        }
        return gson.toJson(event)
    }

    fun serializeAny(obj: Any): String {
        return gson.toJson(obj)
    }
}
