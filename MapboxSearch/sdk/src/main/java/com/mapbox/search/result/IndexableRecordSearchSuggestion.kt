package com.mapbox.search.result

import com.mapbox.search.RequestOptions
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.record.IndexableRecord
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class IndexableRecordSearchSuggestion(
    val record: IndexableRecord,
    override val rawSearchResult: BaseRawSearchResult,
    override val requestOptions: RequestOptions
) : AbstractSearchSuggestion(rawSearchResult) {

    init {
        check(rawSearchResult.type == BaseRawResultType.USER_RECORD)
        checkNotNull(rawSearchResult.layerId)
    }

    override val id: String
        get() = rawSearchResult.userRecordId ?: rawSearchResult.id

    override val name: String
        get() = record.name

    override val address: SearchAddress?
        get() = record.address

    override val type: SearchSuggestionType.IndexableRecordItem
        get() = SearchSuggestionType.IndexableRecordItem(rawSearchResult.layerId!!, record.type)

    override val descriptionText: String?
        get() = record.descriptionText

    override val isBatchResolveSupported: Boolean
        get() = true

    override val makiIcon: String?
        get() = record.makiIcon

    override fun toString(): String {
        return "SearchSuggestion(${baseToString()}, record='$record')"
    }
}
