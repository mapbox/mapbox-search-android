package com.mapbox.search.sample.api

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mapbox.search.autofill.AddressAutofill
import com.mapbox.search.autofill.AddressAutofillOptions
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

            addressAutofill.suggestions(
                query = query,
                options = AddressAutofillOptions()
            ).onValue { suggestions ->
                Log.i("SearchApiExample", "Autofill suggestions: $suggestions")
            }.onError { error ->
                Log.i("SearchApiExample", "Autofill suggestions error", error)
            }
        }
    }
}
