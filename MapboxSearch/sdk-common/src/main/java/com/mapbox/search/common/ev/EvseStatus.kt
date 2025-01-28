package com.mapbox.search.common.ev

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * The status of an EVSE. Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#1422-status-enum)
 * for more details.
 */
@MapboxExperimental
public object EvseStatus {

    /**
     * The EVSE/Connector is able to start a new charging session.
     */
    public const val AVAILABLE: String = "AVAILABLE"

    /**
     * The EVSE/Connector is not accessible because of a physical barrier, as in a car.
     */
    public const val BLOCKED: String = "BLOCKED"

    /**
     * The EVSE/Connector is in use.
     */
    public const val CHARGING: String = "CHARGING"

    /**
     * The EVSE/Connector is not yet active, or temporarily not available for use, but not broken or defect.
     */
    public const val INOPERATIVE: String = "INOPERATIVE"

    /**
     * The EVSE/Connector is out of order, some part/components may be broken/defective.
     */
    public const val OUT_OF_ORDER: String = "OUT_OF_ORDER"

    /**
     * The EVSE/Connector is planned, will be operating soon.
     */
    public const val PLANNED: String = "PLANNED"

    /**
     * The EVSE/Connector was discontinued/removed.
     */
    public const val REMOVED: String = "REMOVED"

    /**
     * The EVSE/Connector is reserved for a particular EV driver and is unavailable for other drivers.
     */
    public const val RESERVED: String = "RESERVED"

    /**
     * The status is unknown.
     */
    public const val UNKNOWN: String = "UNKNOWN"

    /**
     * No status information available (also used when offline).
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        AVAILABLE,
        BLOCKED,
        CHARGING,
        INOPERATIVE,
        OUT_OF_ORDER,
        PLANNED,
        REMOVED,
        RESERVED,
        UNKNOWN,
    )
    public annotation class Type
}
