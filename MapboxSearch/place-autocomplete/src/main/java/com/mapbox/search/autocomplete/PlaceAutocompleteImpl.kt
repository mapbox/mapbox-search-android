package com.mapbox.search.autocomplete

import android.app.Application
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreEngineOptions
import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.core.createCoreReverseGeoOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.engine.TwoStepsToOneStepSearchEngineAdapter
import com.mapbox.search.base.location.LocationEngineAdapter
import com.mapbox.search.base.location.WrapperLocationProvider
import com.mapbox.search.base.record.IndexableRecordResolver
import com.mapbox.search.base.record.SearchHistoryService
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.utils.UserAgentProvider
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.internal.bindgen.QueryType
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class PlaceAutocompleteImpl(
    private val searchEngine: TwoStepsToOneStepSearchEngineAdapter,
    private val resultFactory: PlaceAutocompleteResultFactory = PlaceAutocompleteResultFactory()
) : PlaceAutocomplete {

    override suspend fun suggestions(
        query: TextQuery,
        options: PlaceAutocompleteOptions
    ): Expected<Exception, List<PlaceAutocompleteSuggestion>> {
        val coreOptions = createCoreSearchOptions(
            bbox = query.boundingBox?.mapToCore(),
            countries = options.countries?.map { it.code },
            language = listOf(options.language.code),
            limit = options.limit,
            types = generateCoreTypes(options.administrativeUnits),
            ignoreUR = false,
        )

        return searchEngine.searchResolveImmediately(query = query.query, coreOptions)
            .mapValue { resultFactory.createPlaceAutocompleteSuggestions(it) }
    }

    override suspend fun suggestions(
        point: Point,
        options: PlaceAutocompleteOptions
    ): Expected<Exception, List<PlaceAutocompleteSuggestion>> {
        val coreOptions = createCoreReverseGeoOptions(
            point = point,
            countries = options.countries?.map { it.code },
            language = listOf(options.language.code),
            limit = options.limit,
            types = generateCoreTypes(options.administrativeUnits),
        )

        return searchEngine.reverseGeocoding(coreOptions)
            .mapValue { resultFactory.createPlaceAutocompleteSuggestions(it.first) }
    }

    private fun generateCoreTypes(administrativeUnits: List<AdministrativeUnit>?): List<QueryType> {
        // We should not leave core types list empty or null in order to avoid unsupported types being requested
        val coreTypes = administrativeUnits?.map { it.coreType }
        return if (coreTypes.isNullOrEmpty()) {
            ALL_ADMINISTRATIVE_UNITS
        } else {
            coreTypes
        }
    }

    internal companion object {

        private val ALL_ADMINISTRATIVE_UNITS = AdministrativeUnit.values().toList().map { it.coreType }

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

            return PlaceAutocompleteImpl(engine)
        }
    }
}
