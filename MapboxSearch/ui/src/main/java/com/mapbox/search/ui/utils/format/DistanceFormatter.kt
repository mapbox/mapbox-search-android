package com.mapbox.search.ui.utils.format

import android.content.Context
import com.mapbox.search.ui.R
import com.mapbox.search.ui.view.DistanceUnitType
import java.text.DecimalFormat

internal class DistanceFormatter(private val context: Context) {

    fun format(distanceMeters: Double, distanceUnit: DistanceUnitType): String {
        return when (distanceUnit) {
            DistanceUnitType.METRIC -> formatMetricUnitSystem(distanceMeters)
            DistanceUnitType.IMPERIAL -> formatImperialUnitSystem(distanceMeters)
        }
    }

    private fun formatMetricUnitSystem(distanceMeters: Double): String {
        return if (distanceMeters < KM) {
            context.getString(R.string.mapbox_search_sdk_distance_meters, distanceMeters.toInt().toString())
        } else {
            var distanceInKilometres = distanceMeters / KM
            if (distanceInKilometres > DIST_TO_DROP_DIGITS_AFTER_COMMA_KM) {
                distanceInKilometres = distanceInKilometres.toInt().toDouble()
            }
            context.getString(R.string.mapbox_search_sdk_distance_km, DISTANCE_FORMAT.format(distanceInKilometres))
        }
    }

    private fun formatImperialUnitSystem(distanceMeters: Double): String {
        val miles = metersToMiles(distanceMeters)
        return if (miles < MILES_TO_FOOT_THRESHOLD) {
            val feet = milesToFeet(miles)
            context.getString(R.string.mapbox_search_sdk_distance_feet, feet.toInt().toString())
        } else {
            context.getString(R.string.mapbox_search_sdk_distance_miles, DISTANCE_FORMAT.format(miles))
        }
    }

    private companion object {
        val DISTANCE_FORMAT = DecimalFormat("#.#")

        const val KM = 1000.0
        const val DIST_TO_DROP_DIGITS_AFTER_COMMA_KM = 100.0

        const val MILES_TO_FOOT_THRESHOLD = 0.1

        private const val METERS_TO_MILES_MULTIPLIER = 0.000621371
        private const val MILES_TO_FEET_MULTIPLIER = 5280.0

        fun metersToMiles(meters: Double) = meters * METERS_TO_MILES_MULTIPLIER

        fun milesToFeet(miles: Double) = miles * MILES_TO_FEET_MULTIPLIER
    }
}
