package com.mapbox.search.ui.adapter.autofill

import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.mapbox.search.autofill.AddressAutofill
import com.mapbox.search.autofill.AddressAutofillOptions
import com.mapbox.search.autofill.AddressAutofillSuggestion
import com.mapbox.search.autofill.Query
import com.mapbox.search.base.MapboxApiClient
import com.mapbox.search.base.core.getUserActivityReporter
import com.mapbox.search.base.failDebug
import com.mapbox.search.internal.bindgen.UserActivityReporter
import com.mapbox.search.ui.view.SearchResultAdapterItem
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.UiError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Helper class that implements search-specific logic over [AddressAutofill]
 * and shows search results on the [SearchResultsView].
 */
public class AddressAutofillUiAdapter(

    /**
     * [SearchResultsView] for displaying search results.
     */
    private val view: SearchResultsView,

    /**
     * Address autofill engine.
     */
    private val addressAutofill: AddressAutofill,
) {

    private val itemsCreator = AutofillItemsCreator(view.context)
    private val searchListeners = CopyOnWriteArrayList<SearchListener>()

    @Volatile
    private var latestQueryOptions: Pair<Query, AddressAutofillOptions>? = null

    @Volatile
    private var currentRequestJob: Job? = null

    private var searchResultsShown: Boolean = false

    private val activityReporter: UserActivityReporter? = (addressAutofill as? MapboxApiClient)?.accessToken?.let {
        getUserActivityReporter(it)
    }

    init {
        view.addActionListener(object : SearchResultsView.ActionListener {

            override fun onResultItemClick(item: SearchResultAdapterItem.Result) {
                when (val payload = item.payload) {
                    is AddressAutofillSuggestion -> searchListeners.forEach { it.onSuggestionSelected(payload) }
                    else -> failDebug {
                        "Unknown adapter item payload: $payload"
                    }
                }
            }

            override fun onErrorItemClick(item: SearchResultAdapterItem.Error) {
                view.findViewTreeLifecycleOwner()?.lifecycle?.coroutineScope?.launchWhenStarted {
                    latestQueryOptions?.let { (query, options) ->
                        search(query, options)
                    }
                }
            }

            override fun onHistoryItemClick(item: SearchResultAdapterItem.History) {
                // Should not be called
            }

            override fun onPopulateQueryClick(item: SearchResultAdapterItem.Result) {
                // Should not be called
            }

            override fun onMissingResultFeedbackClick(item: SearchResultAdapterItem.MissingResultFeedback) {
                // Should not be called
            }
        })
    }

    /**
     * Performs suggestions request.
     * @param query The search query.
     * @param options The autofill options.
     */
    @JvmOverloads
    public suspend fun search(query: Query, options: AddressAutofillOptions = AddressAutofillOptions()) {
        activityReporter?.reportActivity("address-autofill-forward-geocoding-ui")

        currentRequestJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }

        latestQueryOptions = query to options

        coroutineScope {
            currentRequestJob = launch {
                withContext(Dispatchers.Main) {
                    if (!searchResultsShown) {
                        view.setAdapterItems(itemsCreator.createForLoading())
                    }
                }

                val response = addressAutofill.suggestions(query, options)
                withContext(Dispatchers.Main) {
                    if (response.isValue) {
                        val suggestions = requireNotNull(response.value)
                        searchResultsShown = true
                        searchListeners.forEach { it.onSuggestionsShown(suggestions) }
                        view.setAdapterItems(itemsCreator.createForSuggestions(suggestions, query.query))
                    } else {
                        val error = requireNotNull(response.error)
                        searchResultsShown = false
                        searchListeners.forEach { it.onError(error) }
                        view.setAdapterItems(itemsCreator.createForError(UiError.createFromException(error)))
                    }
                }
            }
        }
    }

    /**
     * Adds a listener to be notified of search events.
     *
     * @param listener The listener to notify when a search event happened.
     */
    public fun addSearchListener(listener: SearchListener) {
        searchListeners.add(listener)
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeSearchListener(listener: SearchListener) {
        searchListeners.remove(listener)
    }

    /**
     * Search results view listener.
     */
    public interface SearchListener {

        /**
         * Called when the [AddressAutofillSuggestion]s are received and displayed on the [view].
         *
         * @param suggestions List of [AddressAutofillSuggestion] shown.
         * @see AddressAutofill.suggestions
         */
        public fun onSuggestionsShown(suggestions: List<AddressAutofillSuggestion>)

        /**
         * Called when a suggestion is selected by a user.
         *
         * @param suggestion The clicked [AddressAutofillSuggestion].
         */
        public fun onSuggestionSelected(suggestion: AddressAutofillSuggestion)

        /**
         * Called when error occurs during the suggestions request.
         * When this happens, error information is displayed on the [view].
         *
         * @param e Exception, occurred during the request.
         * @see AddressAutofill.suggestions
         */
        public fun onError(e: Exception)
    }
}
