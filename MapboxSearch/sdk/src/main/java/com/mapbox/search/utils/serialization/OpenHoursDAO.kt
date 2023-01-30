package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.search.common.metadata.OpenHours

internal class OpenHoursDAO(
    @SerializedName("mode") val mode: OpenModeDAO? = null,
    @SerializedName("periods") val periods: List<OpenPeriodDAO>? = null
) : DataAccessObject<OpenHours?> {

    override val isValid: Boolean
        get() = mode != null && when (mode) {
            OpenModeDAO.ALWAYS_OPEN,
            OpenModeDAO.TEMPORARILY_CLOSED,
            OpenModeDAO.PERMANENTLY_CLOSED -> true
            OpenModeDAO.SCHEDULED -> !periods?.filter { it.isValid }.isNullOrEmpty()
        }

    override fun createData(): OpenHours {
        return when (mode!!) {
            OpenModeDAO.ALWAYS_OPEN -> OpenHours.AlwaysOpen
            OpenModeDAO.TEMPORARILY_CLOSED -> OpenHours.TemporaryClosed
            OpenModeDAO.PERMANENTLY_CLOSED -> OpenHours.PermanentlyClosed
            OpenModeDAO.SCHEDULED -> {
                val validPeriods = periods?.filter { it.isValid }?.map { it.createData() }
                if (validPeriods.isNullOrEmpty()) {
                    error("OpenHours.periods must not be empty")
                } else {
                    OpenHours.Scheduled(periods = validPeriods)
                }
            }
        }
    }

    companion object {

        fun create(type: OpenHours?): OpenHoursDAO? {
            type ?: return null
            return with(type) {
                val periods = (this as? OpenHours.Scheduled)?.periods ?: emptyList()
                OpenHoursDAO(
                    mode = OpenModeDAO.create(this),
                    periods = periods.map { OpenPeriodDAO.create(it) }
                )
            }
        }
    }
}
