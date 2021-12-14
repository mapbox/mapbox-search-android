package com.mapbox.search.result

import com.mapbox.search.SearchResultMetadata

internal abstract class BaseSearchResult : SearchResult, CoreResponseProvider {

    override val id: String
        get() = originalSearchResult.id

    override val name: String
        get() = originalSearchResult.names[0]

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

    override val etaMinutes: Double?
        get() = originalSearchResult.etaMinutes

    override val metadata: SearchResultMetadata?
        get() = originalSearchResult.metadata

    override val distanceMeters: Double?
        get() = originalSearchResult.distanceMeters

    override fun toString(): String {
        return "SearchResult(" +
                "id='$id', " +
                "name='$name', " +
                "address='$address', " +
                "descriptionText='$descriptionText', " +
                "routablePoints='$routablePoints', " +
                "categories='$categories', " +
                "makiIcon='$makiIcon', " +
                "coordinate='$coordinate', " +
                "types='$types', " +
                "etaMinutes='$etaMinutes', " +
                "metadata='$metadata', " +
                "requestOptions='$requestOptions', " +
                "distanceMeters='$distanceMeters'" +
                ")"
    }
}
