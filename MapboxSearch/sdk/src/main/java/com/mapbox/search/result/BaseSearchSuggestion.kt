package com.mapbox.search.result

import com.mapbox.search.SearchResultMetadata

internal sealed class BaseSearchSuggestion(
    @Transient
    open val originalSearchResult: OriginalSearchResult
) : SearchSuggestion {

    override val id: String
        get() = originalSearchResult.id

    override val name: String
        get() = originalSearchResult.names[0]

    override val matchingName: String?
        get() = originalSearchResult.matchingName

    override val descriptionText: String?
        get() = originalSearchResult.descriptionAddress

    override val address: SearchAddress?
        get() = originalSearchResult.addresses?.first()

    override val distanceMeters: Double?
        get() = originalSearchResult.distanceMeters

    override val makiIcon: String?
        get() = originalSearchResult.icon

    override val etaMinutes: Double?
        get() = originalSearchResult.etaMinutes

    override val metadata: SearchResultMetadata?
        get() = originalSearchResult.metadata

    override val isBatchResolveSupported: Boolean
        get() = originalSearchResult.action?.multiRetrievable == true

    override val serverIndex: Int?
        get() = originalSearchResult.serverIndex

    protected fun baseToString(): String {
        return "id='$id', " +
                "name='$name', " +
                "matchingName='$matchingName', " +
                "address='$address', " +
                "descriptionText='$descriptionText', " +
                "distanceMeters='$distanceMeters', " +
                "makiIcon='$makiIcon', " +
                "type='$type', " +
                "etaMinutes='$etaMinutes', " +
                "metadata='$metadata', " +
                "isBatchResolveSupported='$isBatchResolveSupported', " +
                "serverIndex='$serverIndex', " +
                "requestOptions='$requestOptions'"
    }

    override fun toString(): String {
        return "SearchSuggestion(${baseToString()})"
    }
}
