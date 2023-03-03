package com.mapbox.search

import com.mapbox.search.Reserved.Flags
import com.mapbox.search.base.core.CoreApiType

/**
 * The type of the API used by one of the Search Engines.
 */
public enum class ApiType {

    /**
     * Geocoding API.
     */
    GEOCODING,

    /**
     * Single Box Search API.
     */
    @Reserved(Flags.SBS)
    SBS,
}

@JvmSynthetic
internal fun ApiType.mapToCore(): CoreApiType {
    return when (this) {
        ApiType.GEOCODING -> CoreApiType.GEOCODING
        ApiType.SBS -> CoreApiType.SBS
    }
}
