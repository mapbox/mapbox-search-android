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
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.utils.UserAgentProvider
import com.mapbox.search.base.utils.extension.mapToCore
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class PlaceAutocompleteImpl(
    private val searchEngine: TwoStepsToOneStepSearchEngineAdapter
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
            types = options.administrativeUnits.map { it.coreType },
            ignoreUR = false,
        )

        return searchEngine.searchResolveImmediately(query = query.query, coreOptions)
            .mapValue { it.toPlaceAutocompleteSuggestions() }
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
            types = options.administrativeUnits.map { it.coreType },
        )

        return searchEngine.reverseGeocoding(coreOptions)
            .mapValue { it.first.toPlaceAutocompleteSuggestions() }
    }

    internal companion object {

        private val DEFAULT_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "Place Autocomplete executor")
        }

        private fun List<BaseSearchResult>.toPlaceAutocompleteSuggestions(): List<PlaceAutocompleteSuggestion> {
            return map { it.toPlaceAutocompleteSuggestion() }
        }

        private fun BaseSearchResult.toPlaceAutocompleteSuggestion(): PlaceAutocompleteSuggestion {
            val result = PlaceAutocompleteResult.createFromSearchResult(this)
            return PlaceAutocompleteSuggestion(result)
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
