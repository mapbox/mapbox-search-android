package com.mapbox.search.sample

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.category.SearchCategoriesBottomSheetView
import com.mapbox.search.ui.view.feedback.SearchFeedbackBottomSheetView
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView

class CustomThemeActivity : AppCompatActivity() {

    private lateinit var searchBottomSheetView: SearchBottomSheetView
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView
    private lateinit var searchCategoriesView: SearchCategoriesBottomSheetView
    private lateinit var feedbackBottomSheetView: SearchFeedbackBottomSheetView

    private lateinit var cardsMediator: SearchViewBottomSheetsMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_custom_theme)

        searchBottomSheetView = findViewById(R.id.search_view)
        searchBottomSheetView.initializeSearch(savedInstanceState, SearchBottomSheetView.Configuration())

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
