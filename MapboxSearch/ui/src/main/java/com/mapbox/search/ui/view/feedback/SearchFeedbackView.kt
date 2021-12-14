package com.mapbox.search.ui.view.feedback

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.text.InputType
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.mapbox.search.ResponseInfo
import com.mapbox.search.analytics.FeedbackEvent
import com.mapbox.search.common.extension.isLandscape
import com.mapbox.search.common.failDebug
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.getPixelSize
import com.mapbox.search.ui.utils.extenstion.hideKeyboard
import com.mapbox.search.ui.utils.extenstion.setMarginTop
import com.mapbox.search.ui.utils.wrapWithSearchTheme
import kotlinx.parcelize.Parcelize

@SuppressLint("ClickableViewAccessibility")
internal class SearchFeedbackView : ConstraintLayout {

    private lateinit var feedbackMode: FeedbackMode

    var callback: Callback? = null

    private val title: TextView
    private val feedbackEditText: EditText

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
        val context = if (isInEditMode) {
            wrapWithSearchTheme(context)
        } else {
            context
        }

        isClickable = true
        isFocusable = true

        View.inflate(context, R.layout.mapbox_search_sdk_search_feedback_view, this)

        title = findViewById(R.id.feedback_screen_title)
        feedbackEditText = findViewById(R.id.feedback_edit_text)

        feedbackEditText.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }

        feedbackEditText.setRawInputType(InputType.TYPE_CLASS_TEXT)

        feedbackEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                sendFeedback()
            }
            true
        }

        findViewById<View>(R.id.feedback_submit).setOnClickListener {
            sendFeedback()
        }

        findViewById<View>(R.id.feedback_back_button).setOnClickListener {
            callback?.onBackClick()
        }

        findViewById<View>(R.id.feedback_close_button).setOnClickListener {
            callback?.onCloseClick()
        }

        if (context.isLandscape) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(this)
            constraintSet.clear(R.id.feedback_submit, ConstraintSet.BOTTOM)
            constraintSet.connect(
                R.id.feedback_submit,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM
            )
            constraintSet.applyTo(this)

            updatePadding(bottom = resources.getDimensionPixelSize(R.dimen.mapbox_search_sdk_primary_layout_offset))

            findViewById<View>(R.id.feedback_comment_title).isVisible = false
            feedbackEditText.setMarginTop(context.getPixelSize(R.dimen.mapbox_search_sdk_dimen_5x))
        }
    }

    fun init(feedbackMode: FeedbackMode) {
        this.feedbackMode = feedbackMode

        @StringRes
        fun titleForFeedbackReason(@FeedbackEvent.FeedbackReason feedbackReason: String): Int {
            return when (feedbackReason) {
                FeedbackEvent.FeedbackReason.INCORRECT_NAME -> R.string.mapbox_search_sdk_feedback_card_reason_incorrect_name
                FeedbackEvent.FeedbackReason.INCORRECT_ADDRESS -> R.string.mapbox_search_sdk_feedback_card_reason_incorrect_address
                FeedbackEvent.FeedbackReason.INCORRECT_LOCATION -> R.string.mapbox_search_sdk_feedback_card_reason_incorrect_location
                FeedbackEvent.FeedbackReason.OTHER -> R.string.mapbox_search_sdk_feedback_card_reason_other
                else -> {
                    failDebug { "Unprocessed FeedbackEvent.FeedbackReason: $feedbackReason" }
                    R.string.mapbox_search_sdk_feedback_card_reason_other
                }
            }
        }

        title.setText(
            when (feedbackMode) {
                is FeedbackMode.MissingResult -> R.string.mapbox_search_sdk_search_feedback_screen_title
                is FeedbackMode.IncorrectResult -> titleForFeedbackReason(feedbackMode.reason)
            }
        )
    }

    private fun sendFeedback() {
        if (!::feedbackMode.isInitialized) {
            return
        }

        when (val mode = feedbackMode) {
            is FeedbackMode.MissingResult -> {
                callback?.onSendMissingResultFeedback(
                    text = feedbackEditText.text.toString(),
                    responseInfo = mode.responseInfo,
                )
            }
            is FeedbackMode.IncorrectResult -> {
                callback?.onSendIncorrectResultFeedback(
                    reason = mode.reason,
                    text = feedbackEditText.text.toString(),
                    feedback = mode.feedback,
                )
            }
        }

        feedbackEditText.hideKeyboard()
    }
    sealed class FeedbackMode : Parcelable {

        @Parcelize
        data class MissingResult(val responseInfo: ResponseInfo) : FeedbackMode()

        @Parcelize
        data class IncorrectResult(
            val feedback: IncorrectSearchPlaceFeedback,
            @FeedbackEvent.FeedbackReason
            val reason: String,
        ) : FeedbackMode()
    }

    internal interface Callback : OnFeedbackSubmitCallback {
        fun onBackClick()
        fun onCloseClick()
    }
}
