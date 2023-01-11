package com.mapbox.search.autofill

import com.mapbox.geojson.Point
import com.mapbox.search.base.core.createCoreReverseGeoOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.result.BaseSearchSuggestionType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Temporary implementation of the [AddressAutofill] based on the two-step search.
 */
internal class AddressAutofillImpl(private val searchEngine: AutofillSearchEngine) : AddressAutofill {

    override suspend fun suggestions(point: Point, options: AddressAutofillOptions): AddressAutofillResponse {
        val coreOptions = createCoreReverseGeoOptions(
            point = point,
            countries = options.countries?.map { it.code },
            language = options.language?.let { listOf(it.code) },
        )

        return when (val response = searchEngine.search(coreOptions)) {
            is SearchResultsResponse.Results -> AddressAutofillResponse.Suggestions(
                response.results.toAddressAutofillSuggestions()
            )
            is SearchResultsResponse.Error -> AddressAutofillResponse.Error(response.e)
        }
    }

    override suspend fun suggestions(query: Query, options: AddressAutofillOptions): AddressAutofillResponse {
        val response = searchEngine.search(
            query = query.query,
            options = createCoreSearchOptions(
                countries = options.countries?.map { it.code },
                language = options.language?.let { listOf(it.code) },
                limit = 10,
                ignoreUR = true,
            )
        )

        return when (response) {
            is SearchSuggestionsResponse.Suggestions -> forwardGeocoding(response.suggestions)
            is SearchSuggestionsResponse.Error -> AddressAutofillResponse.Error(response.e)
        }
    }

    private suspend fun forwardGeocoding(suggestions: List<BaseSearchSuggestion>): AddressAutofillResponse {
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
                        .filter { it.type !is BaseSearchSuggestionType.Query }
                        .map { suggestion ->
                            async {
                                searchEngine.select(suggestion)
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

        fun List<BaseSearchResult>.toAddressAutofillSuggestions() = mapNotNull { it.toAddressAutofillSuggestion() }

        fun BaseSearchResult.toAddressAutofillSuggestion(): AddressAutofillSuggestion? {
            // Filtering incomplete results
            val autofillAddress = AddressComponents.fromCoreSdkAddress(address) ?: return null
            val validCoordinate = coordinate

            return AddressAutofillSuggestion(
                name = name,
                formattedAddress = autofillAddress.formattedAddress(),
                address = autofillAddress,
                coordinate = validCoordinate,
            )
        }
    }
}
