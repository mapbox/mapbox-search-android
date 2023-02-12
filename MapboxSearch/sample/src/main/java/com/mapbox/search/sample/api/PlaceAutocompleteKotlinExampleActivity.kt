package com.mapbox.search.sample.api

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.TextQuery
import com.mapbox.search.sample.R

class PlaceAutocompleteKotlinExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val placeAutocomplete = PlaceAutocomplete.create(
            accessToken = getString(R.string.mapbox_access_token),
        )

        lifecycleScope.launchWhenCreated {
            val response = placeAutocomplete.suggestions(
                query = TextQuery.create("Washington DC"),
            )

            response.onValue { results ->
                Log.i("SearchApiExample", "Place Autocomplete results: $results")
            }.onError { e ->
                Log.i("SearchApiExample", "Place Autocomplete error", e)
            }
        }
    }
}
