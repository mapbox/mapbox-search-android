package com.mapbox.search

import android.os.Parcelable
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.core.CoreSearchResponse
import com.mapbox.search.base.result.BaseSearchResponse
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
 *
 * @property isReproducible true, if [coreSearchResponse] is not associated with provided [requestOptions],
 * meaning that [RequestOptions] will not contain all parameters, with which [CoreSearchResponse] may be reproduced.
 *
 * @see SearchSuggestionsCallback
 * @see SearchSelectionCallback
 */
@Parcelize
public class ResponseInfo internal constructor(
    public val requestOptions: RequestOptions,
    @get:JvmSynthetic
    internal val coreSearchResponse: BaseSearchResponse?,
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

@JvmSynthetic
internal fun BaseResponseInfo.mapToPlatform(): ResponseInfo {
    return ResponseInfo(
        requestOptions = requestOptions.mapToPlatform(),
        coreSearchResponse = coreSearchResponse,
        isReproducible = isReproducible,
    )
}
