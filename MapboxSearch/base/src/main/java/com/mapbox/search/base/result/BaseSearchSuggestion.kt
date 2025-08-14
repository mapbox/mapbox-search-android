package com.mapbox.search.base.result

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.CoreRoutablePoint
import com.mapbox.search.base.core.CoreSearchAddress
import java.util.Collections

sealed class BaseSearchSuggestion(
    @Transient
    open val rawSearchResult: BaseRawSearchResult
) : Parcelable {

    open val id: String
        get() = rawSearchResult.id

    open val name: String
        get() = rawSearchResult.names[0]

    open val namePreferred: String?
        get() = rawSearchResult.namePreferred

    open val coordinate: Point?
        get() = rawSearchResult.center

    open val routablePoints: List<CoreRoutablePoint>?
        get() = rawSearchResult.routablePoints

    open val matchingName: String?
        get() = rawSearchResult.matchingName

    open val fullAddress: String?
        get() = rawSearchResult.fullAddress

    open val descriptionText: String?
        get() = rawSearchResult.descriptionAddress

    open val address: CoreSearchAddress?
        get() = rawSearchResult.addresses?.first()

    open val distanceMeters: Double?
        get() = rawSearchResult.distanceMeters

    open val categories: List<String>?
        get() = rawSearchResult.categories

    open val categoryIds: List<String>?
        get() = rawSearchResult.categoryIds

    open val makiIcon: String?
        get() = rawSearchResult.icon

    open val etaMinutes: Double?
        get() = rawSearchResult.etaMinutes

    open val metadata: CoreResultMetadata?
        get() = rawSearchResult.metadata

    open val externalIDs: Map<String, String>
        get() = Collections.unmodifiableMap(rawSearchResult.externalIDs ?: emptyMap())

    open val isBatchResolveSupported: Boolean
        get() = rawSearchResult.action?.multiRetrievable == true

    open val serverIndex: Int?
        get() = rawSearchResult.serverIndex

    abstract val type: BaseSearchSuggestionType

    abstract val requestOptions: BaseRequestOptions
}
