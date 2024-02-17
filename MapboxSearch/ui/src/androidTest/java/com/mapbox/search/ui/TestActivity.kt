package com.mapbox.search.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
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
import com.mapbox.common.location.LocationProvider
import com.mapbox.geojson.Point
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.base.utils.extension.toPoint
import com.mapbox.search.common.DistanceCalculator
import com.mapbox.search.common.tests.FixedPointLocationEngine
import com.mapbox.search.offline.OfflineResponseInfo
import com.mapbox.search.offline.OfflineSearchEngine
import com.mapbox.search.offline.OfflineSearchEngineSettings
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.adapter.engines.SearchEngineUiAdapter
import com.mapbox.search.ui.test.R
import com.mapbox.search.ui.tools.MockWebServerRule
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import java.util.Locale

public class TestActivity : AppCompatActivity() {

    private lateinit var locationProvider: LocationProvider

    private lateinit var toolbar: Toolbar
    private lateinit var searchView: SearchView

    private lateinit var searchResultsView: SearchResultsView
    private lateinit var searchEngineUiAdapter: SearchEngineUiAdapter
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            when {
                !searchPlaceView.isHidden() -> searchPlaceView.hide()
                else -> error("This OnBackPressedCallback should not be enabled")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test)

        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        toolbar = findViewById(R.id.toolbar)
        toolbar.apply {
            title = ""
            setSupportActionBar(this)
        }

        locationProvider = FixedPointLocationEngine(Constants.TEST_USER_LOCATION)

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
                    CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
                )
            )
            isVisible = false
        }

        val searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            apiType = ApiType.SBS,
            settings = SearchEngineSettings(
                locationProvider = locationProvider,
                singleBoxSearchBaseUrl = "http://localhost:${MockWebServerRule.DEFAULT_PORT}/"
            )
        )

        val offlineSearchEngine = OfflineSearchEngine.create(
            OfflineSearchEngineSettings(
                locationProvider = locationProvider,
            )
        )

        searchEngineUiAdapter = SearchEngineUiAdapter(
            view = searchResultsView,
            searchEngine = searchEngine,
            offlineSearchEngine = offlineSearchEngine,
            locationEngine = locationProvider,
        )

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
            }

            override fun onOfflineSearchResultsShown(results: List<OfflineSearchResult>, responseInfo: OfflineResponseInfo) {
                closeSearchView()
            }

            override fun onSuggestionSelected(searchSuggestion: SearchSuggestion): Boolean {
                return false
            }

            override fun onSearchResultSelected(searchResult: SearchResult, responseInfo: ResponseInfo) {
                closeSearchView()
                searchPlaceView.open(SearchPlace.createFromSearchResult(searchResult, responseInfo))
            }

            override fun onOfflineSearchResultSelected(searchResult: OfflineSearchResult, responseInfo: OfflineResponseInfo) {
                closeSearchView()
                searchPlaceView.open(SearchPlace.createFromOfflineSearchResult(searchResult))
            }

            override fun onError(e: Exception) {
                Toast.makeText(applicationContext, "Error happened: $e", Toast.LENGTH_SHORT).show()
            }

            override fun onHistoryItemClick(historyRecord: HistoryRecord) {
                closeSearchView()
                searchPlaceView.open(SearchPlace.createFromIndexableRecord(historyRecord, distanceMeters = null))

                locationProvider.userDistanceTo(historyRecord.coordinate) { distance ->
                    distance?.let {
                        searchPlaceView.updateDistance(distance)
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
            searchPlaceView.hide()
        }

        searchPlaceView.addOnBottomSheetStateChangedListener { _, _ ->
            onBackPressedCallback.isEnabled = !searchPlaceView.isHidden()
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

    private fun closeSearchView() {
        toolbar.collapseActionView()
        searchView.setQuery("", false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.test_activity_menu, menu)

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
        searchView.queryHint = ""
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

    private companion object {
        const val PERMISSIONS_REQUEST_LOCATION = 0

        fun Context.isPermissionGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}
