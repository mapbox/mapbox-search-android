package com.mapbox.search

/**
 * Settings used for [SearchEngine] configuration.
 * @see MapboxSearchSdk.initialize
 */
public class SearchEngineSettings @JvmOverloads constructor(

    /**
     * Geocoding API endpoint URL.
     */
    public val geocodingEndpointBaseUrl: String = DEFAULT_ENDPOINT_GEOCODING,

    /**
     * Single Box Search endpoint URL.
     */
    public val singleBoxSearchBaseUrl: String? = null,
) {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchEngineSettings

        if (geocodingEndpointBaseUrl != other.geocodingEndpointBaseUrl) return false
        if (singleBoxSearchBaseUrl != other.singleBoxSearchBaseUrl) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = geocodingEndpointBaseUrl.hashCode()
        result = 31 * result + (singleBoxSearchBaseUrl?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchEngineSettings(" +
                "geocodingEndpointBaseUrl='$geocodingEndpointBaseUrl', " +
                "singleBoxSearchBaseUrl=$singleBoxSearchBaseUrl" +
                ")"
    }

    private companion object {
        const val DEFAULT_ENDPOINT_GEOCODING: String = "https://api.mapbox.com"
    }
}
