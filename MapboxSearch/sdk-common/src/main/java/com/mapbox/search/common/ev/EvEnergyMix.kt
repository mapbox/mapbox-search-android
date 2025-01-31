package com.mapbox.search.common.ev

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * OCPI EnergyMix. This type is used to specify the energy mix and environmental impact of the supplied energy at a location or in a tariff.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#146-energymix-class)
 * for more details.
 *
 * @property isGreenEnergy True if 100% from regenerative sources. (CO2 and nuclear waste is zero)
 * @property energySources List of of [EvEnergySource] objects representing energy sources of this location’s tariff in key-value pairs (enum + percentage).
 * @property environmentalImpact List of [EvEnvironmentalImpact] objects representing nuclear waste and CO2 exhaust of this location’s tariff in key-value pairs (enum + percentage).
 * @property supplierName Name of the energy supplier, delivering the energy for this location or tariff.
 * @property energyProductName Name of the energy supplier's product/tariff plan used at this location.
 */
@MapboxExperimental
@Parcelize
public class EvEnergyMix @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    public val isGreenEnergy: Boolean?,
    public val energySources: List<EvEnergySource>,
    public val environmentalImpact: List<EvEnvironmentalImpact>,
    public val supplierName: String?,
    public val energyProductName: String?
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EvEnergyMix

        if (isGreenEnergy != other.isGreenEnergy) return false
        if (energySources != other.energySources) return false
        if (environmentalImpact != other.environmentalImpact) return false
        if (supplierName != other.supplierName) return false
        if (energyProductName != other.energyProductName) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = isGreenEnergy.hashCode()
        result = 31 * result + energySources.hashCode()
        result = 31 * result + environmentalImpact.hashCode()
        result = 31 * result + (supplierName?.hashCode() ?: 0)
        result = 31 * result + (energyProductName?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "EvEnergyMix(" +
                "isGreenEnergy=$isGreenEnergy, " +
                "energySources=$energySources, " +
                "environmentalImpact=$environmentalImpact, " +
                "supplierName=$supplierName, " +
                "energyProductName=$energyProductName" +
                ")"
    }
}
