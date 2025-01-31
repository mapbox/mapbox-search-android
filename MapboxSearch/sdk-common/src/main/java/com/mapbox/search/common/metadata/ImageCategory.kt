package com.mapbox.search.common.metadata

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * The category of an image to get the correct usage in a user presentation.
 * One of examples is [OCPI image category](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#1416-imagecategory-enum)
 */
@MapboxExperimental
public object ImageCategory {

    /**
     * Photo of the physical device that contains one or more EVSEs.
     */
    public const val EV_CHARGER: String = "EV_CHARGER"

    /**
     * Logo of an associated roaming network to be displayed with the EVSE, for example, in lists, maps,
     * and detailed information views.
     */
    public const val EV_NETWORK: String = "EV_NETWORK"

    /**
     * Logo of the charge point operator, for example a municipality, to be displayed in the EVSE's
     * detailed information view or in lists and maps, if no network logo is present.
     */
    public const val EV_OPERATOR: String = "EV_OPERATOR"

    /**
     * Logo of the charge point owner, for example a local store, to be displayed in the EVSE's
     * detailed information view.
     */
    public const val EV_OWNER: String = "EV_OWNER"

    /**
     * Location entrance photo. Should show the car entrance to the location from street side.
     */
    public const val ENTRANCE: String = "ENTRANCE"

    /**
     * Location overview photo.
     */
    public const val LOCATION: String = "LOCATION"

    /**
     * Other.
     */
    public const val OTHER: String = "OTHER"

    /**
     * Retention policy for the RoadObjectProvider.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        EV_CHARGER,
        EV_NETWORK,
        EV_OPERATOR,
        EV_OWNER,
        ENTRANCE,
        LOCATION,
        OTHER,
    )
    public annotation class Type
}
