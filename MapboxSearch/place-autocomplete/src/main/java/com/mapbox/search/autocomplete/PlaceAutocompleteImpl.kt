package com.mapbox.search.autocomplete

import android.app.Application
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.MapboxApiClient
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreEngineOptions
import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.core.createCoreReverseGeoOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.core.getUserActivityReporter
import com.mapbox.search.base.engine.TwoStepsToOneStepSearchEngineAdapter
import com.mapbox.search.base.location.LocationEngineAdapter
import com.mapbox.search.base.location.WrapperLocationProvider
import com.mapbox.search.base.record.IndexableRecordResolver
import com.mapbox.search.base.record.SearchHistoryService
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.utils.UserAgentProvider
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.internal.bindgen.QueryType
import com.mapbox.search.internal.bindgen.UserActivityReporterInterface
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class PlaceAutocompleteImpl(
    override val accessToken: String,
    private val searchEngine: TwoStepsToOneStepSearchEngineAdapter,
    private val activityReporter: UserActivityReporterInterface,
    private val resultFactory: PlaceAutocompleteResultFactory = PlaceAutocompleteResultFactory()
) : PlaceAutocomplete, MapboxApiClient {

    override suspend fun suggestions(
        query: String,
        region: BoundingBox?,
        proximity: Point?,
        options: PlaceAutocompleteOptions
    ): Expected<Exception, List<PlaceAutocompleteSuggestion>> {
        activityReporter.reportActivity("place-autocomplete-forward-geocoding")

        val coreOptions = createCoreSearchOptions(
            proximity = proximity,
            bbox = region?.mapToCore(),
            countries = options.countries?.map { it.code },
            language = listOf(options.language.code),
            limit = options.limit,
            types = generateCoreTypes(options.types),
            ignoreUR = false,
        )

        return searchEngine.searchResolveImmediately(query = query, coreOptions)
            .mapValue { resultFactory.createPlaceAutocompleteSuggestions(it) }
    }

    override suspend fun suggestions(
        point: Point,
        options: PlaceAutocompleteOptions
    ): Expected<Exception, List<PlaceAutocompleteSuggestion>> {
        activityReporter.reportActivity("place-autocomplete-reverse-geocoding")

        val coreOptions = createCoreReverseGeoOptions(
            point = point,
            countries = options.countries?.map { it.code },
            language = listOf(options.language.code),
            limit = options.limit,
            types = generateCoreTypes(options.types),
        )

        return searchEngine.reverseGeocoding(coreOptions)
            .mapValue { resultFactory.createPlaceAutocompleteSuggestions(it.first) }
    }

    private fun generateCoreTypes(types: List<PlaceAutocompleteType>?): List<QueryType> {
        // We should not leave core types list empty or null in order to avoid unsupported types being requested
        val coreTypes = types?.map { it.coreType }
        return if (coreTypes.isNullOrEmpty()) {
            ALL_TYPES
        } else {
            coreTypes
        }
    }

    internal companion object {

        private val ALL_TYPES = PlaceAutocompleteType.ALL_DECLARED_TYPES.map { it.coreType }

        private val DEFAULT_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "Place Autocomplete executor")
        }

        fun create(
            accessToken: String,
            app: Application,
            locationEngine: LocationEngine,
        ): PlaceAutocompleteImpl {
            val apiType = CoreApiType.SBS

            val coreEngine = CoreSearchEngine(
                CoreEngineOptions(
                    accessToken,
                    null,
                    apiType,
                    UserAgentProvider.userAgent,
                    null
                ),
                WrapperLocationProvider(
                    LocationEngineAdapter(app, locationEngine),
                    null
                ),
            )

            val engine = TwoStepsToOneStepSearchEngineAdapter(
                apiType = apiType,
                coreEngine = coreEngine,
                requestContextProvider = SearchRequestContextProvider(app),
                historyService = SearchHistoryService.STUB,
                searchResultFactory = SearchResultFactory(IndexableRecordResolver.EMPTY),
                engineExecutorService = DEFAULT_EXECUTOR
            )

            return PlaceAutocompleteImpl(
                accessToken = accessToken,
                searchEngine = engine,
                activityReporter = getUserActivityReporter(accessToken)
            )
        }
    }
}
