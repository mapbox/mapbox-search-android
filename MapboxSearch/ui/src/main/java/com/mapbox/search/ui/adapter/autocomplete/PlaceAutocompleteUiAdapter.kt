package com.mapbox.search.ui.adapter.autocomplete

import android.Manifest
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.mapbox.common.location.LocationService
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteOptions
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.mapbox.search.base.MapboxApiClient
import com.mapbox.search.base.core.getUserActivityReporter
import com.mapbox.search.base.failDebug
import com.mapbox.search.base.location.defaultLocationService
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
     * By default [LocationService] is retrieved from [LocationServiceFactory.getOrCreate].
     * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
     * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
     */
    locationService: LocationService = defaultLocationService(),
) {

    private val itemsCreator = PlaceAutocompleteItemsCreator(view.context, locationService)

    private val searchListeners = CopyOnWriteArrayList<SearchListener>()

    @Volatile
    private var latestQueryOptions: QueryOptions? = null

    @Volatile
    private var currentRequestJob: Job? = null

    private var searchResultsShown: Boolean = false

    private val activityReporter: UserActivityReporter? = (placeAutocomplete as? MapboxApiClient)?.accessToken?.let {
        getUserActivityReporter()
    }

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
                    latestQueryOptions?.let {
                        search(it.query, it.region, it.proximity, it.options)
                    }
                }
            }

            override fun onHistoryItemClick(item: SearchResultAdapterItem.History) {
                // Should not be called
            }

            override fun onPopulateQueryClick(item: SearchResultAdapterItem.Result) {
                when (val payload = item.payload) {
                    is PlaceAutocompleteSuggestion -> searchListeners.forEach { it.onPopulateQueryClick(payload) }
                    else -> failDebug {
                        "Unknown adapter item payload: $payload"
                    }
                }
            }

            override fun onMissingResultFeedbackClick(item: SearchResultAdapterItem.MissingResultFeedback) {
                // Should not be called
            }
        })
    }

    /**
     * Performs suggestions request.
     * @param query The search query.
     * @param region Limit results to only those contained within the supplied bounding box.
     * @param proximity Optional geographic point that bias the response to favor results that are closer to this location.
     * @param options Place autocomplete options.
     */
    @JvmOverloads
    public suspend fun search(
        query: String,
        region: BoundingBox? = null,
        proximity: Point? = null,
        options: PlaceAutocompleteOptions = PlaceAutocompleteOptions()
    ) {
        currentRequestJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }

        latestQueryOptions = QueryOptions(query, region, proximity, options)

        coroutineScope {
            currentRequestJob = launch {
                withContext(Dispatchers.Main) {
                    if (!searchResultsShown) {
                        view.setAdapterItems(itemsCreator.createForLoading())
                    }
                }

                activityReporter?.reportActivity("place-autocomplete-forward-geocoding-ui")

                val response = placeAutocomplete.suggestions(
                    query, region, proximity, options
                )

                withContext(Dispatchers.Main) {
                    if (response.isValue) {
                        val suggestions = requireNotNull(response.value)
                        searchResultsShown = true
                        searchListeners.forEach { it.onSuggestionsShown(suggestions) }
                        view.setAdapterItems(itemsCreator.createForSuggestions(suggestions, query))
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
         * Called when [SearchResultAdapterItem.Result]'s "Populate query" button is clicked.
         * @param suggestion The clicked [PlaceAutocompleteSuggestion].
         */
        public fun onPopulateQueryClick(suggestion: PlaceAutocompleteSuggestion)

        /**
         * Called when error occurs during the suggestions request.
         * When this happens, error information is displayed on the [view].
         *
         * @param e Exception, occurred during the request.
         * @see PlaceAutocomplete.suggestions
         */
        public fun onError(e: Exception)
    }

    private data class QueryOptions(
        val query: String,
        val region: BoundingBox?,
        val proximity: Point?,
        val options: PlaceAutocompleteOptions
    )
}
