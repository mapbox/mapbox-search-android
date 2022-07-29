package com.mapbox.search.result

import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.base.result.BaseRawSearchResult
import java.util.Collections

internal sealed class AbstractSearchSuggestion(
    @Transient
    open val rawSearchResult: BaseRawSearchResult
) : SearchSuggestion {

    override val id: String
        get() = rawSearchResult.id

    override val name: String
        get() = rawSearchResult.names[0]

    override val matchingName: String?
        get() = rawSearchResult.matchingName

    override val descriptionText: String?
        get() = rawSearchResult.descriptionAddress

    override val address: SearchAddress?
        get() = rawSearchResult.addresses?.first()?.mapToPlatform()

    override val distanceMeters: Double?
        get() = rawSearchResult.distanceMeters

    override val categories: List<String>
        get() = rawSearchResult.categories ?: emptyList()

    override val makiIcon: String?
        get() = rawSearchResult.icon

    override val etaMinutes: Double?
        get() = rawSearchResult.etaMinutes

    override val metadata: SearchResultMetadata?
        get() = rawSearchResult.metadata?.let { SearchResultMetadata(it) }

    override val externalIDs: Map<String, String>
        get() = Collections.unmodifiableMap(rawSearchResult.externalIDs ?: emptyMap())

    override val isBatchResolveSupported: Boolean
        get() = rawSearchResult.action?.multiRetrievable == true

    override val serverIndex: Int?
        get() = rawSearchResult.serverIndex

    protected fun baseToString(): String {
        return "id='$id', " +
                "name='$name', " +
                "matchingName='$matchingName', " +
                "address='$address', " +
                "descriptionText='$descriptionText', " +
                "distanceMeters='$distanceMeters', " +
                "categories='$categories', " +
                "makiIcon='$makiIcon', " +
                "type='$type', " +
                "etaMinutes='$etaMinutes', " +
                "metadata='$metadata', " +
                "externalIDs='$externalIDs`, " +
                "isBatchResolveSupported='$isBatchResolveSupported', " +
                "serverIndex='$serverIndex', " +
                "requestOptions='$requestOptions'"
    }

    override fun toString(): String {
        return "SearchSuggestion(${baseToString()})"
    }
}
