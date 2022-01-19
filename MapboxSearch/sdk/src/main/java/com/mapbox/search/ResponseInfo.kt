package com.mapbox.search

import android.os.Parcelable
import com.mapbox.search.core.CoreSearchResponse
import kotlinx.parcelize.Parcelize

/**
 * Information about search response and associated search request.
 *
 * @property requestOptions options for original search request.
 *
 * @property coreSearchResponse native core response for the given [requestOptions].
 *
 * Please note, [coreSearchResponse] will equal `null` for cases, when we don't use native core to execute request,
 * therefore we don't have [CoreSearchResponse].
 *
 * Also, [coreSearchResponse] will equal `null` in the following cases:
 * - User selects [com.mapbox.search.result.SearchSuggestionType.SearchResultSuggestion] search suggestion that represents V5 search result;
 * - User selects [com.mapbox.search.result.SearchSuggestionType.IndexableRecordItem] search suggestion;
 * - Batch retrieve of several search suggestions that represent V5 search result
 *      or when all the suggestions is not resolvable in batch request (SearchSuggestion.isBatchResolveSupported == false);
 *
 * @property isReproducible true, if [coreSearchResponse] is not associated with provided [requestOptions],
 * meaning that [RequestOptions] will not contain all parameters, with which [CoreSearchResponse] may be reproduced.
 *
 * @see SearchSuggestionsCallback
 * @see SearchSelectionCallback
 * @see SearchMultipleSelectionCallback
 */
@Parcelize
public class ResponseInfo internal constructor(
    public val requestOptions: RequestOptions,
    @get:JvmSynthetic
    internal val coreSearchResponse: CoreSearchResponse?,
    @get:JvmSynthetic
    internal val isReproducible: Boolean,
) : Parcelable {

    /**
     * Service response identifier.
     */
    public val responseUuid: String?
        get() = requestOptions.requestContext.responseUuid

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResponseInfo

        if (requestOptions != other.requestOptions) return false
        if (coreSearchResponse != other.coreSearchResponse) return false
        if (isReproducible != other.isReproducible) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = requestOptions.hashCode()
        result = 31 * result + (coreSearchResponse?.hashCode() ?: 0)
        result = 31 * result + isReproducible.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "ResponseInfo(" +
                "requestOptions=$requestOptions, " +
                "responseUuid=$responseUuid, " +
                "coreSearchResponse=$coreSearchResponse, " +
                "isReproducible=$isReproducible" +
                ")"
    }
}
