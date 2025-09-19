package com.mapbox.search.ui.adapter.engines

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.mapbox.search.base.utils.extension.nullIfEmpty
import com.mapbox.search.common.HighlightsCalculator
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.NewSearchResultType
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.utils.maki.MakiToDrawableIdMapper
import com.mapbox.search.ui.utils.offline.createNewSearchResultTypeFromOfflineType
import com.mapbox.search.ui.utils.offline.mapToSdkSearchResultType
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.place.SearchPlace
import java.util.Locale

internal class SearchEntityPresentation(
    private val context: Context,
    @ColorInt
    private val selectionSpanColor: Int = context.resolveAttrOrThrow(R.attr.mapboxSearchSdkPrimaryAccentColor),
    private val highlightsCalculator: HighlightsCalculator = HighlightsCalculator.INSTANCE
) {

    fun getTitle(suggestion: SearchSuggestion): CharSequence {
        return formattedResultName(suggestion.name, suggestion.requestOptions.query)
    }

    fun getTitle(searchResult: OfflineSearchResult, query: String): CharSequence {
        return formattedResultName(searchResult.name, query)
    }

    private fun formattedResultName(name: String, query: String): CharSequence {
        val highlights = highlightsCalculator.highlights(name, query)

        return SpannableString(name).apply {
            highlights.forEach { (start, end) ->
                // Same span object can't be reused for multiple usage,
                // that's why we create a new ForegroundColorSpan on every iteration
                setSpan(ForegroundColorSpan(selectionSpanColor), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
    }

    fun getDescription(suggestion: SearchSuggestion): CharSequence? {
        return if (suggestion.type is SearchSuggestionType.Category) {
            // By design we don't have description text for category
            null
        } else {
            val addressText = suggestion.fullAddress ?: suggestion.address?.formattedAddress()
            when {
                !suggestion.descriptionText.isNullOrBlank() -> suggestion.descriptionText
                !addressText.isNullOrBlank() -> addressText
                else -> getSearchSuggestionTypeName(context, suggestion.type)
            }
        }
    }

    fun getDescription(searchResult: SearchResult): String? {
        val descriptionText = searchResult.descriptionText
        val addressText = searchResult.fullAddress ?: searchResult.address?.formattedAddress()
        return when {
            !descriptionText.isNullOrBlank() -> descriptionText
            !addressText.isNullOrBlank() -> addressText
            searchResult.indexableRecord != null -> getResultTypeName(searchResult.newTypes, searchResult.indexableRecord)
            else -> getResultTypeName(searchResult.newTypes)
        }
    }

    fun getDescription(searchResult: OfflineSearchResult): String? {
        val descriptionText = searchResult.descriptionText
        val addressText = searchResult.address?.mapToSdkSearchResultType()?.formattedAddress()
        return when {
            !descriptionText.isNullOrBlank() -> descriptionText
            !addressText.isNullOrBlank() -> addressText
            else -> getResultTypeName(listOf(createNewSearchResultTypeFromOfflineType(searchResult.newType)))
        }
    }

    fun getAddressOrResultType(searchPlace: SearchPlace): String? {
        val descriptionText = searchPlace.descriptionText
        val addressText = searchPlace.address?.formattedAddress()
        return when {
            !descriptionText.isNullOrBlank() -> descriptionText
            !addressText.isNullOrBlank() -> addressText
            else -> getResultTypeName(searchPlace.newTypes, searchPlace.record)
        }
    }

    fun getAddressOrResultType(record: IndexableRecord): String? {
        val descriptionText = record.descriptionText
        val addressText = record.address?.formattedAddress()
        return when {
            !descriptionText.isNullOrBlank() -> descriptionText
            !addressText.isNullOrBlank() -> addressText
            else -> getResultTypeName(listOf(record.newType), record)
        }
    }

    fun firstCategoryName(context: Context, categories: List<String>?): String? {
        return categories?.asSequence()
            ?.map { Category.findByCanonicalName(it) }
            ?.firstOrNull { it != null }
            ?.let {
                //noinspection DefaultLocale
                context.getString(it.presentation.displayName).replaceFirstChar { char ->
                    if (char.isLowerCase()) {
                        char.titlecase(Locale.getDefault())
                    } else {
                        char.toString()
                    }
                }
            }
    }

    @DrawableRes
    fun getDrawable(suggestion: SearchSuggestion): Int {
        return when (val type = suggestion.type) {
            is SearchSuggestionType.SearchResultSuggestion -> getDrawableForSearchResult(
                maki = suggestion.makiIcon,
                categories = suggestion.categories,
                types = type.newTypes
            )
            is SearchSuggestionType.IndexableRecordItem -> getDrawableForDataProvider(type)
            is SearchSuggestionType.Query -> pickEntityDrawable(
                makiIcon = suggestion.makiIcon,
                categories = emptyList(),
                fallback = R.drawable.mapbox_search_sdk_ic_mdi_search
            )
            is SearchSuggestionType.Category,
            is SearchSuggestionType.Brand -> R.drawable.mapbox_search_sdk_ic_mdi_search
            else -> error("Unknown SearchSuggestionType type: $type.")
        }
    }

    @DrawableRes
    fun getDrawableForSearchResult(result: SearchResult): Int {
        return getDrawableForSearchResult(result.makiIcon, result.categories, result.newTypes)
    }

    @DrawableRes
    fun getDrawable(suggestion: PlaceAutocompleteSuggestion): Int {
        return pickEntityDrawable(
            suggestion.makiIcon, emptyList(), R.drawable.mapbox_search_sdk_ic_search_result_address
        )
    }

    @DrawableRes
    private fun getDrawableForSearchResult(
        maki: String?,
        categories: List<String>?,
        types: List<String>
    ): Int {
        // We expect to have either combination of ADDRESS, COUNTRY, ..., POSTCODE
        // types or only POI type in list, so only first type is needed.
        return when (types.first()) {
            NewSearchResultType.ADDRESS,
            NewSearchResultType.COUNTRY,
            NewSearchResultType.REGION,
            NewSearchResultType.PLACE,
            NewSearchResultType.DISTRICT,
            NewSearchResultType.LOCALITY,
            NewSearchResultType.NEIGHBORHOOD,
            NewSearchResultType.STREET,
            NewSearchResultType.POSTCODE,
            NewSearchResultType.BLOCK -> R.drawable.mapbox_search_sdk_ic_search_result_address
            NewSearchResultType.POI -> pickEntityDrawable(
                maki, categories, R.drawable.mapbox_search_sdk_ic_mdi_search
            )
            else -> R.drawable.mapbox_search_sdk_ic_mdi_search
        }
    }

    @DrawableRes
    private fun getDrawableForDataProvider(indexableRecordItem: SearchSuggestionType.IndexableRecordItem): Int {
        return when {
            indexableRecordItem.isHistoryRecord -> R.drawable.mapbox_search_sdk_ic_history
            indexableRecordItem.isFavoriteRecord -> R.drawable.mapbox_search_sdk_ic_favorite_uncategorized
            else -> R.drawable.mapbox_search_sdk_ic_search_result_place
        }
    }

    @DrawableRes
    private fun pickEntityDrawable(makiIcon: String?, categories: List<String>?, @DrawableRes fallback: Int): Int {
        return MakiToDrawableIdMapper.getDrawableIdByMaki(makiIcon)
            ?: (categories?.asSequence()
                ?.mapNotNull { Category.findByCanonicalName(it)?.presentation }
                ?.firstOrNull()
                ?.icon ?: fallback)
    }

    private fun getResultTypeName(resultTypes: List<String>, record: IndexableRecord?): String? {
        return when (record) {
            is FavoriteRecord -> context.getString(R.string.mapbox_search_sdk_search_result_type_favorite)
            is HistoryRecord -> context.getString(R.string.mapbox_search_sdk_search_result_type_history)
            else -> getResultTypeName(resultTypes)
        }
    }

    private fun getSearchSuggestionTypeName(context: Context, suggestionType: SearchSuggestionType): String? {
        return when (suggestionType) {
            is SearchSuggestionType.IndexableRecordItem -> context.getString(getRecordTypeName(suggestionType))
            is SearchSuggestionType.SearchResultSuggestion -> getResultTypeName(suggestionType.newTypes)
            is SearchSuggestionType.Category,
            is SearchSuggestionType.Query,
            is SearchSuggestionType.Brand -> null
            else -> error("Unknown SearchSuggestionType type: $suggestionType.")
        }
    }

    @StringRes
    private fun getRecordTypeName(indexableRecordItem: SearchSuggestionType.IndexableRecordItem): Int {
        return when {
            indexableRecordItem.isHistoryRecord -> R.string.mapbox_search_sdk_search_result_type_history
            indexableRecordItem.isFavoriteRecord -> R.string.mapbox_search_sdk_search_result_type_favorite
            else -> R.string.mapbox_search_sdk_search_result_type_place
        }
    }

    private fun getResultTypeName(resultTypes: List<String>): String? {
        return resultTypes
            .map { type -> getResultTypeName(type) }
            .filter { it != 0 }
            .joinToString { typeRes -> context.getString(typeRes) }
            .nullIfEmpty()
    }

    @StringRes
    private fun getResultTypeName(@NewSearchResultType.Type resultType: String): Int {
        return when (resultType) {
            NewSearchResultType.ADDRESS -> R.string.mapbox_search_sdk_search_result_type_address
            NewSearchResultType.POI -> R.string.mapbox_search_sdk_search_result_type_poi
            NewSearchResultType.COUNTRY -> R.string.mapbox_search_sdk_search_result_type_country
            NewSearchResultType.REGION -> R.string.mapbox_search_sdk_search_result_type_region
            NewSearchResultType.PLACE -> R.string.mapbox_search_sdk_search_result_type_place
            NewSearchResultType.DISTRICT -> R.string.mapbox_search_sdk_search_result_type_district
            NewSearchResultType.LOCALITY -> R.string.mapbox_search_sdk_search_result_type_locality
            NewSearchResultType.NEIGHBORHOOD -> R.string.mapbox_search_sdk_search_result_type_neighborhood
            NewSearchResultType.STREET -> R.string.mapbox_search_sdk_search_result_type_street
            NewSearchResultType.POSTCODE -> R.string.mapbox_search_sdk_search_result_type_postcode
            NewSearchResultType.BLOCK -> R.string.mapbox_search_sdk_search_result_type_block
            else -> 0
        }
    }
}
