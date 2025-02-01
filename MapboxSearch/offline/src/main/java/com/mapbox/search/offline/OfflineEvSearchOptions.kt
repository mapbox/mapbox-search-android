package com.mapbox.search.offline

import android.os.Parcelable
import com.mapbox.search.base.core.createCoreEvSearchOptions
import com.mapbox.search.base.factory.createCoreConnectorType
import com.mapbox.search.base.utils.extension.safeCompareTo
import com.mapbox.search.common.ev.EvConnectorType
import kotlinx.parcelize.Parcelize

/**
 * Offline EV search options.
 *
 * @property connectorTypes An optional list of the [EvConnectorType.Type] values to be included
 * in the response. By default all connector types will be included in the response.
 * @property operators An optional comma-delimited list of the charge point operators
 * to be included in the response. By default all operators will be included in the response.
 * @property minChargingPower An optional value in watts that sets the lower limit for the
 * charging power supported by EVSEs at a charge point. If [maxChargingPower] is provided,
 * the default value is 0.
 * @property maxChargingPower An optional value in watts that sets the upper limit for the
 * charging power supported by EVSEs at a charge point. If [minChargingPower] is provided,
 * the default value is 500000.
 */
@Parcelize
public class OfflineEvSearchOptions @JvmOverloads public constructor(
    public val connectorTypes: List<String>? = null,
    public val operators: List<String>? = null,
    public val minChargingPower: Float? = null,
    public val maxChargingPower: Float? = null,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineEvSearchOptions

        if (connectorTypes != other.connectorTypes) return false
        if (operators != other.operators) return false
        if (!minChargingPower.safeCompareTo(other.minChargingPower)) return false
        if (!maxChargingPower.safeCompareTo(other.maxChargingPower)) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = connectorTypes?.hashCode() ?: 0
        result = 31 * result + (operators?.hashCode() ?: 0)
        result = 31 * result + (minChargingPower?.hashCode() ?: 0)
        result = 31 * result + (maxChargingPower?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "OfflineEvSearchOptions(" +
                "connectorTypes=$connectorTypes, " +
                "operators=$operators, " +
                "minChargingPower=$minChargingPower, " +
                "maxChargingPower=$maxChargingPower" +
                ")"
    }

    @JvmSynthetic
    internal fun mapToCore() = createCoreEvSearchOptions(
        connectorTypes = connectorTypes?.mapNotNull { createCoreConnectorType(it) },
        operators = operators,
        minChargingPower = minChargingPower,
        maxChargingPower = maxChargingPower,
    )
}
