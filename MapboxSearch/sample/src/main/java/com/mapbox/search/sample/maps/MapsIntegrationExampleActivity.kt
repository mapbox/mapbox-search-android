package com.mapbox.search.sample.maps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
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
import com.mapbox.search.result.SearchResult
import com.mapbox.search.sample.R
import com.mapbox.search.sample.SearchViewBottomSheetsMediator
import com.mapbox.search.sample.SearchViewBottomSheetsMediator.SearchBottomSheetsEventsListener
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.category.SearchCategoriesBottomSheetView
import com.mapbox.search.ui.view.category.SearchCategoriesBottomSheetView.CategoryLoadingStateListener
import com.mapbox.search.ui.view.feedback.SearchFeedbackBottomSheetView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView

class MapsIntegrationExampleActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap

    private lateinit var searchBottomSheetView: SearchBottomSheetView
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView
    private lateinit var searchCategoriesView: SearchCategoriesBottomSheetView
    private lateinit var feedbackBottomSheetView: SearchFeedbackBottomSheetView

    private lateinit var cardsMediator: SearchViewBottomSheetsMediator

    private var markerCoordinates = mutableListOf<Point>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps_integration)

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

        searchBottomSheetView = findViewById(R.id.search_view)
        searchBottomSheetView.initializeSearch(savedInstanceState, SearchBottomSheetView.Configuration())

        searchPlaceView = findViewById<SearchPlaceBottomSheetView>(R.id.search_place_view).apply {
            initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))

            isNavigateButtonVisible = false
            isShareButtonVisible = false
            isFavoriteButtonVisible = false
        }

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

        cardsMediator.addSearchBottomSheetsEventsListener(object : SearchBottomSheetsEventsListener {
            override fun onOpenPlaceBottomSheet(place: SearchPlace) {
                showMarker(place.coordinate)
            }

            override fun onOpenCategoriesBottomSheet(category: Category) {}

            override fun onBackToMainBottomSheet() {
                clearMarkers()
            }
        })

        searchCategoriesView.addCategoryLoadingStateListener(object : CategoryLoadingStateListener {
            override fun onLoadingStart(category: Category) {}

            override fun onCategoryResultsLoaded(category: Category, searchResults: List<SearchResult>) {
                showMarkers(searchResults.mapNotNull { it.coordinate })
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

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        cardsMediator.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    private fun getMapStyleUri(): String {
        val darkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return when (darkMode) {
            Configuration.UI_MODE_NIGHT_YES -> Style.DARK
            Configuration.UI_MODE_NIGHT_NO,
            Configuration.UI_MODE_NIGHT_UNDEFINED -> Style.MAPBOX_STREETS
            else -> error("Unknown night mode: $darkMode")
        }
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
