package com.mapbox.search.analytics.events

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

// Event schema available by link below
// https://github.com/mapbox/event-schema/blob/master/lib/base-schemas/search.query_change.js
internal class QueryChangeEvent : BaseSearchEvent {

    override val isValid: Boolean
        get() = super.isValid && oldQuery != null && newQuery != null && event == EVENT_NAME

    @SerializedName("oldQuery")
    var oldQuery: String? = null

    @SerializedName("newQuery")
    var newQuery: String? = null

    @SerializedName("changeType")
    var changeType: String? = null

    constructor() : super()
    constructor(parcel: Parcel) : super(parcel) {
        oldQuery = parcel.readString()
        newQuery = parcel.readString()
        changeType = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(oldQuery)
        parcel.writeString(newQuery)
        parcel.writeString(changeType)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as QueryChangeEvent

        if (oldQuery != other.oldQuery) return false
        if (newQuery != other.newQuery) return false
        if (changeType != other.changeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (oldQuery?.hashCode() ?: 0)
        result = 31 * result + (newQuery?.hashCode() ?: 0)
        result = 31 * result + (changeType?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "QueryChangeEvent(${super.toString()}, oldQuery=$oldQuery, newQuery=$newQuery, changeType=$changeType)"
    }

    companion object {

        const val EVENT_NAME = "search.query_change"

        @JvmField
        val CREATOR = object : Parcelable.Creator<QueryChangeEvent> {
            override fun createFromParcel(parcel: Parcel): QueryChangeEvent {
                return QueryChangeEvent(parcel)
            }

            override fun newArray(size: Int): Array<QueryChangeEvent?> {
                return arrayOfNulls(size)
            }
        }
    }
}
