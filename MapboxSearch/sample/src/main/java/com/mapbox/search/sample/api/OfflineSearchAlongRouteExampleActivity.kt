package com.mapbox.search.sample.api

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.mapbox.android.gestures.Utils
import com.mapbox.common.TileStore
import com.mapbox.common.location.Location
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchCallback
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchEngineSettings
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.sample.R
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultAdapterItem
import com.mapbox.search.ui.view.SearchResultsView

class OfflineSearchAlongRouteExampleActivity : AppCompatActivity() {

    private lateinit var searchEngine: OfflineSearchEngine

    private lateinit var searchResultsView: SearchResultsView

    private lateinit var queryEditText: EditText
    private lateinit var routePolylineEditText: EditText
    private lateinit var polylinePrecisionRadioGroup: RadioGroup
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapAnnotationsManager: AnnotationsManager

    private var ignoreNextMapIdleEvent: Boolean = false

    private var searchRequestTask: AsyncOperationTask? = null

    private val engineReadyCallback = object : OfflineSearchEngine.EngineReadyCallback {
        override fun onEngineReady() {
            Log.i(OfflineSearchAlongRouteExampleActivity::javaClass.name, "Engine is ready")
        }
    }

    private val searchCallback = object : OfflineSearchCallback {

        override fun onResults(results: List<OfflineSearchResult>, responseInfo: OfflineResponseInfo) {
            Log.i(OfflineSearchAlongRouteExampleActivity::javaClass.name, "Search results: $results")
            val coordinates = results.map { it.coordinate }
            mapAnnotationsManager.showMarkers(coordinates)

            val items = results.map { result ->
                SearchResultAdapterItem.Result(
                    title = result.name,
                    subtitle = null,
                    distanceMeters = null,
                    drawable = com.mapbox.search.ui.R.drawable.mapbox_search_sdk_ic_search_result_address,
                    payload = result
                )
            }

            searchResultsView.setAdapterItems(items)

            searchResultsView.isVisible = true
        }

        override fun onError(e: Exception) {
            Log.i(OfflineSearchAlongRouteExampleActivity::javaClass.name, "Search error", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_search_along_route)

        val tileStore = TileStore.create()


        searchEngine = OfflineSearchEngine.create(
            OfflineSearchEngineSettings(
                tileStore = tileStore
            )
        )

        searchEngine.addEngineReadyCallback(engineReadyCallback)

        queryEditText = findViewById(R.id.query_text)
        routePolylineEditText = findViewById(R.id.route_polyline)
        polylinePrecisionRadioGroup = findViewById(R.id.route_polyline_precision)

        mapView = findViewById(R.id.map)
        mapboxMap = mapView.mapboxMap
        mapAnnotationsManager = OfflineSearchAlongRouteExampleActivity.AnnotationsManager(mapView)

        mapboxMap.loadStyle(Style.MAPBOX_STREETS)

        searchResultsView = findViewById(R.id.search_results_view)

        searchResultsView.initialize(
            SearchResultsView.Configuration(
                commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
            )
        )

        fun Location.toPoint(): Point = Point.fromLngLat(longitude, latitude)
        val locationService = LocationServiceFactory.getOrCreate()
            .getDeviceLocationProvider(null)
            .value
        locationService?.getLastLocation { location ->
            location?.toPoint()?.let {
                mapView.mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(it)
                        .zoom(9.0)
                        .build()
                )
                ignoreNextMapIdleEvent = true
            }
        }

        queryEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchRequestTask = searchEngine.searchAlongRoute(
                    query = queryEditText.text.toString(),
                    proximity = Point.fromLngLat(-77.0274, 38.996),
                    route = mapAnnotationsManager.route!!,
                    callback = searchCallback
                )

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                true
            } else {
                false
            }
        }

        routePolylineEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val polyline = routePolylineEditText.text.toString()
                val route = PolylineUtils.decode(polyline, 5)
                mapAnnotationsManager.showRoute(route)

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                true
            } else {
                false
            }
        }

        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    private fun Context.showToast(@StringRes resId: Int): Unit = Toast.makeText(this, resId, Toast.LENGTH_LONG).show()

    private fun Context.isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun View.hideKeyboard() =
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)

    private companion object {
        val MARKERS_EDGE_OFFSET = Utils.dpToPx(32f).toDouble()
        val PLACE_CARD_HEIGHT = Utils.dpToPx(300f).toDouble()

        val MARKERS_INSETS = EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET
        )

        val MARKERS_INSETS_OPEN_CARD = EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, PLACE_CARD_HEIGHT, MARKERS_EDGE_OFFSET
        )

        const val PERMISSIONS_REQUEST_LOCATION = 0
    }

    private class AnnotationsManager(mapView: MapView) {

        private val mapboxMap = mapView.mapboxMap
        private val circleAnnotationManager = mapView.annotations.createCircleAnnotationManager(null)
        private val polylineAnnotationManager = mapView.annotations.createPolylineAnnotationManager(null)
        private val markers = mutableMapOf<String, Point>()
        var route: List<Point>? = null
            private set

        var onMarkersChangeListener: (() -> Unit)? = null

        val hasRoute: Boolean
            get() = route != null

        val hasMarkers: Boolean
            get() = markers.isNotEmpty()

        fun clearMarkers() {
            markers.clear()
            circleAnnotationManager.deleteAll()
        }

        fun clearRoute() {
            route = null
            polylineAnnotationManager.deleteAll()
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
                markers[annotation.id] = coordinate
            }

            if (route != null) {
                mapboxMap.cameraForCoordinates(
                    route!! + coordinates, OfflineSearchAlongRouteExampleActivity.MARKERS_INSETS, bearing = null, pitch = null
                )
            }
            else if (coordinates.size == 1) {
                CameraOptions.Builder()
                    .center(coordinates.first())
                    .padding(OfflineSearchAlongRouteExampleActivity.MARKERS_INSETS_OPEN_CARD)
                    .zoom(14.0)
                    .build()
            } else {
                mapboxMap.cameraForCoordinates(
                    coordinates, OfflineSearchAlongRouteExampleActivity.MARKERS_INSETS, bearing = null, pitch = null
                )
            }.also {
                mapboxMap.setCamera(it)
            }
            onMarkersChangeListener?.invoke()
        }

        fun showRoute(route: List<Point>) {
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

            val cameraOptions = mapboxMap.cameraForCoordinates(
                route, OfflineSearchAlongRouteExampleActivity.MARKERS_INSETS, bearing = null, pitch = null
            )
            mapboxMap.setCamera(cameraOptions)
        }
    }

}
