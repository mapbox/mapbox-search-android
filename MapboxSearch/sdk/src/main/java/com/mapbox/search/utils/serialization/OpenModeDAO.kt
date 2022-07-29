package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.search.base.utils.printableName
import com.mapbox.search.metadata.OpenHours

internal enum class OpenModeDAO {

    @SerializedName("ALWAYS_OPEN")
    ALWAYS_OPEN,
    @SerializedName("TEMPORARILY_CLOSED")
    TEMPORARILY_CLOSED,
    @SerializedName("PERMANENTLY_CLOSED")
    PERMANENTLY_CLOSED,
    @SerializedName("SCHEDULED")
    SCHEDULED;

    companion object {

        fun create(type: OpenHours): OpenModeDAO {
            return when (type) {
                is OpenHours.AlwaysOpen -> ALWAYS_OPEN
                is OpenHours.TemporaryClosed -> TEMPORARILY_CLOSED
                is OpenHours.PermanentlyClosed -> PERMANENTLY_CLOSED
                is OpenHours.Scheduled -> SCHEDULED
                else -> error("Unknown OpenHours subclass: ${type.javaClass.printableName}.")
            }
        }
    }
}
