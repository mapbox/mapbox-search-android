package com.mapbox.search.common

import android.util.Base64

/**
 * Utility class.
 */
public object OsmIdUtils {
    /**
     * Mapping of supported OSM types -- node, way, and relation
     */
    private val osmTypes = mapOf(0 to "n", 1 to "w", 4 to "r")

    /**
     * Converts the supplied POI ID to a Mapbox ID
     * @param poiId POI ID to convert
     * @return [String] a Mapbox ID
     */
    @JvmStatic
    public fun fromPoiId(poiId: Long): String? {
        val lastDigit = poiId % 10
        return osmTypes[lastDigit.toInt()]?.let { osmType ->
            val originalId = poiId / 10
            val mbxPoiId = "urn:mbxpoi-osm:$osmType$originalId"
            val bytes = mbxPoiId.toByteArray(Charsets.UTF_8)
            return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP)
        }
    }
}
