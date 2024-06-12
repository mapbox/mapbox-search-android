package com.mapbox.search.sample.api

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.mapbox.search.autofill.AddressAutofill
import com.mapbox.search.autofill.AddressAutofillOptions
import com.mapbox.search.autofill.Query
import com.mapbox.search.sample.R

class AddressAutofillKotlinExampleActivity : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_address_autofill_example

    private lateinit var addressAutofill: AddressAutofill

    override fun startExample() {
        // Set your Access Token here if it's not already set in some other way
        // MapboxOptions.accessToken = "<my-access-token>"
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

                    logI("SearchApiExample", "Selecting first suggestion...")

                    val selectionResponse = addressAutofill.select(selectedSuggestion)
                    selectionResponse.onValue { result ->
                        logI("SearchApiExample", "Autofill result", result)
                    }.onError { e ->
                        logE("SearchApiExample", "An error occurred during selection", e)
                    }
                }
            } else {
                logE("SearchApiExample", "Autofill suggestions error", response.error)
            }
            onFinished()
        }
    }
}
