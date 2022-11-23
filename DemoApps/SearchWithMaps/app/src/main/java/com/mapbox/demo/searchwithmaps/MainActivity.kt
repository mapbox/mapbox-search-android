package com.mapbox.demo.searchwithmaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.image.image
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.style
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.ServiceProvider
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchEngineSettings
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.adapter.engines.SearchEngineUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import java.util.concurrent.CopyOnWriteArrayList

class MainActivity : AppCompatActivity() {

    private val serviceProvider = ServiceProvider.INSTANCE
    private lateinit var locationEngine: LocationEngine

    private lateinit var toolbar: Toolbar
    private lateinit var searchView: SearchView

    private lateinit var searchResultsView: SearchResultsView
    private lateinit var searchEngineUiAdapter: SearchEngineUiAdapter
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap

    private val mapMarkersManager = MapMarkersManager()

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            when {
                !searchPlaceView.isHidden() -> {
                    mapMarkersManager.clearMarkers()
                    searchPlaceView.hide()
                }
                mapMarkersManager.markerCoordinates.isNotEmpty() -> {
                    mapMarkersManager.clearMarkers()
                }
                else -> {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        locationEngine = LocationEngineProvider.getBestLocationEngine(applicationContext)

        mapMarkersManager.addOnChangeListener { markers ->
            mapboxMap.getStyle()?.getSourceAs<GeoJsonSource>(SEARCH_PIN_SOURCE_ID)?.featureCollection(
                FeatureCollection.fromFeatures(
                    markers.map { Feature.fromGeometry(it) }
                )
            )
            updateOnBackPressedCallbackEnabled()
        }

        mapView = findViewById(R.id.map_view)
        mapView.getMapboxMap().also { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.loadStyle(
                style(styleUri = Style.MAPBOX_STREETS) {
                    +geoJsonSource(SEARCH_PIN_SOURCE_ID) {
                        featureCollection(
                            FeatureCollection.fromFeatures(
                                mapMarkersManager.markerCoordinates.map {
                                    Feature.fromGeometry(it)
                                }
                            )
                        )
                    }
                    +image(SEARCH_PIN_IMAGE_ID) {
                        bitmap(createSearchPinDrawable().toBitmap(config = Bitmap.Config.ARGB_8888))
                    }
                    +symbolLayer(SEARCH_PIN_LAYER_ID, SEARCH_PIN_SOURCE_ID) {
                        iconImage(SEARCH_PIN_IMAGE_ID)
                        iconAllowOverlap(true)
                    }
                }
            )
        }

        toolbar = findViewById(R.id.toolbar)
        toolbar.apply {
            title = getString(R.string.toolbar_title)
            setSupportActionBar(this)
        }

        searchResultsView = findViewById<SearchResultsView>(R.id.search_results_view).apply {
            initialize(
                SearchResultsView.Configuration(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
            )
            isVisible = false
        }

        val searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            apiType = ApiType.GEOCODING,
            settings = SearchEngineSettings(getString(R.string.mapbox_access_token))
        )

        val offlineSearchEngine = OfflineSearchEngine.create(
            OfflineSearchEngineSettings(getString(R.string.mapbox_access_token))
        )

        searchEngineUiAdapter = SearchEngineUiAdapter(
            view = searchResultsView,
            searchEngine = searchEngine,
            offlineSearchEngine = offlineSearchEngine,
        )

        searchEngineUiAdapter.addSearchListener(object : SearchEngineUiAdapter.SearchListener {

            override fun onSuggestionsShown(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                // Nothing to do
            }

            override fun onCategoryResultsShown(
                suggestion: SearchSuggestion,
                results: List<SearchResult>,
                responseInfo: ResponseInfo
            ) {
                closeSearchView()
                showMarkers(results.map { it.coordinate })
            }

            override fun onOfflineSearchResultsShown(results: List<OfflineSearchResult>, responseInfo: OfflineResponseInfo) {
                closeSearchView()
                showMarkers(results.map { it.coordinate })
            }

            override fun onSuggestionSelected(searchSuggestion: SearchSuggestion): Boolean {
                return false
            }

            override fun onSearchResultSelected(searchResult: SearchResult, responseInfo: ResponseInfo) {
                closeSearchView()
                searchPlaceView.open(SearchPlace.createFromSearchResult(searchResult, responseInfo))
                showMarker(searchResult.coordinate)
            }

            override fun onOfflineSearchResultSelected(searchResult: OfflineSearchResult, responseInfo: OfflineResponseInfo) {
                closeSearchView()
                searchPlaceView.open(SearchPlace.createFromOfflineSearchResult(searchResult))
                showMarker(searchResult.coordinate)
            }

            override fun onError(e: Exception) {
                Toast.makeText(applicationContext, "Error happened: $e", Toast.LENGTH_SHORT).show()
            }

            override fun onHistoryItemClick(historyRecord: HistoryRecord) {
                closeSearchView()
                searchPlaceView.open(SearchPlace.createFromIndexableRecord(historyRecord, distanceMeters = null))

                userDistanceTo(historyRecord.coordinate) { distance ->
                    distance?.let {
                        searchPlaceView.updateDistance(distance)
                    }
                }

                showMarker(historyRecord.coordinate)
            }

            override fun onPopulateQueryClick(suggestion: SearchSuggestion, responseInfo: ResponseInfo) {
                if (::searchView.isInitialized) {
                    searchView.setQuery(suggestion.name, true)
                }
            }

            override fun onFeedbackItemClick(responseInfo: ResponseInfo) {
                // Not implemented
            }
        })

        searchPlaceView = findViewById(R.id.search_place_view)
        searchPlaceView.initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))

