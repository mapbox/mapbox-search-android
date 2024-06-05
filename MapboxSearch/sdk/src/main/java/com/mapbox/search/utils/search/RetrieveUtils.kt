package com.mapbox.search.utils.search

import com.mapbox.search.ApiType
import com.mapbox.search.SearchOptions
import com.mapbox.search.internal.bindgen.RequestOptions
import com.mapbox.search.internal.bindgen.ResultType
import com.mapbox.search.internal.bindgen.SearchResult
import com.mapbox.search.internal.bindgen.SuggestAction
import com.mapbox.search.mapToCore

internal object RetrieveUtils {
    private const val NO_SESSION_IDENTIFIER = "<NO SESSION IDENTIFIER>"
    private val EMPTY_SEARCH_OPTIONS = SearchOptions.Builder().build().mapToCore()

    /**
     * Empty request options
     */
    val EMPTY_REQUEST_OPTIONS = RequestOptions(
        "",
        "",
        EMPTY_SEARCH_OPTIONS,
        false,
        false,
        NO_SESSION_IDENTIFIER,
    )

    /**
     * Creates a [SearchResult] to perform a retrieve my Mapbox ID.
     *  @param apiType [ApiType] that this request is for
     *  @param mapboxId the ID of the POI to retrieve
     *  @return [SearchResult] that with a [SuggestAction] for retrieve
     *  @throws [UnsupportedOperationException] when the [ApiType] is anything but [ApiType.SBS] or [ApiType.SEARCH_BOX]
     */
    fun createSearchResultForRetrieve(apiType: ApiType, mapboxId: String): SearchResult {
        val suggestAction = when (apiType) {
            ApiType.SBS, ApiType.SEARCH_BOX -> {
                SuggestAction(
                    "retrieve",
                    "",
                    null,
                    """{"id":"$mapboxId"}""".toByteArray(),
                    false
                )
            }
            else -> {
                throw UnsupportedOperationException("Retrieve is not supported for $apiType API type")
            }
        }

        // the only property that matters here is the suggest action
        return SearchResult(
            "",
            "",
            listOf(ResultType.POI),
            listOf(),
            listOf(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            0,
            suggestAction,
            null,
        )
    }
}
