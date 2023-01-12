package com.mapbox.search.sample

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
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
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchEngineSettings
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.sample.api.AddressAutofillKotlinExampleActivity
import com.mapbox.search.sample.api.CategorySearchJavaExampleActivity
import com.mapbox.search.sample.api.CategorySearchKotlinExampleActivity
import com.mapbox.search.sample.api.CustomIndexableDataProviderJavaExample
import com.mapbox.search.sample.api.CustomIndexableDataProviderKotlinExample
import com.mapbox.search.sample.api.DiscoverApiKotlinExampleActivity
import com.mapbox.search.sample.api.FavoritesDataProviderJavaExample
import com.mapbox.search.sample.api.FavoritesDataProviderKotlinExample
import com.mapbox.search.sample.api.ForwardGeocodingBatchResolvingJavaExampleActivity
import com.mapbox.search.sample.api.ForwardGeocodingBatchResolvingKotlinExampleActivity
import com.mapbox.search.sample.api.ForwardGeocodingJavaExampleActivity
import com.mapbox.search.sample.api.ForwardGeocodingKotlinExampleActivity
import com.mapbox.search.sample.api.HistoryDataProviderJavaExample
import com.mapbox.search.sample.api.HistoryDataProviderKotlinExample
import com.mapbox.search.sample.api.JapanSearchJavaExampleActivity
import com.mapbox.search.sample.api.JapanSearchKotlinExampleActivity
import com.mapbox.search.sample.api.OfflineReverseGeocodingJavaExampleActivity
import com.mapbox.search.sample.api.OfflineReverseGeocodingKotlinExampleActivity
import com.mapbox.search.sample.api.OfflineSearchJavaExampleActivity
import com.mapbox.search.sample.api.OfflineSearchKotlinExampleActivity
import com.mapbox.search.sample.api.ReverseGeocodingJavaExampleActivity
import com.mapbox.search.sample.api.ReverseGeocodingKotlinExampleActivity
import com.mapbox.search.ui.adapter.engines.SearchEngineUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import java.util.concurrent.CopyOnWriteArrayList

class MainActivity : AppCompatActivity() {

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
                    if (BuildConfig.DEBUG) {
                        error("This OnBackPressedCallback should not be enabled")
                    }
                    Log.i("SearchApiExample", "This OnBackPressedCallback should not be enabled")
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
                style(styleUri = getMapStyleUri()) {
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
            title = getString(R.string.simple_ui_toolbar_title)
            setSupportActionBar(this)
        }

        val apiType = if (BuildConfig.ENABLE_SBS) {
            ApiType.SBS
        } else {
            ApiType.GEOCODING
        }

        searchResultsView = findViewById<SearchResultsView>(R.id.search_results_view).apply {
            initialize(
                SearchResultsView.Configuration(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
            )
            isVisible = false
        }

        val searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            apiType = apiType,
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

                locationEngine.userDistanceTo(this@MainActivity, historyRecord.coordinate) { distance ->
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
            startActivity(IntentUtils.geoIntent(searchPlace.coordinate))
        }

        searchPlaceView.addOnShareClickListener { searchPlace ->
            startActivity(IntentUtils.shareIntent(searchPlace))
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
        menuInflater.inflate(R.menu.main_activity_options_menu, menu)

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.open_address_autofill_ui_example -> {
                startActivity(Intent(this, AddressAutofillUiActivity::class.java))
                true
            }
            R.id.open_address_autofill_example -> {
                startActivity(Intent(this, AddressAutofillKotlinExampleActivity::class.java))
                true
            }
            R.id.open_discover_api_ui_example -> {
                startActivity(Intent(this, DiscoverApiUiActivity::class.java))
                true
            }
            R.id.open_discover_api_example -> {
                startActivity(Intent(this, DiscoverApiKotlinExampleActivity::class.java))
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
            R.id.open_japan_search_kt_example -> {
                startActivity(Intent(this, JapanSearchKotlinExampleActivity::class.java))
                true
            }
            R.id.open_japan_search_java_example -> {
                startActivity(Intent(this, JapanSearchJavaExampleActivity::class.java))
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

        fun createSearchPinDrawable(): ShapeDrawable {
            val size = dpToPx(24)
            val drawable = ShapeDrawable(OvalShape())
            drawable.intrinsicWidth = size
            drawable.intrinsicHeight = size
            DrawableCompat.setTint(drawable, Color.RED)
            return drawable
        }
    }
}
