package com.mapbox.search.base.result

import android.os.Parcelable
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.utils.orientation.ScreenOrientation
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
data class SearchRequestContext(
    val apiType: CoreApiType,
    val keyboardLocale: Locale? = null,
    val screenOrientation: ScreenOrientation? = null,
    val responseUuid: String? = null
) : Parcelable
