package com.mapbox.search.analytics.events

import android.os.Parcel
import android.os.Parcelable

// Event schema available by link below
// https://github.com/mapbox/event-schema/blob/master/lib/base-schemas/search.start.js
internal class SearchStartEvent : BaseSearchSessionEvent {

    override val isValid: Boolean
        get() = super.isValid && event == EVENT_NAME

    constructor() : super()
    constructor(parcel: Parcel) : super(parcel)

    override fun toString(): String {
        return "SearchStartEvent(${super.toString()})"
    }

    internal companion object {

        const val EVENT_NAME = "search.start"

        @JvmField
        val CREATOR = object : Parcelable.Creator<SearchStartEvent> {
            override fun createFromParcel(parcel: Parcel): SearchStartEvent {
                return SearchStartEvent(parcel)
            }

            override fun newArray(size: Int): Array<SearchStartEvent?> {
                return arrayOfNulls(size)
            }
        }
    }
}
