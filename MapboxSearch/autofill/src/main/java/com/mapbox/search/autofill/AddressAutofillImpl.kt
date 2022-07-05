package com.mapbox.search.autofill

import com.mapbox.geojson.Point
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchOptions
import com.mapbox.search.SelectOptions
import com.mapbox.search.autofill.ktx.SearchMultipleSelectionResponse
import com.mapbox.search.autofill.ktx.SearchResultsResponse
import com.mapbox.search.autofill.ktx.SearchSelectionResponse
import com.mapbox.search.autofill.ktx.SearchSuggestionsResponse
import com.mapbox.search.autofill.ktx.search
import com.mapbox.search.autofill.ktx.select
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Temporary implementation of the [AddressAutofill] based on the two-step search.
 */
internal class AddressAutofillImpl(private val searchEngine: SearchEngine) : AddressAutofill {

    override suspend fun suggestions(point: Point, options: AddressAutofillOptions): AddressAutofillResponse {
        val response = searchEngine.search(
            options = ReverseGeoOptions(
                center = point,
                countries = options.countries?.map { it.toCoreSdkType() },
                languages = options.language?.let { listOf(it.toCoreSdkType()) },
            )
        )

        return when (response) {
            is SearchResultsResponse.Results -> AddressAutofillResponse.Suggestions(
                response.results.toAddressAutofillSuggestions()
            )
            is SearchResultsResponse.Error -> AddressAutofillResponse.Error(response.e)
        }
    }

    override suspend fun suggestions(query: String, options: AddressAutofillOptions): AddressAutofillResponse {
        val response = searchEngine.search(
            query = query,
            options = SearchOptions(
                countries = options.countries?.map { it.toCoreSdkType() },
                languages = options.language?.let { listOf(it.toCoreSdkType()) },
                ignoreIndexableRecords = true,
            )
        )

        return when (response) {
            is SearchSuggestionsResponse.Suggestions -> forwardGeocoding(response.suggestions)
            is SearchSuggestionsResponse.Error -> AddressAutofillResponse.Error(response.e)
        }
    }

    private suspend fun forwardGeocoding(suggestions: List<SearchSuggestion>): AddressAutofillResponse {
        return when {
            suggestions.isEmpty() -> {
                AddressAutofillResponse.Suggestions(emptyList())
            }
            suggestions.all { it.isBatchResolveSupported } -> {
                when (val selectionResponse = searchEngine.select(suggestions)) {
                    is SearchMultipleSelectionResponse.Results -> AddressAutofillResponse.Suggestions(
                        selectionResponse.results.toAddressAutofillSuggestions()
                    )
                    is SearchMultipleSelectionResponse.Error -> AddressAutofillResponse.Error(
                        selectionResponse.e
                    )
                }
            }
            else -> {
                coroutineScope {
                    val deferred: List<Deferred<SearchSelectionResponse>> = suggestions
                        // Filtering in order to avoid infinite recursion
                        // because of some specific suggestions like "Did you mean recursion?"
                        .filter { it.type !is SearchSuggestionType.Query }
                        .map { suggestion ->
                            async {
                                searchEngine.select(suggestion, SelectOptions(addResultToHistory = false))
                            }
                        }

                    val selectionResponses = deferred.map { it.await() }

                    val responses: List<AddressAutofillResponse> = selectionResponses
                        .map { selectionResponse ->
                            when (selectionResponse) {
                                is SearchSelectionResponse.Suggestions -> {
                                    forwardGeocoding(selectionResponse.suggestions)
                                }
                                is SearchSelectionResponse.Result -> {
                                    val autofillSuggestion = selectionResponse.result.toAddressAutofillSuggestion()
                                    if (autofillSuggestion != null) {
                                        AddressAutofillResponse.Suggestions(listOf(autofillSuggestion))
                                    } else {
                                        AddressAutofillResponse.Suggestions(emptyList())
                                    }
                                }
                                is SearchSelectionResponse.CategoryResult -> {
                                    AddressAutofillResponse.Suggestions(selectionResponse.results.toAddressAutofillSuggestions())
                                }
                                is SearchSelectionResponse.Error -> {
                                    AddressAutofillResponse.Error(selectionResponse.e)
                                }
                            }
                        }

                    // If at least one response completed successfully, return it.
                    if (responses.isNotEmpty() && responses.all { it is AddressAutofillResponse.Error }) {
                        responses.first()
                    } else {
                        responses.asSequence()
                            .mapNotNull { it as? AddressAutofillResponse.Suggestions }
                            .map { it.suggestions }
                            .flatten()
                            .toList()
                            .let {
                                AddressAutofillResponse.Suggestions(it)
                            }
                    }
                }
            }
        }
    }

    private companion object {

        fun List<SearchResult>.toAddressAutofillSuggestions() = mapNotNull { it.toAddressAutofillSuggestion() }

        fun SearchResult.toAddressAutofillSuggestion(): AddressAutofillSuggestion? {
            // Filtering incomplete results
            val autofillAddress = AddressComponents.fromCoreSdkAddress(address) ?: return null
            val formattedAddress = descriptionText ?: autofillAddress.formattedAddress()
            val validCoordinate = coordinate ?: return null

            return AddressAutofillSuggestion(
                formattedAddress = formattedAddress,
                address = autofillAddress,
                coordinate = validCoordinate,
            )
        }
    }
}
