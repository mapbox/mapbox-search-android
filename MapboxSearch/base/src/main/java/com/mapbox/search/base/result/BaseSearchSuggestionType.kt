package com.mapbox.search.base.result

import android.os.Parcelable
import com.mapbox.search.base.assertDebug
import kotlinx.parcelize.Parcelize

sealed class BaseSearchSuggestionType : Parcelable {

    @Parcelize
    data class SearchResultSuggestion internal constructor(val types: List<BaseSearchResultType>) : BaseSearchSuggestionType() {

        constructor(vararg types: BaseSearchResultType) : this(types.asList())

        init {
            assertDebug(types.isNotEmpty()) { "Provided types should not be empty!" }
        }
    }

    @Parcelize
    data class Category internal constructor(val canonicalName: String) : BaseSearchSuggestionType()

    @Parcelize
    data class Brand(val brandName: String, val brandId: String) : BaseSearchSuggestionType()

    @Parcelize
    object Query : BaseSearchSuggestionType()

    @Parcelize
    data class IndexableRecordItem internal constructor(
        val dataProviderName: String,
        val type: BaseSearchResultType,
    ) : BaseSearchSuggestionType()
}
