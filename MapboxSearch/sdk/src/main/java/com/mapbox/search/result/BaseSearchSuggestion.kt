package com.mapbox.search.result

internal abstract class BaseSearchSuggestion : SearchSuggestion, CoreResponseProvider {

    override val id: String
        get() = originalSearchResult.id

    override val name: String
        get() = originalSearchResult.names[0]

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

    override val isBatchResolveSupported: Boolean
        get() = originalSearchResult.action?.multiRetrievable == true

    protected fun baseToString(): String {
        return "id='$id', " +
                "name='$name', " +
                "address='$address', " +
                "descriptionText='$descriptionText', " +
                "distanceMeters='$distanceMeters', " +
                "makiIcon='$makiIcon', " +
                "type='$type', " +
                "etaMinutes='$etaMinutes', " +
                "isBatchResolveSupported='$isBatchResolveSupported', " +
                "requestOptions='$requestOptions'"
    }

    override fun toString(): String {
        return "SearchSuggestion(${baseToString()})"
    }
}
