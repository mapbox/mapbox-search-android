package com.mapbox.search.result

import android.os.Parcelable
import com.mapbox.search.base.core.CoreResultAccuracy
import com.mapbox.search.base.failDebug
import kotlinx.parcelize.Parcelize

/**
 * A point accuracy metric for the returned address feature.
 *
 * Note that this list is subject to change. For details on these options,
 * see [Point accuracy for address features](https://docs.mapbox.com/api/search/geocoding/#point-accuracy-for-address-features).
 */
public abstract class ResultAccuracy : Parcelable {

    /**
     * Result is for a specific building/entrance.
     */
    @Parcelize
    public object Rooftop : ResultAccuracy()

    /**
     * Result is derived from a parcel centroid.
     */
    @Parcelize
    public object Parcel : ResultAccuracy()

    /**
     * Result is a known address point but has no specific accuracy.
     */
    @Parcelize
    public object Point : ResultAccuracy()

    /**
     * Result has been interpolated from an address range.
     */
    @Parcelize
    public object Interpolated : ResultAccuracy()

    /**
     * Result is for a block or intersection.
     */
    @Parcelize
    public object Intersection : ResultAccuracy()

    /**
     * Result is an approximate location.
     */
    @Parcelize
    public object Approximate : ResultAccuracy()

    /**
     * Result is a street centroid.
     */
    @Parcelize
    public object Street : ResultAccuracy()
}

@JvmSynthetic
internal fun ResultAccuracy.mapToCore(): CoreResultAccuracy? {
    return when (this) {
        is ResultAccuracy.Point -> CoreResultAccuracy.POINT
        is ResultAccuracy.Rooftop -> CoreResultAccuracy.ROOFTOP
        is ResultAccuracy.Parcel -> CoreResultAccuracy.PARCEL
        is ResultAccuracy.Interpolated -> CoreResultAccuracy.INTERPOLATED
        is ResultAccuracy.Intersection -> CoreResultAccuracy.INTERSECTION
        is ResultAccuracy.Approximate -> CoreResultAccuracy.APPROXIMATE
        is ResultAccuracy.Street -> CoreResultAccuracy.STREET
        else -> {
            failDebug {
                "Unprocessed accuracy type: $this"
            }
            null
        }
    }
}

@JvmSynthetic
internal fun CoreResultAccuracy.mapToPlatform(): ResultAccuracy {
    return when (this) {
        CoreResultAccuracy.POINT -> ResultAccuracy.Point
        CoreResultAccuracy.ROOFTOP -> ResultAccuracy.Rooftop
        CoreResultAccuracy.PARCEL -> ResultAccuracy.Parcel
        CoreResultAccuracy.INTERPOLATED -> ResultAccuracy.Interpolated
        CoreResultAccuracy.INTERSECTION -> ResultAccuracy.Intersection
        CoreResultAccuracy.APPROXIMATE -> ResultAccuracy.Approximate
        CoreResultAccuracy.STREET -> ResultAccuracy.Street
    }
}
