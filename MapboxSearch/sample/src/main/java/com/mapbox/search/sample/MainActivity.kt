package com.mapbox.search.sample

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
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
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.mapbox.android.gestures.Utils.dpToPx
import com.mapbox.common.location.LocationProvider
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.base.location.defaultLocationProvider
import com.mapbox.search.base.utils.extension.toPoint
import com.mapbox.search.common.DistanceCalculator
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchEngineSettings
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.sample.api.AddressAutofillKotlinExampleActivity
import com.mapbox.search.sample.api.CategorySearchJavaExampleActivity
import com.mapbox.search.sample.api.CategorySearchKotlinExampleActivity
import com.mapbox.search.sample.api.CustomIndexableDataProviderJavaExample
import com.mapbox.search.sample.api.CustomIndexableDataProviderKotlinExample
import com.mapbox.search.sample.api.DiscoverJavaExampleActivity
import com.mapbox.search.sample.api.DiscoverKotlinExampleActivity
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
import com.mapbox.search.sample.api.OfflineSearchAlongRouteExampleActivity
import com.mapbox.search.sample.api.OfflineSearchJavaExampleActivity
import com.mapbox.search.sample.api.OfflineSearchKotlinExampleActivity
import com.mapbox.search.sample.api.PlaceAutocompleteKotlinExampleActivity
import com.mapbox.search.sample.api.ReverseGeocodingJavaExampleActivity
import com.mapbox.search.sample.api.ReverseGeocodingKotlinExampleActivity
import com.mapbox.search.ui.adapter.engines.SearchEngineUiAdapter
import com.mapbox.search.ui.utils.Debouncer
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchMode
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfTransformation

class MainActivity : AppCompatActivity() {

    private val locationProvider: LocationProvider? = defaultLocationProvider()

    private val debouncer = Debouncer(300L)

    private lateinit var toolbar: Toolbar
    private lateinit var searchView: SearchView

    private lateinit var searchResultsView: SearchResultsView
    private lateinit var searchEngineUiAdapter: SearchEngineUiAdapter
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView

