package com.mapbox.search.sample

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.mapbox.android.gestures.Utils.dpToPx
import com.mapbox.common.location.LocationProvider
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.search.base.utils.extension.toPoint
import com.mapbox.search.category.Category
import com.mapbox.search.category.CategoryAddress
import com.mapbox.search.category.CategoryOptions
import com.mapbox.search.category.CategoryQuery
import com.mapbox.search.category.CategoryResult
import com.mapbox.search.common.DistanceCalculator
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import java.util.UUID

class CategoryActivity : AppCompatActivity() {

    private lateinit var category: Category
    private lateinit var locationProvider: LocationProvider

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapMarkersManager: MapMarkersManager

    private lateinit var searchNearby: View
    private lateinit var searchThisArea: View

    private lateinit var searchPlaceView: SearchPlaceBottomSheetView

    private fun defaultDeviceLocationProvider(): LocationProvider =
        LocationServiceFactory.getOrCreate()
            .getDeviceLocationProvider(null)
            .value
            ?: throw Exception("Failed to get device location provider")

    private fun Context.showToast(@StringRes resId: Int): Unit = Toast.makeText(this, resId, Toast.LENGTH_LONG).show()

    private fun Context.isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        // Set your Access Token here if it's not already set in some other way
        // MapboxOptions.accessToken = "<my-access-token>"
        category = Category.create()
        locationProvider = defaultDeviceLocationProvider()

        mapView = findViewById(R.id.map_view)
        mapMarkersManager = MapMarkersManager(mapView)
        mapView.mapboxMap.also { mapboxMap ->
            this.mapboxMap = mapboxMap

            mapboxMap.loadStyle(Style.MAPBOX_STREETS) {
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
        }

        searchNearby = findViewById(R.id.search_nearby)
        searchNearby.setOnClickListener {
            locationProvider.getLastLocation { location ->
                if (location == null) {
                    return@getLastLocation
                }

                lifecycleScope.launchWhenStarted {
                    val response = category.search(
                        query = CategoryQuery.Category.COFFEE_SHOP_CAFE,
                        proximity = location.toPoint(),
                        options = CategoryOptions(limit = 20)
                    )

                    response.onValue { results ->
                        mapMarkersManager.showResults(results)
                    }.onError { e ->
                        Log.d("CategoryApiExample", "Error happened during search request", e)
                        showToast(R.string.category_search_error)
                    }
                }
            }
        }

        searchThisArea = findViewById(R.id.search_this_area)
        searchThisArea.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                val response = category.search(
                    query = CategoryQuery.Category.COFFEE_SHOP_CAFE,
                    region = mapboxMap.getBounds().bounds.toBoundingBox(),
                    options = CategoryOptions(limit = 20)
                )

                response.onValue { results ->
                    mapMarkersManager.showResults(results)
                }.onError { e ->
                    Log.d("CategoryApiExample", "Error happened during search request", e)
                    showToast(R.string.category_search_error)
                }
            }
        }

        searchPlaceView = findViewById<SearchPlaceBottomSheetView>(R.id.search_place_view).apply {
            initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
            isFavoriteButtonVisible = false
            addOnCloseClickListener {
                mapMarkersManager.adjustMarkersForClosedCard()
                searchPlaceView.hide()
            }
        }

        mapMarkersManager.onResultClickListener = { result ->
            mapMarkersManager.adjustMarkersForOpenCard()
            searchPlaceView.open(result.toSearchPlace())
            locationProvider.userDistanceTo(result.coordinate) { distance ->
                distance?.let { searchPlaceView.updateDistance(distance) }
            }
        }

        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
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

    private class MapMarkersManager(mapView: MapView) {

        private val annotations = mutableMapOf<String, CategoryResult>()
        private val mapboxMap: MapboxMap = mapView.mapboxMap
        private val pointAnnotationManager = mapView.annotations.createPointAnnotationManager(null)
        private val pinBitmap = mapView.context.bitmapFromDrawableRes(R.drawable.red_marker)

        var onResultClickListener: ((CategoryResult) -> Unit)? = null

        init {
            pointAnnotationManager.addClickListener {
                annotations[it.id]?.let { result ->
                    onResultClickListener?.invoke(result)
                }
                true
            }
        }

        private fun Context.bitmapFromDrawableRes(@DrawableRes resId: Int): Bitmap = BitmapFactory.decodeResource(resources, resId)

        fun clearMarkers() {
            pointAnnotationManager.deleteAll()
            annotations.clear()
        }

        fun adjustMarkersForOpenCard() {
            val coordinates = annotations.values.map { it.coordinate }
            val cameraOptions = mapboxMap.cameraForCoordinates(
                coordinates, MARKERS_INSETS_OPEN_CARD, bearing = null, pitch = null
            )
            mapboxMap.setCamera(cameraOptions)
        }

        fun adjustMarkersForClosedCard() {
            val coordinates = annotations.values.map { it.coordinate }
            val cameraOptions = mapboxMap.cameraForCoordinates(
                coordinates, MARKERS_INSETS, bearing = null, pitch = null
            )
            mapboxMap.setCamera(cameraOptions)
        }

        fun showResults(results: List<CategoryResult>) {
            clearMarkers()
            if (results.isEmpty()) {
                return
            }

            val coordinates = ArrayList<Point>(results.size)
            results.forEach { result ->
                val options = PointAnnotationOptions()
                    .withPoint(result.coordinate)
                    .withIconImage(pinBitmap)
                    .withIconAnchor(IconAnchor.BOTTOM)

                val annotation = pointAnnotationManager.create(options)
                annotations[annotation.id] = result
                coordinates.add(result.coordinate)
            }

            val cameraOptions = mapboxMap.cameraForCoordinates(
                coordinates, MARKERS_INSETS, bearing = null, pitch = null
            )
            mapboxMap.setCamera(cameraOptions)
        }
    }

    private companion object {

        const val PERMISSIONS_REQUEST_LOCATION = 0

        val MARKERS_BOTTOM_OFFSET = dpToPx(176f).toDouble()
        val MARKERS_EDGE_OFFSET = dpToPx(64f).toDouble()
        val PLACE_CARD_HEIGHT = dpToPx(300f).toDouble()

        val MARKERS_INSETS = EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_BOTTOM_OFFSET, MARKERS_EDGE_OFFSET
        )

        val MARKERS_INSETS_OPEN_CARD = EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, PLACE_CARD_HEIGHT, MARKERS_EDGE_OFFSET
        )

        fun CategoryAddress.toSearchAddress(): SearchAddress {
            return SearchAddress(
                houseNumber = houseNumber,
                street = street,
                neighborhood = neighborhood,
                locality = locality,
                postcode = postcode,
                place = place,
                district = district,
                region = region,
                country = country
            )
        }

        fun CategoryResult.toSearchPlace(): SearchPlace {
            return SearchPlace(
                id = name + UUID.randomUUID().toString(),
                name = name,
                descriptionText = null,
                address = address.toSearchAddress(),
                resultTypes = listOf(SearchResultType.POI),
                record = null,
                coordinate = coordinate,
                routablePoints = routablePoints,
                categories = categories,
                makiIcon = makiIcon,
                metadata = null,
                distanceMeters = null,
                feedback = null,
            )
        }
    }
}
