package com.mapbox.search.sample.api

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mapbox.android.gestures.Utils
import com.mapbox.common.TileStore
import com.mapbox.common.location.Location
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchCallback
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchEngineSettings
import com.mapbox.search.offline.OfflineSearchOptions
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.sample.R
import com.mapbox.search.sample.databinding.ActivityOfflineSearchAlongRouteBinding
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultAdapterItem
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import com.mapbox.turf.TurfMisc

class OfflineSearchAlongRouteExampleActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapAnnotationsManager: AnnotationsManager
    private lateinit var binding: ActivityOfflineSearchAlongRouteBinding
    private lateinit var viewModel: SearchAlongRouteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineSearchAlongRouteBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[SearchAlongRouteViewModel::class.java]
        setContentView(binding.root)

        binding.distanceAlongRoute.isEnabled = false

        mapView = binding.map
        mapboxMap = mapView.mapboxMap
        mapAnnotationsManager = OfflineSearchAlongRouteExampleActivity.AnnotationsManager(mapView)

        mapboxMap.loadStyle(Style.MAPBOX_STREETS)

        binding.searchResultsView.initialize(
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
                viewModel.updateProximity(it)
            }
        }

        binding.queryText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.updateSearchQuery(binding.queryText.text.toString())
                v.hideKeyboard()
                true
            } else {
                false
            }
        }

        binding.routePolyline.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.updateRoute(binding.routePolyline.text.toString())
                v.hideKeyboard()
                true
            } else {
                false
            }
        }

        binding.distanceAlongRoute.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seek: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                // do nothing
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // do nothing
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                viewModel.updateDistanceAlongRoute(seek.progress)
            }
        })

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

    override fun onResume() {
        super.onResume()

        viewModel.getSearchAlongRouteData().observe(this, Observer { searchAlongRoute ->
            Log.i(OfflineSearchAlongRouteExampleActivity::javaClass.name, "Search results: $searchAlongRoute")

            val (state, options, results) = searchAlongRoute

            if (options.route.isNotEmpty()) {
                binding.distanceAlongRoute.isEnabled = options.route.isNotEmpty()
                mapAnnotationsManager.showRoute(options.route, options.proximity)
            } else {
                mapAnnotationsManager.clearRoute()
            }

            when (state) {
                State.IDLE -> {
                    binding.searchResultsView.isVisible = false
                    binding.progressBar.isVisible = false
                }

                State.RUNNING -> {
                    binding.searchResultsView.isVisible = false
                    binding.progressBar.isVisible = true
                }

                State.DONE -> {
                    val coordinates = results!!.map { it.coordinate }
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

                    binding.searchResultsView.setAdapterItems(items)

                    binding.progressBar.isVisible = false
                    binding.searchResultsView.isVisible = true
                }

                State.ERROR -> {
                    Toast.makeText(this, "Error: Unable to perform search", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

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

            if (route != null) {
                mapboxMap.cameraForCoordinates(
                    route!! + coordinates, OfflineSearchAlongRouteExampleActivity.MARKERS_INSETS, bearing = null, pitch = null
                )
            } else if (coordinates.size == 1) {
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

            val cameraOptions = mapboxMap.cameraForCoordinates(
                route, OfflineSearchAlongRouteExampleActivity.MARKERS_INSETS, bearing = null, pitch = null
            )
            mapboxMap.setCamera(cameraOptions)
        }
    }

    class SearchAlongRouteViewModel() : ViewModel() {
        private val searchAlongRouteData = MutableLiveData<SearchAlongRoute>()
        private val searchEngine: OfflineSearchEngine
        private var searchRequestTask: AsyncOperationTask? = null

        private val engineReadyCallback = object : OfflineSearchEngine.EngineReadyCallback {
            override fun onEngineReady() {
                Log.i(OfflineSearchAlongRouteExampleActivity::javaClass.name, "Engine is ready")
            }
        }

        private val searchCallback = object : OfflineSearchCallback {

            override fun onResults(results: List<OfflineSearchResult>, responseInfo: OfflineResponseInfo) {
                val currentSar = searchAlongRouteData.value
                searchAlongRouteData.value = SearchAlongRoute(
                    state = State.DONE,
                    options = currentSar!!.options,
                    results = results
                )
            }

            override fun onError(e: Exception) {
                Log.i(OfflineSearchAlongRouteExampleActivity::javaClass.name, "Search error", e)
            }
        }

        init {
            val tileStore = TileStore.create()

            searchEngine = OfflineSearchEngine.create(
                OfflineSearchEngineSettings(tileStore = tileStore)
            )

            searchEngine.addEngineReadyCallback(engineReadyCallback)
        }

        fun getSearchAlongRouteData(): LiveData<SearchAlongRoute> = searchAlongRouteData

        fun updateSearchQuery(query: String) {
            cancelSearch()

            val currentRequest = searchAlongRouteData.value
            val newSearchOptions = currentRequest?.options?.copy(query = query) ?: SearchAlongRouteOptions(query = query)

            runSearch(newSearchOptions)

            searchAlongRouteData.value = SearchAlongRoute(state = State.RUNNING, options = newSearchOptions)
        }

        fun updateRoute(polyline: String, precision: Int = 5) {
            cancelSearch()

            val route = PolylineUtils.decode(polyline, precision)

            val currentRequest = searchAlongRouteData.value
            val newSearchOptions = currentRequest?.options?.copy(route = route) ?: SearchAlongRouteOptions(route = route)

            var state = State.IDLE

            if (newSearchOptions.query.isNotBlank()) {
                runSearch(newSearchOptions)
                state = State.RUNNING
            }

            searchAlongRouteData.value = SearchAlongRoute(state = state, options = newSearchOptions)
        }

        fun updateDistanceAlongRoute(percent: Int) {
            cancelSearch()

            val currentRequest = searchAlongRouteData.value
            val route = currentRequest?.options?.route

            if (route != null) {
                val proximity = if (percent > 0) {
                    val segments = route.zipWithNext()
                    val totalDistance = segments.sumOf { segment ->
                        TurfMeasurement.distance(segment.first, segment.second)
                    }
                    val distanceTravelled = totalDistance * (percent / 100.0)
                    TurfMisc.lineSliceAlong(
                        LineString.fromLngLats(route),
                        0.0,
                        distanceTravelled,
                        TurfConstants.UNIT_KILOMETERS
                    ).coordinates().last()
                } else {
                    route.first()
                }

                updateProximity(proximity)
            }
        }

        fun updateProximity(proximity: Point) {
            cancelSearch()

            val currentRequest = searchAlongRouteData.value
            val newSearchOptions = currentRequest?.options?.copy(proximity = proximity) ?: SearchAlongRouteOptions(proximity = proximity)

            var state = State.IDLE

            if (newSearchOptions.query.isNotBlank()) {
                runSearch(newSearchOptions)
                state = State.RUNNING
            }

            searchAlongRouteData.value = SearchAlongRoute(state = state, options = newSearchOptions)
        }

        private fun runSearch(options: SearchAlongRouteOptions) {
            searchRequestTask = if (options.route.isNotEmpty()) {
                 searchEngine.searchAlongRoute(
                    query = options.query,
                    proximity = options.proximity,
                    route = options.route,
                    callback = searchCallback
                )
            } else {
                searchEngine.search(
                    query = options.query,
                    options = OfflineSearchOptions(),
                    callback = searchCallback
                )
            }
        }

        private fun cancelSearch() {
            if (searchRequestTask != null && searchRequestTask?.isDone != true) {
                searchRequestTask!!.cancel()
            }
        }
    }

    enum class State {
        IDLE, RUNNING, DONE, ERROR
    }

    data class SearchAlongRoute(
        val state: State = State.IDLE,
        val options: SearchAlongRouteOptions = SearchAlongRouteOptions(),
        val results: List<OfflineSearchResult>? = null
    )

    data class SearchAlongRouteOptions(
        val query: String = "",
        val proximity: Point = Point.fromLngLat(0.0, 0.0),
        val route: List<Point> = arrayListOf()
    )
}
