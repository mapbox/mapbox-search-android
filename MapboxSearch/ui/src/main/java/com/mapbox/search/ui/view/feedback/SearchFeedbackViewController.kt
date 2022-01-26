package com.mapbox.search.ui.view.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mapbox.search.ResponseInfo
import com.mapbox.search.ui.view.common.BaseSearchController
import com.mapbox.search.ui.view.feedback.SearchFeedbackView.FeedbackMode

internal class SearchFeedbackViewController : BaseSearchController {

    var callback: SearchFeedbackView.Callback? = null

    private val feedbackMode: FeedbackMode

    override val cardDraggingAllowed: Boolean = true

    constructor(feedbackMode: FeedbackMode) : super(bundleFeedbackMode(feedbackMode)) {
        this.feedbackMode = feedbackMode
    }

    // Android Studio marks this constructor as unused, but it's needed for Controller
    constructor(bundle: Bundle) : super(bundle) {
        feedbackMode = requireNotNull(bundle.getParcelable(BUNDLE_KEY_FeedbackMode))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return SearchFeedbackView(container.context).apply {
            init(feedbackMode)

            callback = object : SearchFeedbackView.Callback {
                override fun onBackClick() {
                    this@SearchFeedbackViewController.callback?.onBackClick()
                }

                override fun onCloseClick() {
                    this@SearchFeedbackViewController.callback?.onCloseClick()
                }

                override fun onSendMissingResultFeedback(text: String, responseInfo: ResponseInfo) {
                    this@SearchFeedbackViewController.callback?.onSendMissingResultFeedback(text, responseInfo)
                }

                override fun onSendIncorrectResultFeedback(
                    reason: String,
                    text: String,
                    feedback: IncorrectSearchPlaceFeedback
                ) {
                    this@SearchFeedbackViewController.callback?.onSendIncorrectResultFeedback(reason, text, feedback)
                }
            }
        }
    }

    internal companion object {

        const val TAG = "controller.tag.SearchFeedbackViewController"

        private const val BUNDLE_KEY_FeedbackMode = "key.FeedbackMode"

        private fun bundleFeedbackMode(feedbackMode: FeedbackMode): Bundle {
            return Bundle().apply {
                putParcelable(BUNDLE_KEY_FeedbackMode, feedbackMode)
            }
        }
    }
}
