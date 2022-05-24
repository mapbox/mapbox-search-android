package com.mapbox.search.analytics

import com.mapbox.search.CompletionCallback
import com.mapbox.search.ResponseInfo
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import java.util.concurrent.Executor

/**
 * Class for tracking analytics events from user side.
 */
public interface AnalyticsService {

    /**
     * Creates raw feedback event. This event may be cached and used later.
     * @param searchResult search result, for which raw feedback event will be created.
     * @param responseInfo search context, associated with provided *searchResult*.
     * @param executor Executor used for result dispatching. By default result is dispatched on the main thread.
     * @param callback The callback to retrieve result.
     */
    @Deprecated(
        "This function returns raw event in a very specific format and should not be used",
        replaceWith = ReplaceWith("Replace with own implementation specific for your analytics system")
    )
    public fun createRawFeedbackEvent(
        searchResult: SearchResult,
        responseInfo: ResponseInfo,
        executor: Executor,
        callback: CompletionCallback<String>
    )

    /**
     * Creates raw feedback event. This event may be cached and used later.
     * @param searchSuggestion search suggestion, for which raw feedback event will be created.
     * @param responseInfo search context, associated with provided *searchSuggestion*.
     * @param executor Executor used for result dispatching. By default result is dispatched on the main thread.
     * @param callback The callback to retrieve result.
     */
    @Deprecated(
        "This function returns raw event in a very specific format and should not be used",
        replaceWith = ReplaceWith("Replace with own implementation specific for your analytics system")
    )
    public fun createRawFeedbackEvent(
        searchSuggestion: SearchSuggestion,
        responseInfo: ResponseInfo,
        executor: Executor,
        callback: CompletionCallback<String>
    )

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
