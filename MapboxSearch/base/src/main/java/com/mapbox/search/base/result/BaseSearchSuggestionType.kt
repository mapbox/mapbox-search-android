package com.mapbox.search.base.result

import android.os.Parcelable
import com.mapbox.search.base.assertDebug
import com.mapbox.search.base.record.BaseIndexableRecord
import kotlinx.parcelize.Parcelize

sealed class BaseSearchSuggestionType : Parcelable {

    @Parcelize
    data class SearchResultSuggestion internal constructor(val types: List<BaseSearchResultType>) : BaseSearchSuggestionType() {

        init {
            assertDebug(types.isNotEmpty()) { "Provided types should not be empty!" }
        }

        constructor(vararg types: BaseSearchResultType) : this(types.asList())
    }

    @Parcelize
    data class Category internal constructor(val canonicalName: String) : BaseSearchSuggestionType()

    @Parcelize
    data class Brand(val brandName: String, val brandId: String) : BaseSearchSuggestionType()

    @Parcelize
    object Query : BaseSearchSuggestionType()

    @Parcelize
    data class IndexableRecordItem internal constructor(
        val record: BaseIndexableRecord,
        val dataProviderName: String,
    ) : BaseSearchSuggestionType()
}
