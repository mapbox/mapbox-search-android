package com.mapbox.search.analytics.events

import android.os.Parcel
import com.google.gson.annotations.SerializedName

internal abstract class BaseSearchSessionEvent : BaseSearchEvent {

    override val isValid: Boolean
        get() = super.isValid && queryString != null

    @SerializedName("cached")
    var cached: Boolean? = null

    @SerializedName("queryString")
    var queryString: String? = null

    constructor() : super()
    constructor(parcel: Parcel) : super(parcel) {
        cached = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        queryString = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeValue(cached)
        parcel.writeString(queryString)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as BaseSearchSessionEvent

        if (cached != other.cached) return false
        if (queryString != other.queryString) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (cached?.hashCode() ?: 0)
        result = 31 * result + (queryString?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "${super.toString()}, cached=$cached, queryString=$queryString"
    }
}
