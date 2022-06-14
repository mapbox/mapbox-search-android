package com.mapbox.search.sample.api

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.autofill.AddressAutofill
import com.mapbox.search.autofill.AddressAutofillOptions
import com.mapbox.search.autofill.AddressAutofillResponse

class AddressAutofillKotlinExampleActivity : AppCompatActivity() {

    private lateinit var addressAutofill: AddressAutofill

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addressAutofill = AddressAutofill.create(MapboxSearchSdk.createSearchEngine())

        lifecycleScope.launchWhenCreated {
            val response = addressAutofill.suggestions(
                query = "street",
                options = AddressAutofillOptions()
            )

            when (response) {
                is AddressAutofillResponse.Suggestions -> {
                    Log.i("SearchApiExample", "Autofill suggestions: ${response.suggestions}")
                }
                is AddressAutofillResponse.Error -> {
                    Log.i("SearchApiExample", "Autofill suggestions error", response.error)
                }
            }
        }
    }
}
