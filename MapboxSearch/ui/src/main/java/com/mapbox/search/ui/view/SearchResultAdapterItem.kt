package com.mapbox.search.ui.view

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.mapbox.search.ResponseInfo
import com.mapbox.search.base.utils.extension.safeCompareTo
import com.mapbox.search.record.HistoryRecord

/**
 * [com.mapbox.search.ui.view.SearchResultsView] adapter items.
 * @see [com.mapbox.search.ui.view.SearchResultsView.setAdapterItems]
 */
public abstract class SearchResultAdapterItem {

    /**
     * Item that represents spinning progress view.
     */
    public object Loading : SearchResultAdapterItem()

    /**
     * Item that represents `Recent searches` header for the search history.
     * @see [com.mapbox.search.record.HistoryDataProvider]
     */
    public object RecentSearchesHeader : SearchResultAdapterItem()

    /**
     * Item that represents header for the empty search history.
     * @see [com.mapbox.search.record.HistoryDataProvider]
     */
    public object EmptyHistory : SearchResultAdapterItem()

    /**
     * Item that represents one search history entry.
     * @param record The [HistoryRecord] for the item.
     * @param isFavorite The flag that denotes whether the [record] is also part of the [com.mapbox.search.record.FavoritesDataProvider].
     */
    public class History(
        public val record: HistoryRecord,
        public val isFavorite: Boolean,
    ) : SearchResultAdapterItem() {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as History

            if (record != other.record) return false
            if (isFavorite != other.isFavorite) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            var result = record.hashCode()
            result = 31 * result + isFavorite.hashCode()
            return result
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "History(record=$record, isFavorite=$isFavorite)"
        }
    }

    /**
     * Item that represents view for a case where search engine returns nothing.
     */
    public object EmptySearchResults : SearchResultAdapterItem()

    /**
     * Item that represents any search result. This can be a result from any search engine, or use case, for example,
     * [com.mapbox.search.result.SearchSuggestion], [com.mapbox.search.result.SearchResult],
     * [com.mapbox.search.offline.OfflineSearchResult], etc.
     *
     * @param title The title view text. Usually, this is a name of the search result.
     *
     * @param subtitle The subtitle view text. Usually, this is an address of the search result, or POI category.
     *
     * @param distanceMeters The distance in meters from user's location to the search result.
     *
     * @param drawable The drawable id that represents the search result.
     * For example, this can be address icon, or category. See [Maki](https://github.com/mapbox/maki/) icons.
     *
     * @param drawableColor The color that can be used for [drawable] tint.
     * If empty, the default color will be chosen automatically.
     *
     * @param isPopulateQueryVisible The flag that denotes whether the `Populate` view will be shown.
     * Usually, when a user clicks on that view, search result's name should be used to populate search query
     * and initiate a search request with query's text. False by default.
     *
     * @param payload Anything that can be used to associate this item with the search result.
     */
    public class Result @JvmOverloads public constructor(
        public val title: CharSequence,
        public val subtitle: CharSequence?,
        public val distanceMeters: Double?,
        @DrawableRes
        public val drawable: Int,
        @ColorInt
        public val drawableColor: Int? = null,
        public val isPopulateQueryVisible: Boolean = false,
        public val payload: Any? = null,
    ) : SearchResultAdapterItem() {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Result

            if (title != other.title) return false
            if (subtitle != other.subtitle) return false
            if (!distanceMeters.safeCompareTo(other.distanceMeters)) return false
            if (drawable != other.drawable) return false
            if (drawableColor != other.drawableColor) return false
            if (isPopulateQueryVisible != other.isPopulateQueryVisible) return false
            if (payload != other.payload) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            var result = title.hashCode()
            result = 31 * result + (subtitle?.hashCode() ?: 0)
            result = 31 * result + (distanceMeters?.hashCode() ?: 0)
            result = 31 * result + drawable
            result = 31 * result + (drawableColor?.hashCode() ?: 0)
            result = 31 * result + isPopulateQueryVisible.hashCode()
            result = 31 * result + (payload?.hashCode() ?: 0)
            return result
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "Result(" +
                    "title=$title, " +
                    "subtitle=$subtitle, " +
                    "distanceMeters=$distanceMeters, " +
                    "drawable=$drawable, " +
                    "drawableColor=$drawableColor, " +
                    "isPopulateQueryVisible=$isPopulateQueryVisible, " +
                    "payload=$payload" +
                    ")"
        }
    }

    /**
     * Item that represents `Missing result?` button.
     * @param responseInfo The [ResponseInfo] that's needed for constructing feedback metadata.
     */
    public class MissingResultFeedback(
        public val responseInfo: ResponseInfo,
    ) : SearchResultAdapterItem() {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MissingResultFeedback

            if (responseInfo != other.responseInfo) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return responseInfo.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "MissingResultFeedback(responseInfo=$responseInfo)"
        }
    }

    /**
     * Item that represents error view and `Retry` button.
     * @param uiError The error type and information.
     */
    public class Error(
        public val uiError: UiError,
    ) : SearchResultAdapterItem() {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Error

            if (uiError != other.uiError) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return uiError.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "Error(uiError=$uiError)"
        }
    }
}
