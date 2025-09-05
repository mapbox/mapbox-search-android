package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.search.common.metadata.ParkingData

internal class ParkingDataDAO(
    @SerializedName("totalCapacity") val totalCapacity: Int? = null,
    @SerializedName("reservedForDisabilities") val reservedForDisabilities: Int? = null
) : DataAccessObject<ParkingData> {

    override val isValid: Boolean
        get() = totalCapacity != null && reservedForDisabilities != null

    override fun createData(): ParkingData {
        return ParkingData(
            totalCapacity = totalCapacity!!,
            reservedForDisabilities = reservedForDisabilities!!
        )
    }

    companion object {

        fun create(parking: ParkingData?): ParkingDataDAO? {
            parking ?: return null
            return with(parking) {
                ParkingDataDAO(
                    totalCapacity = totalCapacity,
                    reservedForDisabilities = reservedForDisabilities
                )
            }
        }
    }
}
