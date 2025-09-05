package com.mapbox.search

import com.mapbox.search.Reserved.Flags
import com.mapbox.search.base.core.CoreApiType

/**
 * The type of the API used by one of the Search Engines.
 */
public enum class ApiType {

    /**
     * Geocoding v5 API.
     *
     * Note: The [ApiType.GEOCODING] API type is supported in compatibility mode. For example,
     * the Geocoding v5 API will align with Search Box response types. If your application
     * relies on searching for POIs like restaurants, gas stations, and landmarks, we recommend
     * transitioning to the [ApiType.SEARCH_BOX] api. If your use case is primarily
     * address and place search, you can continue using Geocoding v5 without interruption
     *
     * Additionally, please note that Points of Interest (POI) data will be removed
     * from the Geocoding v5 API on December 20, 2024.
     *
     * For more information, visit [Geocoding v5 API page](https://docs.mapbox.com/api/search/geocoding-v5/).
     */
    GEOCODING,

    /**
     * Single Box Search API. Deprecated, use [ApiType.SEARCH_BOX] instead.
     */
    @Reserved(Flags.SBS)
    @Deprecated("SBS Api Type is deprecated, use SEARCH_BOX instead", ReplaceWith("SEARCH_BOX"))
    SBS,

    /**
     * Search Box API.
     *
     * For more information, visit [Search Box API page](https://docs.mapbox.com/api/search/search-box/).
     */
    SEARCH_BOX,
}

@Suppress("DEPRECATION")
@JvmSynthetic
internal fun ApiType.mapToCore(): CoreApiType {
    return when (this) {
        ApiType.GEOCODING -> CoreApiType.GEOCODING
        ApiType.SBS -> CoreApiType.SBS
        ApiType.SEARCH_BOX -> CoreApiType.SEARCH_BOX
    }
}
