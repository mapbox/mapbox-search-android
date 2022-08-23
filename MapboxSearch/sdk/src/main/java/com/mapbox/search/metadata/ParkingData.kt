package com.mapbox.search.metadata

import com.mapbox.search.base.assertDebug
import com.mapbox.search.base.core.CoreParkingData

/**
 * Parking information for the POI.
 */
public class ParkingData(

    /**
     * Number of parking spots.
     */
    public val totalCapacity: Int,

    /**
     * Number of spots for persons with disabilities.
     */
    public val reservedForDisabilities: Int
) {

    init {
        assertDebug(totalCapacity >= 0) {
            "Negative `totalCapacity`: $totalCapacity"
        }
        assertDebug(reservedForDisabilities >= 0) {
            "Negative `reservedForDisabilities`: $reservedForDisabilities"
        }
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParkingData

        if (totalCapacity != other.totalCapacity) return false
        if (reservedForDisabilities != other.reservedForDisabilities) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = totalCapacity
        result = 31 * result + reservedForDisabilities
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "ParkingData(" +
                "totalCapacity=$totalCapacity, " +
                "reservedForDisabilities=$reservedForDisabilities" +
                ")"
    }
}

@JvmSynthetic
internal fun CoreParkingData.mapToPlatform() = ParkingData(
    totalCapacity = capacity,
    reservedForDisabilities = forDisabilities
)

@JvmSynthetic
internal fun ParkingData.mapToCore() = CoreParkingData(
    totalCapacity,
    reservedForDisabilities
)
