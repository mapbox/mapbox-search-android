package com.mapbox.search.sample.api

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mapbox.search.autofill.AddressAutofill
import com.mapbox.search.autofill.AddressAutofillOptions
import com.mapbox.search.autofill.AddressAutofillResponse
import com.mapbox.search.autofill.Query
import com.mapbox.search.sample.R

class AddressAutofillKotlinExampleActivity : AppCompatActivity() {

    private lateinit var addressAutofill: AddressAutofill

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addressAutofill = AddressAutofill.create(
            accessToken = getString(R.string.mapbox_access_token),
        )

        lifecycleScope.launchWhenCreated {
            val query = Query.create("740 15th St NW, Washington") ?: return@launchWhenCreated

            val response = addressAutofill.suggestions(
                query = query,
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
