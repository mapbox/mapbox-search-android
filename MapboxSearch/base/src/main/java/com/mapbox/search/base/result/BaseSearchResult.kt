package com.mapbox.search.base.result

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.core.CoreResultAccuracy
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.CoreRoutablePoint
import com.mapbox.search.base.record.BaseIndexableRecord
import java.util.Collections

abstract class BaseSearchResult(
    @Transient
    open val rawSearchResult: BaseRawSearchResult
) : Parcelable {

    open val id: String
        get() = rawSearchResult.id

    open val name: String
        get() = rawSearchResult.names[0]

    open val matchingName: String?
        get() = rawSearchResult.matchingName

    open val descriptionText: String?
        get() = rawSearchResult.descriptionAddress

    open val address: BaseSearchAddress?
        get() = rawSearchResult.addresses?.get(0)

    open val routablePoints: List<CoreRoutablePoint>?
        get() = rawSearchResult.routablePoints

    open val categories: List<String>?
        get() = rawSearchResult.categories

    open val makiIcon: String?
        get() = rawSearchResult.icon

    open val accuracy: CoreResultAccuracy?
        get() = rawSearchResult.accuracy

    open val etaMinutes: Double?
        get() = rawSearchResult.etaMinutes

    open val metadata: CoreResultMetadata?
        get() = rawSearchResult.metadata

    open val externalIDs: Map<String, String>
        get() = Collections.unmodifiableMap(rawSearchResult.externalIDs ?: emptyMap())

    open val distanceMeters: Double?
        get() = rawSearchResult.distanceMeters

    open val serverIndex: Int?
        get() = rawSearchResult.serverIndex

    abstract val requestOptions: BaseRequestOptions

    abstract val coordinate: Point

    abstract val types: List<BaseSearchResultType>

    abstract val baseType: Type

    val indexableRecord: BaseIndexableRecord?
        get() = (baseType as? Type.IndexableRecordSearchResult)?.record

    sealed class Type {
        data class ServerResult(val coordinate: Point) : Type()
        data class IndexableRecordSearchResult(val record: BaseIndexableRecord) : Type()
    }
}
