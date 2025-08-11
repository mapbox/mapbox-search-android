package com.mapbox.search.base.record

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.CoreRoutablePoint
import com.mapbox.search.base.core.CoreSearchAddress
import com.mapbox.search.base.result.BaseSearchResultType
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseIndexableRecord(
    val id: String,
    val name: String,
    val descriptionText: String?,
    val address: CoreSearchAddress?,
    val routablePoints: List<CoreRoutablePoint>?,
    val categories: List<String>?,
    val makiIcon: String?,
    val coordinate: Point,
    val type: BaseSearchResultType,
    val metadata: CoreResultMetadata?,
    val indexTokens: List<String>,
    // TODO used to store resolved IndexableRecords from the "SDK" module. We'll get rid of this when move IndexableRecord to a common module
    val sdkResolvedRecord: Parcelable
) : Parcelable
