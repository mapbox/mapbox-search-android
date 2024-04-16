package com.mapbox.search.sample.api

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.sample.R

class PlaceAutocompleteKotlinExampleActivity : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_place_autocomplete_kotlin_example

    override fun startExample() {
        // Set your Access Token here if it's not already set in some other way
        // MapboxOptions.accessToken = "<my-access-token>"
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

                    logI("SearchApiExample", "Selecting first suggestion...")

                    val selectionResponse = placeAutocomplete.select(selectedSuggestion)
                    selectionResponse.onValue { result ->
                        logI("SearchApiExample", "Place Autocomplete result", result)
                    }.onError { e ->
                        logE("SearchApiExample", "An error occurred during selection", e)
                    }
                }
            } else {
                logE("SearchApiExample", "Place Autocomplete error", response.error)
            }
            onFinished()
        }
    }
}
