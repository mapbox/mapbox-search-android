package com.mapbox.search.result

import com.mapbox.search.SearchResultMetadata
import java.util.Collections

internal abstract class BaseSearchResult(
    @Transient
    open val originalSearchResult: OriginalSearchResult
) : SearchResult {

    override val id: String
        get() = originalSearchResult.id

    override val name: String
        get() = originalSearchResult.names[0]

    override val matchingName: String?
        get() = originalSearchResult.matchingName

    override val descriptionText: String?
        get() = originalSearchResult.descriptionAddress

    // TODO allow user to access all the data in all the languages
    override val address: SearchAddress?
        get() = originalSearchResult.addresses?.get(0)

    override val routablePoints: List<RoutablePoint>?
        get() = originalSearchResult.routablePoints

    override val categories: List<String>
        get() = originalSearchResult.categories ?: emptyList()

    override val makiIcon: String?
        get() = originalSearchResult.icon

    override val accuracy: ResultAccuracy?
        get() = originalSearchResult.accuracy

    override val etaMinutes: Double?
        get() = originalSearchResult.etaMinutes

    override val metadata: SearchResultMetadata?
        get() = originalSearchResult.metadata

    override val externalIDs: Map<String, String>
        get() = Collections.unmodifiableMap(originalSearchResult.externalIDs ?: emptyMap())

    override val distanceMeters: Double?
        get() = originalSearchResult.distanceMeters

    override val serverIndex: Int?
        get() = originalSearchResult.serverIndex

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
