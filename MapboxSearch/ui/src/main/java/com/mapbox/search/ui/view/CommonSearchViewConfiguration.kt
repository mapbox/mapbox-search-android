package com.mapbox.search.ui.view

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Common configuration options used for Search SDK views.
 */
@Parcelize
public class CommonSearchViewConfiguration(

    /**
     * [DistanceUnitType] used in the view. By default unit type is determined from the current locale.
     */
    public val distanceUnitType: DistanceUnitType = DistanceUnitType.getFromLocale(Locale.getDefault()),
) : Parcelable
