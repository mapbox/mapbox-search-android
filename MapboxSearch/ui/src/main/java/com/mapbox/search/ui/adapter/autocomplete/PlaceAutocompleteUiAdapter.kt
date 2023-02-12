package com.mapbox.search.ui.adapter.autocomplete

import android.Manifest
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteOptions
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.mapbox.search.autocomplete.TextQuery
import com.mapbox.search.base.failDebug
import com.mapbox.search.base.location.defaultLocationEngine
import com.mapbox.search.ui.view.SearchResultAdapterItem
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.UiError
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Helper class that implements search-specific logic over [PlaceAutocomplete]
 * and shows search results on the [SearchResultsView].
 */
public class PlaceAutocompleteUiAdapter(

    /**
     * [SearchResultsView] for displaying search results.
     */
    private val view: SearchResultsView,

    /**
     * Place autocomplete engine.
     */
    private val placeAutocomplete: PlaceAutocomplete,

    /**
     * The mechanism responsible for providing location approximations to the SDK.
     * By default [LocationEngine] is retrieved from [LocationEngineProvider.getBestLocationEngine].
     * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
     * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
     */
    locationEngine: LocationEngine = defaultLocationEngine(),
) {

    private val itemsCreator = PlaceAutocompleteItemsCreator(view.context, locationEngine)

    private val searchListeners = CopyOnWriteArrayList<SearchListener>()

    @Volatile
    private var latestQueryOptions: Pair<TextQuery, PlaceAutocompleteOptions>? = null

    @Volatile
    private var currentRequestJob: Job? = null

    private var searchResultsShown: Boolean = false

    init {
        view.addActionListener(object : SearchResultsView.ActionListener {

            override fun onResultItemClick(item: SearchResultAdapterItem.Result) {
                when (val payload = item.payload) {
                    is PlaceAutocompleteSuggestion -> searchListeners.forEach { it.onSuggestionSelected(payload) }
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
    public suspend fun search(query: TextQuery, options: PlaceAutocompleteOptions = PlaceAutocompleteOptions()) {
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

                val response = placeAutocomplete.suggestions(query, options)
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
         * Called when the [PlaceAutocompleteSuggestion]s are received and displayed on the [view].
         * @param suggestions List of [PlaceAutocompleteSuggestion] shown.
         * @see PlaceAutocomplete.suggestions
         */
        public fun onSuggestionsShown(suggestions: List<PlaceAutocompleteSuggestion>)

        /**
         * Called when a suggestion is selected by a user.
         * @param suggestion The clicked [PlaceAutocompleteSuggestion].
         */
        public fun onSuggestionSelected(suggestion: PlaceAutocompleteSuggestion)

        /**
         * Called when error occurs during the suggestions request.
         * When this happens, error information is displayed on the [view].
         *
         * @param e Exception, occurred during the request.
         * @see PlaceAutocomplete.suggestions
         */
        public fun onError(e: Exception)
    }
}
