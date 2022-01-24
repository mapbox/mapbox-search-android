package com.mapbox.search.ui.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.OfflineSearchEngine
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchOptions
import com.mapbox.search.common.failDebug
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.SearchBottomSheetBehavior
import com.mapbox.search.ui.utils.extenstion.findControllerByTag
import com.mapbox.search.ui.utils.extenstion.getPixelSize
import com.mapbox.search.ui.utils.extenstion.hideKeyboard
import com.mapbox.search.ui.utils.extenstion.isCollapsed
import com.mapbox.search.ui.utils.extenstion.isExpanded
import com.mapbox.search.ui.utils.extenstion.isHidden
import com.mapbox.search.ui.utils.extenstion.popToRootImmediately
import com.mapbox.search.ui.utils.extenstion.resetControllers
import com.mapbox.search.ui.utils.extenstion.unwrapActivityOrNull
import com.mapbox.search.ui.utils.extenstion.withHorizontalAnimation
import com.mapbox.search.ui.utils.wrapWithSearchTheme
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.category.CategorySuggestionSearchViewController
import com.mapbox.search.ui.view.favorite.FavoriteTemplate
import com.mapbox.search.ui.view.favorite.FavoriteTemplate.Companion.HOME_DEFAULT_TEMPLATE_ID
import com.mapbox.search.ui.view.favorite.FavoriteTemplate.Companion.WORK_DEFAULT_TEMPLATE_ID
import com.mapbox.search.ui.view.feedback.DefaultOnFeedbackSubmitCallback
import com.mapbox.search.ui.view.feedback.IncorrectSearchPlaceFeedback
import com.mapbox.search.ui.view.feedback.SearchFeedbackView
import com.mapbox.search.ui.view.feedback.SearchFeedbackViewController
import com.mapbox.search.ui.view.main.MainScreenViewController
import kotlinx.parcelize.Parcelize
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.annotation.AnnotationRetention.SOURCE

/**
 * View implements forward geocoding functionality. Also can add, edit or delete favorites.
 *
 * Note that [SearchBottomSheetView.initializeSearch] has to be called in order to make this view work properly.
 */
