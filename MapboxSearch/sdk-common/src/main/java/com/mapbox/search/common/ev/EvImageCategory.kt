package com.mapbox.search.common.ev

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * OCPI ImageCategory. The category of an image to get the correct usage in a user presentation.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#1416-imagecategory-enum)
 * for more details.
 */
@MapboxExperimental
public object EvImageCategory {

    /**
     * Photo of the physical device that contains one or more EVSEs.
     */
    public const val CHARGER: String = "CHARGER"

    /**
     * Location entrance photo. Should show the car entrance to the location from street side.
     */
    public const val ENTRANCE: String = "ENTRANCE"

    /**
     * Location overview photo.
     */
    public const val LOCATION: String = "LOCATION"

    /**
     * Logo of an associated roaming network to be displayed with the EVSE, for example, in lists, maps,
     * and detailed information views.
     */
    public const val NETWORK: String = "NETWORK"

    /**
     * Logo of the charge point operator, for example a municipality, to be displayed in the EVSE's
     * detailed information view or in lists and maps, if no network logo is present.
     */
    public const val OPERATOR: String = "OPERATOR"

    /**
     * Other.
     */
    public const val OTHER: String = "OTHER"

    /**
     * Logo of the charge point owner, for example a local store, to be displayed in the EVSE's
     * detailed information view.
     */
    public const val OWNER: String = "OWNER"

    /**
     * Retention policy for the RoadObjectProvider.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        CHARGER,
        ENTRANCE,
        LOCATION,
        NETWORK,
        OPERATOR,
        OTHER,
        OWNER,
    )
    public annotation class Type
}
