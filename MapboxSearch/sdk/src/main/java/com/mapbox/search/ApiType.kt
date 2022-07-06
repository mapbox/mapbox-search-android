package com.mapbox.search

import com.mapbox.search.Reserved.Flags
import com.mapbox.search.core.CoreApiType

/**
 * Experimental API, can be changed or removed in the next SDK releases.
 *
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

    /**
     * Autofill API.
     */
    AUTOFILL,
}

@JvmSynthetic
internal fun ApiType.mapToCore(): CoreApiType {
    return when (this) {
        ApiType.GEOCODING -> CoreApiType.GEOCODING
        ApiType.SBS -> CoreApiType.SBS
        ApiType.AUTOFILL -> CoreApiType.AUTOFILL
    }
}
