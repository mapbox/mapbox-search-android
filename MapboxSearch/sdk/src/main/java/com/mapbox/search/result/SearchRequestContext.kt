package com.mapbox.search.result

import android.os.Parcelable
import com.mapbox.search.ApiType
import com.mapbox.search.utils.orientation.ScreenOrientation
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
internal data class SearchRequestContext(
    val apiType: ApiType,
    val keyboardLocale: Locale? = null,
    val screenOrientation: ScreenOrientation? = null,
    val responseUuid: String? = null
) : Parcelable
