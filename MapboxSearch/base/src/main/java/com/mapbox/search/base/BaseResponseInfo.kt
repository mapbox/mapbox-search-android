package com.mapbox.search.base

import android.os.Parcelable
import com.mapbox.search.base.result.BaseSearchResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseResponseInfo internal constructor(
    val requestOptions: BaseRequestOptions,
    val coreSearchResponse: BaseSearchResponse?,
    val isReproducible: Boolean,
) : Parcelable
