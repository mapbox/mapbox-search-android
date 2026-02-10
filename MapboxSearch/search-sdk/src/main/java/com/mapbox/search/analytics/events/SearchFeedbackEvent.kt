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
    var proximity: DoubleArray? = null,

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
    var resultCoordinates: DoubleArray? = null,

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchFeedbackEvent

        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (autocomplete != other.autocomplete) return false
        if (routing != other.routing) return false
        if (fuzzyMatch != other.fuzzyMatch) return false
        if (limit != other.limit) return false
        if (mapZoom != other.mapZoom) return false
        if (mapCenterLatitude != other.mapCenterLatitude) return false
        if (mapCenterLongitude != other.mapCenterLongitude) return false
        if (resultIndex != other.resultIndex) return false
        if (isTest != other.isTest) return false
        if (cached != other.cached) return false
        if (event != other.event) return false
        if (created != other.created) return false
        if (sessionIdentifier != other.sessionIdentifier) return false
        if (userAgent != other.userAgent) return false
        if (boundingBox != other.boundingBox) return false
        if (country != other.country) return false
        if (types != other.types) return false
        if (endpoint != other.endpoint) return false
        if (orientation != other.orientation) return false
        if (!proximity.contentEquals(other.proximity)) return false
        if (language != other.language) return false
        if (keyboardLocale != other.keyboardLocale) return false
        if (schema != other.schema) return false
        if (feedbackReason != other.feedbackReason) return false
        if (feedbackText != other.feedbackText) return false
        if (selectedItemName != other.selectedItemName) return false
        if (resultId != other.resultId) return false
        if (responseUuid != other.responseUuid) return false
        if (feedbackId != other.feedbackId) return false
        if (screenshot != other.screenshot) return false
        if (!resultCoordinates.contentEquals(other.resultCoordinates)) return false
        if (requestParamsJson != other.requestParamsJson) return false
        if (appMetadata != other.appMetadata) return false
        if (searchResultsJson != other.searchResultsJson) return false
        if (queryString != other.queryString) return false

        return true
    }

    override fun hashCode(): Int {
        var result = latitude?.hashCode() ?: 0
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + (autocomplete?.hashCode() ?: 0)
        result = 31 * result + (routing?.hashCode() ?: 0)
        result = 31 * result + (fuzzyMatch?.hashCode() ?: 0)
        result = 31 * result + (limit ?: 0)
        result = 31 * result + (mapZoom?.hashCode() ?: 0)
        result = 31 * result + (mapCenterLatitude?.hashCode() ?: 0)
        result = 31 * result + (mapCenterLongitude?.hashCode() ?: 0)
        result = 31 * result + (resultIndex ?: 0)
        result = 31 * result + (isTest?.hashCode() ?: 0)
        result = 31 * result + (cached?.hashCode() ?: 0)
        result = 31 * result + (event?.hashCode() ?: 0)
        result = 31 * result + (created?.hashCode() ?: 0)
        result = 31 * result + (sessionIdentifier?.hashCode() ?: 0)
        result = 31 * result + (userAgent?.hashCode() ?: 0)
        result = 31 * result + (boundingBox?.hashCode() ?: 0)
        result = 31 * result + (country?.hashCode() ?: 0)
        result = 31 * result + (types?.hashCode() ?: 0)
        result = 31 * result + (endpoint?.hashCode() ?: 0)
        result = 31 * result + (orientation?.hashCode() ?: 0)
        result = 31 * result + (proximity?.contentHashCode() ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        result = 31 * result + (keyboardLocale?.hashCode() ?: 0)
        result = 31 * result + (schema?.hashCode() ?: 0)
        result = 31 * result + (feedbackReason?.hashCode() ?: 0)
        result = 31 * result + (feedbackText?.hashCode() ?: 0)
        result = 31 * result + (selectedItemName?.hashCode() ?: 0)
        result = 31 * result + (resultId?.hashCode() ?: 0)
        result = 31 * result + (responseUuid?.hashCode() ?: 0)
        result = 31 * result + (feedbackId?.hashCode() ?: 0)
        result = 31 * result + (screenshot?.hashCode() ?: 0)
        result = 31 * result + (resultCoordinates?.contentHashCode() ?: 0)
        result = 31 * result + (requestParamsJson?.hashCode() ?: 0)
        result = 31 * result + (appMetadata?.hashCode() ?: 0)
        result = 31 * result + (searchResultsJson?.hashCode() ?: 0)
        result = 31 * result + (queryString?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "SearchFeedbackEvent(" +
                "event=$event, " +
                "created=$created, " +
                "latitude=$latitude, " +
                "longitude=$longitude, " +
                "sessionIdentifier=$sessionIdentifier, " +
                "userAgent=$userAgent, " +
                "boundingBox=$boundingBox, " +
                "autocomplete=$autocomplete, " +
                "routing=$routing, " +
                "country=$country, " +
                "types=$types, " +
                "endpoint=$endpoint, " +
                "orientation=$orientation, " +
                "proximity=${proximity.contentToString()}, " +
                "fuzzyMatch=$fuzzyMatch, " +
                "limit=$limit, " +
                "language=$language, " +
                "keyboardLocale=$keyboardLocale, " +
                "mapZoom=$mapZoom, " +
                "mapCenterLatitude=$mapCenterLatitude, " +
                "mapCenterLongitude=$mapCenterLongitude, " +
                "schema=$schema, " +
                "feedbackReason=$feedbackReason, " +
                "feedbackText=$feedbackText, " +
                "resultIndex=$resultIndex, " +
                "selectedItemName=$selectedItemName, " +
                "resultId=$resultId, " +
                "responseUuid=$responseUuid, " +
                "feedbackId=$feedbackId, " +
                "isTest=$isTest, " +
                "screenshot=$screenshot, " +
                "resultCoordinates=${resultCoordinates.contentToString()}, " +
                "requestParamsJson=$requestParamsJson, " +
                "appMetadata=$appMetadata, " +
                "searchResultsJson=$searchResultsJson, " +
                "cached=$cached, " +
                "queryString=$queryString, " +
                "isValid=$isValid" +
                ")"
    }

    companion object {
        const val EVENT_NAME = "search.feedback"
    }
}
