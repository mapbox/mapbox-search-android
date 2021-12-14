package com.mapbox.search.ui.view.feedback

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.mapbox.search.analytics.FeedbackEvent
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.getDrawableCompat
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.utils.extenstion.setCompoundDrawableStartWithIntrinsicBounds
import com.mapbox.search.ui.utils.extenstion.setTintCompat

internal class FeedbackReasonView : ConstraintLayout {

    var callback: Callback? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        isClickable = true
        isFocusable = true

        View.inflate(context, R.layout.mapbox_search_sdk_feedback_card_reason_view, this)

        val drawableColor = context.resolveAttrOrThrow(R.attr.mapboxSearchSdkPrimaryTextColor)

        fun initReasonButton(
            @IdRes viewId: Int,
            @DrawableRes drawableRes: Int,
            @FeedbackEvent.FeedbackReason reason: String
        ) {
            with(findViewById<TextView>(viewId)) {
                setOnClickListener {
                    callback?.onFeedbackReasonClick(reason)
                }

                setCompoundDrawableStartWithIntrinsicBounds(
                    context.getDrawableCompat(drawableRes)?.setTintCompat(drawableColor)
                )
            }
        }

        initReasonButton(
            viewId = R.id.feedback_reason_incorrect_name,
            drawableRes = R.drawable.mapbox_search_sdk_ic_feedback_reason_incorrect_name,
            reason = FeedbackEvent.FeedbackReason.INCORRECT_NAME,
        )

        initReasonButton(
            viewId = R.id.feedback_reason_incorrect_address,
            drawableRes = R.drawable.mapbox_search_sdk_ic_feedback_reason_incorrect_address,
            reason = FeedbackEvent.FeedbackReason.INCORRECT_ADDRESS,
        )

        initReasonButton(
            viewId = R.id.feedback_reason_incorrect_location,
            drawableRes = R.drawable.mapbox_search_sdk_ic_feedback_reason_incorrect_location,
            reason = FeedbackEvent.FeedbackReason.INCORRECT_LOCATION,
        )

        initReasonButton(
            viewId = R.id.feedback_reason_incorrect_other,
            drawableRes = R.drawable.mapbox_search_sdk_ic_three_dots,
            reason = FeedbackEvent.FeedbackReason.OTHER,
        )

        findViewById<View>(R.id.feedback_reason_back_button).setOnClickListener {
            callback?.onBackClick()
        }

        findViewById<View>(R.id.feedback_reason_close_button).setOnClickListener {
            callback?.onCloseClick()
        }
    }

    interface Callback {
        fun onBackClick()
        fun onCloseClick()
        fun onFeedbackReasonClick(@FeedbackEvent.FeedbackReason reason: String)
    }
}
