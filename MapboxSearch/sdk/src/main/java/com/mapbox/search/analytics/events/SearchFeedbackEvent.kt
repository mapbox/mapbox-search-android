package com.mapbox.search.analytics.events

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

// Event schema available by link below
// https://github.com/mapbox/event-schema/blob/master/lib/base-schemas/search.feedback.js
internal class SearchFeedbackEvent : BaseSearchSessionEvent {

    override val isValid: Boolean
        get() = super.isValid && feedbackReason?.isNotEmpty() == true && resultIndex != null &&
                selectedItemName != null && event == EVENT_NAME && appMetadata?.isValid != false

    @SerializedName("feedbackReason")
    var feedbackReason: String? = null

    @SerializedName("feedbackText")
    var feedbackText: String? = null

    @SerializedName("resultIndex")
    var resultIndex: Int? = null

    @SerializedName("selectedItemName")
    var selectedItemName: String? = null

    @SerializedName("resultId")
    var resultId: String? = null

    @SerializedName("responseUuid")
    var responseUuid: String? = null

    @SerializedName("feedbackId")
    var feedbackId: String? = null

    @SerializedName("isTest")
    var isTest: Boolean? = null

    @SerializedName("screenshot")
    var screenshot: String? = null

    @SerializedName("resultCoordinates")
    var resultCoordinates: List<Double>? = null

    @SerializedName("requestParamsJSON")
    var requestParamsJson: String? = null

    @SerializedName("appMetadata")
    var appMetadata: AppMetadata? = null

    @SerializedName("searchResultsJSON")
    var searchResultsJson: String? = null

    constructor() : super()
    constructor(parcel: Parcel) : super(parcel) {
        feedbackReason = parcel.readString()
        feedbackText = parcel.readString()
        resultIndex = parcel.readValue(Int::class.java.classLoader) as? Int
        selectedItemName = parcel.readString()
        resultId = parcel.readString()
        responseUuid = parcel.readString()
        feedbackId = parcel.readString()
        isTest = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        screenshot = parcel.readString()
        @Suppress("UNCHECKED_CAST")
        resultCoordinates = parcel.readSerializable() as? List<Double>
        requestParamsJson = parcel.readString()
        appMetadata = parcel.readParcelable(AppMetadata::class.java.classLoader)
        searchResultsJson = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(feedbackReason)
        parcel.writeString(feedbackText)
        parcel.writeValue(resultIndex)
        parcel.writeString(selectedItemName)
        parcel.writeString(resultId)
        parcel.writeString(responseUuid)
        parcel.writeString(feedbackId)
        parcel.writeValue(isTest)
        parcel.writeString(screenshot)
        parcel.writeSerializable(resultCoordinates?.let { ArrayList<Double>(it) })
        parcel.writeString(requestParamsJson)
        parcel.writeParcelable(appMetadata, flags)
        parcel.writeString(searchResultsJson)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SearchFeedbackEvent

        if (feedbackReason != other.feedbackReason) return false
        if (feedbackText != other.feedbackText) return false
        if (resultIndex != other.resultIndex) return false
        if (selectedItemName != other.selectedItemName) return false
        if (resultId != other.resultId) return false
        if (responseUuid != other.responseUuid) return false
        if (feedbackId != other.feedbackId) return false
        if (isTest != other.isTest) return false
        if (screenshot != other.screenshot) return false
        if (resultCoordinates != other.resultCoordinates) return false
        if (requestParamsJson != other.requestParamsJson) return false
        if (appMetadata != other.appMetadata) return false
        if (searchResultsJson != other.searchResultsJson) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (feedbackReason?.hashCode() ?: 0)
        result = 31 * result + (feedbackText?.hashCode() ?: 0)
        result = 31 * result + (resultIndex ?: 0)
        result = 31 * result + (selectedItemName?.hashCode() ?: 0)
        result = 31 * result + (resultId?.hashCode() ?: 0)
        result = 31 * result + (responseUuid?.hashCode() ?: 0)
        result = 31 * result + (feedbackId?.hashCode() ?: 0)
        result = 31 * result + (isTest?.hashCode() ?: 0)
        result = 31 * result + (screenshot?.hashCode() ?: 0)
        result = 31 * result + (resultCoordinates?.hashCode() ?: 0)
        result = 31 * result + (requestParamsJson?.hashCode() ?: 0)
        result = 31 * result + (appMetadata?.hashCode() ?: 0)
        result = 31 * result + (searchResultsJson?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "SearchFeedbackEvent(" +
                "${super.toString()}, feedbackReason=$feedbackReason, feedbackText=$feedbackText, " +
                "resultIndex=$resultIndex, selectedItemName=$selectedItemName, resultId=$resultId, " +
                "responseUuid=$responseUuid, feedbackId=$feedbackId, isTest=$isTest, " +
                "screenshot=${screenshot?.run { "$length byte(s)" }}, resultCoordinates=$resultCoordinates, " +
                "requestParamsJson=$requestParamsJson, appMetadata=$appMetadata, " +
                "searchResultsJson=$searchResultsJson" +
                ")"
    }

    companion object {

        const val EVENT_NAME = "search.feedback"

        @JvmField
        val CREATOR = object : Parcelable.Creator<SearchFeedbackEvent> {
            override fun createFromParcel(parcel: Parcel): SearchFeedbackEvent {
                return SearchFeedbackEvent(parcel)
            }

            override fun newArray(size: Int): Array<SearchFeedbackEvent?> {
                return arrayOfNulls(size)
            }
        }
    }
}
