package com.mapbox.search.common.ev

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * OCPI ConnectorFormat. The format of the connector, whether it is a socket or a plug.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#144-connectorformat-enum)
 * for more details.
 */
@MapboxExperimental
public object EvConnectorFormat {

    /**
     * The connector is a socket; the EV user needs to bring a fitting plug.
     */
    public const val SOCKET: String = "SOCKET"

    /**
     * The connector is an attached cable; the EV users car needs to have a fitting inlet.
     */
    public const val CABLE: String = "CABLE"

    /**
     * Unknown connector format.
     */
    public const val UNKNOWN: String = "UNKNOWN"

    /**
     * Retention policy for the ConnectorFormat.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        SOCKET,
        CABLE,
        UNKNOWN,
    )
    public annotation class Type
}
