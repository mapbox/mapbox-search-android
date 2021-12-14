package com.mapbox.search.ui.view.feedback

import android.os.Parcelable
import com.mapbox.search.ResponseInfo
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import kotlinx.parcelize.Parcelize

/**
 * Metadata for a search place required to report a feedback.
 */
public abstract class IncorrectSearchPlaceFeedback : Parcelable {

    /**
     * Metadata for a search place based on [SearchResult].
     */
    @Parcelize
    public class SearchResultFeedback(

        /**
         * [SearchResult] used for search place creation.
         */
        public val searchResult: SearchResult,

        /**
         * Response info associated with [searchResult].
         */
        public val responseInfo: ResponseInfo,
    ) : IncorrectSearchPlaceFeedback() {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SearchResultFeedback

            if (searchResult != other.searchResult) return false
            if (responseInfo != other.responseInfo) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            var result = searchResult.hashCode()
            result = 31 * result + responseInfo.hashCode()
            return result
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "SearchResultFeedback(searchResult=$searchResult, responseInfo=$responseInfo)"
        }
    }

    /**
     * Metadata for a search place based on [HistoryRecord].
     */
    @Parcelize
    public class HistoryFeedback(

        /**
         * [HistoryRecord] used for search place creation.
         */
        public val historyRecord: HistoryRecord,
    ) : IncorrectSearchPlaceFeedback() {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as HistoryFeedback

            if (historyRecord != other.historyRecord) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return historyRecord.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "HistoryFeedback(historyRecord=$historyRecord)"
        }
    }

    /**
     * Feedback information about a search place based on [FavoriteRecord].
     */
    @Parcelize
    public class FavoriteFeedback(

        /**
         * [FavoriteRecord] used for search place creation.
         */
        public val favoriteRecord: FavoriteRecord,
    ) : IncorrectSearchPlaceFeedback() {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FavoriteFeedback

            if (favoriteRecord != other.favoriteRecord) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return favoriteRecord.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "FavoriteFeedback(favoriteRecord=$favoriteRecord)"
        }
    }
}
