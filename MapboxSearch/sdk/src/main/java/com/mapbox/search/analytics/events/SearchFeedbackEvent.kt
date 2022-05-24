package com.mapbox.search.analytics.events

import com.google.gson.annotations.SerializedName

// https://github.com/mapbox/event-schema/blob/master/lib/base-schemas/search.feedback.js
internal data class SearchFeedbackEvent(
    @SerializedName("event")
    var event: String? = null,

    @SerializedName("created")
    var created: String? = null,

    @SerializedName("lat")
    var latitude: Double? = null,

    @SerializedName("lng")
    var longitude: Double? = null,

    @SerializedName("sessionIdentifier")
    var sessionIdentifier: String? = null,

    @SerializedName("userAgent")
    var userAgent: String? = null,

    @SerializedName("bbox")
    var boundingBox: List<Double>? = null,

    @SerializedName("autocomplete")
    var autocomplete: Boolean? = null,

    @SerializedName("routing")
    var routing: Boolean? = null,

    @SerializedName("country")
    var country: List<String>? = null,

    @SerializedName("types")
    var types: List<String>? = null,

    @SerializedName("endpoint")
    var endpoint: String? = null,

    @SerializedName("orientation")
    var orientation: String? = null,

    @SerializedName("proximity")
    var proximity: List<Double>? = null,

    @SerializedName("fuzzyMatch")
    var fuzzyMatch: Boolean? = null,

    @SerializedName("limit")
    var limit: Int? = null,

    @SerializedName("language")
    var language: List<String>? = null,

    @SerializedName("keyboardLocale")
    var keyboardLocale: String? = null,

    @SerializedName("mapZoom")
    var mapZoom: Float? = null,

    @SerializedName("mapCenterLat")
    var mapCenterLatitude: Double? = null,

    @SerializedName("mapCenterLng")
    var mapCenterLongitude: Double? = null,

    @SerializedName("schema")
    var schema: String? = null,

    @SerializedName("feedbackReason")
    var feedbackReason: String? = null,

    @SerializedName("feedbackText")
    var feedbackText: String? = null,

    @SerializedName("resultIndex")
    var resultIndex: Int? = null,

    @SerializedName("selectedItemName")
    var selectedItemName: String? = null,

    @SerializedName("resultId")
    var resultId: String? = null,

    @SerializedName("responseUuid")
    var responseUuid: String? = null,

    @SerializedName("feedbackId")
    var feedbackId: String? = null,

    @SerializedName("isTest")
    var isTest: Boolean? = null,

    @SerializedName("screenshot")
    var screenshot: String? = null,

    @SerializedName("resultCoordinates")
    var resultCoordinates: List<Double>? = null,

    @SerializedName("requestParamsJSON")
    var requestParamsJson: String? = null,

    @SerializedName("appMetadata")
    var appMetadata: AppMetadata? = null,

    @SerializedName("searchResultsJSON")
    var searchResultsJson: String? = null,

    @SerializedName("cached")
    var cached: Boolean? = null,

    @SerializedName("queryString")
    var queryString: String? = null,
) {

    val isValid: Boolean
        get() = created != null && sessionIdentifier != null && feedbackReason?.isNotEmpty() == true &&
                resultIndex != null && selectedItemName != null && event == EVENT_NAME &&
                appMetadata?.isValid != false && queryString != null

    companion object {
        const val EVENT_NAME = "search.feedback"
    }
}
