package com.mapbox.search.sample

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteOptions
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.mapbox.search.autocomplete.PlaceAutocompleteType
import com.mapbox.search.base.location.defaultLocationProvider
import com.mapbox.search.base.utils.extension.toPoint
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView

class PlaceAutocompleteUiActivity : AppCompatActivity() {

    private lateinit var placeAutocomplete: PlaceAutocomplete

    private lateinit var searchResultsView: SearchResultsView
    private lateinit var placeAutocompleteUiAdapter: PlaceAutocompleteUiAdapter

    private lateinit var queryEditText: EditText

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapMarkersManager: MapMarkersManager

    private lateinit var searchPlaceView: SearchPlaceBottomSheetView

    private var ignoreNextQueryUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_autocomplete)

        placeAutocomplete = PlaceAutocomplete.create()

        queryEditText = findViewById(R.id.query_text)

        mapView = findViewById(R.id.map_view)
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

        mapMarkersManager = MapMarkersManager(mapView)

        mapboxMap.addOnMapLongClickListener {
            reverseGeocoding(it)
            return@addOnMapLongClickListener true
        }

        searchResultsView = findViewById(R.id.search_results_view)

        searchResultsView.initialize(
            SearchResultsView.Configuration(
                commonConfiguration = CommonSearchViewConfiguration()
            )
        )

        placeAutocompleteUiAdapter = PlaceAutocompleteUiAdapter(
            view = searchResultsView,
            placeAutocomplete = placeAutocomplete
        )

        searchPlaceView = findViewById<SearchPlaceBottomSheetView>(R.id.search_place_view).apply {
            initialize(CommonSearchViewConfiguration())

            isFavoriteButtonVisible = false

            addOnCloseClickListener {
                hide()
                closePlaceCard()
            }

            addOnNavigateClickListener { searchPlace ->
                startActivity(geoIntent(searchPlace.coordinate))
            }

            addOnShareClickListener { searchPlace ->
                startActivity(shareIntent(searchPlace))
            }
        }

        defaultLocationProvider()?.getLastLocation { location ->
            location?.toPoint()?.let { point ->
                mapView.mapboxMap.setCamera(
                    CameraOptions.Builder()
                        .center(point)
                        .zoom(9.0)
                        .build()
                )
            }
        }

        placeAutocompleteUiAdapter.addSearchListener(object : PlaceAutocompleteUiAdapter.SearchListener {

            override fun onSuggestionsShown(suggestions: List<PlaceAutocompleteSuggestion>) {
                // Nothing to do
            }

            override fun onSuggestionSelected(suggestion: PlaceAutocompleteSuggestion) {
                openPlaceCard(suggestion)
            }

            override fun onPopulateQueryClick(suggestion: PlaceAutocompleteSuggestion) {
                queryEditText.setText(suggestion.name)
            }

            override fun onError(e: Exception) {
                // Nothing to do
            }
        })

        queryEditText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                if (ignoreNextQueryUpdate) {
                    ignoreNextQueryUpdate = false
                } else {
                    closePlaceCard()
                }

                lifecycleScope.launchWhenStarted {
                    placeAutocompleteUiAdapter.search(text.toString())
                    searchResultsView.isVisible = text.isNotEmpty()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Nothing to do
            }

            override fun afterTextChanged(s: Editable) {
                // Nothing to do
            }
        })

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

    private fun reverseGeocoding(point: Point) {
        val types: List<PlaceAutocompleteType> = when (mapboxMap.cameraState.zoom) {
            in 0.0..4.0 -> REGION_LEVEL_TYPES
            in 4.0..6.0 -> DISTRICT_LEVEL_TYPES
            in 6.0..12.0 -> LOCALITY_LEVEL_TYPES
            else -> ALL_TYPES
        }

        lifecycleScope.launchWhenStarted {
            val response = placeAutocomplete.suggestions(point, PlaceAutocompleteOptions(types = types))
            response.onValue { suggestions ->
                if (suggestions.isEmpty()) {
                    showToast(R.string.place_autocomplete_reverse_geocoding_error_message)
                } else {
                    openPlaceCard(suggestions.first())
                }
            }.onError { error ->
                Log.d(LOG_TAG, "Reverse geocoding error", error)
                showToast(R.string.place_autocomplete_reverse_geocoding_error_message)
            }
        }
    }

    private fun Context.showToast(@StringRes resId: Int): Unit = Toast.makeText(this, resId, Toast.LENGTH_LONG).show()

    private fun Context.isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun geoIntent(point: Point): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${point.latitude()}, ${point.longitude()}"))

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

    private fun openPlaceCard(suggestion: PlaceAutocompleteSuggestion) {
        ignoreNextQueryUpdate = true
        queryEditText.setText("")

        lifecycleScope.launchWhenStarted {
            placeAutocomplete.select(suggestion).onValue { result ->
                mapMarkersManager.showMarker(suggestion.coordinate)
                searchPlaceView.open(SearchPlace.createFromPlaceAutocompleteResult(result))
                queryEditText.hideKeyboard()
                searchResultsView.isVisible = false
            }.onError { error ->
                Log.d(LOG_TAG, "Suggestion selection error", error)
                showToast(R.string.place_autocomplete_selection_error)
            }
        }
    }

    private fun closePlaceCard() {
        searchPlaceView.hide()
        mapMarkersManager.clearMarkers()
    }

    private fun View.hideKeyboard() =
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)

    private class MapMarkersManager(mapView: MapView) {

        private val mapboxMap = mapView.mapboxMap
        private val circleAnnotationManager = mapView.annotations.createCircleAnnotationManager(null)
        private val markers = mutableMapOf<String, Point>()

        fun clearMarkers() {
            markers.clear()
            circleAnnotationManager.deleteAll()
        }

        fun showMarker(coordinate: Point) {
            clearMarkers()

            val circleAnnotationOptions: CircleAnnotationOptions = CircleAnnotationOptions()
                .withPoint(coordinate)
                .withCircleRadius(8.0)
                .withCircleColor("#ee4e8b")
                .withCircleStrokeWidth(2.0)
                .withCircleStrokeColor("#ffffff")

            val annotation = circleAnnotationManager.create(circleAnnotationOptions)
            markers[annotation.id] = coordinate

            CameraOptions.Builder()
                .center(coordinate)
                .padding(MARKERS_INSETS_OPEN_CARD)
                .zoom(14.0)
                .build().also {
                    mapboxMap.setCamera(it)
                }
        }
    }

    private companion object {

        const val PERMISSIONS_REQUEST_LOCATION = 0

        const val LOG_TAG = "AutocompleteUiActivity"

        val MARKERS_EDGE_OFFSET = dpToPx(64).toDouble()
        val PLACE_CARD_HEIGHT = dpToPx(300).toDouble()
        val MARKERS_TOP_OFFSET = dpToPx(88).toDouble()

        val MARKERS_INSETS_OPEN_CARD = EdgeInsets(
            MARKERS_TOP_OFFSET, MARKERS_EDGE_OFFSET, PLACE_CARD_HEIGHT, MARKERS_EDGE_OFFSET
        )

        val REGION_LEVEL_TYPES = listOf(
            PlaceAutocompleteType.AdministrativeUnit.Country,
            PlaceAutocompleteType.AdministrativeUnit.Region
        )

        val DISTRICT_LEVEL_TYPES = REGION_LEVEL_TYPES + listOf(
            PlaceAutocompleteType.AdministrativeUnit.Postcode,
            PlaceAutocompleteType.AdministrativeUnit.District
        )

        val LOCALITY_LEVEL_TYPES = DISTRICT_LEVEL_TYPES + listOf(
            PlaceAutocompleteType.AdministrativeUnit.Place,
            PlaceAutocompleteType.AdministrativeUnit.Locality
        )

        private val ALL_TYPES = listOf(
            PlaceAutocompleteType.Poi,
            PlaceAutocompleteType.AdministrativeUnit.Country,
            PlaceAutocompleteType.AdministrativeUnit.Region,
            PlaceAutocompleteType.AdministrativeUnit.Postcode,
            PlaceAutocompleteType.AdministrativeUnit.District,
            PlaceAutocompleteType.AdministrativeUnit.Place,
            PlaceAutocompleteType.AdministrativeUnit.Locality,
            PlaceAutocompleteType.AdministrativeUnit.Neighborhood,
            PlaceAutocompleteType.AdministrativeUnit.Street,
            PlaceAutocompleteType.AdministrativeUnit.Address,
        )

        private fun dpToPx(dp: Int): Int = (dp * Resources.getSystem().displayMetrics.density).toInt()
    }
}
