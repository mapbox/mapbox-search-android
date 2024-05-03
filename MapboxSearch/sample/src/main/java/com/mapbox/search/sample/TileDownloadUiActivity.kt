package com.mapbox.search.sample

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.mapbox.android.gestures.Utils.dpToPx
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.common.location.LocationProvider
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.base.location.defaultLocationProvider
import com.mapbox.search.base.utils.extension.toPoint
import com.mapbox.search.common.DistanceCalculator
import com.mapbox.search.offline.OfflineIndexChangeEvent
import com.mapbox.search.offline.OfflineIndexErrorEvent
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
import com.mapbox.search.ui.view.SearchMode
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfTransformation.circle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TileDownloadUiActivity : AppCompatActivity() {

    private val locationProvider: LocationProvider? = defaultLocationProvider()

    private lateinit var toolbar: Toolbar
    private lateinit var searchView: SearchView

    private lateinit var searchResultsView: SearchResultsView
    private lateinit var searchEngineUiAdapter: SearchEngineUiAdapter
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView

    private lateinit var mapView: MapView
    private lateinit var viewAnnotationManager: ViewAnnotationManager
    private lateinit var mapMarkersManager: MapMarkersManager

    private lateinit var offlineSearchEngine: OfflineSearchEngine
    private lateinit var tileStore: TileStore

    private lateinit var circleInputLayout: LinearLayout
    private lateinit var circleInputText: EditText
    private lateinit var circleCenterPoint: EditText
    private lateinit var circleInputSubmit: Button
    private lateinit var searchTypeSwitch: SwitchCompat

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            when {
                searchPlaceView.isHidden().not() -> {
                    mapMarkersManager.clearMarkers()
                    searchPlaceView.hide()
                }
                mapMarkersManager.hasMarkers -> {
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
        setContentView(R.layout.activity_tiles)

        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        circleInputLayout = findViewById(R.id.circleInputLayout)
        circleInputLayout.visibility = View.INVISIBLE

        circleInputText = findViewById(R.id.circleInputText)
        circleCenterPoint = findViewById(R.id.circleCenterPoint)
        circleInputSubmit = findViewById(R.id.submitCircleRadius)
        circleInputSubmit.setOnClickListener {
            val rawRadius = circleInputText.text.toString()
            Log.i("MAP-CLICK", "RADIUS is '$rawRadius'")

            circleInputLayout.visibility = View.INVISIBLE
            circleInputText.setText(R.string.default_radius)

            val radius = rawRadius.toIntOrNull()
            val centerText = circleCenterPoint.text.toString()
            val lonAndLat = centerText.split("|")
            val lon = lonAndLat.getOrNull(0)?.toDouble()
            val lat = lonAndLat.getOrNull(1)?.toDouble()
            if (radius == null || radius == 0) {
                Log.w("MAP-CLICK", "Can't parse radius, '$rawRadius'")
            } else if (lon == null || lat == null) {
                Log.w("MAP-CLICK", "Can't parse lon & lat, original point is '$centerText'")
            } else lifecycleScope.launch(Dispatchers.IO) {
                val centerPoint = Point.fromLngLat(lon, lat)

                mapView.viewAnnotationManager.addViewAnnotation(
                    // Specify the layout resource id
                    resId = R.layout.index_region_center,
                    // Set any view annotation options
                    options = viewAnnotationOptions {
                        annotationAnchor { anchor(ViewAnnotationAnchor.CENTER) }
                        allowOverlap(true)
                        geometry(centerPoint)
                    }
                )

                val downloadViewAnnotation = mapView.viewAnnotationManager.addViewAnnotation(
                    // Specify the layout resource id
                    resId = R.layout.index_region_progress,
                    // Set any view annotation options
                    options = viewAnnotationOptions {
                        annotationAnchor { anchor(ViewAnnotationAnchor.BOTTOM) }
                        allowOverlap(true)
                        geometry(centerPoint)
                    }
                )

                val viewGroup = (downloadViewAnnotation as ViewGroup)
                val downloadProgress = viewGroup[0] as TextView

                val geometry = circle(
                    centerPoint, radius.toDouble(), 64,
                    TurfConstants.UNIT_KILOMETRES
                )
                // Create an instance of the Annotation API and get the polygon manager.
                val annotationApi = mapView.annotations
                val polygonAnnotationManager = annotationApi.createPolygonAnnotationManager()

                // Set options for the resulting fill layer.
                val polygonAnnotationOptions: PolygonAnnotationOptions = PolygonAnnotationOptions()
                    .withGeometry(geometry)
                    // Style the polygon that will be added to the map.
                    .withFillColor("#ee4e8b")
                    .withFillOpacity(0.6)
                // Add the resulting polygon to the map.
                polygonAnnotationManager.create(polygonAnnotationOptions)

                loadRegion(centerPoint, geometry, radius, downloadProgress)
            }
        }

        searchTypeSwitch = findViewById(R.id.searchTypeSwitch)
        val tapHelpText = findViewById<TextView>(R.id.tapHelpText)
        searchTypeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                searchEngineUiAdapter.searchMode = SearchMode.ONLINE
                tapHelpText.alpha = 0.0F
                circleInputLayout.visibility = View.INVISIBLE
            } else {
                searchEngineUiAdapter.searchMode = SearchMode.OFFLINE
                tapHelpText.alpha = 1.0F
                circleInputText.setText(R.string.default_radius)
            }
        }

        mapView = findViewById(R.id.map_view)
        mapView.mapboxMap.also { mapboxMap ->
            mapboxMap.loadStyle(getMapStyleUri())

            mapView.location.updateSettings {
                enabled = true
            }

            mapView.location.addOnIndicatorPositionChangedListener(object : OnIndicatorPositionChangedListener {
                override fun onIndicatorPositionChanged(point: Point) {
                    val defaultLocation = Point.fromLngLat(13.404832349386826, 52.51908033891081)
                    mapView.mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(defaultLocation)
                            .zoom(11.0)
                            .build()
                    )

                    mapView.location.removeOnIndicatorPositionChangedListener(this)
                }
            })
        }
        viewAnnotationManager = mapView.viewAnnotationManager

        mapView.mapboxMap.addOnMapClickListener(object : OnMapClickListener {
            override fun onMapClick(point: Point): Boolean {
                Log.i("MAP-CLICK", "The point $point was clicked!")
                if (searchTypeSwitch.isChecked) {
                    return true
                }
                circleInputLayout.visibility = View.VISIBLE
                val pointStr = "${point.longitude()}|${point.latitude()}"
                circleCenterPoint.setText(pointStr)
                return true
            }
        })

        mapMarkersManager = MapMarkersManager(mapView)
        mapMarkersManager.onMarkersChangeListener = {
            updateOnBackPressedCallbackEnabled()
        }

        toolbar = findViewById(R.id.toolbar)
        toolbar.apply {
            title = getString(R.string.simple_ui_toolbar_title)
            setSupportActionBar(this)
        }
        toolbar.setTitle(R.string.action_tile_download_ui_example)
        toolbar.setNavigationIcon(R.drawable.mapbox_search_sdk_close_drawable)
        toolbar.setNavigationOnClickListener {
            this.finish()
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
            settings = SearchEngineSettings()
        )

        // we need to create custom tilestore to manually handle tile region options
        tileStore = TileStore.create()

        // Remove previously download regions
        tileStore.getAllTileRegions {
            val regions = it.value
            if (regions != null) {
                for (region in regions) {
                    tileStore.removeTileRegion(region.id)
                }
            }
        }

        // create an engine and bind it with tilestore
        offlineSearchEngine = OfflineSearchEngine.create(
            OfflineSearchEngineSettings(
                tileStore = tileStore,
            )
        )

        searchEngineUiAdapter = SearchEngineUiAdapter(
            view = searchResultsView,
            searchEngine = searchEngine,
            offlineSearchEngine = offlineSearchEngine,
        )

        searchEngineUiAdapter.searchMode = SearchMode.ONLINE

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
                Log.i("OFFLINE", "Got offline results")
                closeSearchView()
                searchPlaceView.open(SearchPlace.createFromOfflineSearchResult(searchResult))
                mapMarkersManager.showMarker(searchResult.coordinate)
            }

            override fun onError(e: Exception) {
                Toast.makeText(this@TileDownloadUiActivity, "Error happened: $e", Toast.LENGTH_SHORT).show()
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

        searchPlaceView.addOnShareClickListener { searchPlace ->
            startActivity(shareIntent(searchPlace))
        }

        searchPlaceView.addOnBottomSheetStateChangedListener { _, _ ->
            updateOnBackPressedCallbackEnabled()
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

    private fun loadRegion(centerPoint: Point, geometry: Geometry, radiusKm: Int, downloadProgress: TextView) {
        // configure address tiles download
        val descriptors = listOf(OfflineSearchEngine.createTilesetDescriptor())

        val loadOptions = TileRegionLoadOptions
            .Builder()
            .descriptors(descriptors)
            .geometry(geometry)
            .acceptExpired(true)
            .build()

        val regionId = createRegionId(centerPoint, radiusKm)
        Log.i("SearchApiExample", "Loading regions: $regionId")

        // add index observer callback to track downloads
        offlineSearchEngine.addOnIndexChangeListener(object : OfflineSearchEngine.OnIndexChangeListener {
            override fun onIndexChange(event: OfflineIndexChangeEvent) {
                if ((event.regionId == regionId) && (event.type == OfflineIndexChangeEvent.EventType.ADD || event.type == OfflineIndexChangeEvent.EventType.UPDATE)) {
                    Log.i("SearchApiExample", "${event.regionId} was successfully added or updated")
                }
            }

            override fun onError(event: OfflineIndexErrorEvent) {
                Log.i("SearchApiExample", "Offline index error: ${event.regionId} $event")
            }
        })

        // start tiles download
        tileStore.loadTileRegion(
            regionId,
            loadOptions,
            { progress ->
                lifecycleScope.launch(Dispatchers.Main) {
                    val donePercent = (progress.completedResourceSize.toDouble() / progress.requiredResourceCount.toDouble()).toInt()
                    Log.i("SearchApiExample", "Loading $regionId, done: $donePercent progress: $progress")
                    downloadProgress.text = getString(R.string.downloaded_pc_text, donePercent)
                }
            },
            { result ->
                lifecycleScope.launch(Dispatchers.Main) {
                    val msg: String
                    if (result.isValue) {
                        msg = "Region '$regionId' was downloaded"
                        downloadProgress.text = getString(R.string.downloaded_100pc)
                        delay(2000)
                        downloadProgress.text = ""
                    } else {
                        msg = "Error download region '$regionId', err: ${result.error}"
                    }
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun createRegionId(point: Point, radius: Int) = "${point.longitude()}-${point.latitude()}-$radius"

    private fun updateOnBackPressedCallbackEnabled() {
        onBackPressedCallback.isEnabled = searchPlaceView.isHidden().not() || mapMarkersManager.hasMarkers
    }

    private fun closeSearchView() {
        toolbar.collapseActionView()
        searchView.setQuery("", false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.simple_ui_activity_options_menu, menu)

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
        searchView.setOnSearchClickListener {
            Log.i("SEARCH-CLICK", "WAS CLICKED!")
            circleInputLayout.visibility = View.INVISIBLE
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val proximity = mapView.mapboxMap.cameraState.center
                Log.i("MAP-VIEW", "MAP bounds: $proximity")

                searchEngineUiAdapter.search(newText)
                return false
            }
        })
        return true
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
                CameraOptions
                    .Builder()
                    .center(coordinates.first())
                    .padding(MARKERS_INSETS_OPEN_CARD)
                    .zoom(14.0)
                    .build()
            } else {
                mapboxMap.cameraForCoordinates(
                    coordinates = coordinates,
                    coordinatesPadding = MARKERS_INSETS,
                    bearing = null,
                    pitch = null
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
    }
}
