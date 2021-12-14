package com.mapbox.search.ui.view.feedback

import com.mapbox.search.ResponseInfo
import com.mapbox.search.analytics.AnalyticsService
import com.mapbox.search.analytics.FeedbackEvent
import com.mapbox.search.analytics.MissingResultFeedbackEvent
import com.mapbox.search.common.failDebug
import com.mapbox.search.common.printableName

internal interface OnFeedbackSubmitCallback {

    fun onSendMissingResultFeedback(text: String, responseInfo: ResponseInfo)

    fun onSendIncorrectResultFeedback(
        @FeedbackEvent.FeedbackReason reason: String,
        text: String,
        feedback: IncorrectSearchPlaceFeedback,
    )
}

internal class DefaultOnFeedbackSubmitCallback(
    private val analyticsService: AnalyticsService
) : OnFeedbackSubmitCallback {

    override fun onSendMissingResultFeedback(text: String, responseInfo: ResponseInfo) {
        val feedback = MissingResultFeedbackEvent(
            responseInfo = responseInfo,
            text = text,
        )
        analyticsService.sendMissingResultFeedback(feedback)
    }

    override fun onSendIncorrectResultFeedback(reason: String, text: String, feedback: IncorrectSearchPlaceFeedback) {
        val event = FeedbackEvent(reason = reason, text = text)

        when (feedback) {
            is IncorrectSearchPlaceFeedback.SearchResultFeedback -> {
                analyticsService.sendFeedback(
                    searchResult = feedback.searchResult,
                    responseInfo = feedback.responseInfo,
                    event = event
                )
            }
            is IncorrectSearchPlaceFeedback.HistoryFeedback -> {
                analyticsService.sendFeedback(historyRecord = feedback.historyRecord, event = event)
            }
            is IncorrectSearchPlaceFeedback.FavoriteFeedback -> {
                analyticsService.sendFeedback(favoriteRecord = feedback.favoriteRecord, event = event)
            }
            else -> {
                failDebug { "Unprocessed IncorrectSearchPlaceFeedback type: ${feedback.javaClass.printableName}" }
            }
        }
    }
}
