package com.mapbox.search.record

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.base.core.CoreUserRecord
import com.mapbox.search.base.record.BaseIndexableRecord
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.mapToBase
import com.mapbox.search.result.mapToCore

/**
 * Defines data for index that represents external data to be included in search functionality.
 */
public interface IndexableRecord : Parcelable {

    /**
     * Record unique identifier.
     * @see [com.mapbox.search.result.SearchResult.id].
     */
    public val id: String

    /**
     * Record name.
     * @see [com.mapbox.search.result.SearchResult.name].
     */
    public val name: String

    /**
     * Additional description for the record.
     * @see [com.mapbox.search.result.SearchResult.descriptionText].
     */
    public val descriptionText: String?

    /**
     * Record address.
     * @see [com.mapbox.search.result.SearchResult.address].
     */
    public val address: SearchAddress?

    /**
     * List of points near [coordinate], that represents entries to associated building.
     * @see [com.mapbox.search.result.SearchResult.routablePoints].
     */
    public val routablePoints: List<RoutablePoint>?

    /**
     * Record categories.
     * @see [com.mapbox.search.result.SearchResult.categories].
     */
    public val categories: List<String>?

    /**
     * Mapbox Maki icon id.
     * @see [com.mapbox.search.result.SearchResult.makiIcon].
     */
    public val makiIcon: String?

    /**
     * Record coordinate.
     * @see [com.mapbox.search.result.SearchResult.coordinate].
     */
    public val coordinate: Point

    /**
     * Type of the search result represented by the record.
     * @see [com.mapbox.search.result.SearchResult.types].
     */
    public val type: SearchResultType

    /**
     * Search result metadata containing geo place's detailed information if available.
     * @see [com.mapbox.search.result.SearchResult.metadata].
     */
    public val metadata: SearchResultMetadata?

    /**
     * Additional string literals that should be included in search index. For example, you may provide non-official names to force search engine match them.
     */
    public val indexTokens: List<String>
}

@JvmSynthetic
internal fun IndexableRecord.mapToCore() = CoreUserRecord(
    id,
    name,
    coordinate,
    address?.mapToCore(),
    categories,
    indexTokens,
    type.mapToCore(), // TODO(search-sdk/#526): consider multiple types for IndexableRecord
)

@JvmSynthetic
internal fun IndexableRecord.mapToBase(): BaseIndexableRecord {
    return BaseIndexableRecord(
        id = id,
        name = name,
        descriptionText = descriptionText,
        address = address?.mapToCore(),
        routablePoints = routablePoints?.map { it.mapToCore() },
        categories = categories,
        makiIcon = makiIcon,
        coordinate = coordinate,
        type = type.mapToBase(),
        metadata = metadata?.coreMetadata,
        indexTokens = indexTokens,
        sdkResolvedRecord = this
    )
}