        searchPlaceView.addOnCloseClickListener {
            mapMarkersManager.clearMarkers()
            searchPlaceView.hide()
        }

        searchPlaceView.addOnNavigateClickListener { searchPlace ->
            startActivity(geoIntent(searchPlace.coordinate))
        }

        searchPlaceView.addOnShareClickListener { searchPlace ->
            startActivity(shareIntent(searchPlace))
        }

        searchPlaceView.addOnFeedbackClickListener { _, _ ->
            // Not implemented
        }

        searchPlaceView.addOnBottomSheetStateChangedListener { _, _ ->
            updateOnBackPressedCallbackEnabled()
        }

        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    private fun updateOnBackPressedCallbackEnabled() {
        onBackPressedCallback.isEnabled =
            !searchPlaceView.isHidden() || mapMarkersManager.markerCoordinates.isNotEmpty()
    }

    private fun closeSearchView() {
        toolbar.collapseActionView()
        searchView.setQuery("", false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchActionView = menu.findItem(R.id.action_search)
        searchActionView.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                searchPlaceView.hide()
                searchResultsView.isVisible = true
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                searchResultsView.isVisible = false
                return true
            }
        })

        searchView = searchActionView.actionView as SearchView
        searchView.queryHint = getString(R.string.query_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchEngineUiAdapter.search(newText)
                return false
            }
        })
        return true
    }

    private fun userDistanceTo(destination: Point, callback: (Double?) -> Unit) {
        lastKnownLocation { location ->
            if (location == null) {
                callback(null)
            } else {
                val distance = serviceProvider
                    .distanceCalculator(latitude = location.latitude())
                    .distance(location, destination)
                callback(distance)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun lastKnownLocation(callback: (Point?) -> Unit) {
        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            callback(null)
        }

        locationEngine.getLastLocation(object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult?) {
                val location = (result?.locations?.lastOrNull() ?: result?.lastLocation)?.let { location ->
                    Point.fromLngLat(location.longitude, location.latitude)
                }
                callback(location)
            }

            override fun onFailure(p0: Exception) {
                callback(null)
            }
        })
    }

    private fun showMarkers(coordinates: List<Point>) {
        if (coordinates.isEmpty()) {
            mapMarkersManager.clearMarkers()
            return
        } else if (coordinates.size == 1) {
            showMarker(coordinates.first())
            return
        }

        val cameraOptions = mapboxMap.cameraForCoordinates(
            coordinates, markersPaddings, bearing = null, pitch = null
        )

        if (cameraOptions.center == null) {
            mapMarkersManager.clearMarkers()
            return
        }

        showMarkers(cameraOptions, coordinates)
    }

    private fun showMarker(coordinate: Point) {
        val cameraOptions = CameraOptions.Builder()
            .center(coordinate)
            .padding(EdgeInsets(.0, .0, dpToPx(300).toDouble(), .0))
            .zoom(10.0)
            .build()

        showMarkers(cameraOptions, listOf(coordinate))
    }

    private fun showMarkers(cameraOptions: CameraOptions, coordinates: List<Point>) {
        mapMarkersManager.setMarkers(coordinates)
        mapboxMap.setCamera(cameraOptions)
    }

    private class MapMarkersManager {

        private val changeListeners = CopyOnWriteArrayList<OnChangeListener>()
        private val _markerCoordinates = mutableListOf<Point>()

        val markerCoordinates: List<Point>
            get() = _markerCoordinates

        fun clearMarkers() {
            if (_markerCoordinates.isNotEmpty()) {
                _markerCoordinates.clear()
                changeListeners.forEach { it.onMarkersChanged(_markerCoordinates) }
            }
        }

        fun setMarkers(coordinates: List<Point>) {
            if (coordinates != _markerCoordinates) {
                _markerCoordinates.clear()
                _markerCoordinates.addAll(coordinates)
                changeListeners.forEach { it.onMarkersChanged(_markerCoordinates) }
            }
        }

        fun addOnChangeListener(listener: OnChangeListener) {
            changeListeners.add(listener)
        }

        fun interface OnChangeListener {
            fun onMarkersChanged(points: List<Point>)
        }
    }

    private companion object {

        const val SEARCH_PIN_SOURCE_ID = "search.pin.source.id"
        const val SEARCH_PIN_IMAGE_ID = "search.pin.image.id"
        const val SEARCH_PIN_LAYER_ID = "search.pin.layer.id"

        val markersPaddings: EdgeInsets = dpToPx(64).toDouble()
            .let { mapPadding ->
                EdgeInsets(mapPadding, mapPadding, mapPadding, mapPadding)
            }

        const val PERMISSIONS_REQUEST_LOCATION = 0

        fun Context.isPermissionGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }

        fun createSearchPinDrawable(): ShapeDrawable {
            val size = dpToPx(24)
            val drawable = ShapeDrawable(OvalShape())
            drawable.intrinsicWidth = size
            drawable.intrinsicHeight = size
            DrawableCompat.setTint(drawable, Color.RED)
            return drawable
        }

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        fun geoIntent(point: Point): Intent {
            return Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${point.latitude()}, ${point.longitude()}"))
        }

        fun shareIntent(searchPlace: SearchPlace): Intent {
            val text = "${searchPlace.name}. " +
                    "Address: ${searchPlace.address?.formattedAddress(SearchAddress.FormatStyle.Short) ?: "unknown"}. " +
                    "Geo coordinate: (lat=${searchPlace.coordinate.latitude()}, lon=${searchPlace.coordinate.longitude()})"

            return Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
        }
    }
}
