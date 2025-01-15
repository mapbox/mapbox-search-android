package com.mapbox.search.extensions

import com.mapbox.common.MapboxOptions
import com.mapbox.common.location.LocationProvider
import com.mapbox.navigation.ev.ExperimentalPreviewMapboxEvAPI
import com.mapbox.navigation.ev.model.ChargingStation
import com.mapbox.navigation.ev.search.MapboxEvSearchClientFactory
import com.mapbox.search.ApiType
import com.mapbox.search.CategorySearchOptions
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.base.ExperimentalMapboxSearchAPI
import com.mapbox.search.base.location.defaultLocationProvider
import com.mapbox.search.result.SearchResult

@ExperimentalPreviewMapboxEvAPI
@ExperimentalMapboxSearchAPI
public interface AdvancedCategorySearchEngine {

    public suspend fun search(
        categoryName: String,
        options: CategorySearchOptions,
    ): Result<List<SearchResult>>

    public suspend fun search(
        categoryNames: List<String>,
        options: CategorySearchOptions,
    ): Result<List<SearchResult>>

    /**
     * Companion object.
     */
    public companion object {

        @OptIn(ExperimentalPreviewMapboxEvAPI::class)
        @JvmStatic
        public fun createSearchEngine(
            locationProvider: LocationProvider? = defaultLocationProvider(),
        ): AdvancedCategorySearchEngine {
            val searchEngine = SearchEngine.createSearchEngine(
                ApiType.SEARCH_BOX,
                SearchEngineSettings(locationProvider),
            )

            val evSearchClient = MapboxEvSearchClientFactory.getInstance(MapboxOptions.accessToken)
            return AdvancedCategorySearchEngineImpl(locationProvider,searchEngine, evSearchClient)
        }
    }
}