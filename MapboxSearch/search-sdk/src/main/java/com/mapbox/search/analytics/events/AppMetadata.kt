package com.mapbox.search.analytics.events

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AppMetadata(
    @SerializedName("name") var name: String? = null,
    @SerializedName("version") var version: String? = null,
    @SerializedName("userId") var userId: String? = null,
    @SerializedName("sessionId") var sessionId: String? = null
) : Parcelable {

    val isValid: Boolean
        get() = name.run { this == null || length > 1 } &&
                version.run { this == null || length > 1 }
}
