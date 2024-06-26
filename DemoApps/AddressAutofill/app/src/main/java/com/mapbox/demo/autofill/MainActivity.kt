package com.mapbox.demo.autofill

import android.Manifest
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mapbox.common.location.LocationProvider
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.search.autofill.AddressAutofill
import com.mapbox.search.autofill.AddressAutofillOptions
import com.mapbox.search.autofill.AddressAutofillResult
import com.mapbox.search.autofill.AddressAutofillSuggestion
import com.mapbox.search.autofill.Query
import com.mapbox.search.ui.adapter.autofill.AddressAutofillUiAdapter
import com.mapbox.search.ui.adapter.location.setLocationObservationTimeout
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var addressAutofill: AddressAutofill
    private lateinit var locationProvider: LocationProvider

    private lateinit var searchResultsView: SearchResultsView
    private lateinit var searchEngineUiAdapter: AddressAutofillUiAdapter

    private lateinit var queryEditText: EditText

    private lateinit var apartmentEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var stateEditText: EditText
    private lateinit var zipEditText: EditText
    private lateinit var fullAddress: TextView
    private lateinit var pinCorrectionNote: TextView
    private lateinit var mapView: MapView
    private lateinit var mapPin: View
    private lateinit var mapboxMap: MapboxMap

    private var ignoreNextMapIdleEvent: Boolean = false
    private var ignoreNextQueryTextUpdate: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setLocationObservationTimeout(5_000L)

        locationProvider = LocationServiceFactory.getOrCreate()
            .getDeviceLocationProvider(null)
            .value
            ?: throw IllegalStateException("No LocationProvider found")

        addressAutofill = AddressAutofill.create(locationProvider = locationProvider)

        queryEditText = findViewById(R.id.query_text)
        apartmentEditText = findViewById(R.id.address_apartment)
        cityEditText = findViewById(R.id.address_city)
        stateEditText = findViewById(R.id.address_state)
        zipEditText = findViewById(R.id.address_zip)
        fullAddress = findViewById(R.id.full_address)
        pinCorrectionNote = findViewById(R.id.pin_correction_note)

        mapPin = findViewById(R.id.map_pin)
        mapView = findViewById(R.id.map)
        mapboxMap = mapView.mapboxMap
        mapboxMap.loadStyle(Style.MAPBOX_STREETS)

        mapboxMap.subscribeMapIdle {
            if (ignoreNextMapIdleEvent) {
                ignoreNextMapIdleEvent = false
                return@subscribeMapIdle
            }

            val mapCenter = mapboxMap.cameraState.center
            findAddress(mapCenter)
        }

        searchResultsView = findViewById(R.id.search_results_view)

        searchResultsView.initialize(
            SearchResultsView.Configuration(
                commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
            )
        )

        searchEngineUiAdapter = AddressAutofillUiAdapter(
            view = searchResultsView,
            addressAutofill = addressAutofill
        )

        searchEngineUiAdapter.addSearchListener(object : AddressAutofillUiAdapter.SearchListener {

            override fun onSuggestionSelected(suggestion: AddressAutofillSuggestion) {
                showAddressAutofillSuggestion(
                    suggestion,
                    fromReverseGeocoding = false,
                )
            }

            override fun onSuggestionsShown(suggestions: List<AddressAutofillSuggestion>) {
                // Nothing to do
            }

            override fun onError(e: Exception) {
                // Nothing to do
            }
        })

        queryEditText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                if (ignoreNextQueryTextUpdate) {
                    ignoreNextQueryTextUpdate = false
                    return
                }

                val query = Query.create(text.toString())
                if (query != null) {
                    lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            searchEngineUiAdapter.search(query)
                        }
                    }
                }
                searchResultsView.isVisible = query != null
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
        } else {
            locationProvider.lastKnownLocation(this) { point ->
                point?.let {
                    mapView.mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(9.0)
                            .build()
                    )
                    ignoreNextMapIdleEvent = true
                }
            }
        }
    }

    private fun findAddress(point: Point) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val response = addressAutofill.reverse(point, AddressAutofillOptions())
                response.onValue { suggestions ->
                    if (suggestions.isEmpty()) {
                        showToast(R.string.address_autofill_error_pin_correction)
                    } else {
                        showAddressAutofillSuggestion(
                            suggestions.first().suggestion,
                            fromReverseGeocoding = true
                        )
                    }
                }.onError {
                    showToast(R.string.address_autofill_error_pin_correction)
                }
            }
        }
    }

    private fun showAddressAutofillSuggestion(suggestion: AddressAutofillSuggestion, fromReverseGeocoding: Boolean) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val response = addressAutofill.select(suggestion)
                response.onValue { result ->
                    showAddressAutofillResult(result, fromReverseGeocoding)
                }.onError {
                    showToast(R.string.address_autofill_error_select)
                }
            }
        }
    }

    private fun showAddressAutofillResult(result: AddressAutofillResult, fromReverseGeocoding: Boolean) {
        val address = result.address

        cityEditText.setText(address.place)
        stateEditText.setText(address.region)
        zipEditText.setText(address.postcode)

        fullAddress.isVisible = true
        fullAddress.text = result.suggestion.formattedAddress

        pinCorrectionNote.isVisible = true

        if (!fromReverseGeocoding) {
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(result.coordinate)
                    .zoom(16.0)
                    .build()
            )
            ignoreNextMapIdleEvent = true
            mapPin.isVisible = true
        }

        ignoreNextQueryTextUpdate = true
        queryEditText.setText(
            listOfNotNull(
                address.houseNumber,
                address.street
            ).joinToString()
        )
        queryEditText.clearFocus()

        searchResultsView.isVisible = false
        searchResultsView.hideKeyboard()
    }

    private companion object {
        const val PERMISSIONS_REQUEST_LOCATION = 0
    }
}