@Suppress("TooManyFunctions")
public class SearchBottomSheetView @JvmOverloads constructor(
    outerContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : SearchSdkFrameLayout(wrapWithSearchTheme(outerContext), attrs, defStyleAttr, defStyleRes), CoordinatorLayout.AttachedBehavior {

    /**
     * This property is responsible for possibility to toggle [SearchBottomSheetView] to hidden state by dragging down.
     */
    public var isHideableByDrag: Boolean = false
        set(value) {
            field = value
            updateCardHideable()
        }

    private val behavior = SearchBottomSheetBehavior<View>().apply {
        isHideable = isHideableByDrag
    }

    /**
     * The current state of the bottom sheet.
     */
    @get:BottomSheetState
    public val state: Int
        get() = fromBottomSheetState(behavior.state)

    /**
     * Search mode of this view, if mode is [SearchMode.ONLINE] [SearchEngine] will be used, [OfflineSearchEngine] otherwise.
     */
    public var searchMode: SearchMode = SearchMode.ONLINE
        set(value) {
            field = value
            onSearchModeChangedListeners.forEach { it(value) }
        }

    /**
     * [SearchOptions] that will be used for search requests.
     */
    public var searchOptions: SearchOptions = GlobalViewPreferences.DEFAULT_SEARCH_OPTIONS
        set(value) {
            field = value
            router.findControllerByTag<MainScreenViewController>(MainScreenViewController.TAG)?.searchOptions = value
        }

    private val onSearchModeChangedListeners = CopyOnWriteArrayList<(SearchMode) -> Unit>()

    @BottomSheetBehavior.State
    private var previousNonHiddenState: Int = behavior.state

    private var isInitialized = false

    private val screensContainer: ViewGroup

    private val onOnFeedbackClickListeners = CopyOnWriteArrayList<OnFeedbackClickListener>()
    private val onFeedbackSubmitClickListeners = CopyOnWriteArrayList<OnFeedbackSubmitClickListener>()
    private val onBottomSheetStateChangedListeners = CopyOnWriteArrayList<OnBottomSheetStateChangedListener>()
    private val onCategoryClickListeners = CopyOnWriteArrayList<OnCategoryClickListener>()
    private val onSearchResultClickListeners = CopyOnWriteArrayList<OnSearchResultClickListener>()
    private val onHistoryClickListeners = CopyOnWriteArrayList<OnHistoryClickListener>()
    private val onFavoriteClickListeners = CopyOnWriteArrayList<OnFavoriteClickListener>()

    private lateinit var router: Router

    private val defaultOnFeedbackSubmitCallback = DefaultOnFeedbackSubmitCallback(
        MapboxSearchSdk.serviceProvider.analyticsService()
    )

    internal var draggingAllowed: Boolean
        get() = behavior.draggingAllowed
        set(value) {
            behavior.draggingAllowed = value
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.mapbox_search_sdk_search_screen_container, this, true)
        screensContainer = findViewById(R.id.screen_container)

        behavior.addOnStateChangedListener { newState, fromUser ->
            updateCardHideable()

            if (behavior.isCollapsed || behavior.isExpanded) {
                previousNonHiddenState = newState
            }

            if (behavior.isCollapsed || behavior.isHidden) {
                hideKeyboard()
            }

            if (behavior.isCollapsed) {
                router.findControllerByTag<MainScreenViewController>(MainScreenViewController.TAG)?.let {
                    // If the view is not attached now and we receive the new event, it may be lost
                    // because the view will try to restore its previous state on the next onRestoreInstanceState() call
                    if (!it.isAttached) {
                        it.ignoreNextRestoreInstanceState()
                    }
                }
                router.popToRoot()
            }

            @BottomSheetState
            val bottomSheetState = fromBottomSheetState(newState)
            onBottomSheetStateChangedListeners.forEach { it.onStateChanged(bottomSheetState, fromUser) }
        }
    }

    /**
     * Initializes inner state of [SearchBottomSheetView]. Mandatory method for proper work.
     */
    public fun initializeSearch(savedInstanceState: Bundle?, configuration: Configuration) {
        check(!isInitialized) {
            "Already initialized"
        }
        isInitialized = true

        router = Conductor.attachRouter(context.unwrapActivityOrNull()!!, screensContainer, savedInstanceState)

        // TODO(https://github.com/mapbox/mapbox-search-android/issues/16) replace Conductor with more suitable for UI SDK case mechanism
        // Regardless of passed savedInstanceState, conductor stores it's state during activity lifecycle.
        // If savedInstanceState is null here and router already has a root controller,
        // most probably this is an integration bug (developers didn't provide savedInstanceState) or
        // this view is used in multiple fragments within one activity.
        // In first case developers will probably fix it when notice,
        // in seconds case we can't guarantee proper state restoration of views so we are going to recreate them
        if (savedInstanceState == null && router.hasRootController()) {
            router.resetControllers()
        }

        if (!router.hasRootController()) {
            val mainScreenViewController = MainScreenViewController(configuration, searchOptions)
            bindMainScreenControllerEvents(mainScreenViewController)
            val transaction = RouterTransaction.with(mainScreenViewController)
                .tag(MainScreenViewController.TAG)
            router.setRoot(transaction)
        } else {
            // After state restoration, we can't guarantee that the search session is still alive,
            // so we can't continue the search from the saved search suggestion store in CategorySuggestionSearchView.
            // In this case we return user to the main search screen with restored search query.
            if (savedInstanceState != null && router.getControllerWithTag(CategorySuggestionSearchViewController.TAG) != null) {
                router.popToRootImmediately()
            }

            val mainScreenController = router.findControllerByTag<MainScreenViewController>(MainScreenViewController.TAG)
            if (mainScreenController == null) {
                failDebug { "Can't restore main screen controller" }
            } else {
                mainScreenController.configuration = configuration
                mainScreenController.searchOptions = searchOptions
                bindMainScreenControllerEvents(mainScreenController)
            }

            router.findControllerByTag<SearchFeedbackViewController>(SearchFeedbackViewController.TAG)?.let {
                bindSearchFeedbackViewController(it)
            }
        }
    }

    // If card is hidden it means that it was hidden intentionally by users and shouldn't change its state by internal triggers
    private fun changeCardStateIfNonHidden(@BottomSheetBehavior.State newState: Int) {
        if (!behavior.isHidden) {
            setBottomSheetState(newState)
        }
    }

    private fun setBottomSheetState(@BottomSheetBehavior.State newState: Int) {
        behavior.isHideable = when (newState) {
            BottomSheetBehavior.STATE_HIDDEN -> true
            else -> isHideableByDrag
        }
        behavior.state = newState
    }

    private fun updateCardHideable() {
        if (behavior.isCollapsed || behavior.isExpanded) {
            behavior.isHideable = isHideableByDrag
        }
        // Or else do nothing as the card is in intermediate state, or has been hidden programmatically,
        // or by a user and shouldn't change its isHideable property.
        // Property will be updated in the next state change event.
    }

    private fun bindMainScreenControllerEvents(controller: MainScreenViewController) {
        controller.cardStateListener = object : CardStateListener {
            override fun onExpandCard() {
                changeCardStateIfNonHidden(BottomSheetBehavior.STATE_EXPANDED)
            }

            override fun onCollapseCard() {
                changeCardStateIfNonHidden(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
        controller.onCategoryClickListener = { category ->
            onCategoryClickListeners.forEach { it.onCategoryClick(category) }
        }
        controller.onSearchResultClickListener = { searchResult, responseInfo ->
            onSearchResultClickListeners.forEach { it.onSearchResultClick(searchResult, responseInfo) }
        }
        controller.onHistoryItemClickListener = { historyRecord ->
            onHistoryClickListeners.forEach { it.onHistoryClick(historyRecord) }
        }
        controller.onFavoriteClickListener = { favorite ->
            onFavoriteClickListeners.forEach { it.onFavoriteClick(favorite) }
        }
        controller.onFeedbackClickListener = { responseInfo ->
            val processed = onOnFeedbackClickListeners.any { it.onMissingResultClick(responseInfo) }
            if (!processed) {
                val feedbackMode = SearchFeedbackView.FeedbackMode.MissingResult(responseInfo)

                val feedbackViewController = SearchFeedbackViewController(feedbackMode)
                bindSearchFeedbackViewController(feedbackViewController)

                val transaction = RouterTransaction.with(feedbackViewController)
                    .tag(SearchFeedbackViewController.TAG)
                    .withHorizontalAnimation()

                router.pushController(transaction)
            }
        }
    }

    private fun bindSearchFeedbackViewController(controller: SearchFeedbackViewController) {
        controller.callback = object : SearchFeedbackView.Callback {
            override fun onSendMissingResultFeedback(text: String, responseInfo: ResponseInfo) {
                val processed = onFeedbackSubmitClickListeners.any {
                    it.onSendMissingResultFeedback(text, responseInfo)
                }

                if (!processed) {
                    defaultOnFeedbackSubmitCallback.onSendMissingResultFeedback(text, responseInfo)
                }

                router.popCurrentController()
            }

            override fun onSendIncorrectResultFeedback(
                reason: String,
                text: String,
                feedback: IncorrectSearchPlaceFeedback
            ) {
                failDebug { "Shouldn't be called in SearchBottomSheetView context" }
            }

            override fun onBackClick() {
                router.popCurrentController()
            }

            override fun onCloseClick() {
                changeCardStateIfNonHidden(BottomSheetBehavior.STATE_COLLAPSED)
            }
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
        if (!::router.isInitialized || !router.hasRootController()) {
            return
        }
        router.findControllerByTag<MainScreenViewController>(MainScreenViewController.TAG)
            ?.mainScreenView()?.cardPeekHeight()?.let { peekHeight ->
                if (peekHeight > 0) {
                    behavior.peekHeight = peekHeight + context.getPixelSize(R.dimen.mapbox_search_sdk_search_view_margin_top)
                }
            }
    }

    /**
     * Switch [SearchBottomSheetView] to expanded state.
     * @see hide
     * @see open
     * @see restorePreviousNonHiddenState
     */
    public fun expand() {
        setBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
    }

    /**
     * Switch [SearchBottomSheetView] to collapsed state, but when part of it is still visible.
     * @see expand
     * @see hide
     * @see restorePreviousNonHiddenState
     */
    public fun open() {
        setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED)
    }

    /**
     * Switch [SearchBottomSheetView] to hidden state, when 0% of it is visible on the screen.
     * @see expand
     * @see open
     * @see restorePreviousNonHiddenState
     */
    public fun hide() {
        setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
    }

    /**
     * Check if view is hidden in current moment.
     * @return true, if [SearchBottomSheetView] is in hidden state.
     */
    public fun isHidden(): Boolean = behavior.isHidden

    /**
     * Restores previous view state if [SearchBottomSheetView] is currently in hidden state.
     * @see expand
     * @see hide
     * @see open
     */
    public fun restorePreviousNonHiddenState() {
        if (behavior.isHidden) {
            setBottomSheetState(previousNonHiddenState)
        }
    }

    /**
     * Back button handler.
     * @return true if a click has been processed.
     */
    public fun handleOnBackPressed(): Boolean {
        return when {
            behavior.isHidden -> false
            // Fix for Router, it detaches current view even though it doesn't pop it
            router.backstackSize > 1 && router.handleBack() -> true
            behavior.isExpanded -> {
                setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED)
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
            peekHeight = behavior.peekHeight,
            previousNonHiddenState = previousNonHiddenState,
            isHideableByDrag = isHideableByDrag,
            searchMode = searchMode,
            searchOptions = searchOptions,
            baseState = super.onSaveInstanceState()
        )
    }

    /**
     * @suppress
     */
    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? SavedState
        super.onRestoreInstanceState(savedState?.superState)
        savedState?.let {
            searchMode = it.searchMode
            searchOptions = it.searchOptions
            isHideableByDrag = it.isHideableByDrag
            previousNonHiddenState = it.previousNonHiddenState
            setBottomSheetState(it.cardState)
            behavior.peekHeight = it.peekHeight
        }
    }

    internal fun addOnSearchModeChangedListener(listener: (SearchMode) -> Unit) {
        onSearchModeChangedListeners.add(listener)
    }

    internal fun removeOnSearchModeChangedListener(listener: (SearchMode) -> Unit) {
        onSearchModeChangedListeners.remove(listener)
    }

    /**
     * Adds a listener to be notified of "Missing feedback?" button clicks.
     *
     * @param listener The listener to notify when "feedback" button clicked.
     */
    public fun addOnFeedbackClickListener(listener: OnFeedbackClickListener) {
        onOnFeedbackClickListeners.add(listener)
    }

    /**
     * Removes a previously added [OnFeedbackClickListener].
     *
     * @param listener The listener to remove.
     */
    public fun removeOnFeedbackClickListener(listener: OnFeedbackClickListener) {
        onOnFeedbackClickListeners.remove(listener)
    }

    /**
     * Adds a listener to be notified of "feedback submit" button click.
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
     * Adds a listener to be notified of category clicks.
     *
     * @param listener The listener to notify when category clicked.
     */
    public fun addOnCategoryClickListener(listener: OnCategoryClickListener) {
        onCategoryClickListeners.add(listener)
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeOnCategoryClickListener(listener: OnCategoryClickListener) {
        onCategoryClickListeners.remove(listener)
    }

    /**
     * Adds a listener to be notified of clicks on search result.
     *
     * @param listener The listener to notify when search results clicked.
     */
    public fun addOnSearchResultClickListener(listener: OnSearchResultClickListener) {
        onSearchResultClickListeners.add(listener)
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeOnSearchResultClickListener(listener: OnSearchResultClickListener) {
        onSearchResultClickListeners.remove(listener)
    }

    /**
     * Adds a listener to be notified of clicks on history records.
     *
     * @param listener The listener to notify when history record clicked.
     */
    public fun addOnHistoryClickListener(listener: OnHistoryClickListener) {
        onHistoryClickListeners.add(listener)
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeOnHistoryClickListener(listener: OnHistoryClickListener) {
        onHistoryClickListeners.remove(listener)
    }

    /**
     * Adds a listener to be notified of clicks on favorites.
     *
     * @param listener The listener to notify when favorite clicked.
     */
    public fun addOnFavoriteClickListener(listener: OnFavoriteClickListener) {
        onFavoriteClickListeners.add(listener)
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeOnFavoriteClickListener(listener: OnFavoriteClickListener) {
        onFavoriteClickListeners.remove(listener)
    }

    /**
     * Interface for a listener to be invoked when "Missing result?" button is clicked.
     */
    public fun interface OnFeedbackClickListener {

        /**
         * Called when "Missing result?" button is clicked.
         * This allows listeners to get a chance to process click on their own.
         *
         * [SearchBottomSheetView] will not process the click if this function returns true.
         * If multiple listeners are registered and any of them has processed the click,
         * all the remaining listeners will not be invoked.
         *
         * By default, [SearchBottomSheetView] will open a window where a user can add additional feedback text.
         * If a [OnFeedbackClickListener] that returns true is added, [OnFeedbackSubmitClickListener] won't be triggered.
         *
         * @param responseInfo Information about response, in which user couldn't find appropriate POI / place.
         *
         * @return True if the listener has processed the click, false otherwise.
         */
        public fun onMissingResultClick(responseInfo: ResponseInfo): Boolean
    }

    /**
     * Interface for a listener to be invoked when "Feedback submit" button is clicked.
     * If a [OnFeedbackClickListener] that returns true is added, [OnFeedbackSubmitClickListener] won't be triggered.
     */
    public fun interface OnFeedbackSubmitClickListener {

        /**
         * Called when "Feedback submit" button is clicked.
         * This allows listeners to get a chance to process click on their own.
         *
         * [SearchBottomSheetView] will not process the click if this function returns true.
         * If multiple listeners are registered and any of them has processed the click,
         * all the remaining listeners will not be invoked.
         *
         * By default, [SearchBottomSheetView] will submit feedback info to a corresponding function
         * [com.mapbox.search.analytics.AnalyticsService.sendMissingResultFeedback].
         *
         * @param text User's feedback text.
         * @param responseInfo Information about response, in which user couldn't find appropriate POI / place.
         *
         * @return True if the listener has processed the click, false otherwise.
         */
        public fun onSendMissingResultFeedback(text: String, responseInfo: ResponseInfo): Boolean
    }

    /**
     * Listener for watching bottom sheet state changes events.
     * @see addOnBottomSheetStateChangedListener
     * @see removeOnBottomSheetStateChangedListener
     */
    public fun interface OnBottomSheetStateChangedListener {

        /**
         * Called when the bottom sheet changes its state.
         * @param newState The new state.
         * @param fromUser true if the change event was triggered by a user, for example, by card swipe.
         */
        public fun onStateChanged(@BottomSheetState newState: Int, fromUser: Boolean)
    }

    /**
     * Listener for category item click processing.
     * @see addOnCategoryClickListener
     * @see removeOnCategoryClickListener
     */
    public fun interface OnCategoryClickListener {

        /**
         * Called, when category item, represented by [Category], clicked.
         */
        public fun onCategoryClick(category: Category)
    }

    /**
     * Listener for search result item click processing.
     * @see addOnSearchResultClickListener
     * @see removeOnSearchResultClickListener
     */
    public fun interface OnSearchResultClickListener {

        /**
         * Called, when search result item clicked.
         */
        public fun onSearchResultClick(searchResult: SearchResult, responseInfo: ResponseInfo)
    }

    /**
     * Interface definition for a listener to be invoked when a [HistoryRecord] is clicked.
     *
     * @see addOnHistoryClickListener
     * @see removeOnHistoryClickListener
     */
    public fun interface OnHistoryClickListener {

        /**
         * Called when a [HistoryRecord] has been clicked.
         */
        public fun onHistoryClick(history: HistoryRecord)
    }

    /**
     * Interface definition for a listener to be invoked when a [FavoriteRecord] is clicked.
     *
     * @see addOnFavoriteClickListener
     * @see removeOnFavoriteClickListener
     */
    public fun interface OnFavoriteClickListener {

        /**
         * Called when a [FavoriteRecord] has been clicked.
         */
        public fun onFavoriteClick(favorite: FavoriteRecord)
    }

    /**
     * Anchor for the bottom sheet collapsed state, defines height of the bottom sheet when it is collapsed.
     */
    public enum class CollapsedStateAnchor {
        /**
         * When the bottom sheet is in collapsed state, only search bar is visible.
         */
        SEARCH_BAR,

        /**
         * When the bottom sheet is in collapsed state, search bar and hot categories are visible.
         */
        HOT_CATEGORIES
    }

    @Parcelize
    private class SavedState(
        @BottomSheetBehavior.State val cardState: Int,
        val peekHeight: Int,
        val previousNonHiddenState: Int,
        val isHideableByDrag: Boolean,
        val searchOptions: SearchOptions,
        val searchMode: SearchMode,
        private val baseState: Parcelable?
    ) : BaseSavedState(baseState)

    /**
     * [SearchBottomSheetView] configuration class.
     */
    @Parcelize
    public class Configuration(

        /**
         * [CollapsedStateAnchor] to define anchor for the bottom sheet collapsed state.
         */
        public val collapsedStateAnchor: CollapsedStateAnchor = DEFAULT_ANCHOR,

        /**
         * [Category] items to show on hot categories panel.
         */
        public val hotCategories: List<Category> = DEFAULT_HOT_CATEGORIES,

        /**
         * [FavoriteTemplate] items to show in favorites list.
         * Each of item is always shown in favorites list even if coordinate and address are not set.
         * These items can not be deleted from UI by user.
         */
        public val favoriteTemplates: List<FavoriteTemplate> = DEFAULT_FAVORITE_TEMPLATES,

        /**
         * Other common configuration options used for Search SDK views.
         */
        public val commonSearchViewConfiguration: CommonSearchViewConfiguration = CommonSearchViewConfiguration(),
    ) : Parcelable {

        private constructor(builder: Builder) : this(
            collapsedStateAnchor = builder.collapsedStateAnchor,
            hotCategories = builder.hotCategories,
            favoriteTemplates = builder.favoriteTemplates,
            commonSearchViewConfiguration = builder.commonSearchViewConfiguration,
        )

        /**
         * Builder for comfortable creation of [Configuration] instance.
         */
        public class Builder {

            /**
             * [CollapsedStateAnchor] to define anchor for the bottom sheet collapsed state.
             */
            public var collapsedStateAnchor: CollapsedStateAnchor = DEFAULT_ANCHOR
                private set

            /**
             * [Category] items to show on hot categories panel.
             */
            public var hotCategories: List<Category> = DEFAULT_HOT_CATEGORIES
                private set

            /**
             * [FavoriteTemplate] items to show in favorites list. Each of item is always shown in favorites list even if coordinate and address are not set. These items can not be deleted from UI by user.
             */
            public var favoriteTemplates: List<FavoriteTemplate> = DEFAULT_FAVORITE_TEMPLATES
                private set

            /**
             * Other common configuration options used for Search SDK views.
             */
            public var commonSearchViewConfiguration: CommonSearchViewConfiguration = CommonSearchViewConfiguration()
                private set

            /**
             * Defines [CollapsedStateAnchor] anchor for the bottom sheet collapsed state.
             */
            public fun setCollapsedStateAnchor(collapsedStateAnchor: CollapsedStateAnchor): Builder = apply {
                this.collapsedStateAnchor = collapsedStateAnchor
            }

            /**
             * Defines [Category] items to show on hot categories panel.
             */
            public fun hotCategories(hotCategories: List<Category>): Builder = apply { this.hotCategories = hotCategories }

            /**
             * Defines [FavoriteTemplate] items to show in favorites list.
             */
            public fun favoriteTemplates(favoriteTemplates: List<FavoriteTemplate>): Builder = apply {
                this.favoriteTemplates = favoriteTemplates
            }

            /**
             * Defines other common configuration options used for Search SDK views.
             */
            public fun commonSearchViewConfiguration(
                commonSearchViewConfiguration: CommonSearchViewConfiguration
            ): Builder = apply {
                this.commonSearchViewConfiguration = commonSearchViewConfiguration
            }

            /**
             * Create [Configuration] instance from builder data.
             */
            public fun build(): Configuration = Configuration(this)
        }

        private companion object {

            val DEFAULT_ANCHOR = CollapsedStateAnchor.HOT_CATEGORIES

            val DEFAULT_HOT_CATEGORIES: List<Category> = listOf(
                Category.GAS_STATION,
                Category.ATM,
                Category.COFFEE_SHOP_CAFE,
                Category.GYM_FITNESS
            )

            val DEFAULT_FAVORITE_TEMPLATES: List<FavoriteTemplate> = listOf(
                FavoriteTemplate(
                    id = HOME_DEFAULT_TEMPLATE_ID,
                    nameId = R.string.mapbox_search_sdk_favorite_template_home_name,
                    resourceId = R.drawable.mapbox_search_sdk_ic_favorite_home
                ),
                FavoriteTemplate(
                    id = WORK_DEFAULT_TEMPLATE_ID,
                    nameId = R.string.mapbox_search_sdk_favorite_template_work_name,
                    resourceId = R.drawable.mapbox_search_sdk_ic_favorite_work
                )
            )
        }
    }

    /**
     * State of the bottom sheet.
     */
    @IntDef(HIDDEN, COLLAPSED, EXPANDED, DRAGGING, SETTLING)
    @Retention(SOURCE)
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
         * Collapsed state.
         */
        public const val COLLAPSED: Int = 2

        /**
         * Expanded state.
         */
        public const val EXPANDED: Int = 3

        /**
         * Dragging state.
         */
        public const val DRAGGING: Int = 4

        /**
         * Settling state.
         */
        public const val SETTLING: Int = 5

        @BottomSheetState
        @JvmSynthetic
        internal fun fromBottomSheetState(@BottomSheetBehavior.State state: Int): Int {
            return when (state) {
                BottomSheetBehavior.STATE_COLLAPSED, BottomSheetBehavior.STATE_HALF_EXPANDED -> COLLAPSED
                BottomSheetBehavior.STATE_EXPANDED -> EXPANDED
                BottomSheetBehavior.STATE_DRAGGING -> DRAGGING
                BottomSheetBehavior.STATE_SETTLING -> SETTLING
                BottomSheetBehavior.STATE_HIDDEN -> HIDDEN
                else -> throw IllegalStateException("Unprocessed state: $state")
            }
        }
    }
}
