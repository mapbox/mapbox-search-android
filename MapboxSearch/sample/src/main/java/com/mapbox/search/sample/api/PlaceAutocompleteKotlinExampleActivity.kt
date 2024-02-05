package com.mapbox.search.sample.api

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mapbox.search.autocomplete.PlaceAutocomplete

class PlaceAutocompleteKotlinExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val placeAutocomplete = PlaceAutocomplete.create()

        lifecycleScope.launchWhenCreated {
            val response = placeAutocomplete.suggestions(
                query = "Washington DC",
            )

            if (response.isValue) {
                val suggestions = requireNotNull(response.value)

                Log.i("SearchApiExample", "Place Autocomplete suggestions: $suggestions")

                if (suggestions.isNotEmpty()) {
                    // Supposing that a user has selected (clicked in UI) the first suggestion
                    val selectedSuggestion = suggestions.first()

                    Log.i("SearchApiExample", "Selecting first suggestion...")

                    val selectionResponse = placeAutocomplete.select(selectedSuggestion)
                    selectionResponse.onValue { result ->
                        Log.i("SearchApiExample", "Place Autocomplete result: $result")
                    }.onError { e ->
                        Log.i("SearchApiExample", "An error occurred during selection", e)
                    }
                }
            } else {
                Log.i("SearchApiExample", "Place Autocomplete error", response.error)
            }
        }
    }
}
