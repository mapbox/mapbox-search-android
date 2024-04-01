package com.mapbox.search.sample

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
import androidx.lifecycle.lifecycleScope
import com.mapbox.android.core.location.LocationEngineProvider
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
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView

class AddressAutofillUiActivity : AppCompatActivity() {

    private lateinit var addressAutofill: AddressAutofill

    private lateinit var searchResultsView: SearchResultsView
    private lateinit var addressAutofillUiAdapter: AddressAutofillUiAdapter

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
        setContentView(R.layout.activity_address_autofill)

        addressAutofill = AddressAutofill.create(getString(R.string.mapbox_access_token))

        queryEditText = findViewById(R.id.query_text)
        apartmentEditText = findViewById(R.id.address_apartment)
        cityEditText = findViewById(R.id.address_city)
        stateEditText = findViewById(R.id.address_state)
        zipEditText = findViewById(R.id.address_zip)
        fullAddress = findViewById(R.id.full_address)
        pinCorrectionNote = findViewById(R.id.pin_correction_note)

        mapPin = findViewById(R.id.map_pin)
        mapView = findViewById(R.id.map)
        mapboxMap = mapView.getMapboxMap()
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS)
        mapboxMap.addOnMapIdleListener {
            if (ignoreNextMapIdleEvent) {
                ignoreNextMapIdleEvent = false
                return@addOnMapIdleListener
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

        addressAutofillUiAdapter = AddressAutofillUiAdapter(
            view = searchResultsView,
            addressAutofill = addressAutofill
        )

        LocationEngineProvider.getBestLocationEngine(applicationContext).lastKnownLocation(this) { point ->
            point?.let {
                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(point)
                        .zoom(9.0)
                        .build()
                )
                ignoreNextMapIdleEvent = true
            }
        }

        addressAutofillUiAdapter.addSearchListener(object : AddressAutofillUiAdapter.SearchListener {

            override fun onSuggestionSelected(suggestion: AddressAutofillSuggestion) {
                selectSuggestion(suggestion)
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
                    lifecycleScope.launchWhenStarted {
                        addressAutofillUiAdapter.search(query)
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
        }
    }

    private fun findAddress(point: Point) {
        lifecycleScope.launchWhenStarted {
            val response = addressAutofill.reverse(point, AddressAutofillOptions())
            response.onValue { result ->
                if (result.isEmpty()) {
                    showToast(R.string.address_autofill_error_pin_correction)
                } else {
                    showAddressAutofillResult(result.first(), fromReverseGeocoding = true)
                }
            }.onError {
                showToast(R.string.address_autofill_error_pin_correction)
            }
        }
    }

    private fun selectSuggestion(suggestion: AddressAutofillSuggestion) {
        lifecycleScope.launchWhenStarted {
            val response = addressAutofill.select(suggestion)
            response.onValue { result ->
                showAddressAutofillResult(result, fromReverseGeocoding = false)
            }.onError {
                showToast(R.string.address_autofill_error_select)
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
            mapView.getMapboxMap().setCamera(
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
