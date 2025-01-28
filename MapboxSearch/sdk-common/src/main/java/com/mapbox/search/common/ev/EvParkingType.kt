package com.mapbox.search.common.ev

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * OCPI ParkingType. Reflects the general type of the charge point’s location.
 * May be used for user information. Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#1418-parkingtype-enum)
 * for more details.
 */
@MapboxExperimental
public object EvParkingType {

    /**
     * Location on a parking facility/rest area along a motorway, freeway, interstate, highway etc.
     */
    public const val ALONG_MOTORWAY: String = "ALONG_MOTORWAY"

    /**
     * Multistorey car park.
     */
    public const val PARKING_GARAGE: String = "PARKING_GARAGE"

    /**
     * A cleared area that is intended for parking vehicles, as in at super markets, bars, etc.
     */
    public const val PARKING_LOT: String = "PARKING_LOT"

    /**
     * Location is on the driveway of a house/building.
     */
    public const val ON_DRIVEWAY: String = "ON_DRIVEWAY"

    /**
     * Parking in public space along a street.
     */
    public const val ON_STREET: String = "ON_STREET"

    /**
     * Multistorey car park, mainly underground.
     */
    public const val UNDERGROUND_GARAGE: String = "UNDERGROUND_GARAGE"

    /**
     * Unknown parking type.
     */
    public const val UNKNOWN: String = ""

    /**
     * Retention policy for the RoadObjectProvider
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        ALONG_MOTORWAY,
        PARKING_GARAGE,
        PARKING_LOT,
        ON_DRIVEWAY,
        ON_STREET,
        UNDERGROUND_GARAGE,
        UNKNOWN,
    )
    public annotation class Type
}
