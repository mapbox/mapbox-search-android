package com.mapbox.search.analytics.events

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class SearchResultsInfo(
    @SerializedName("results") var results: List<SearchResultEntry>? = null,
    @SerializedName("multiStepSearch") var multiStepSearch: Boolean? = null,
) : Parcelable
