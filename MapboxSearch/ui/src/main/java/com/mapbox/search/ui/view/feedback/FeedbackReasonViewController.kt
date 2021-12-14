package com.mapbox.search.ui.view.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller

internal class FeedbackReasonViewController : Controller {

    var callback: FeedbackReasonView.Callback? = null

    constructor() : super()

    // Android Studio marks this constructor as unused, but it's needed for Controller
    constructor(args: Bundle?) : super(args)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return FeedbackReasonView(container.context).apply {
            callback = object : FeedbackReasonView.Callback {
                override fun onBackClick() {
                    this@FeedbackReasonViewController.callback?.onBackClick()
                }

                override fun onCloseClick() {
                    this@FeedbackReasonViewController.callback?.onCloseClick()
                }

                override fun onFeedbackReasonClick(reason: String) {
                    this@FeedbackReasonViewController.callback?.onFeedbackReasonClick(reason)
                }
            }
        }
    }

    companion object {
        const val TAG = "controller.tag.FeedbackReasonViewController"
    }
}
