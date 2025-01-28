package com.mapbox.search.common.ev

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * OCPI StatusSchedule. This type is used to schedule status period in the future. Refer to
 * this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#1423-statusschedule-class)
 * for more details.
 *
 * @property periodBegin Begin timestamp in RFC 3339 format of the scheduled period.
 * @property periodEnd End timestamp in RFC 3339 format of the scheduled period, if known.
 * @property status Status value during the scheduled period.
 */
@MapboxExperimental
@Parcelize
public class EvStatusSchedule @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    public val periodBegin: String,
    public val periodEnd: String? = null,
    @EvseStatus.Type public val status: String,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EvStatusSchedule

        if (periodBegin != other.periodBegin) return false
        if (periodEnd != other.periodEnd) return false
        if (status != other.status) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = periodBegin.hashCode()
        result = 31 * result + (periodEnd?.hashCode() ?: 0)
        result = 31 * result + status.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "ChargingStatusSchedule(" +
                "periodBegin=$periodBegin, " +
                "periodEnd=$periodEnd, " +
                "status=$status" +
                ")"
    }
}
