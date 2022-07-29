package com.mapbox.search.base

import android.os.Parcelable
import com.mapbox.search.base.core.CoreRequestOptions
import com.mapbox.search.base.result.SearchRequestContext
import kotlinx.parcelize.Parcelize

// TODO do we need this class?
@Parcelize
data class BaseRequestOptions(
    val core: CoreRequestOptions,
    val requestContext: SearchRequestContext,
) : Parcelable
