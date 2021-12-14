package com.mapbox.search.ui.view.feedback

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.mapbox.search.ResponseInfo
import com.mapbox.search.analytics.FeedbackEvent
import com.mapbox.search.common.failDebug
import com.mapbox.search.ui.utils.extenstion.findControllerByTag
import com.mapbox.search.ui.utils.extenstion.resetControllers
import com.mapbox.search.ui.utils.extenstion.unwrapActivityOrNull
import com.mapbox.search.ui.utils.extenstion.withHorizontalAnimation

internal class SearchFeedbackBottomSheetViewNavigation(
    private val context: Context,
    private val screensContainer: ViewGroup,
) {

    var feedback: IncorrectSearchPlaceFeedback? = null
    var callback: Callback? = null

    private lateinit var router: Router

    fun initialize(savedInstanceState: Bundle?) {
        router = Conductor.attachRouter(context.unwrapActivityOrNull()!!, screensContainer, savedInstanceState)

        if (savedInstanceState == null && router.hasRootController()) {
            router.resetControllers()
        }

        if (!router.hasRootController()) {
            val controller = FeedbackReasonViewController()
            bindFeedbackReasonViewController(controller)
            val with = RouterTransaction.with(controller)
                .tag(FeedbackReasonViewController.TAG)
            router.setRoot(with)
        } else {
            val reasonViewController = router.findControllerByTag<FeedbackReasonViewController>(
                FeedbackReasonViewController.TAG
            )
            if (reasonViewController == null) {
                failDebug { "Can't restore root controller" }
            } else {
                bindFeedbackReasonViewController(reasonViewController)
            }

            router.findControllerByTag<SearchFeedbackViewController>(SearchFeedbackViewController.TAG)?.let {
                bindSearchFeedbackViewController(it)
            }
        }
    }

    private fun onFeedbackReasonChosen(@FeedbackEvent.FeedbackReason feedbackReason: String) {
        val feedbackInstance = feedback
        if (feedbackInstance == null) {
            failDebug { "Card has been opened without initialized data" }
        } else {
            val feedbackMode = SearchFeedbackView.FeedbackMode.IncorrectResult(feedbackInstance, feedbackReason)
            val controller = SearchFeedbackViewController(feedbackMode)
            bindSearchFeedbackViewController(controller)

            val transaction = RouterTransaction.with(controller)
                .tag(SearchFeedbackViewController.TAG)
                .withHorizontalAnimation()

            router.pushController(transaction)
        }
    }

    private fun bindFeedbackReasonViewController(controller: FeedbackReasonViewController) {
        controller.callback = object : FeedbackReasonView.Callback {
            override fun onBackClick() {
                callback?.onBackClicked()
            }

            override fun onCloseClick() {
                callback?.onCloseClicked()
            }

            override fun onFeedbackReasonClick(reason: String) {
                onFeedbackReasonChosen(reason)
            }
        }
    }

    private fun bindSearchFeedbackViewController(controller: SearchFeedbackViewController) {
        controller.callback = object : SearchFeedbackView.Callback {
            override fun onSendMissingResultFeedback(text: String, responseInfo: ResponseInfo) {
                failDebug { "Shouldn't be called in FeedbackBottomSheetView context" }
            }

            override fun onSendIncorrectResultFeedback(
                reason: String,
                text: String,
                feedback: IncorrectSearchPlaceFeedback
            ) {
                callback?.onSendIncorrectResultFeedback(reason, text, feedback)
            }

            override fun onBackClick() {
                callback?.onBackClicked()
            }

            override fun onCloseClick() {
                callback?.onCloseClicked()
            }
        }
    }

    fun popToRoot() {
        router.popToRoot()
    }

    fun handleOnBackPressed(): Boolean {
        return router.backstackSize > 1 && router.handleBack()
    }

    interface Callback : OnFeedbackSubmitCallback {
        fun onBackClicked()
        fun onCloseClicked()
    }
}
