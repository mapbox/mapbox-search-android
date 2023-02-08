package com.mapbox.search.ui.view.place

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.search.ServiceProvider
import com.mapbox.search.base.failDebug
import com.mapbox.search.base.throwDebug
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.ui.R
import com.mapbox.search.ui.adapter.engines.SearchEntityPresentation
import com.mapbox.search.ui.utils.SearchBottomSheetBehavior
import com.mapbox.search.ui.utils.extenstion.expand
import com.mapbox.search.ui.utils.extenstion.hide
import com.mapbox.search.ui.utils.extenstion.isHidden
import com.mapbox.search.ui.utils.extenstion.setTextAndHideIfBlank
import com.mapbox.search.ui.utils.format.DistanceFormatter
import com.mapbox.search.ui.utils.wrapWithSearchTheme
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.SearchSdkFrameLayout
import com.mapbox.search.ui.view.common.MapboxSdkButtonWithIcon
import kotlinx.parcelize.Parcelize
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.annotation.AnnotationRetention.SOURCE

/**
 * View can be used to show selected item from search result.
 *
 * Note that [SearchPlaceBottomSheetView.initialize] has to be called in order to make this view work properly.
 */
@Suppress("TooManyFunctions")
public class SearchPlaceBottomSheetView @JvmOverloads constructor(
    outerContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : SearchSdkFrameLayout(wrapWithSearchTheme(outerContext), attrs, defStyleAttr, defStyleRes), CoordinatorLayout.AttachedBehavior {

    private val favoritesDataProvider = ServiceProvider.INSTANCE.favoritesDataProvider()
    private var addFavoriteTask: AsyncOperationTask? = null

    private val behavior = SearchBottomSheetBehavior<View>().apply {
        isHideable = true
        state = BottomSheetBehavior.STATE_HIDDEN
    }

    private val distanceFormatter = DistanceFormatter(context)

    private val searchEntityPresentation = SearchEntityPresentation(context)

    private var commonSearchViewConfiguration = CommonSearchViewConfiguration()
    private var isInitialized = false

    private val name: TextView
    private val categoryText: TextView
    private val addressText: TextView
    private val distanceText: TextView
    private val navigateButton: View
    private val shareButton: View
    private val shareButtonGroup: View
    private val favoriteButton: MapboxSdkButtonWithIcon
    private val favoriteButtonGroup: View
    private val feedbackButton: View
    private val feedbackButtonGroup: View

    private var latestSearchPlace: SearchPlace? = null

    private val onBottomSheetStateChangedListeners = CopyOnWriteArrayList<OnBottomSheetStateChangedListener>()
    private val onCloseClickListeners = CopyOnWriteArrayList<OnCloseClickListener>()
    private val onNavigateClickListeners = CopyOnWriteArrayList<OnNavigateClickListener>()
    private val onShareClickListeners = CopyOnWriteArrayList<OnShareClickListener>()
    private val onFeedbackClickListeners = CopyOnWriteArrayList<OnFeedbackClickListener>()
    private val onSearchPlaceAddedToFavoritesListeners = CopyOnWriteArrayList<OnSearchPlaceAddedToFavoritesListener>()

    /**
     * The current state of the bottom sheet.
     */
    @get:BottomSheetState
    public val state: Int
        get() = fromBottomSheetState(behavior.state)

    /**
     * Visibility of navigate button.
     */
    public var isNavigateButtonVisible: Boolean = true
        set(value) {
            field = value
            navigateButton.isVisible = value
        }

    /**
     * Visibility of share button.
     */
    public var isShareButtonVisible: Boolean = true
        set(value) {
            field = value
            shareButtonGroup.isVisible = value
        }

    /**
     * Visibility of favorite button.
     */
    public var isFavoriteButtonVisible: Boolean = true
        set(value) {
            field = value
            favoriteButtonGroup.isVisible = value
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.mapbox_search_sdk_place_card_bottom_sheet, this, true)

        name = findViewById(R.id.search_result_name)
        categoryText = findViewById(R.id.search_result_category)
        addressText = findViewById(R.id.search_result_address)
        distanceText = findViewById(R.id.search_result_distance)
        navigateButton = findViewById(R.id.search_result_button_navigate)

        shareButton = findViewById(R.id.search_result_button_share)
        shareButtonGroup = findViewById(R.id.share_button_group)

        favoriteButton = findViewById(R.id.search_result_button_favorite)
        favoriteButtonGroup = findViewById(R.id.favorite_button_group)

        feedbackButton = findViewById(R.id.search_result_button_feedback)
        feedbackButtonGroup = findViewById(R.id.feedback_button_group)

        findViewById<View>(R.id.card_close_button).setOnClickListener {
            onCloseClickListeners.forEach { it.onCloseClick() }
        }

        behavior.addOnStateChangedListener { newState, fromUser ->
            @BottomSheetState
            val bottomSheetState = fromBottomSheetState(newState)
            onBottomSheetStateChangedListeners.forEach { it.onStateChanged(bottomSheetState, fromUser) }
        }
    }

    /**
     * Initializes the inner state of this view and defines configuration options.
     * It's obligatory to call this method as soon as the view is created.
     *
     * @param commonSearchViewConfiguration Configuration options.
     *
     * @throws [IllegalStateException] if this method has already been called.
     */
    public fun initialize(commonSearchViewConfiguration: CommonSearchViewConfiguration) {
        check(!isInitialized) {
            "Already initialized"
        }
        isInitialized = true
        this.commonSearchViewConfiguration = commonSearchViewConfiguration
    }

    private fun checkInitialized() {
        check(isInitialized) {
            "Initialize this view first"
        }
    }

    /**
     * Switch [SearchPlaceBottomSheetView] to opened state and show [searchPlace] inside of it.
     * @param searchPlace search place to show inside of [SearchPlaceBottomSheetView].
     * @throws [IllegalStateException] if [SearchPlaceBottomSheetView.initialize] has not been called.
     * @see hide
     */
    public fun open(searchPlace: SearchPlace) {
        checkInitialized()

        setSearchPlace(searchPlace)
        behavior.expand()
    }

    /**
     * Update distance to the current [SearchPlace].
     *
     * @return True if distance was updated, false otherwise.
     * @throws [IllegalStateException] if [SearchPlaceBottomSheetView.initialize] has not been called.
     */
    public fun updateDistance(distanceMeters: Double): Boolean {
        checkInitialized()

        if (latestSearchPlace != null) {
            latestSearchPlace = latestSearchPlace?.copy(distanceMeters = distanceMeters)
            updateDistanceUi(latestSearchPlace?.distanceMeters)
        }

        return latestSearchPlace != null
    }

    /**
     * Switch [SearchPlaceBottomSheetView] to hidden state.
     *
     * @throws [IllegalStateException] if [SearchPlaceBottomSheetView.initialize] has not been called.
     * @see open
     */
    public fun hide() {
        checkInitialized()
        behavior.hide()
    }

    /**
     * Check if [SearchPlaceBottomSheetView] is hidden.
     *
     * @return true if hidden.
     * @throws [IllegalStateException] if [SearchPlaceBottomSheetView.initialize] has not been called.
     */
    public fun isHidden(): Boolean {
        checkInitialized()
        return behavior.isHidden
    }

    private fun setSearchPlace(searchPlace: SearchPlace) {
        addFavoriteTask?.cancel()
        addFavoriteTask = null

        latestSearchPlace = searchPlace

        name.text = searchPlace.name
        categoryText.setTextAndHideIfBlank(searchEntityPresentation.firstCategoryName(context, searchPlace.categories))
        addressText.text = searchEntityPresentation.getAddressOrResultType(searchPlace)
        feedbackButtonGroup.isVisible = searchPlace.feedback != null

        updateDistanceUi(searchPlace.distanceMeters)
        populateFavoriteButtonInfo(searchPlace) { newFavorite ->
            latestSearchPlace = latestSearchPlace?.copy(record = newFavorite)
            latestSearchPlace?.let { place ->
                populateFavoriteButtonInfo(
                    place,
                    animate = true
                )
            }
        }

        navigateButton.setOnClickListener {
            onNavigateClickListeners.forEach { it.onNavigateClick(searchPlace) }
        }

        shareButton.setOnClickListener {
            onShareClickListeners.forEach { it.onShareClick(searchPlace) }
        }

        feedbackButton.setOnClickListener {
            val feedback = searchPlace.feedback
            if (feedback == null) {
                failDebug { "`Feedback` button clicked for a SearchPlace without feedback info" }
                return@setOnClickListener
            }
            onFeedbackClickListeners.forEach { it.onFeedbackClick(searchPlace, feedback) }
        }
    }

    private fun updateDistanceUi(distanceMeters: Double?) {
        val text = if (distanceMeters != null) {
            distanceFormatter.format(distanceMeters, commonSearchViewConfiguration.distanceUnitType)
        } else {
            null
        }
        distanceText.setTextAndHideIfBlank(text)
    }

    private fun populateFavoriteButtonInfo(
        searchPlace: SearchPlace,
        overriddenAddedToFavorite: Boolean = searchPlace.record is FavoriteRecord,
        animate: Boolean = false,
        onFavoriteAdded: ((FavoriteRecord) -> Unit)? = null
    ) {
        if (overriddenAddedToFavorite) {
            Triple(
                R.string.mapbox_search_sdk_place_card_added_to_favorites,
                R.drawable.mapbox_search_sdk_ic_added_to_favorites,
                null
            )
        } else {
            val listener: (View) -> Unit = {
                if (addFavoriteTask == null || addFavoriteTask?.isCancelled == true) {
                    val newFavorite = searchPlace.toUserFavorite()
                    addFavoriteTask = favoritesDataProvider.upsert(
                        newFavorite,
                        object : CompletionCallback<Unit> {
                            override fun onComplete(result: Unit) {
                                onFavoriteAdded?.invoke(newFavorite)
                                onSearchPlaceAddedToFavoritesListeners.forEach {
                                    it.onSearchPlaceAddedToFavorites(searchPlace, newFavorite)
                                }
                            }

                            override fun onError(e: Exception) {
                                // Shouldn't happen with favorites
                                throwDebug(e) {
                                    "Unable to add favorite: ${e.message}"
                                }
                            }
                        })
                }
            }
            Triple(
                R.string.mapbox_search_sdk_place_card_add_to_favorites,
                R.drawable.mapbox_search_sdk_ic_add_favorite,
                listener
            )
        }.let { (text, icon, onClickListener) ->
            favoriteButton.setText(text, animate)
            favoriteButton.setIcon(icon, animate)
            favoriteButton.setOnClickListener(onClickListener)
        }
    }

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
     * @suppress
     */
    override fun onSaveInstanceState(): Parcelable {
        return SavedState(
            behavior.state,
            behavior.peekHeight,
            latestSearchPlace,
            commonSearchViewConfiguration,
            super.onSaveInstanceState()
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
            behavior.peekHeight = peekHeight
            this@SearchPlaceBottomSheetView.commonSearchViewConfiguration = configuration
            if (searchPlace != null && latestSearchPlace == null) {
                setSearchPlace(searchPlace)
            }
        }
    }

    override fun onDetachedFromWindow() {
        addFavoriteTask?.cancel()
        addFavoriteTask = null
        super.onDetachedFromWindow()
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
     * Adds a listener to be notified of navigate button click.
     *
     * @param listener The listener to notify when navigate button clicked.
     */
    public fun addOnNavigateClickListener(listener: OnNavigateClickListener) {
        onNavigateClickListeners.add(listener)
    }

    /**
     * Removes a previously added [OnNavigateClickListener].
     *
     * @param listener The listener to remove.
     */
    public fun removeOnNavigateClickListener(listener: OnNavigateClickListener) {
        onNavigateClickListeners.remove(listener)
    }

    /**
     * Adds a listener to be notified of share button click.
     *
     * @param listener The listener to notify when share button clicked.
     */
    public fun addOnShareClickListener(listener: OnShareClickListener) {
        onShareClickListeners.add(listener)
    }

    /**
     * Removes a previously added [OnShareClickListener].
     *
     * @param listener The listener to remove.
     */
    public fun removeOnShareClickListener(listener: OnShareClickListener) {
        onShareClickListeners.remove(listener)
    }

    /**
     * Adds a listener to be notified of feedback button click.
     *
     * @param listener The listener to notify when feedback button clicked.
     */
    public fun addOnFeedbackClickListener(listener: OnFeedbackClickListener) {
        onFeedbackClickListeners.add(listener)
    }

    /**
     * Removes a previously added [OnFeedbackClickListener].
     *
     * @param listener The listener to remove.
     */
    public fun removeOnFeedbackClickListener(listener: OnFeedbackClickListener) {
        onFeedbackClickListeners.remove(listener)
    }

    /**
     * Adds a listener to be notified of events when search place was added to favorites.
     *
     * @param listener The listener to notify when share button clicked.
     */
    public fun addOnSearchPlaceAddedToFavoritesListener(listener: OnSearchPlaceAddedToFavoritesListener) {
        onSearchPlaceAddedToFavoritesListeners.add(listener)
    }

    /**
     * Removes a previously added [OnSearchPlaceAddedToFavoritesListener].
     *
     * @param listener The listener to remove.
     */
    public fun removeOnSearchPlaceAddedToFavoritesListener(listener: OnSearchPlaceAddedToFavoritesListener) {
        onSearchPlaceAddedToFavoritesListeners.remove(listener)
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
     * Listener for navigate button click events.
     */
    public fun interface OnNavigateClickListener {

        /**
         * Called when navigate button clicked.
         */
        public fun onNavigateClick(searchPlace: SearchPlace)
    }

    /**
     * Listener for share button click events.
     */
    public fun interface OnShareClickListener {

        /**
         * Called when share button clicked.
         */
        public fun onShareClick(searchPlace: SearchPlace)
    }

    /**
     * Listener for feedback button click events.
     */
    public fun interface OnFeedbackClickListener {

        /**
         * Called when feedback button clicked.
         *
         * @param searchPlace [SearchPlace] associated with this [SearchPlaceBottomSheetView].
         * @param feedback Information about a place required to report a feedback.
         */
        public fun onFeedbackClick(searchPlace: SearchPlace, feedback: IncorrectSearchPlaceFeedback)
    }

    /**
     * Listener for events when search place was added to favorites.
     */
    public fun interface OnSearchPlaceAddedToFavoritesListener {

        /**
         * Called when specific [SearchPlace] added to favorites.
         *
         * @param searchPlace place that is added to favorites.
         * @param favorite favorite record associated with specified [searchPlace].
         */
        public fun onSearchPlaceAddedToFavorites(searchPlace: SearchPlace, favorite: FavoriteRecord)
    }

    @Parcelize
    private class SavedState(
        val cardState: Int,
        val peekHeight: Int,
        val searchPlace: SearchPlace?,
        val configuration: CommonSearchViewConfiguration,
        private val baseState: Parcelable?
    ) : BaseSavedState(baseState)

    /**
     * State of the bottom sheet.
     */
    @IntDef(HIDDEN, OPEN, DRAGGING, SETTLING)
    @Retention(SOURCE)
    public annotation class BottomSheetState

    /**
     * Companion object.
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
                BottomSheetBehavior.STATE_COLLAPSED, BottomSheetBehavior.STATE_HALF_EXPANDED, BottomSheetBehavior.STATE_EXPANDED -> OPEN
                BottomSheetBehavior.STATE_DRAGGING -> DRAGGING
                BottomSheetBehavior.STATE_SETTLING -> SETTLING
                BottomSheetBehavior.STATE_HIDDEN -> HIDDEN
                else -> throw IllegalStateException("Unprocessed state: $state")
            }
        }

        private fun SearchPlace.toUserFavorite(): FavoriteRecord {
            return if (record is FavoriteRecord) {
                record
            } else {
                FavoriteRecord(
                    id = id,
                    name = name,
                    coordinate = coordinate,
                    descriptionText = descriptionText,
                    address = address,
                    type = resultTypes.first(),
                    makiIcon = makiIcon,
                    categories = categories,
                    routablePoints = routablePoints,
                    metadata = metadata
                )
            }
        }
    }
}