    private lateinit var mapView: MapView
    private lateinit var mapMarkersManager: MapMarkersManager

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            when {
                !searchPlaceView.isHidden() -> {
                    mapMarkersManager.clearMarkers()
                    searchPlaceView.hide()
                }
                mapMarkersManager.hasMarkers -> {
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

        mapView = findViewById(R.id.map_view)
        mapView.mapboxMap.also { mapboxMap ->
            mapboxMap.loadStyle(getMapStyleUri())

            mapView.location.updateSettings {
                enabled = true
            }

            mapView.location.addOnIndicatorPositionChangedListener(object : OnIndicatorPositionChangedListener {
                override fun onIndicatorPositionChanged(point: Point) {
                    mapView.mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(14.0)
                            .build()
                    )

                    mapView.location.removeOnIndicatorPositionChangedListener(this)
                }
            })
        }

        // only support for ApiType.SBS
        if (BuildConfig.API_TYPE == ApiType.SBS) {
            mapView.mapboxMap.addOnMapClickListener { point ->
                val screenCoords = mapView.mapboxMap.pixelForCoordinate(point)

                mapView.mapboxMap.queryRenderedFeatures(
                    RenderedQueryGeometry(screenCoords),
                    RenderedQueryOptions(listOf("poi-label"), null)
                ) {
                    it.value?.firstOrNull()?.queriedFeature.let { queriedFeature ->
                        queriedFeature?.feature?.let { feature ->
                            searchEngineUiAdapter.select(
                                feature
                            )
                        }
                    }
                }

                true
            }
        }

        mapMarkersManager = MapMarkersManager(mapView)
        mapMarkersManager.onMarkersChangeListener = {
            updateOnBackPressedCallbackEnabled()
        }

        toolbar = findViewById(R.id.toolbar)
        toolbar.apply {
            title = getString(R.string.simple_ui_toolbar_title)
            setSupportActionBar(this)
        }

        searchResultsView = findViewById<SearchResultsView>(R.id.search_results_view).apply {
            initialize(
                SearchResultsView.Configuration(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
            )
            isVisible = false
        }

        val searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            apiType = BuildConfig.API_TYPE,
            settings = SearchEngineSettings()
        )

        val offlineSearchEngine = OfflineSearchEngine.create(
            OfflineSearchEngineSettings()
        )

        searchEngineUiAdapter = SearchEngineUiAdapter(
            view = searchResultsView,
            searchEngine = searchEngine,
            offlineSearchEngine = offlineSearchEngine,
        )

        searchEngineUiAdapter.searchMode = SearchMode.AUTO

        searchEngineUiAdapter.addSearchListener(object : SearchEngineUiAdapter.SearchListener {

            override fun onSuggestionsShown(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                // Nothing to do
            }

            override fun onSearchResultsShown(
                suggestion: SearchSuggestion,
                results: List<SearchResult>,
                responseInfo: ResponseInfo
            ) {
                closeSearchView()
                mapMarkersManager.showMarkers(results.map { it.coordinate })
            }

            override fun onOfflineSearchResultsShown(results: List<OfflineSearchResult>, responseInfo: OfflineResponseInfo) {
                // Nothing to do
            }

            override fun onSuggestionSelected(searchSuggestion: SearchSuggestion): Boolean {
                return false
            }

            override fun onSearchResultSelected(searchResult: SearchResult, responseInfo: ResponseInfo) {
                closeSearchView()
                searchPlaceView.open(SearchPlace.createFromSearchResult(searchResult, responseInfo))
                mapMarkersManager.showMarker(searchResult.coordinate)
            }

            override fun onOfflineSearchResultSelected(searchResult: OfflineSearchResult, responseInfo: OfflineResponseInfo) {
                closeSearchView()
                searchPlaceView.open(SearchPlace.createFromOfflineSearchResult(searchResult))
                mapMarkersManager.showMarker(searchResult.coordinate)
            }

            override fun onError(e: Exception) {
                Toast.makeText(applicationContext, "Error happened: $e", Toast.LENGTH_SHORT).show()
            }

            override fun onHistoryItemClick(historyRecord: HistoryRecord) {
                closeSearchView()
                searchPlaceView.open(SearchPlace.createFromIndexableRecord(historyRecord, distanceMeters = null))

                locationProvider?.userDistanceTo(historyRecord.coordinate) { distance ->
                    distance?.let {
                        searchPlaceView.updateDistance(distance)
                    }
                }

                mapMarkersManager.showMarker(historyRecord.coordinate)
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

    private fun LocationProvider.userDistanceTo(destination: Point, callback: (Double?) -> Unit) {
        getLastLocation { location ->
            if (location == null) {
                callback(null)
            } else {
                val distance = DistanceCalculator.instance(latitude = location.latitude)
                    .distance(location.toPoint(), destination)
                callback(distance)
            }
        }
    }

    private fun shareIntent(searchPlace: SearchPlace): Intent {
        val text = "${searchPlace.name}. " +
                "Address: ${searchPlace.address?.formattedAddress(SearchAddress.FormatStyle.Short) ?: "unknown"}. " +
                "Geo coordinate: (lat=${searchPlace.coordinate.latitude()}, lon=${searchPlace.coordinate.longitude()})"

        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    private fun geoIntent(point: Point): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${point.latitude()}, ${point.longitude()}"))

    private fun Context.isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun updateOnBackPressedCallbackEnabled() {
        onBackPressedCallback.isEnabled = !searchPlaceView.isHidden() || mapMarkersManager.hasMarkers
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
                debouncer.debounce {
                    searchEngineUiAdapter.search(newText)
                }
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
            R.id.open_discover_ui_example -> {
                startActivity(Intent(this, DiscoverActivity::class.java))
                true
            }
            R.id.open_discover_kotlin_example -> {
                startActivity(Intent(this, DiscoverKotlinExampleActivity::class.java))
                true
            }
            R.id.open_discover_java_example -> {
                startActivity(Intent(this, DiscoverJavaExampleActivity::class.java))
                true
            }
            R.id.open_place_autocomplete_ui_example -> {
                startActivity(Intent(this, PlaceAutocompleteUiActivity::class.java))
                true
            }
            R.id.open_place_autocomplete_kotlin_example -> {
                startActivity(Intent(this, PlaceAutocompleteKotlinExampleActivity::class.java))
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
            R.id.open_discover_search_kt_example -> {
                startActivity(Intent(this, CategorySearchKotlinExampleActivity::class.java))
                true
            }
            R.id.open_discover_search_java_example -> {
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
            R.id.open_offline_search_along_route_example -> {
                startActivity(Intent(this, OfflineSearchAlongRouteExampleActivity::class.java))
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

    private class MapMarkersManager(mapView: MapView) {

        private val mapboxMap = mapView.mapboxMap
        private val circleAnnotationManager = mapView.annotations.createCircleAnnotationManager(null)
        private val markers = mutableMapOf<String, Point>()

        var onMarkersChangeListener: (() -> Unit)? = null

        val hasMarkers: Boolean
            get() = markers.isNotEmpty()

        fun clearMarkers() {
            markers.clear()
            circleAnnotationManager.deleteAll()
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

            if (coordinates.size == 1) {
                CameraOptions.Builder()
                    .center(coordinates.first())
                    .padding(MARKERS_INSETS_OPEN_CARD)
                    .zoom(14.0)
                    .build()
            } else {
                mapboxMap.cameraForCoordinates(
                    coordinates, MARKERS_INSETS, bearing = null, pitch = null
                )
            }.also {
                mapboxMap.setCamera(it)
            }
            onMarkersChangeListener?.invoke()
        }
    }

    private companion object {

        val MARKERS_EDGE_OFFSET = dpToPx(64f).toDouble()
        val PLACE_CARD_HEIGHT = dpToPx(300f).toDouble()

        val MARKERS_INSETS = EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET
        )

        val MARKERS_INSETS_OPEN_CARD = EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, PLACE_CARD_HEIGHT, MARKERS_EDGE_OFFSET
        )

        const val PERMISSIONS_REQUEST_LOCATION = 0
    }
}
