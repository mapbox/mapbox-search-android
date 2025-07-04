package com.mapbox.search.common.parking

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * Parking information for POIs that represent parking facilities, e.g., parking lots,
 * garages, street parking etc.
 */
@MapboxExperimental
@Parcelize
public class ParkingInfo(

    /**
     * Number of parking spots.
     */
    public val capacity: Int?,

    /**
     * Rate information for parking.
     */
    public val rateInfo: ParkingRateInfo?,

    /**
     * Number of available parking spots.
     */
    public val availability: Int?,

    /**
     * Availability or Probability based indicator, based on reported or predicted availability/probability.
     */
    @ParkingAvailabilityLevel.Type
    public val availabilityLevel: String?,

    /**
     * Timestamp when availability was last updated.
     */
    public val availabilityUpdatedAt: String?,

    /**
     * Availability trend over time.
     */
    @ParkingTrend.Type
    public val trend: String?,

    /**
     * Payment types accepted, values declared in [ParkingPaymentMethod.Type].
     */
    public val paymentMethods: List<String>?,

    /**
     * Payment types accepted, values declared in [ParkingPaymentType.Type].
     */
    public val paymentTypes: List<String>?,

    /**
     * Parking restrictions, values declared in [ParkingRestriction.Type].
     */
    public val restrictions: List<String>?,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParkingInfo

        if (capacity != other.capacity) return false
        if (rateInfo != other.rateInfo) return false
        if (availability != other.availability) return false
        if (availabilityLevel != other.availabilityLevel) return false
        if (availabilityUpdatedAt != other.availabilityUpdatedAt) return false
        if (trend != other.trend) return false
        if (paymentMethods != other.paymentMethods) return false
        if (paymentTypes != other.paymentTypes) return false
        if (restrictions != other.restrictions) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = capacity ?: 0
        result = 31 * result + (rateInfo?.hashCode() ?: 0)
        result = 31 * result + (availability ?: 0)
        result = 31 * result + (availabilityLevel?.hashCode() ?: 0)
        result = 31 * result + (availabilityUpdatedAt?.hashCode() ?: 0)
        result = 31 * result + (trend?.hashCode() ?: 0)
        result = 31 * result + (paymentMethods?.hashCode() ?: 0)
        result = 31 * result + (paymentTypes?.hashCode() ?: 0)
        result = 31 * result + (restrictions?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "ParkingInfo(" +
                "capacity=$capacity, " +
                "rateInfo=$rateInfo, " +
                "availability=$availability, " +
                "availabilityLevel=$availabilityLevel, " +
                "availabilityUpdatedAt=$availabilityUpdatedAt, " +
                "trend=$trend, " +
                "paymentMethods=$paymentMethods, " +
                "paymentTypes=$paymentTypes, " +
                "restrictions=$restrictions" +
                ")"
    }
}
