package com.mapbox.search.sample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.ResponseInfo
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.sample.api.AddressAutofillKotlinExampleActivity
import com.mapbox.search.sample.api.CategorySearchJavaExampleActivity
import com.mapbox.search.sample.api.CategorySearchKotlinExampleActivity
import com.mapbox.search.sample.api.CustomIndexableDataProviderJavaExample
import com.mapbox.search.sample.api.CustomIndexableDataProviderKotlinExample
import com.mapbox.search.sample.api.FavoritesDataProviderJavaExample
import com.mapbox.search.sample.api.FavoritesDataProviderKotlinExample
import com.mapbox.search.sample.api.ForwardGeocodingBatchResolvingJavaExampleActivity
import com.mapbox.search.sample.api.ForwardGeocodingBatchResolvingKotlinExampleActivity
import com.mapbox.search.sample.api.ForwardGeocodingJavaExampleActivity
import com.mapbox.search.sample.api.ForwardGeocodingKotlinExampleActivity
import com.mapbox.search.sample.api.HistoryDataProviderJavaExample
import com.mapbox.search.sample.api.HistoryDataProviderKotlinExample
import com.mapbox.search.sample.api.OfflineReverseGeocodingJavaExampleActivity
import com.mapbox.search.sample.api.OfflineReverseGeocodingKotlinExampleActivity
import com.mapbox.search.sample.api.OfflineSearchJavaExampleActivity
import com.mapbox.search.sample.api.OfflineSearchKotlinExampleActivity
import com.mapbox.search.sample.api.ReverseGeocodingJavaExampleActivity
import com.mapbox.search.sample.api.ReverseGeocodingKotlinExampleActivity
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView

class MainActivity : AppCompatActivity() {

    private val serviceProvider = MapboxSearchSdk.serviceProvider
    private lateinit var locationEngine: LocationEngine

    private lateinit var toolbar: Toolbar
    private lateinit var searchView: SearchView

    private lateinit var searchResultsView: SearchResultsView
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private var markerCoordinates = mutableListOf<Point>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationEngine = LocationEngineProvider.getBestLocationEngine(applicationContext)

