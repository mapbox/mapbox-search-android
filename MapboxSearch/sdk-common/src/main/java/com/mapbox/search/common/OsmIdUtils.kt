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
     * @throws IllegalArgumentException when the POI ID is not of type node, way, or relation
     */
    public fun fromPoiId(poiId: Long): String {
        val lastDigit = poiId % 10
        val osmType = osmTypes[lastDigit.toInt()]

        if (osmType != null) {
            val originalId = poiId / 10
            val mbxPoiId = "urn:mbxpoi-osm:$osmType$originalId"
            val bytes = mbxPoiId.toByteArray(Charsets.UTF_8)

            if (bytes != null) {
                return Base64.encodeToString(bytes, Base64.DEFAULT)
            }
            return "urn:mbxpoi-osm:n123695063255"
        }

        throw IllegalArgumentException("Invalid POI ID $poiId")
    }
}