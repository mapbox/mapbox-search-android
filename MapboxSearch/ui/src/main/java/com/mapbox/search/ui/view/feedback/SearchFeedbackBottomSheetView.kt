package com.mapbox.search.ui.view.feedback

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.ResponseInfo
import com.mapbox.search.analytics.FeedbackEvent
import com.mapbox.search.common.failDebug
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.SearchBottomSheetBehavior
import com.mapbox.search.ui.utils.extenstion.expand
import com.mapbox.search.ui.utils.extenstion.hide
import com.mapbox.search.ui.utils.extenstion.hideKeyboard
import com.mapbox.search.ui.utils.extenstion.isHidden
import com.mapbox.search.ui.utils.wrapWithSearchTheme
import com.mapbox.search.ui.view.SearchSdkFrameLayout
import kotlinx.parcelize.Parcelize
import java.util.concurrent.CopyOnWriteArrayList

/**
 * View that implements feedback reporting.
 *
 * Note that [SearchFeedbackBottomSheetView.initialize] has to be called in order to make this class work properly.
 */
public class SearchFeedbackBottomSheetView @JvmOverloads constructor(
    outerContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : SearchSdkFrameLayout(wrapWithSearchTheme(outerContext), attrs, defStyleAttr, defStyleRes), CoordinatorLayout.AttachedBehavior {

    private val behavior = SearchBottomSheetBehavior<View>().apply {
        isHideable = true
        state = BottomSheetBehavior.STATE_HIDDEN
    }

    private val screensContainer: ViewGroup

    private val onFeedbackSubmitClickListeners = CopyOnWriteArrayList<OnFeedbackSubmitClickListener>()
    private val onBottomSheetStateChangedListeners = CopyOnWriteArrayList<OnBottomSheetStateChangedListener>()
    private val onCloseClickListeners = CopyOnWriteArrayList<OnCloseClickListener>()

    private var isInitialized = false

    private val navigation: SearchFeedbackBottomSheetViewNavigation

    private val defaultOnFeedbackSubmitCallback = DefaultOnFeedbackSubmitCallback(
        MapboxSearchSdk.serviceProvider.analyticsService()
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.mapbox_search_sdk_feedback_card_container, this, true)
        screensContainer = findViewById(R.id.feedback_screen_container)

        navigation = SearchFeedbackBottomSheetViewNavigation(context, screensContainer)
        navigation.callback = object : SearchFeedbackBottomSheetViewNavigation.Callback {
            override fun onBackClicked() {
                handleOnBackPressed()
            }

            override fun onCloseClicked() {
                onCloseClickListeners.forEach { it.onCloseClick() }
            }

            override fun onSendMissingResultFeedback(text: String, responseInfo: ResponseInfo) {
                failDebug { "Shouldn't be called in FeedbackBottomSheetView context" }
            }

            override fun onSendIncorrectResultFeedback(
                reason: String,
                text: String,
                feedback: IncorrectSearchPlaceFeedback
            ) {
                val processed = onFeedbackSubmitClickListeners.any {
                    it.onSendIncorrectResultFeedback(reason, text, feedback)
                }

                if (!processed) {
                    defaultOnFeedbackSubmitCallback.onSendIncorrectResultFeedback(reason, text, feedback)
                }

                navigation.popToRoot()
                handleOnBackPressed()
            }
        }

        behavior.addOnStateChangedListener { newState, fromUser ->
            @BottomSheetState
            val bottomSheetState = fromBottomSheetState(newState)

            if (bottomSheetState == HIDDEN) {
                navigation.popToRoot()
                hideKeyboard()
            }

            onBottomSheetStateChangedListeners.forEach { it.onStateChanged(bottomSheetState, fromUser) }
        }
    }

    /**
     * Initializes [SearchFeedbackViewController] with the passed state.
     * If this function is not called, view's state might be not restored properly and other functions might throw exceptions.
     *
     * @param savedInstanceState State passed to [android.app.Activity] lifecycle functions.
     *
     * @throws IllegalStateException If this function called more that once.
     */
    public fun initialize(savedInstanceState: Bundle?) {
        check(!isInitialized) {
            "Already initialized"
        }
        isInitialized = true
        navigation.initialize(savedInstanceState)
    }

    /**
     * Switch [SearchFeedbackBottomSheetView] to open state.
     *
     * @param feedback Information about a place for which a user is going to report feedback.
     *
     * @throws IllegalStateException If [initialize] has not been called before.
     *
     * @see hide
     */
    public fun open(feedback: IncorrectSearchPlaceFeedback) {
        check(isInitialized) {
            "Call initialize() first"
        }

        navigation.feedback = feedback
        behavior.expand()
    }

    /**
     * Switch [SearchFeedbackBottomSheetView] to hidden state.
     * @see open
     */
    public fun hide() {
        behavior.hide()
    }

    /**
     * Check if [SearchFeedbackBottomSheetView] is hidden.
     * @return true if hidden.
     */
    public fun isHidden(): Boolean = behavior.isHidden

    /**
     * @suppress
     */
    override fun getBehavior(): CoordinatorLayout.Behavior<*> = behavior

    /**
     * @suppress
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        // Collapsed state == Expanded state
        behavior.peekHeight = height
    }

    /**
     * Back button handler.
     * @return true if a click has been processed.
     */
    public fun handleOnBackPressed(): Boolean {
        return when {
            !isHidden() && navigation.handleOnBackPressed() -> true
            !isHidden() -> {
                hide()
                true
            }
            else -> false
        }
    }

    /**
     * @suppress
     */
    override fun onSaveInstanceState(): Parcelable {
        return SavedState(
            cardState = behavior.state,
            feedback = navigation.feedback,
            baseState = super.onSaveInstanceState(),
        )
    }

    /**
     * @suppress
     */
    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? SavedState
        super.onRestoreInstanceState(savedState?.superState)
        savedState?.apply {
            behavior.state = cardState
            navigation.feedback = feedback
        }
    }

    /**
     * Adds a listener to be notified of "feedback" button click.
     *
     * @param listener The listener to notify when "feedback" button clicked.
     */
    public fun addOnFeedbackSubmitClickListener(listener: OnFeedbackSubmitClickListener) {
        onFeedbackSubmitClickListeners.add(listener)
    }

    /**
     * Removes a previously added [OnFeedbackSubmitClickListener].
     *
     * @param listener The listener to remove.
     */
    public fun removeOnFeedbackSubmitClickListener(listener: OnFeedbackSubmitClickListener) {
        onFeedbackSubmitClickListeners.remove(listener)
    }

    /**
     * Adds a listener to be notified of bottom sheet events.
     *
     * @param listener The listener to notify when bottom sheet state changes.
     */
    public fun addOnBottomSheetStateChangedListener(listener: OnBottomSheetStateChangedListener) {
        onBottomSheetStateChangedListeners.add(listener)
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeOnBottomSheetStateChangedListener(listener: OnBottomSheetStateChangedListener) {
        onBottomSheetStateChangedListeners.remove(listener)
    }

    /**
     * Adds a listener to be notified of close button click.
     *
     * @param listener The listener to notify when close button clicked.
     */
    public fun addOnCloseClickListener(listener: OnCloseClickListener) {
        onCloseClickListeners.add(listener)
    }

    /**
     * Removes a previously added [OnCloseClickListener].
     *
     * @param listener The listener to remove.
     */
    public fun removeOnCloseClickListener(listener: OnCloseClickListener) {
        onCloseClickListeners.remove(listener)
    }

    /**
     * Interface for a listener to be invoked when "Feedback submit" button is clicked.
     */
    public fun interface OnFeedbackSubmitClickListener {

        /**
         * Called when "Feedback submit" button is clicked.
         * This allows listeners to get a chance to process click on their own.
         *
         * [SearchFeedbackBottomSheetView] will not process the click if this function returns true.
         * If multiple listeners are registered and any of them has processed the click,
         * all the remaining listeners will not be invoked.
         *
         * By default, [SearchFeedbackBottomSheetView] will submit feedback info to a corresponding function
         * [com.mapbox.search.analytics.AnalyticsService.sendFeedback].
         *
         * @param reason Reason for user's feedback.
         * @param text User's feedback text. Should be provided, if "Other" feedback reason was specified.
         * @param feedback Metadata for a search place required to report a feedback.
         *
         * @return True if the listener has processed the click, false otherwise.
         */
        public fun onSendIncorrectResultFeedback(
            @FeedbackEvent.FeedbackReason reason: String,
            text: String,
            feedback: IncorrectSearchPlaceFeedback
        ): Boolean
    }

    /**
     * Listener for close button click events.
     */
    public fun interface OnCloseClickListener {

        /**
         * Called when close button clicked.
         */
        public fun onCloseClick()
    }

    /**
     * Listener for bottom sheet state changes events.
     */
    public fun interface OnBottomSheetStateChangedListener {

        /**
         * Called when the bottom sheet changes its state.
         *
         * @param newState The new state.
         * @param fromUser true if the change event was triggered by a user, for example, by card swipe.
         */
        public fun onStateChanged(@BottomSheetState newState: Int, fromUser: Boolean)
    }

    @Parcelize
    private class SavedState(
        val cardState: Int,
        val feedback: IncorrectSearchPlaceFeedback?,
        private val baseState: Parcelable?
    ) : BaseSavedState(baseState)

    /**
    * State of the bottom sheet.
    */
    @IntDef(HIDDEN, OPEN, DRAGGING, SETTLING)
    @Retention(AnnotationRetention.SOURCE)
    public annotation class BottomSheetState

    /**
     * @suppress
     */
    public companion object {

        /**
         * Hidden state.
         */
        public const val HIDDEN: Int = 1

        /**
         * Opened state.
         */
        public const val OPEN: Int = 2

        /**
         * Dragging state.
         */
        public const val DRAGGING: Int = 3

        /**
         * Settling state
         */
        public const val SETTLING: Int = 4

        @BottomSheetState
        @JvmSynthetic
        internal fun fromBottomSheetState(@BottomSheetBehavior.State state: Int): Int {
            return when (state) {
                BottomSheetBehavior.STATE_COLLAPSED,
                BottomSheetBehavior.STATE_HALF_EXPANDED,
                BottomSheetBehavior.STATE_EXPANDED -> OPEN
                BottomSheetBehavior.STATE_DRAGGING -> DRAGGING
                BottomSheetBehavior.STATE_SETTLING -> SETTLING
                BottomSheetBehavior.STATE_HIDDEN -> HIDDEN
                else -> throw IllegalStateException("Unprocessed state: $state")
            }
        }
    }
}
