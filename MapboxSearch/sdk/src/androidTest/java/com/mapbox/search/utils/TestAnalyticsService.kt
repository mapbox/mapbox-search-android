package com.mapbox.search.utils

import com.mapbox.search.CompletionCallback
import com.mapbox.search.ResponseInfo
import com.mapbox.search.analytics.FeedbackEvent
import com.mapbox.search.analytics.InternalAnalyticsService
import com.mapbox.search.analytics.MissingResultFeedbackEvent
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import java.util.concurrent.Executor

internal class TestAnalyticsService : InternalAnalyticsService {

    val capturedErrors: List<Throwable>
        get() = errors

    private var errors: MutableList<Throwable> = mutableListOf()

    override fun createRawFeedbackEvent(
        searchResult: SearchResult,
        responseInfo: ResponseInfo,
        executor: Executor,
        callback: CompletionCallback<String>
    ) {
        throw NotImplementedError()
    }

    override fun createRawFeedbackEvent(
        searchSuggestion: SearchSuggestion,
        responseInfo: ResponseInfo,
        executor: Executor,
        callback: CompletionCallback<String>
    ) {
        throw NotImplementedError()
    }

    override fun sendFeedback(searchResult: SearchResult, responseInfo: ResponseInfo, event: FeedbackEvent) {
        // Not implemented
    }

    override fun sendFeedback(searchSuggestion: SearchSuggestion, responseInfo: ResponseInfo, event: FeedbackEvent) {
        // Not implemented
    }

    override fun sendFeedback(historyRecord: HistoryRecord, event: FeedbackEvent) {
        // Not implemented
    }

    override fun sendFeedback(favoriteRecord: FavoriteRecord, event: FeedbackEvent) {
        // Not implemented
    }

    override fun sendMissingResultFeedback(event: MissingResultFeedbackEvent) {
        // Not implemented
    }

    override fun reportError(throwable: Throwable) {
        errors.add(throwable)
    }

    override fun setAccessToken(accessToken: String) {
        // Nothing to do
    }

    fun reset() {
        errors = mutableListOf()
    }
}
