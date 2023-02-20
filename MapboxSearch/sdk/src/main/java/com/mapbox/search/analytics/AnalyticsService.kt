package com.mapbox.search.analytics

import com.mapbox.search.ResponseInfo
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion

/**
 * Class for tracking analytics events from user side.
 */
public interface AnalyticsService {

    /**
     * Sends feedback event to analytics.
     * @param searchResult search result, for which feedback is given.
     * @param responseInfo search context, associated with provided *searchResult*.
     * @param event extra information for feedback, provided by user.
     */
    public fun sendFeedback(searchResult: SearchResult, responseInfo: ResponseInfo, event: FeedbackEvent)

    /**
     * Sends feedback event to analytics.
     * @param searchSuggestion search suggestion, for which feedback is given.
     * @param responseInfo search context, associated with provided *searchSuggestion*.
     * @param event extra information for feedback, provided by user.
     */
    public fun sendFeedback(searchSuggestion: SearchSuggestion, responseInfo: ResponseInfo, event: FeedbackEvent)

    /**
     * Sends feedback event to analytics.
     * @param historyRecord history record, for which feedback is given.
     * @param event extra information for feedback, provided by user.
     */
    public fun sendFeedback(historyRecord: HistoryRecord, event: FeedbackEvent)

    /**
     * Sends feedback event to analytics.
     * @param favoriteRecord favorite record, for which feedback is given.
     * @param event extra information for feedback, provided by user.
     */
    public fun sendFeedback(favoriteRecord: FavoriteRecord, event: FeedbackEvent)

    /**
     * Sends missing result feedback event to analytics.
     * @param event extra information for feedback, provided by user.
     */
    public fun sendMissingResultFeedback(event: MissingResultFeedbackEvent)
}
