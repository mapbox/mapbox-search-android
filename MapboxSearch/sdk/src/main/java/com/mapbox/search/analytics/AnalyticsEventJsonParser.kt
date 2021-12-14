package com.mapbox.search.analytics

import com.google.gson.Gson
import com.mapbox.search.analytics.events.BaseSearchEvent
import com.mapbox.search.analytics.events.QueryChangeEvent
import com.mapbox.search.analytics.events.SearchFeedbackEvent
import com.mapbox.search.analytics.events.SearchSelectEvent
import com.mapbox.search.analytics.events.SearchStartEvent
import org.json.JSONObject

internal class AnalyticsEventJsonParser {

    // Telemetry SDK uses Gson to serialize event, so we have 2 options:
    // 1. Annotate Event's field with Gson's @SerializedName annotation
    // 2. Make sure that field name is the same as required json attribute, in this case we have to be sure that proguard won't change it
    // We use the first approach. Need to change parser and get rid of Gson when Telemetry SDK removes it as well.
    private val gson: Gson = Gson()

    /**
     * @throws org.json.JSONException if passed [jsonEvent] doesn't have "event" attribute.
     * @throws com.google.gson.JsonSyntaxException if [jsonEvent] is not a valid representation for an object of subtype of BaseSearchEvent
     * @throws IllegalArgumentException if [jsonEvent] has unknown "event" type
     */
    fun parse(jsonEvent: String): BaseSearchEvent {
        val jsonObject = JSONObject(jsonEvent)
        return when (val event = jsonObject.getString(BaseSearchEvent.EVENT_ATTRIBUTE_NAME)) {
            SearchSelectEvent.EVENT_NAME -> gson.fromJson(jsonEvent, SearchSelectEvent::class.java)
            SearchStartEvent.EVENT_NAME -> gson.fromJson(jsonEvent, SearchStartEvent::class.java)
            QueryChangeEvent.EVENT_NAME -> gson.fromJson(jsonEvent, QueryChangeEvent::class.java)
            SearchFeedbackEvent.EVENT_NAME -> gson.fromJson(jsonEvent, SearchFeedbackEvent::class.java)
            else -> throw IllegalArgumentException("Unknown event type $event")
        }
    }

    fun serialize(event: BaseSearchEvent): String {
        require(event.validateEventName()) {
            "BaseSearchEvent should contain valid \"event\"`property! Event: $event"
        }

        return gson.toJson(event)
    }

    private fun BaseSearchEvent.validateEventName(): Boolean {
        return when (this) {
            is SearchSelectEvent -> event == SearchSelectEvent.EVENT_NAME
            is SearchStartEvent -> event == SearchStartEvent.EVENT_NAME
            is QueryChangeEvent -> event == QueryChangeEvent.EVENT_NAME
            is SearchFeedbackEvent -> event == SearchFeedbackEvent.EVENT_NAME
            else -> false
        }
    }
}