        mapView = findViewById(R.id.map_view)
        mapView.getMapboxMap().also { mapboxMap ->
            this.mapboxMap = mapboxMap
            mapboxMap.loadStyle(
                style(styleUri = getMapStyleUri()) {
                    +geoJsonSource(SEARCH_PIN_SOURCE_ID) {
                        featureCollection(
                            FeatureCollection.fromFeatures(
                                markerCoordinates.map {
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
            title = getString(R.string.simple_ui_toolbar_title)
            setSupportActionBar(this)
        }

        searchResultsView = findViewById<SearchResultsView>(R.id.search_results_view).apply {
            initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
            isVisible = false
        }

        searchResultsView.addSearchListener(object : SearchResultsView.SearchListener {

            override fun onCategoryResult(
                suggestion: SearchSuggestion,
                results: List<SearchResult>,
                responseInfo: ResponseInfo
            ) {
                closeSearchView()
                showMarkers(results.mapNotNull { it.coordinate })
            }

            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                // Nothing to do
            }

            override fun onSearchResult(searchResult: SearchResult, responseInfo: ResponseInfo) {
                closeSearchView()
                val coordinate = searchResult.coordinate
                if (coordinate != null) {
                    searchPlaceView.open(SearchPlace.createFromSearchResult(searchResult, responseInfo, coordinate))
                    showMarker(coordinate)
                }
            }

            override fun onOfflineSearchResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                closeSearchView()
                showMarkers(results.mapNotNull { it.coordinate })
            }

            override fun onError(e: Exception) {
                Toast.makeText(applicationContext, "Error happened: $e", Toast.LENGTH_SHORT).show()
            }

            override fun onHistoryItemClicked(historyRecord: HistoryRecord) {
                closeSearchView()
                val coordinate = historyRecord.coordinate
                if (coordinate != null) {
                    searchPlaceView.open(SearchPlace.createFromIndexableRecord(historyRecord, coordinate, distanceMeters = null))

                    userDistanceTo(coordinate) { distance ->
                        distance?.let {
                            searchPlaceView.updateDistance(distance)
                        }
                    }

                    showMarker(coordinate)
                }
            }

            override fun onPopulateQueryClicked(suggestion: SearchSuggestion, responseInfo: ResponseInfo) {
                if (::searchView.isInitialized) {
                    searchView.setQuery(suggestion.name, true)
                }
            }

            override fun onFeedbackClicked(responseInfo: ResponseInfo) {
                // Not implemented
            }
        })

        searchPlaceView = findViewById(R.id.search_place_view)
        searchPlaceView.initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))

        searchPlaceView.addOnCloseClickListener {
            clearMarkers()
            searchPlaceView.hide()
        }

        searchPlaceView.addOnNavigateClickListener { searchPlace ->
            startActivity(IntentUtils.geoIntent(searchPlace.coordinate))
        }

        searchPlaceView.addOnShareClickListener { searchPlace ->
            startActivity(IntentUtils.shareIntent(searchPlace))
        }

        searchPlaceView.addOnFeedbackClickListener { _, _ ->
            // Not implemented
        }

        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    override fun onBackPressed() {
        when {
            !searchPlaceView.isHidden() -> {
                clearMarkers()
                searchPlaceView.hide()
            }
            markerCoordinates.isNotEmpty() -> {
                clearMarkers()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun closeSearchView() {
        toolbar.collapseActionView()
        searchView.setQuery("", false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity_options_menu, menu)

        val searchActionView = menu.findItem(R.id.action_search)
        searchActionView.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchPlaceView.hide()
                searchResultsView.isVisible = true
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
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
                searchResultsView.search(newText)
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.open_address_autofill_example -> {
                startActivity(Intent(this, AddressAutofillKotlinExampleActivity::class.java))
                true
            }
            R.id.open_custom_data_provider_kt_example -> {
                startActivity(Intent(this, CustomIndexableDataProviderKotlinExample::class.java))
                true
            }
            R.id.open_custom_data_provider_java_example -> {
                startActivity(Intent(this, CustomIndexableDataProviderJavaExample::class.java))
                true
            }
            R.id.custom_theme_example -> {
                startActivity(Intent(this, CustomThemeActivity::class.java))
                true
            }
            R.id.open_forward_geocoding_kt_example -> {
                startActivity(Intent(this, ForwardGeocodingKotlinExampleActivity::class.java))
                true
            }
            R.id.open_forward_geocoding_java_example -> {
                startActivity(Intent(this, ForwardGeocodingJavaExampleActivity::class.java))
                true
            }
            R.id.open_forward_geocoding_batch_resolving_kt_example -> {
                startActivity(Intent(this, ForwardGeocodingBatchResolvingKotlinExampleActivity::class.java))
                true
            }
            R.id.open_forward_geocoding_batch_resolving_java_example -> {
                startActivity(Intent(this, ForwardGeocodingBatchResolvingJavaExampleActivity::class.java))
                true
            }
            R.id.open_reverse_geocoding_kt_example -> {
                startActivity(Intent(this, ReverseGeocodingKotlinExampleActivity::class.java))
                true
            }
            R.id.open_reverse_geocoding_java_example -> {
                startActivity(Intent(this, ReverseGeocodingJavaExampleActivity::class.java))
                true
            }
            R.id.open_category_search_kt_example -> {
                startActivity(Intent(this, CategorySearchKotlinExampleActivity::class.java))
                true
            }
            R.id.open_category_search_java_example -> {
                startActivity(Intent(this, CategorySearchJavaExampleActivity::class.java))
                true
            }
            R.id.open_offline_search_java_example -> {
                startActivity(Intent(this, OfflineSearchJavaExampleActivity::class.java))
                true
            }
            R.id.open_offline_search_kt_example -> {
                startActivity(Intent(this, OfflineSearchKotlinExampleActivity::class.java))
                true
            }
            R.id.open_offline_reverse_geocoding_java_example -> {
                startActivity(Intent(this, OfflineReverseGeocodingJavaExampleActivity::class.java))
                true
            }
            R.id.open_offline_reverse_geocoding_kt_example -> {
                startActivity(Intent(this, OfflineReverseGeocodingKotlinExampleActivity::class.java))
                true
            }
            R.id.open_history_data_provider_java_example -> {
                startActivity(Intent(this, HistoryDataProviderJavaExample::class.java))
                true
            }
            R.id.open_history_data_provider_kt_example -> {
                startActivity(Intent(this, HistoryDataProviderKotlinExample::class.java))
                true
            }
            R.id.open_favorites_data_provider_java_example -> {
                startActivity(Intent(this, FavoritesDataProviderJavaExample::class.java))
                true
            }
            R.id.open_favorites_data_provider_kt_example -> {
                startActivity(Intent(this, FavoritesDataProviderKotlinExample::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getMapStyleUri(): String {
        return when (val darkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> Style.DARK
            Configuration.UI_MODE_NIGHT_NO,
            Configuration.UI_MODE_NIGHT_UNDEFINED -> Style.MAPBOX_STREETS
            else -> error("Unknown mode: $darkMode")
        }
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
            clearMarkers()
            return
        } else if (coordinates.size == 1) {
            showMarker(coordinates.first())
            return
        }

        val cameraOptions = mapboxMap.cameraForCoordinates(
            coordinates, markersPaddings, bearing = null, pitch = null
        )

        if (cameraOptions.center == null) {
            clearMarkers()
            return
        }

        showMarkers(cameraOptions, coordinates)
    }

    private fun showMarker(coordinate: Point) {
        val cameraOptions = CameraOptions.Builder()
            .center(coordinate)
            .zoom(10.0)
            .build()

        showMarkers(cameraOptions, listOf(coordinate))
    }

    private fun showMarkers(cameraOptions: CameraOptions, coordinates: List<Point>) {
        markerCoordinates.clear()
        markerCoordinates.addAll(coordinates)
        updateMarkersOnMap()

        mapboxMap.setCamera(cameraOptions)
    }

    private fun clearMarkers() {
        markerCoordinates.clear()
        updateMarkersOnMap()
    }

    private fun updateMarkersOnMap() {
        mapboxMap.getStyle()?.getSourceAs<GeoJsonSource>(SEARCH_PIN_SOURCE_ID)?.featureCollection(
            FeatureCollection.fromFeatures(
                markerCoordinates.map {
                    Feature.fromGeometry(it)
                }
            )
        )
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
    }
}
