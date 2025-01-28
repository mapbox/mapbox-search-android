package com.mapbox.search.common.ev

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * OCPI Capability. The capabilities of an EVSE.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#143-capability-enum)
 * for more details.
 */
@MapboxExperimental
public object EvseCapability {

    /**
     * The EVSE supports charging profiles.
     */
    public const val CHARGING_PROFILE_CAPABLE: String = "CHARGING_PROFILE_CAPABLE"

    /**
     * The EVSE supports charging preferences.
     */
    public const val CHARGING_PREFERENCES_CAPABLE: String = "CHARGING_PREFERENCES_CAPABLE"

    /**
     * EVSE has a payment terminal that supports chip cards.
     */
    public const val CHIP_CARD_SUPPORT: String = "CHIP_CARD_SUPPORT"

    /**
     * EVSE has a payment terminal that supports contactless cards.
     */
    public const val CONTACTLESS_CARD_SUPPORT: String = "CONTACTLESS_CARD_SUPPORT"

    /**
     * EVSE has a payment terminal that makes it possible to pay for charging using a credit card.
     */
    public const val CREDIT_CARD_PAYABLE: String = "CREDIT_CARD_PAYABLE"

    /**
     * EVSE has a payment terminal that makes it possible to pay for charging using a debit card.
     */
    public const val DEBIT_CARD_PAYABLE: String = "DEBIT_CARD_PAYABLE"

    /**
     * EVSE has a payment terminal with a pin-code entry device.
     */
    public const val PED_TERMINAL: String = "PED_TERMINAL"

    /**
     * The EVSE can remotely be started/stopped.
     */
    public const val REMOTE_START_STOP_CAPABLE: String = "REMOTE_START_STOP_CAPABLE"

    /**
     * The EVSE can be reserved.
     */
    public const val RESERVABLE: String = "RESERVABLE"

    /**
     * Charging at this EVSE can be authorized with an RFID token.
     */
    public const val RFID_READER: String = "RFID_READER"

    /**
     * When a StartSession is received by this EVSE, the eMSP is required to add the optional c
     * onnector_id field in the StartSession object.
     */
    public const val START_SESSION_CONNECTOR_REQUIRED: String = "START_SESSION_CONNECTOR_REQUIRED"

    /**
     * This EVSE supports token groups, two or more tokens work as one, so that a session
     * can be started with one token and stopped with another
     * (handy when an EV driver has both a card and key-fob).
     */
    public const val TOKEN_GROUP_CAPABLE: String = "TOKEN_GROUP_CAPABLE"

    /**
     * Connectors have mechanical lock that can be requested by the eMSP to be unlocked.
     */
    public const val UNLOCK_CAPABLE: String = "UNLOCK_CAPABLE"

    /**
     * The capability of the EVSE is unknown.
     */
    public const val UNKNOWN: String = "UNKNOWN"

    /**
     * Retention policy for the EvseCapability.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        CHARGING_PROFILE_CAPABLE,
        CHARGING_PREFERENCES_CAPABLE,
        CHIP_CARD_SUPPORT,
        CONTACTLESS_CARD_SUPPORT,
        CREDIT_CARD_PAYABLE,
        DEBIT_CARD_PAYABLE,
        PED_TERMINAL,
        REMOTE_START_STOP_CAPABLE,
        RESERVABLE,
        RFID_READER,
        START_SESSION_CONNECTOR_REQUIRED,
        TOKEN_GROUP_CAPABLE,
        UNLOCK_CAPABLE,
        UNKNOWN,
    )
    public annotation class Type
}
