package com.mapbox.search.common.ev

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * OCPI Facility. A facility to which a charging location directly belongs.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#1412-facility-enum)
 * for more details.
 */
@MapboxExperimental
public object EvFacility {

    /**
     * A hotel.
     */
    public const val HOTEL: String = "HOTEL"

    /**
     * A restaurant.
     */
    public const val RESTAURANT: String = "RESTAURANT"

    /**
     * A cafe.
     */
    public const val CAFE: String = "CAFE"

    /**
     * A mall or shopping center.
     */
    public const val MALL: String = "MALL"

    /**
     * A supermarket.
     */
    public const val SUPERMARKET: String = "SUPERMARKET"

    /**
     * Sport facilities: gym, field, etc.
     */
    public const val SPORT: String = "SPORT"

    /**
     * A recreation area.
     */
    public const val RECREATION_AREA: String = "RECREATION_AREA"

    /**
     * Located in, or close to, a park, nature reserve, etc.
     */
    public const val NATURE: String = "NATURE"

    /**
     * A museum.
     */
    public const val MUSEUM: String = "MUSEUM"

    /**
     * A bike/e-bike/e-scooter sharing location.
     */
    public const val BIKE_SHARING: String = "BIKE_SHARING"

    /**
     * A bus stop.
     */
    public const val BUS_STOP: String = "BUS_STOP"

    /**
     * A taxi stand.
     */
    public const val TAXI_STAND: String = "TAXI_STAND"

    /**
     * A tram stop/station.
     */
    public const val TRAM_STOP: String = "TRAM_STOP"

    /**
     * A metro station.
     */
    public const val METRO_STATION: String = "METRO_STATION"

    /**
     * A train station.
     */
    public const val TRAIN_STATION: String = "TRAIN_STATION"

    /**
     * An airport.
     */
    public const val AIRPORT: String = "AIRPORT"

    /**
     * A parking lot.
     */
    public const val PARKING_LOT: String = "PARKING_LOT"

    /**
     * A carpool parking.
     */
    public const val CARPOOL_PARKING: String = "CARPOOL_PARKING"

    /**
     * A fuel station.
     */
    public const val FUEL_STATION: String = "FUEL_STATION"

    /**
     * Wifi or other type of internet available.
     */
    public const val WIFI: String = "WIFI"

    /**
     * Unknown type.
     */
    public const val UNKNOWN: String = "UNKNOWN"

    /**
     * Retention policy for the RoadObjectProvider.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        HOTEL,
        RESTAURANT,
        CAFE,
        MALL,
        SUPERMARKET,
        SPORT,
        RECREATION_AREA,
        NATURE,
        MUSEUM,
        BIKE_SHARING,
        BUS_STOP,
        TAXI_STAND,
        TRAM_STOP,
        METRO_STATION,
        TRAIN_STATION,
        AIRPORT,
        PARKING_LOT,
        CARPOOL_PARKING,
        FUEL_STATION,
        WIFI,
        UNKNOWN,
    )
    public annotation class Type
}
