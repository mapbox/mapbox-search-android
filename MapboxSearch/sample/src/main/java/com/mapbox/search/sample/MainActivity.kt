package com.mapbox.search.sample

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mapbox.search.result.SearchResult
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
import com.mapbox.search.sample.maps.MapsIntegrationExampleActivity
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.SearchMode
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.category.SearchCategoriesBottomSheetView
import com.mapbox.search.ui.view.feedback.SearchFeedbackBottomSheetView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView

class MainActivity : AppCompatActivity() {

    private lateinit var searchBottomSheetView: SearchBottomSheetView
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView
    private lateinit var searchCategoriesView: SearchCategoriesBottomSheetView
    private lateinit var feedbackBottomSheetView: SearchFeedbackBottomSheetView

    private lateinit var cardsMediator: SearchViewBottomSheetsMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Toolbar>(R.id.toolbar).apply {
            title = ""
            setSupportActionBar(this)
        }

        val configuration = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            SearchBottomSheetView.Configuration(
                collapsedStateAnchor = SearchBottomSheetView.CollapsedStateAnchor.SEARCH_BAR
            )
        } else {
            SearchBottomSheetView.Configuration()
        }

        searchBottomSheetView = findViewById(R.id.search_view)
        searchBottomSheetView.initializeSearch(savedInstanceState, configuration)
        searchBottomSheetView.isHideableByDrag = true
        searchBottomSheetView.searchMode = SearchMode.AUTO

        searchPlaceView = findViewById(R.id.search_place_view)
        searchPlaceView.initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))

        searchCategoriesView = findViewById(R.id.search_categories_view)
        searchCategoriesView.initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))

        feedbackBottomSheetView = findViewById(R.id.search_feedback_view)
        feedbackBottomSheetView.initialize(savedInstanceState)

        cardsMediator = SearchViewBottomSheetsMediator(
            searchBottomSheetView,
            searchPlaceView,
            searchCategoriesView,
            feedbackBottomSheetView,
        )

        savedInstanceState?.let {
            cardsMediator.onRestoreInstanceState(it)
        }

        findViewById<View>(R.id.root).setOnClickListener {
            if (searchPlaceView.isHidden() && searchCategoriesView.isHidden()) {
                if (searchBottomSheetView.isHidden()) {
                    searchBottomSheetView.open()
                } else {
                    searchBottomSheetView.hide()
                }
            }
        }

        // Process bottom sheets events
        cardsMediator.addSearchBottomSheetsEventsListener(object : SearchViewBottomSheetsMediator.SearchBottomSheetsEventsListener {
            override fun onOpenPlaceBottomSheet(place: SearchPlace) {}

            override fun onOpenCategoriesBottomSheet(category: Category) {}

            override fun onBackToMainBottomSheet() {}
        })

        searchPlaceView.addOnNavigateClickListener { searchPlace ->
            startActivity(IntentUtils.geoIntent(searchPlace.coordinate))
        }

        searchPlaceView.addOnShareClickListener { searchPlace ->
            startActivity(IntentUtils.shareIntent(searchPlace))
        }

        searchCategoriesView.addCategoryLoadingStateListener(object : SearchCategoriesBottomSheetView.CategoryLoadingStateListener {
            override fun onLoadingStart(category: Category) {}

            override fun onCategoryResultsLoaded(category: Category, searchResults: List<SearchResult>) {
                Toast.makeText(
                    applicationContext,
                    "Loaded ${searchResults.size} results for $category",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onLoadingError(category: Category) {}
        })

        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    override fun onBackPressed() {
        if (!cardsMediator.handleOnBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.open_simple_ui -> {
                startActivity(Intent(this, SimpleUiSearchActivity::class.java))
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
            R.id.open_maps_integration_example -> {
                startActivity(Intent(this, MapsIntegrationExampleActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        cardsMediator.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    private companion object {

        const val PERMISSIONS_REQUEST_LOCATION = 0

        fun Context.isPermissionGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}
