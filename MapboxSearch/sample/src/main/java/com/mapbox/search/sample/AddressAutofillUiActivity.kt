package com.mapbox.search.sample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.search.autofill.AddressAutofill
import com.mapbox.search.autofill.AddressAutofillSuggestion
import com.mapbox.search.autofill.Query
import com.mapbox.search.ui.adapter.autofill.AddressAutofillUiAdapter
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView

class AddressAutofillUiActivity : AppCompatActivity() {

    private lateinit var searchResultsView: SearchResultsView
    private lateinit var searchEngineUiAdapter: AddressAutofillUiAdapter

    private lateinit var queryEditText: EditText

    private lateinit var apartmentEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var stateEditText: EditText
    private lateinit var zipEditText: EditText
    private lateinit var fullAddress: TextView
    private lateinit var mapView: MapView

    private var ignoreNextQueryTextUpdate: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_autofill)

        queryEditText = findViewById(R.id.query_text)
        apartmentEditText = findViewById(R.id.address_apartment)
        cityEditText = findViewById(R.id.address_city)
        stateEditText = findViewById(R.id.address_state)
        zipEditText = findViewById(R.id.address_zip)
        fullAddress = findViewById(R.id.full_address)

        mapView = findViewById(R.id.map)
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)

        searchResultsView = findViewById(R.id.search_results_view)

        searchResultsView.initialize(
            SearchResultsView.Configuration(
                commonConfiguration = CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL)
            )
        )

        searchEngineUiAdapter = AddressAutofillUiAdapter(
            view = searchResultsView,
            addressAutofill = AddressAutofill.create(BuildConfig.MAPBOX_API_TOKEN)
        )

        LocationEngineProvider.getBestLocationEngine(applicationContext).lastKnownLocationOrNull(this) { point ->
            point?.let {
                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(point)
                        .zoom(9.0)
                        .build()
                )
            }
        }

        searchEngineUiAdapter.addSearchListener(object : AddressAutofillUiAdapter.SearchListener {

            override fun onSuggestionSelected(suggestion: AddressAutofillSuggestion) {
                val address = suggestion.result().address
                cityEditText.setText(address.place)
                stateEditText.setText(address.region)
                zipEditText.setText(address.postcode)

                fullAddress.isVisible = true
                fullAddress.text = suggestion.formattedAddress

                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(suggestion.coordinate)
                        .zoom(16.0)
                        .build()
                )
                addAnnotationToMap(suggestion.coordinate)

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
                        searchEngineUiAdapter.search(query)
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

    private fun addAnnotationToMap(coordinate: Point) {
        convertDrawableToBitmap(this, R.drawable.red_marker)?.let {
            val annotationApi = mapView.annotations
            val pointAnnotationManager = annotationApi.createPointAnnotationManager()
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(coordinate)
                .withIconImage(it)
                .withIconAnchor(IconAnchor.BOTTOM)

            pointAnnotationManager.create(pointAnnotationOptions)
        }
    }

    private companion object {

        const val PERMISSIONS_REQUEST_LOCATION = 0

        fun Context.isPermissionGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        val Context.inputMethodManager: InputMethodManager
            get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        fun View.hideKeyboard() {
            context.inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
        }

        fun convertDrawableToBitmap(context: Context, @DrawableRes resourceId: Int): Bitmap? {
            return AppCompatResources.getDrawable(context, resourceId)?.let {
                convertDrawableToBitmap(it)
            }
        }

        fun convertDrawableToBitmap(sourceDrawable: Drawable): Bitmap? {
            return if (sourceDrawable is BitmapDrawable) {
                sourceDrawable.bitmap
            } else {
                val constantState = sourceDrawable.constantState ?: return null
                val drawable = constantState.newDrawable().mutate()
                val bitmap: Bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )

                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        }

        @SuppressLint("MissingPermission")
        fun LocationEngine.lastKnownLocationOrNull(context: Context, callback: (Point?) -> Unit) {
            if (!PermissionsManager.areLocationPermissionsGranted(context)) {
                callback(null)
            }

            val locationCallback = object : LocationEngineCallback<LocationEngineResult> {
                override fun onSuccess(result: LocationEngineResult?) {
                    val location = (result?.locations?.lastOrNull() ?: result?.lastLocation)?.let { location ->
                        Point.fromLngLat(location.longitude, location.latitude)
                    }
                    callback(location)
                }

                override fun onFailure(exception: Exception) {
                    callback(null)
                }
            }
            getLastLocation(locationCallback)
        }
    }
}
