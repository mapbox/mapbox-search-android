package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreDistanceCalculator
import com.mapbox.search.base.core.CoreDistanceCalculatorInterface

/**
 * Distance calculator for WGS84 (Earth as spheroid).
 * Should be initialized by latitude and used in some not far around area.
 *
 * Fast and precise for nearby points (distance <= 200 km). Expected error is 0.04%.
 *
 * In other cases (distance > 200 km), uses [DistanceCalculator.distanceOnSphere] function.
 *
 * To obtain [DistanceCalculator] instance, please, use [MapboxSearchSdk.serviceProvider].
 *
 * @return Distance in meters.
*/
public interface DistanceCalculator {

    /**
     * Calculates distance between two coordinates.
     *
     * @return calculated distance in meters.
     */
    public fun distance(from: Point, to: Point): Double

    /**
     * @suppress
     */
    public companion object {

        /**
         * This method provides better result precision with minimal error level, than [distance].
         * @return distance between two coordinates in meters.
         */
        @JvmStatic
        public fun distanceOnSphere(from: Point, to: Point): Double {
            return CoreDistanceCalculator.distanceOnSphere(from, to)
        }
    }
}

internal class DistanceCalculatorImpl(
    private val coreDistanceCalculator: CoreDistanceCalculatorInterface
) : DistanceCalculator {

    /**
     * @param latitude the area in which fast distance calculation is performed.
     */
    constructor(latitude: Double) : this(CoreDistanceCalculator(latitude))

    override fun distance(from: Point, to: Point): Double {
        return coreDistanceCalculator.distance(from, to)
    }
}
