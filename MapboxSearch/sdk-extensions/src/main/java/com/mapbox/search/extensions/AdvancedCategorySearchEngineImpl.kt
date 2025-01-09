package com.mapbox.search.extensions

import android.util.Log
import kotlinx.coroutines.async
import com.mapbox.common.location.LocationProvider
import com.mapbox.navigation.ev.ExperimentalPreviewMapboxEvAPI
import com.mapbox.navigation.ev.model.ChargingStation
import com.mapbox.navigation.ev.search.MapboxEvSearchClient
import com.mapbox.navigation.ev.search.MapboxEvSearchOptions
import com.mapbox.search.CategorySearchOptions
import com.mapbox.search.SearchEngine
import com.mapbox.search.base.ExperimentalMapboxSearchAPI
import com.mapbox.search.extensions.utils.SearchResultFactory
import com.mapbox.search.extensions.utils.categorySearch
import com.mapbox.search.extensions.utils.lastKnownLocation
import com.mapbox.search.extensions.utils.searchChargers
import com.mapbox.search.result.SearchResult
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalPreviewMapboxEvAPI::class, ExperimentalMapboxSearchAPI::class)
internal class AdvancedCategorySearchEngineImpl(
    private val locationProvider: LocationProvider?,
    private val searchEngine: SearchEngine,
    private val evSearchClient: MapboxEvSearchClient,
    private val querySelector: QuerySelector = QuerySelector(),
    private val resultFactory: SearchResultFactory = SearchResultFactory()
) : AdvancedCategorySearchEngine {

    override suspend fun search(
        categoryName: String,
        options: CategorySearchOptions,
    ): Result<List<SearchResult>> {
        return search(categoryName.split(","), options)
    }

    override suspend fun search(
        categoryNames: List<String>,
        options: CategorySearchOptions,
    ): Result<List<SearchResult>> = coroutineScope {
        val fixedCategories = categoryNames.map {
            it.replace(QUERY_REGEX, " ").trim().lowercase()
        }

        var hasEvQuery = false
        val nonEvCategoryNames = fixedCategories.filter {
            if (querySelector.isEvQuery(it)) {
                hasEvQuery = true
                false
            } else {
                true
            }
        }

        val searchEngineResultsDeferred = async {
            searchEngine.categorySearch(nonEvCategoryNames, options).map {
                it.first
            }
        }

        val evFinderResultsDeferred = async {
            if (hasEvQuery) {
                evSearch(options).map { stations ->
                    stations.map {
                        resultFactory.createFromChargingStation(it, options.proximity, options)
                    }
                }
            } else {
                Result.success(emptyList())
            }
        }

        val searchEngineResult = searchEngineResultsDeferred.await()
        val evFinderResult = evFinderResultsDeferred.await()

        if (searchEngineResult.isSuccess && evFinderResult.isSuccess) {
            Result.success(searchEngineResult.getOrEmptyList() + evFinderResult.getOrEmptyList())
        } else if (searchEngineResult.isFailure && evFinderResult.isFailure) {
            val e1 = searchEngineResult.exceptionOrNull()
            val e2 = evFinderResult.exceptionOrNull()
            Result.failure(
                Exception(
                    "Multiple errors occurred during request. " +
                            "1. ${e1?.message}, " +
                            "2. ${e2?.message}",
                    e1,
                )
            )
        } else if (searchEngineResult.isFailure) {
            Log.e(
                LOG_TAG,
                "Swallowed error of the Search Engine",
                searchEngineResult.exceptionOrNull()
            )
            evFinderResult
        } else {
            Log.e(
                LOG_TAG,
                "Swallowed error of the EV charge finder",
                evFinderResult.exceptionOrNull()
            )
            searchEngineResult
        }
    }

    private suspend fun evSearch(options: CategorySearchOptions): Result<List<ChargingStation>> {
        val routeOptions = options.routeOptions
        val bbox = options.boundingBox
        val proximity = options.proximity ?: locationProvider?.lastKnownLocation()

        val evOptions = with(MapboxEvSearchOptions.Builder()) {
            options.limit?.let {
                withResultLimit(it)
            }
            proximity?.let {
                withProximityPointForSorting(proximity)
            }
            this
        }.build()

        return when {
            routeOptions != null -> evSearchClient.searchChargersAlongRoute(
                routeOptions.route,
                evOptions,
            )

            bbox != null -> evSearchClient.searchChargersInBounds(
                bbox.northeast(),
                bbox.southwest(),
                evOptions,
            )

            proximity != null -> evSearchClient.searchChargers(proximity, evOptions)
            else -> Result.failure(
                Exception(
                    "At least one of proximity, route options, or bounding box must be provided"
                )
            )
        }
    }

    private companion object {
        const val LOG_TAG = "AdvancedSearchEngine"
        val QUERY_REGEX = Regex("[-_]")

        fun <T> Result<List<T>>.getOrEmptyList() = getOrNull() ?: emptyList()
    }
}