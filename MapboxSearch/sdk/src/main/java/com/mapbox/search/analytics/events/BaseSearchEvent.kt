package com.mapbox.search.analytics.events

import android.os.Parcel
import com.google.gson.annotations.SerializedName
import com.mapbox.android.telemetry.Event

// Event schemas, that shares common fields:
// https://github.com/mapbox/event-schema/blob/master/lib/base-schemas/search.start.js
// https://github.com/mapbox/event-schema/blob/master/lib/base-schemas/search.select.js
// https://github.com/mapbox/event-schema/blob/master/lib/base-schemas/search.query_change.js
// https://github.com/mapbox/event-schema/blob/master/lib/base-schemas/search.feedback.js
internal abstract class BaseSearchEvent : Event {

    public open val isValid: Boolean
        get() = event != null && created != null && sessionIdentifier != null

    @SerializedName(EVENT_ATTRIBUTE_NAME)
    var event: String? = null

    @SerializedName("created")
    var created: String? = null

    @SerializedName("lat")
    var latitude: Double? = null

    @SerializedName("lng")
    var longitude: Double? = null

    @SerializedName("sessionIdentifier")
    var sessionIdentifier: String? = null

    @SerializedName("userAgent")
    var userAgent: String? = null

    @SerializedName("bbox")
    var boundingBox: List<Double>? = null

    @SerializedName("autocomplete")
    var autocomplete: Boolean? = null

    @SerializedName("routing")
    var routing: Boolean? = null

    @SerializedName("country")
    var country: List<String>? = null

    @SerializedName("types")
    var types: List<String>? = null

    @SerializedName("endpoint")
    var endpoint: String? = null

    @SerializedName("orientation")
    var orientation: String? = null

    @SerializedName("proximity")
    var proximity: List<Double>? = null

    @SerializedName("fuzzyMatch")
    var fuzzyMatch: Boolean? = null

    @SerializedName("limit")
    var limit: Int? = null

    @SerializedName("language")
    var language: List<String>? = null

    @SerializedName("keyboardLocale")
    var keyboardLocale: String? = null

    @SerializedName("mapZoom")
    var mapZoom: Float? = null

    @SerializedName("mapCenterLat")
    var mapCenterLatitude: Double? = null

    @SerializedName("mapCenterLng")
    var mapCenterLongitude: Double? = null

    @SerializedName("schema")
    var schema: String? = null

    constructor() : super()
    constructor(parcel: Parcel) : this() {
        event = parcel.readString()
        created = parcel.readString()
        latitude = parcel.readValue(Double::class.java.classLoader) as? Double
        longitude = parcel.readValue(Double::class.java.classLoader) as? Double
        sessionIdentifier = parcel.readString()
        userAgent = parcel.readString()
        autocomplete = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        routing = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        country = parcel.createStringArrayList()
        types = parcel.createStringArrayList()
        endpoint = parcel.readString()
        orientation = parcel.readString()
        fuzzyMatch = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        limit = parcel.readValue(Int::class.java.classLoader) as? Int
        language = parcel.createStringArrayList()
        keyboardLocale = parcel.readString()
        mapZoom = parcel.readValue(Float::class.java.classLoader) as? Float
        mapCenterLatitude = parcel.readValue(Double::class.java.classLoader) as? Double
        mapCenterLongitude = parcel.readValue(Double::class.java.classLoader) as? Double
        @Suppress("UNCHECKED_CAST")
        proximity = parcel.readSerializable() as? List<Double>
        @Suppress("UNCHECKED_CAST")
        boundingBox = parcel.readSerializable() as? List<Double>
        schema = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(event)
        parcel.writeString(created)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
        parcel.writeString(sessionIdentifier)
        parcel.writeString(userAgent)
        parcel.writeValue(autocomplete)
        parcel.writeValue(routing)
        parcel.writeStringList(country)
        parcel.writeStringList(types)
        parcel.writeString(endpoint)
        parcel.writeString(orientation)
        parcel.writeValue(fuzzyMatch)
        parcel.writeValue(limit)
        parcel.writeStringList(language)
        parcel.writeString(keyboardLocale)
        parcel.writeValue(mapZoom)
        parcel.writeValue(mapCenterLatitude)
        parcel.writeValue(mapCenterLongitude)
        parcel.writeSerializable(proximity?.let { ArrayList<Double>(it) })
        parcel.writeSerializable(boundingBox?.let { ArrayList<Double>(it) })
        parcel.writeString(schema)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseSearchEvent

        if (event != other.event) return false
        if (created != other.created) return false
        if (latitude != other.latitude) return false
        if (longitude != other.longitude) return false
        if (sessionIdentifier != other.sessionIdentifier) return false
        if (userAgent != other.userAgent) return false
        if (boundingBox != other.boundingBox) return false
        if (autocomplete != other.autocomplete) return false
        if (routing != other.routing) return false
        if (country != other.country) return false
        if (types != other.types) return false
        if (endpoint != other.endpoint) return false
        if (orientation != other.orientation) return false
        if (proximity != other.proximity) return false
        if (fuzzyMatch != other.fuzzyMatch) return false
        if (limit != other.limit) return false
        if (language != other.language) return false
        if (keyboardLocale != other.keyboardLocale) return false
        if (mapZoom != other.mapZoom) return false
        if (mapCenterLatitude != other.mapCenterLatitude) return false
        if (mapCenterLongitude != other.mapCenterLongitude) return false
        if (schema != other.schema) return false

        return true
    }

    override fun hashCode(): Int {
        var result = event?.hashCode() ?: 0
        result = 31 * result + (created?.hashCode() ?: 0)
        result = 31 * result + (latitude?.hashCode() ?: 0)
        result = 31 * result + (longitude?.hashCode() ?: 0)
        result = 31 * result + (sessionIdentifier?.hashCode() ?: 0)
        result = 31 * result + (userAgent?.hashCode() ?: 0)
        result = 31 * result + (boundingBox?.hashCode() ?: 0)
        result = 31 * result + (autocomplete?.hashCode() ?: 0)
        result = 31 * result + (routing?.hashCode() ?: 0)
        result = 31 * result + (country?.hashCode() ?: 0)
        result = 31 * result + (types?.hashCode() ?: 0)
        result = 31 * result + (endpoint?.hashCode() ?: 0)
        result = 31 * result + (orientation?.hashCode() ?: 0)
        result = 31 * result + (proximity?.hashCode() ?: 0)
        result = 31 * result + (fuzzyMatch?.hashCode() ?: 0)
        result = 31 * result + (limit ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        result = 31 * result + (keyboardLocale?.hashCode() ?: 0)
        result = 31 * result + (mapZoom?.hashCode() ?: 0)
        result = 31 * result + (mapCenterLatitude?.hashCode() ?: 0)
        result = 31 * result + (mapCenterLongitude?.hashCode() ?: 0)
        result = 31 * result + (schema?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "event=$event, created=$created, latitude=$latitude, longitude=$longitude, sessionIdentifier=$sessionIdentifier, " +
                "userAgent=$userAgent, boundingBox=$boundingBox, autocomplete=$autocomplete, routing=$routing, " +
                "country=$country, types=$types, endpoint=$endpoint, orientation=$orientation, proximity=$proximity, " +
                "fuzzyMatch=$fuzzyMatch, limit=$limit, language=$language, keyboardLocale=$keyboardLocale, " +
                "mapZoom=$mapZoom, mapCenterLatitude=$mapCenterLatitude, mapCenterLongitude=$mapCenterLongitude, schema=$schema"
    }

    internal companion object {
        const val EVENT_ATTRIBUTE_NAME = "event"
    }
}
