package com.mapbox.search.tests_support.record

import com.mapbox.geojson.Point
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResultType
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
internal data class CustomRecord(
    override val id: String,
    override val name: String,
    override val coordinate: Point?,
    override val type: SearchResultType,
    val provider: Provider,
    override val descriptionText: String? = null,
    override val address: SearchAddress? = null,
    override val routablePoints: List<RoutablePoint>? = null,
    override val categories: List<String>? = null,
    override val makiIcon: String? = null,
    override val metadata: SearchResultMetadata? = null,
    override val indexTokens: List<String> = emptyList(),
) : IndexableRecord {

    internal enum class Provider {
        CLOUD,
        LOCAL,
    }

    internal companion object {

        @JvmStatic
        fun create(name: String, coordinate: Point?, provider: Provider): CustomRecord {
            return CustomRecord(
                id = UUID.randomUUID().toString(),
                name = name,
                coordinate = coordinate,
                type = SearchResultType.POI,
                provider = provider,
            )
        }
    }
}
