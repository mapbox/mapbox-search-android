package com.mapbox.search.sample.advanced

import com.mapbox.android.gestures.Utils.dpToPx
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager

internal class AdvancedSearchMapAnnotationManager(mapView: MapView) {

    private val mapboxMap = mapView.mapboxMap
    private val circleAnnotationManager = mapView.annotations.createCircleAnnotationManager(
        AnnotationConfig(
            layerId = "markers"
        )
    )
    private val polylineAnnotationManager = mapView.annotations.createPolylineAnnotationManager(
        AnnotationConfig(
            layerId = "route",
            belowLayerId = "markers"
        )
    )
    private val markers = mutableMapOf<String, CircleAnnotation>()
    private var pointAlongRoute: CircleAnnotation? = null
    var route: List<Point>? = null
        private set

    var onMarkersChangeListener: (() -> Unit)? = null

    val hasRoute: Boolean
        get() = route != null

    val hasMarkers: Boolean
        get() = markers.isNotEmpty()

    fun clearMarkers() {
        circleAnnotationManager.delete(markers.values.toList())
        markers.clear()
    }

    fun clearRoute() {
        route = null
        polylineAnnotationManager.deleteAll()

        if (pointAlongRoute != null) {
            circleAnnotationManager.delete(pointAlongRoute!!)
            pointAlongRoute = null
        }
    }

    fun clearAll() {
        clearMarkers()
        clearRoute()
    }

    fun showMarker(coordinate: Point) {
        showMarkers(listOf(coordinate))
    }

    fun showMarkers(coordinates: List<Point>) {
        clearMarkers()
        if (coordinates.isEmpty()) {
            onMarkersChangeListener?.invoke()
            return
        }

        coordinates.forEach { coordinate ->
            val circleAnnotationOptions: CircleAnnotationOptions = CircleAnnotationOptions()
                .withPoint(coordinate)
                .withCircleRadius(8.0)
                .withCircleColor("#ee4e8b")
                .withCircleStrokeWidth(2.0)
                .withCircleStrokeColor("#ffffff")

            val annotation = circleAnnotationManager.create(circleAnnotationOptions)
            markers[annotation.id] = annotation
        }

        val onOptionsReadyCallback: (CameraOptions) -> Unit = {
            mapboxMap.setCamera(it)
            onMarkersChangeListener?.invoke()
        }

        val emptyCameraOptions = CameraOptions.Builder().build()
        if (route != null) {
            mapboxMap.cameraForCoordinates(
                route!! + coordinates,
                emptyCameraOptions,
                MARKERS_INSETS,
                null,
                null,
                onOptionsReadyCallback,
            )
        } else if (coordinates.size == 1) {
            val options = CameraOptions.Builder()
                .center(coordinates.first())
                .padding(MARKERS_INSETS_OPEN_CARD)
                .zoom(14.0)
                .build()

            onOptionsReadyCallback(options)
        } else {
            mapboxMap.cameraForCoordinates(
                coordinates,
                emptyCameraOptions,
                MARKERS_INSETS,
                null,
                null,
                onOptionsReadyCallback,
            )
        }
    }

    fun showRoute(route: List<Point>, par: Point? = null) {
        if (route.isEmpty()) {
            return
        }

        clearAll()

        this.route = route

        val polylineAnnotationOptions: PolylineAnnotationOptions = PolylineAnnotationOptions()
            .withPoints(route)
            .withLineColor("#007bff")
            .withLineBorderColor("#0056b3")
            .withLineWidth(5.0)

        polylineAnnotationManager.create(polylineAnnotationOptions)

        if (par != null) {
            val circleAnnotationOptions: CircleAnnotationOptions = CircleAnnotationOptions()
                .withPoint(par)
                .withCircleRadius(6.0)
                .withCircleColor("#4caf50")
                .withCircleStrokeWidth(2.0)
                .withCircleStrokeColor("#ffffff")

            pointAlongRoute = circleAnnotationManager.create(circleAnnotationOptions)
        }

        mapboxMap.cameraForCoordinates(
            route, CameraOptions.Builder().build(), MARKERS_INSETS, null, null,
        ) {
            mapboxMap.setCamera(it)
        }
    }

    private companion object {

        val MARKERS_BOTTOM_OFFSET = dpToPx(176f).toDouble()
        val MARKERS_EDGE_OFFSET = dpToPx(64f).toDouble()
        val PLACE_CARD_HEIGHT = dpToPx(300f).toDouble()

        val MARKERS_INSETS = EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_BOTTOM_OFFSET, MARKERS_EDGE_OFFSET
        )

        val MARKERS_INSETS_OPEN_CARD = EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, PLACE_CARD_HEIGHT, MARKERS_EDGE_OFFSET
        )
    }
}