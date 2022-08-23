package com.mapbox.search.result

import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.base.result.BaseRawSearchResult
import java.util.Collections

internal abstract class AbstractSearchResult(
    @Transient
    open val rawSearchResult: BaseRawSearchResult
) : SearchResult {

    override val id: String
        get() = rawSearchResult.id

    override val name: String
        get() = rawSearchResult.names[0]

    override val matchingName: String?
        get() = rawSearchResult.matchingName

    override val descriptionText: String?
        get() = rawSearchResult.descriptionAddress

    // TODO allow user to access all the data in all the languages
    override val address: SearchAddress?
        get() = rawSearchResult.addresses?.get(0)?.mapToPlatform()

    override val routablePoints: List<RoutablePoint>?
        get() = rawSearchResult.routablePoints?.map { it.mapToPlatform() }

    override val categories: List<String>
        get() = rawSearchResult.categories ?: emptyList()

    override val makiIcon: String?
        get() = rawSearchResult.icon

    override val accuracy: ResultAccuracy?
        get() = rawSearchResult.accuracy?.mapToPlatform()

    override val etaMinutes: Double?
        get() = rawSearchResult.etaMinutes

    override val metadata: SearchResultMetadata?
        get() = rawSearchResult.metadata?.let { SearchResultMetadata(it) }

    override val externalIDs: Map<String, String>
        get() = Collections.unmodifiableMap(rawSearchResult.externalIDs ?: emptyMap())

    override val distanceMeters: Double?
        get() = rawSearchResult.distanceMeters

    override val serverIndex: Int?
        get() = rawSearchResult.serverIndex

    override fun toString(): String {
        return "SearchResult(" +
                "id='$id', " +
                "name='$name', " +
                "matchingName='$matchingName', " +
                "address='$address', " +
                "descriptionText='$descriptionText', " +
                "routablePoints='$routablePoints', " +
                "categories='$categories', " +
                "makiIcon='$makiIcon', " +
                "coordinate='$coordinate', " +
                "accuracy='$accuracy', " +
                "types='$types', " +
                "etaMinutes='$etaMinutes', " +
                "metadata='$metadata', " +
                "externalIDs='$externalIDs`, " +
                "distanceMeters='$distanceMeters', " +
                "serverIndex='$serverIndex', " +
                "requestOptions='$requestOptions'" +
                ")"
    }
}
