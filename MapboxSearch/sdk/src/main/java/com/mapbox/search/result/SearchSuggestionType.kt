package com.mapbox.search.result

import android.os.Parcelable
import com.mapbox.search.base.assertDebug
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.record.IndexableRecord
import kotlinx.parcelize.Parcelize

/**
 * Type of the search suggestion.
 */
public abstract class SearchSuggestionType internal constructor() : Parcelable {

    /**
     * Search suggestion of the [SearchResultSuggestion] type points to the only [SearchResult] that will be returned after selection.
     *
     * @property types - types of the [SearchResult] that will be resolved after selection of the search suggestion.
     */
    @Parcelize
    public class SearchResultSuggestion internal constructor(
        public val types: List<SearchResultType>
    ) : SearchSuggestionType() {

        internal constructor(vararg types: SearchResultType) : this(types.asList())

        init {
            assertDebug(types.isNotEmpty()) { "Provided types should not be empty!" }
        }

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SearchResultSuggestion

            if (types != other.types) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return types.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "SearchResultSuggestion(types=$types)"
        }
    }

    /**
     * Search suggestion of the [Category] type points to the list of [SearchResult] that will be returned after selection.
     *
     * @property canonicalName - the canonical name of the category.
     */
    @Parcelize
    public class Category internal constructor(
        public val canonicalName: String
    ) : SearchSuggestionType() {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Category

            if (canonicalName != other.canonicalName) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return canonicalName.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "Category(canonicalName='$canonicalName')"
        }
    }

    /**
     * Search suggestion of the [Brand] type points to a list of [SearchResult] that will be returned after selection.
     *
     * @property brandName - the name of the brand.
     * @property brandId - the id of the brand.
     */
    @Parcelize
    public class Brand(
        public val brandName: String,
        public val brandId: String,
    ) : SearchSuggestionType() {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Brand

            if (brandName != other.brandName) return false
            if (brandId != other.brandId) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            var result = brandName.hashCode()
            result = 31 * result + brandId.hashCode()
            return result
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "Brand(brandName='$brandName', brandId='$brandId')"
        }
    }

    /**
     * [Query] suggestion type points to a new list of [SearchSuggestion], i.e. selection of this suggestion type will result in new suggestions.
     */
    @Parcelize
    public object Query : SearchSuggestionType()

    /**
     * Search suggestion of the [IndexableRecordItem] type points to the search results with [IndexableRecord] that will be returned after selection.
     *
     * @property dataProviderName - the id of the data provider.
     * @property type - type of the [com.mapbox.search.record.IndexableRecord] that will be resolved after selection of the search suggestion.
     */
    @Parcelize
    public class IndexableRecordItem internal constructor(
        public val dataProviderName: String,
        public val type: SearchResultType,
    ) : SearchSuggestionType() {

        /**
         * True if the resulting record belongs to the [HistoryDataProvider].
         */
        public val isHistoryRecord: Boolean
            get() = dataProviderName == HistoryDataProvider.PROVIDER_NAME

        /**
         * True if the resulting record belongs to the [FavoritesDataProvider].
         */
        public val isFavoriteRecord: Boolean
            get() = dataProviderName == FavoritesDataProvider.PROVIDER_NAME

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as IndexableRecordItem

            if (dataProviderName != other.dataProviderName) return false
            if (type != other.type) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            var result = dataProviderName.hashCode()
            result = 31 * result + type.hashCode()
            return result
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "IndexableRecordItem(dataProviderName='$dataProviderName', type=$type)"
        }
    }
}
