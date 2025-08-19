@file:Suppress("DEPRECATION")

package com.mapbox.search.tests_support

import com.mapbox.geojson.Point
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResultType
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class StubIndexableRecord(
    override val id: String = "STUB",
    override val name: String = "STUB",
    override val coordinate: Point = Point.fromLngLat(10.0, 20.0),
    override val descriptionText: String? = null,
    override val address: SearchAddress? = null,
    override val indexTokens: List<String> = emptyList(),
    override val routablePoints: List<RoutablePoint>? = emptyList(),
    override val categories: List<String>? = null,
    override val makiIcon: String? = null,
    override val metadata: SearchResultMetadata? = null
) : IndexableRecord {

    @Deprecated("Deprecated")
    @IgnoredOnParcel
    override val type: SearchResultType = SearchResultType.POI
}
