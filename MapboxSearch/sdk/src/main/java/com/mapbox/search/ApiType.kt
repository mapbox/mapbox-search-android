package com.mapbox.search

import com.mapbox.search.base.core.CoreApiType

/**
 * The type of the API used by one of the Search Engines.
 */
public abstract class ApiType {

    /**
     * Geocoding API.
     * @see [Geocoding Api documentation](https://docs.mapbox.com/api/search/geocoding/)
     */
    public object Geocoding : ApiType()

    /**
     * Search Box API.
     * @see [Search Box Api documentation](https://docs.mapbox.com/api/search/search-box/)
     */
    public object SearchBox : ApiType()
}

@JvmSynthetic
internal fun ApiType.mapToCore(): CoreApiType {
    return when (this) {
        is ApiType.Geocoding -> CoreApiType.GEOCODING
        is ApiType.SearchBox -> CoreApiType.SEARCH_BOX
        else -> error("Unknown Api Type")
    }
}
