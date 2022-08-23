package com.mapbox.search.base.result

import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.record.BaseIndexableRecord
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseIndexableRecordSearchSuggestion(
    val record: BaseIndexableRecord,
    override val rawSearchResult: BaseRawSearchResult,
    override val requestOptions: BaseRequestOptions
) : BaseSearchSuggestion(rawSearchResult) {

    init {
        check(rawSearchResult.type == BaseRawResultType.USER_RECORD)
        checkNotNull(rawSearchResult.layerId)
    }

    override val id: String
        get() = rawSearchResult.userRecordId ?: rawSearchResult.id

    override val name: String
        get() = record.name

    override val address: BaseSearchAddress?
        get() = record.address

    override val type: BaseSearchSuggestionType.IndexableRecordItem
        get() = BaseSearchSuggestionType.IndexableRecordItem(rawSearchResult.layerId!!, record.type)

    override val descriptionText: String?
        get() = record.descriptionText

    override val isBatchResolveSupported: Boolean
        get() = true

    override val makiIcon: String?
        get() = record.makiIcon
}
