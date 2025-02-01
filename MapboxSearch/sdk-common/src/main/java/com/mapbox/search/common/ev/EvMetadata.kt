package com.mapbox.search.common.ev

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * EV-related metadata container.
 *
 * @property evLocation The [EvLocation] object describes the location and its properties where
 * a group of EVSEs that belong together are installed.
 */
@MapboxExperimental
@Parcelize
public class EvMetadata @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    public val evLocation: EvLocation?,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EvMetadata

        return evLocation == other.evLocation
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return evLocation?.hashCode() ?: 0
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "EvMetadata(evLocation=$evLocation)"
    }
}
