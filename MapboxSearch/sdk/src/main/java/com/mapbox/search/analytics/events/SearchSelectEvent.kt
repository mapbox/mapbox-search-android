package com.mapbox.search.analytics.events

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

// Event schema available by link below
// https://github.com/mapbox/event-schema/blob/master/lib/base-schemas/search.select.js
internal class SearchSelectEvent : BaseSearchSessionEvent {

    override val isValid: Boolean
        get() = super.isValid && resultIndex != null && event == EVENT_NAME

    @SerializedName("resultIndex")
    var resultIndex: Int? = null

    @SerializedName("resultPlaceName")
    var resultPlaceName: String? = null

    @SerializedName("resultId")
    var resultId: String? = null

    constructor() : super()
    constructor(parcel: Parcel) : super(parcel) {
        resultPlaceName = parcel.readString()
        resultIndex = parcel.readValue(Int::class.java.classLoader) as? Int
        resultId = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(resultPlaceName)
        parcel.writeValue(resultIndex)
        parcel.writeString(resultId)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SearchSelectEvent

        if (resultIndex != other.resultIndex) return false
        if (resultPlaceName != other.resultPlaceName) return false
        if (resultId != other.resultId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (resultIndex ?: 0)
        result = 31 * result + (resultPlaceName?.hashCode() ?: 0)
        result = 31 * result + (resultId?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "SearchSelectEvent(${super.toString()}, resultIndex=$resultIndex, resultPlaceName=$resultPlaceName, resultId=$resultId)"
    }

    companion object {

        const val EVENT_NAME = "search.select"

        @JvmField
        val CREATOR = object : Parcelable.Creator<SearchSelectEvent> {
            override fun createFromParcel(parcel: Parcel): SearchSelectEvent {
                return SearchSelectEvent(parcel)
            }

            override fun newArray(size: Int): Array<SearchSelectEvent?> {
                return arrayOfNulls(size)
            }
        }
    }
}
