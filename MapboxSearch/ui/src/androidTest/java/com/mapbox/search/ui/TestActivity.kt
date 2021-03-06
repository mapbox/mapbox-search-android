package com.mapbox.search.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.LocaleList
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.search.ApiType
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.OfflineSearchEngineSettings
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.common.tests.BuildConfig
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.test.R
import com.mapbox.search.ui.tools.MockWebServerRule
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import java.util.Locale

public class TestActivity : AppCompatActivity() {

    private val serviceProvider = MapboxSearchSdk.serviceProvider
    private lateinit var locationEngine: LocationEngine

    private lateinit var toolbar: Toolbar
    private lateinit var searchView: SearchView

    private lateinit var searchResultsView: SearchResultsView
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test)

        toolbar = findViewById(R.id.toolbar)
        toolbar.apply {
            title = ""
            setSupportActionBar(this)
        }

        locationEngine = FixedPointLocationEngine(Point.fromLngLat(-122.084000, 37.421998))

        // Ensure distance formatting uses miles (not meters).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(Locale.US, Locale.ENGLISH)
            LocaleList.setDefault(localeList)
        } else {
            Locale.setDefault(Locale.US)
        }

        searchResultsView = findViewById<SearchResultsView>(R.id.search_results_view).apply {
            initialize(
                SearchResultsView.Configuration(
                    commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL),
                    searchEngineSettings = SearchEngineSettings(
                        accessToken = BuildConfig.MAPBOX_API_TOKEN,
                        locationEngine = locationEngine,
                        singleBoxSearchBaseUrl = "http://localhost:${MockWebServerRule.DEFAULT_PORT}/"
                    ),
                    offlineSearchEngineSettings = OfflineSearchEngineSettings(
                        accessToken = BuildConfig.MAPBOX_API_TOKEN,
                        locationEngine = locationEngine,
                    ),
                    apiType = ApiType.SBS,
                )
            )
            isVisible = false
        }

        searchResultsView.addSearchListener(object : SearchResultsView.SearchListener {

            override fun onCategoryResult(
                suggestion: SearchSuggestion,
                results: List<SearchResult>,
                responseInfo: ResponseInfo
            ) {
                closeSearchView()
            }

            override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                // Nothing to do
            }

            override fun onSearchResult(searchResult: SearchResult, responseInfo: ResponseInfo) {
                closeSearchView()
                val coordinate = searchResult.coordinate
                if (coordinate != null) {
                    searchPlaceView.open(SearchPlace.createFromSearchResult(searchResult, responseInfo, coordinate))
                }
            }

            override fun onOfflineSearchResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                closeSearchView()
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
            searchPlaceView.hide()
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
            !searchPlaceView.isHidden() -> searchPlaceView.hide()
            else -> super.onBackPressed()
        }
    }

    private fun closeSearchView() {
        toolbar.collapseActionView()
        searchView.setQuery("", false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.test_activity_menu, menu)

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
        searchView.queryHint = ""
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

    private class FixedPointLocationEngine(private val location: Location) : LocationEngine {

        constructor(point: Point) : this(point.toLocation())

        override fun getLastLocation(locationEngineCallback: LocationEngineCallback<LocationEngineResult>) {
            locationEngineCallback.onSuccess(LocationEngineResult.create(location))
        }

        override fun requestLocationUpdates(
            locationEngineRequest: LocationEngineRequest,
            locationEngineCallback: LocationEngineCallback<LocationEngineResult>,
            looper: Looper?
        ) {
            val callbackRunnable = Runnable {
                locationEngineCallback.onSuccess(LocationEngineResult.create(location))
            }

            if (looper != null) {
                Handler(looper).post(callbackRunnable)
            } else {
                callbackRunnable.run()
            }
        }

        override fun requestLocationUpdates(locationEngineRequest: LocationEngineRequest, pendingIntent: PendingIntent?) {
            throw NotImplementedError()
        }

        override fun removeLocationUpdates(locationEngineCallback: LocationEngineCallback<LocationEngineResult>) {
            // Do nothing
        }

        override fun removeLocationUpdates(pendingIntent: PendingIntent?) {
            // Do nothing
        }

        private companion object {
            fun Point.toLocation(): Location {
                val location = Location("")
                location.latitude = latitude()
                location.longitude = longitude()
                return location
            }
        }
    }

    private companion object {
        const val PERMISSIONS_REQUEST_LOCATION = 0

        fun Context.isPermissionGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}
