package com.mapbox.search.analytics.events

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class SearchResultEntry(
    @SerializedName("name") var name: String? = null,
    @SerializedName("address") var address: String? = null,
    @SerializedName("coordinates") var coordinates: List<Double>? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("language") var language: List<String>? = null,
    @SerializedName("result_type") var types: List<String>? = null,
    @SerializedName("external_ids") var externalIDs: Map<String, String>? = null,
    @SerializedName("category") var category: List<String>? = null,
) : Parcelable
