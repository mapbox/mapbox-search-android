package com.mapbox.search.ui.utils

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.IndexableRecordSearchResult
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.SearchSuggestionType
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.maki.MakiToDrawableIdMapper
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.place.SearchPlace
import java.util.Locale

internal object SearchEntityPresentation {

    @DrawableRes
    fun pickEntityDrawable(makiIcon: String?, categories: List<String>?, @DrawableRes fallback: Int): Int {
        return MakiToDrawableIdMapper.getDrawableIdByMaki(makiIcon) ?: categories?.asSequence()
                ?.map { Category.findByCanonicalName(it) }
                ?.firstOrNull { it != null }?.presentation?.icon ?: fallback
    }

    fun getAddressOrResultType(context: Context, searchSuggestion: SearchSuggestion): String? {
        val descriptionText = searchSuggestion.descriptionText
        val addressText = searchSuggestion.address?.formattedAddress()
        return when {
            !descriptionText.isNullOrBlank() -> descriptionText
            !addressText.isNullOrBlank() -> addressText
            else -> getSearchSuggestionTypeName(context, searchSuggestion.type)
        }
    }

    fun getAddressOrResultType(context: Context, searchResult: SearchResult): String {
        val descriptionText = searchResult.descriptionText
        val addressText = searchResult.address?.formattedAddress()
        return when {
            !descriptionText.isNullOrBlank() -> descriptionText
            !addressText.isNullOrBlank() -> addressText
            searchResult is IndexableRecordSearchResult -> getResultTypeName(context, searchResult.types, searchResult.record)
            else -> getResultTypeName(context, searchResult.types)
        }
    }

    fun getAddressOrResultType(context: Context, searchPlace: SearchPlace): String {
        val descriptionText = searchPlace.descriptionText
        val addressText = searchPlace.address?.formattedAddress()
        return when {
            !descriptionText.isNullOrBlank() -> descriptionText
            !addressText.isNullOrBlank() -> addressText
            else -> getResultTypeName(context, searchPlace.resultTypes, searchPlace.record)
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

    private fun getResultTypeName(
        context: Context,
        resultTypes: List<SearchResultType>,
        record: IndexableRecord?
    ): String {
        return when (record) {
            is FavoriteRecord -> context.getString(R.string.mapbox_search_sdk_search_result_type_favorite)
            is HistoryRecord -> context.getString(R.string.mapbox_search_sdk_search_result_type_history)
            else -> getResultTypeName(context, resultTypes)
        }
    }

    private fun getSearchSuggestionTypeName(context: Context, suggestionType: SearchSuggestionType): String? {
        return when (suggestionType) {
            is SearchSuggestionType.IndexableRecordItem -> context.getString(getRecordTypeName(suggestionType))
            is SearchSuggestionType.SearchResultSuggestion -> getResultTypeName(context, suggestionType.types)
            is SearchSuggestionType.Category, is SearchSuggestionType.Query -> null
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

    private fun getResultTypeName(context: Context, resultTypes: List<SearchResultType>): String {
        return resultTypes.joinToString { type -> context.getString(getResultTypeName(type)) }
    }

    @StringRes
    private fun getResultTypeName(resultType: SearchResultType): Int {
        return when (resultType) {
            SearchResultType.ADDRESS -> R.string.mapbox_search_sdk_search_result_type_address
            SearchResultType.POI -> R.string.mapbox_search_sdk_search_result_type_poi
            SearchResultType.COUNTRY -> R.string.mapbox_search_sdk_search_result_type_country
            SearchResultType.REGION -> R.string.mapbox_search_sdk_search_result_type_region
            SearchResultType.PLACE -> R.string.mapbox_search_sdk_search_result_type_place
            SearchResultType.DISTRICT -> R.string.mapbox_search_sdk_search_result_type_district
            SearchResultType.LOCALITY -> R.string.mapbox_search_sdk_search_result_type_locality
            SearchResultType.NEIGHBORHOOD -> R.string.mapbox_search_sdk_search_result_type_neighborhood
            SearchResultType.STREET -> R.string.mapbox_search_sdk_search_result_type_street
            SearchResultType.POSTCODE -> R.string.mapbox_search_sdk_search_result_type_postcode
        }
    }
}
