package com.mapbox.search.common.ev

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.common.parking.ParkingType
import kotlinx.parcelize.Parcelize

/**
 * OCPI Connector. A Connector is the socket or cable and plug available for the EV to use.
 * A single EVSE may provide multiple Connectors but only one of them can be in use at the same time.
 * A Connector always belongs to an EVSE object. Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#mod_locations_connector_object)
 * for more details.
 *
 * @property id Identifier of the Connector within the EVSE. Two Connectors may have the same id
 * as long as they do not belong to the same EVSE object.
 * @property standard [EvConnectorType.Type] value indicating the standard of the installed connector.
 * @property format [EvConnectorFormat.Type] value indicating the format (socket/cable) of the installed connector.
 * @property powerType [ParkingType.Type] value indicating electrical power configuration.
 * @property maxVoltage Maximum voltage of the connector (line to neutral for AC_3_PHASE), in volt (V).
 * For example: DC Chargers might vary the voltage during charging when battery almost full.
 * @property maxAmperage Maximum amperage of the connector, in ampere (A).
 * @property maxElectricPower Maximum electric power that can be delivered by this connector, in Watts (W).
 * Used when the maximum electric power is lower than the calculated value from voltage and amperage.
 * For example: A DC Charge Point which can deliver up to 920V and up to 400A can be limited to a maximum
 * of 150kW (maxElectricPower = 150000). Depending on the car, it may supply max voltage or current,
 * but not both at the same time. For AC Charge Points, the amount of phases used can also influence the maximum power.
 * @property tariffIds Identifiers of the valid charging tariffs. Multiple tariffs are possible, but only one
 * of each Tariff.type can be active at the same time. Tariffs with the same type are only allowed if they are not
 * active at the same time: start_date_time and end_date_time period not overlapping. Only included in the response
 * of the Get Charge Point details by ID API.
 * @property termsAndConditions URL to the operator’s terms and conditions.
 * @property lastUpdated Timestamp in RFC 3339 format when this Connector was last updated (or created).
 */
@MapboxExperimental
@Parcelize
public class EvConnector @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    public val id: String?,
    @EvConnectorType.Type public val standard: String,
    @EvConnectorFormat.Type public val format: String,
    @EvPowerType.Type public val powerType: String,
    public val maxVoltage: Int,
    public val maxAmperage: Int,
    public val maxElectricPower: Int? = null,
    public val tariffIds: List<String>,
    public val termsAndConditions: String? = null,
    public val lastUpdated: String?,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EvConnector

        if (id != other.id) return false
        if (standard != other.standard) return false
        if (format != other.format) return false
        if (powerType != other.powerType) return false
        if (maxVoltage != other.maxVoltage) return false
        if (maxAmperage != other.maxAmperage) return false
        if (maxElectricPower != other.maxElectricPower) return false
        if (tariffIds != other.tariffIds) return false
        if (termsAndConditions != other.termsAndConditions) return false
        if (lastUpdated != other.lastUpdated) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + standard.hashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + powerType.hashCode()
        result = 31 * result + maxVoltage
        result = 31 * result + maxAmperage
        result = 31 * result + (maxElectricPower ?: 0)
        result = 31 * result + tariffIds.hashCode()
        result = 31 * result + (termsAndConditions?.hashCode() ?: 0)
        result = 31 * result + lastUpdated.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "EvConnector(" +
                "id=$id, " +
                "standard=$standard, " +
                "format=$format, " +
                "powerType=$powerType, " +
                "maxVoltage=$maxVoltage, " +
                "maxAmperage=$maxAmperage, " +
                "maxElectricPower=$maxElectricPower, " +
                "tariffIds=$tariffIds, " +
                "termsAndConditions=$termsAndConditions, " +
                "lastUpdated=$lastUpdated" +
                ")"
    }
}
