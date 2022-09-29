package com.mapbox.search.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.mapbox.search.SearchOptions
import com.mapbox.search.ui.utils.OffsetItemDecoration
import com.mapbox.search.ui.utils.wrapWithSearchTheme
import com.mapbox.search.ui.view.adapter.SearchHistoryViewHolder
import com.mapbox.search.ui.view.adapter.SearchResultViewHolder
import com.mapbox.search.ui.view.adapter.SearchViewResultsAdapter
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Simplified search view.
 *
 * Note that [SearchResultsView.initialize] has to be called in order to make this view work properly.
 */
@Suppress("TooManyFunctions")
public class SearchResultsView @JvmOverloads constructor(
    outerContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(wrapWithSearchTheme(outerContext), attrs, defStyleAttr) {

    /**
     * [SearchOptions] that will be used for search requests.
     */
    public var defaultSearchOptions: SearchOptions = GlobalViewPreferences.DEFAULT_SEARCH_OPTIONS

    private var isInitialized = false

    private lateinit var searchAdapter: SearchViewResultsAdapter

    private val actionListeners = CopyOnWriteArrayList<ActionListener>()

    /**
     * Returns adapter items that are currently shown in this [RecyclerView]
     */
    public val adapterItems: List<SearchResultAdapterItem>
        get() = searchAdapter.items

    private val adapterListener = object : SearchViewResultsAdapter.Listener {

        override fun onHistoryItemClick(item: SearchResultAdapterItem.History) {
            actionListeners.forEach { it.onHistoryItemClick(item) }
        }

        override fun onResultItemClick(item: SearchResultAdapterItem.Result) {
            actionListeners.forEach { it.onResultItemClick(item) }
        }

        override fun onPopulateQueryClick(item: SearchResultAdapterItem.Result) {
            actionListeners.forEach { it.onPopulateQueryClick(item) }
        }

        override fun onMissingResultFeedbackClick(item: SearchResultAdapterItem.MissingResultFeedback) {
            actionListeners.forEach { it.onMissingResultFeedbackClick(item) }
        }

        override fun onErrorItemClick(item: SearchResultAdapterItem.Error) {
            actionListeners.forEach { it.onErrorItemClick(item) }
        }
    }

    /**
     * Initializes the inner state of this view and defines configuration options.
     * It's obligatory to call this method as soon as the view is created.
     *
     * @param configuration Configuration options.
     *
     * @throws [IllegalStateException] if this method has already been called.
     */
    public fun initialize(configuration: Configuration) {
        check(!isInitialized) {
            "Already initialized"
        }
        isInitialized = true

        searchAdapter = SearchViewResultsAdapter(configuration.commonConfiguration.distanceUnitType)

        super.setLayoutManager(LinearLayoutManager(context))
        super.setAdapter(searchAdapter)

        searchAdapter.listener = adapterListener

        (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        addItemDecoration(
            OffsetItemDecoration(
                context,
                applyPredicate = { viewHolder ->
                    when (viewHolder) {
                        is SearchHistoryViewHolder,
                        is SearchResultViewHolder -> true
                        else -> false
                    }
                }
            )
        )
    }

    /**
     * Sets [items] as [RecyclerView.Adapter]'s content.
     * @throws IllegalStateException if [initialize] has not been called before earlier.
     */
    public fun setAdapterItems(items: List<SearchResultAdapterItem>) {
        check(isInitialized) {
            "Initialize this view first"
        }
        searchAdapter.items = items
    }

    /**
     * @suppress
     */
    override fun setLayoutManager(layout: LayoutManager?) {
        throw IllegalStateException("Don't call this function, internal LayoutManager is used")
    }

    /**
     * @suppress
     */
    override fun setAdapter(adapter: Adapter<*>?) {
        throw IllegalStateException("Don't call this function, internal adapter is used")
    }

    /**
     * Adds a listener to be notified of view actions.
     *
     * @param listener The listener to notify when a view action happened.
     */
    public fun addActionListener(listener: ActionListener) {
        actionListeners.add(listener)
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeActionListener(listener: ActionListener) {
        actionListeners.remove(listener)
    }

    /**
     * Search results view action listener.
     */
    public interface ActionListener {

        /**
         * Called when the history item is clicked.
         * @param item The clicked [SearchResultAdapterItem.History] item.
         */
        public fun onHistoryItemClick(item: SearchResultAdapterItem.History)

        /**
         * Called when [RecyclerView.Adapter]'s [SearchResultAdapterItem.Result] item is clicked.
         * @param item The clicked [SearchResultAdapterItem.Result] item.
         */
        public fun onResultItemClick(item: SearchResultAdapterItem.Result)

        /**
         * Called when [SearchResultAdapterItem.Result]'s "Populate query" button is clicked.
         * Note that this function can be called only if [SearchResultAdapterItem.Result.isPopulateQueryVisible] true.
         * @param item The clicked [SearchResultAdapterItem.Result] item.
         */
        public fun onPopulateQueryClick(item: SearchResultAdapterItem.Result)

        /**
         * Called when the "Missing result" button is clicked.
         * @param item The clicked [SearchResultAdapterItem.MissingResultFeedback] item.
         */
        public fun onMissingResultFeedbackClick(item: SearchResultAdapterItem.MissingResultFeedback)

        /**
         * Called when [RecyclerView.Adapter]'s [SearchResultAdapterItem.Error] item (i.e. `Retry` button) is clicked.
         * @param item The clicked [SearchResultAdapterItem.Error] item.
         */
        public fun onErrorItemClick(item: SearchResultAdapterItem.Error)
    }

    /**
     * Options used for [SearchResultsView] configuration.
     * // TODO FIXME do we need this?
     */
    public class Configuration public constructor(

        /**
         * Common configuration options used for Search SDK views.
         */
        public val commonConfiguration: CommonSearchViewConfiguration,
    )
}
