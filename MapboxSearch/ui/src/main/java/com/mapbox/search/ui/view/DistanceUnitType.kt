package com.mapbox.search.ui.view

import android.icu.util.LocaleData
import android.icu.util.ULocale
import android.os.Build
import com.mapbox.search.result.SearchResult
import java.util.Locale

/**
 * Distance unit type for visual information. Note that this won't change other results such as
 * raw distance in [SearchResult.distanceMeters] which will always be returned in meters.
 */
public enum class DistanceUnitType {

    /**
     * Imperial unit type.
     */
    IMPERIAL,

    /**
     * Metric unit type.
     */
    METRIC;

    internal companion object {

        @JvmSynthetic
        fun getFromLocale(locale: Locale): DistanceUnitType {
            return if (Build.VERSION.SDK_INT >= 28) {
                when (LocaleData.getMeasurementSystem(ULocale.forLocale(locale))) {
                    LocaleData.MeasurementSystem.UK, LocaleData.MeasurementSystem.US -> IMPERIAL
                    else -> METRIC
                }
            } else {
                when (locale.country.uppercase(locale)) {
                    "US", "LR", "MM", "GB" -> IMPERIAL
                    else -> METRIC
                }
            }
        }
    }
}
