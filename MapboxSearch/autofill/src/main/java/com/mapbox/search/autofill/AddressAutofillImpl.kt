package com.mapbox.search.autofill

import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory.createError
import com.mapbox.bindgen.ExpectedFactory.createValue
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.core.createCoreReverseGeoOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.result.BaseSearchSuggestionType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal sealed class SearchSelectionResponse {

    data class Suggestions(
        val suggestions: List<BaseSearchSuggestion>,
        val responseInfo: BaseResponseInfo,
    ) : SearchSelectionResponse()

    data class Result(
        val suggestion: BaseSearchSuggestion,
        val result: BaseSearchResult,
        val responseInfo: BaseResponseInfo,
    ) : SearchSelectionResponse()

    data class CategoryResult(
        val suggestion: BaseSearchSuggestion,
        val results: List<BaseSearchResult>,
        val responseInfo: BaseResponseInfo,
    ) : SearchSelectionResponse()
}

/**
 * Temporary implementation of the [AddressAutofill] based on the two-step search.
 */
internal class AddressAutofillImpl(private val searchEngine: AutofillSearchEngine) : AddressAutofill {

    override suspend fun suggestions(point: Point, options: AddressAutofillOptions): Expected<Exception, List<AddressAutofillSuggestion>> {
        val coreOptions = createCoreReverseGeoOptions(
            point = point,
            countries = options.countries?.map { it.code },
            language = options.language?.let { listOf(it.code) },
        )

        return searchEngine.search(coreOptions).mapValue { (results, _) ->
            results.toAddressAutofillSuggestions()
        }
    }

    override suspend fun suggestions(query: Query, options: AddressAutofillOptions): Expected<Exception, List<AddressAutofillSuggestion>> {
        val response = searchEngine.search(
            query = query.query,
            options = createCoreSearchOptions(
                countries = options.countries?.map { it.code },
                language = options.language?.let { listOf(it.code) },
                limit = 10,
                ignoreUR = true,
            )
        )

        return if (response.isValue) {
            forwardGeocoding(requireNotNull(response.value).first)
        } else {
            createError(requireNotNull(response.error))
        }
    }

    private suspend fun forwardGeocoding(suggestions: List<BaseSearchSuggestion>): Expected<Exception, List<AddressAutofillSuggestion>> {
        return when {
            suggestions.isEmpty() -> {
                createValue(emptyList())
            }
            suggestions.all { it.isBatchResolveSupported } -> {
                searchEngine.select(suggestions).mapValue { (_, results, _) ->
                    results.toAddressAutofillSuggestions()
                }
            }
            else -> {
                coroutineScope {
                    val deferred: List<Deferred<Expected<Exception, SearchSelectionResponse>>> = suggestions
                        // Filtering in order to avoid infinite recursion
                        // because of some specific suggestions like "Did you mean recursion?"
                        .filter { it.type !is BaseSearchSuggestionType.Query }
                        .map { suggestion ->
                            async {
                                searchEngine.select(suggestion)
                            }
                        }

                    val selectionResponses = deferred.map { it.await() }

                    val responses: List<Expected<Exception, List<AddressAutofillSuggestion>>> = selectionResponses
                        .map { result ->
                            if (result.isValue) {
                                when (val response = requireNotNull(result.value)) {
                                    is SearchSelectionResponse.Suggestions -> {
                                        forwardGeocoding(response.suggestions)
                                    }
                                    is SearchSelectionResponse.Result -> {
                                        val autofillSuggestion = response.result.toAddressAutofillSuggestion()
                                        if (autofillSuggestion != null) {
                                            createValue(listOf(autofillSuggestion))
                                        } else {
                                            createValue(emptyList())
                                        }
                                    }
                                    is SearchSelectionResponse.CategoryResult -> {
                                        createValue(response.results.toAddressAutofillSuggestions())
                                    }
                                }
                            } else {
                                createError(requireNotNull(result.error))
                            }
                        }

                    // If at least one response completed successfully, return it.
                    if (responses.isNotEmpty() && responses.all { it.isError }) {
                        responses.first()
                    } else {
                        responses.asSequence()
                            .mapNotNull { it.value }
                            .flatten()
                            .toList()
                            .let {
                                createValue(it)
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
            val autofillAddress = AddressComponents.fromCoreSdkAddress(address, metadata) ?: return null
            val validCoordinate = coordinate

            return AddressAutofillSuggestion(
                name = name,
                formattedAddress = fullAddress ?: autofillAddress.formattedAddress(),
                address = autofillAddress,
                coordinate = validCoordinate,
            )
        }
    }
}
