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

            val response = addressAutofill.suggestions(
                query = query,
                options = AddressAutofillOptions()
            )

            if (response.isValue) {
                val suggestions = requireNotNull(response.value)
                Log.i("SearchApiExample", "Autofill suggestions: $suggestions")

                if (suggestions.isNotEmpty()) {
                    // Supposing that a user has selected (clicked in UI) the first suggestion
                    val selectedSuggestion = suggestions.first()

                    Log.i("SearchApiExample", "Selecting first suggestion...")

                    val selectionResponse = addressAutofill.select(selectedSuggestion)
                    selectionResponse.onValue { result ->
                        Log.i("SearchApiExample", "Autofill result: $result")
                    }.onError { e ->
                        Log.i("SearchApiExample", "An error occurred during selection", e)
                    }
                }
            } else {
                Log.i("SearchApiExample", "Autofill suggestions error", response.error)
            }
        }
    }
}
