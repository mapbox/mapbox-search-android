package com.mapbox.search.sample.util

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon

object GeometryGenerator {

    private const val DEGREES_PER_KM = 111.0

    public fun generateCircle(center: Point, radiusKm: Double, numPoints: Int = 100): Geometry {
        val circlePoints = mutableListOf<Point>()
        val radiusDeg = radiusKm / DEGREES_PER_KM

        for (i in 1..numPoints) {
            val theta = 2 * PI * (i / numPoints.toDouble())

            val deltaLat = radiusDeg * cos(theta)
            val deltaLon = radiusDeg * sin(theta) / cos(Math.toRadians(center.latitude()))

            val lat = center.latitude() + deltaLat
            val lon = center.longitude() + deltaLon

            circlePoints.add(Point.fromLngLat(lon, lat))
        }

        // close off our circle
        circlePoints.add(circlePoints.first())

        return Polygon.fromLngLats(listOf(circlePoints))
    }
}