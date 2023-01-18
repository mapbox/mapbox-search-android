package com.mapbox.search.common

import com.mapbox.geojson.Point

internal typealias CoreDistanceCalculator = com.mapbox.search.internal.bindgen.DistanceCalculator
internal typealias CoreDistanceCalculatorInterface = com.mapbox.search.internal.bindgen.DistanceCalculatorInterface

/**
 * Distance calculator for WGS84 (Earth as spheroid).
 * Should be initialized by latitude and used in some not far around area.
 *
 * Fast and precise for nearby points (distance <= 200 km). Expected error is 0.04%.
 *
 * In other cases (distance > 200 km), uses [DistanceCalculator.distanceOnSphere] function.
 *
 * To obtain [DistanceCalculator] instance, please, use [com.mapbox.search.ServiceProvider.distanceCalculator].
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
     * Companion object.
     */
    public companion object {

        /**
         * Provides entity to calculate distances between geographical points.
         * @param latitude the area in which fast distance calculation is performed.
         * If second point's latitude far from latitude from constructor,
         * better to use static [DistanceCalculator.distanceOnSphere] to minimize error level.
         * @return [DistanceCalculator] instance.
         */
        @JvmStatic
        public fun instance(latitude: Double): DistanceCalculator {
            return DistanceCalculatorImpl(latitude)
        }

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
