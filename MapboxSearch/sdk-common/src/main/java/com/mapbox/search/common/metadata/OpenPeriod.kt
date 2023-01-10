package com.mapbox.search.common.metadata

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Time interval, when POI is available.
 */
@Parcelize
public data class OpenPeriod(

    /**
     * Time, when POI opens.
     */
    public val open: WeekTimestamp,

    /**
     * Time, when POI closes.
     */
    public val closed: WeekTimestamp
) : Parcelable
