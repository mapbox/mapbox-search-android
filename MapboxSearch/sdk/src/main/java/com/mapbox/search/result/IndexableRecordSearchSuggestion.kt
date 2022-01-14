package com.mapbox.search.result

import com.mapbox.search.RequestOptions
import com.mapbox.search.record.IndexableRecord
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class IndexableRecordSearchSuggestion(
    val record: IndexableRecord,
    override val originalSearchResult: OriginalSearchResult,
    override val requestOptions: RequestOptions
) : BaseSearchSuggestion(originalSearchResult) {

    init {
        check(originalSearchResult.type == OriginalResultType.USER_RECORD)
        checkNotNull(originalSearchResult.layerId)
    }

    override val id: String
        get() = originalSearchResult.userRecordId ?: originalSearchResult.id

    override val name: String
        get() = record.name

    override val address: SearchAddress?
        get() = record.address

    override val type: SearchSuggestionType.IndexableRecordItem
        get() = SearchSuggestionType.IndexableRecordItem(originalSearchResult.layerId!!, record.type)

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
