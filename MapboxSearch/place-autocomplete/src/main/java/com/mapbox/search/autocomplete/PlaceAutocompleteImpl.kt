package com.mapbox.search.autocomplete

import android.app.Application
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion.Underlying
import com.mapbox.search.base.MapboxApiClient
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreEngineOptions
import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.core.createCoreReverseGeoOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.core.getUserActivityReporter
import com.mapbox.search.base.engine.TwoStepsToOneStepSearchEngineAdapter
import com.mapbox.search.base.engine.TwoStepsToOneStepSearchEngineAdapter.SearchSelectionResponse
import com.mapbox.search.base.failDebug
import com.mapbox.search.base.location.LocationEngineAdapter
import com.mapbox.search.base.location.WrapperLocationProvider
import com.mapbox.search.base.record.IndexableRecordResolver
import com.mapbox.search.base.record.SearchHistoryService
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.result.BaseSearchSuggestionType
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.utils.UserAgentProvider
import com.mapbox.search.base.utils.extension.flatMap
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.base.utils.extension.suspendFlatMap
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
            origin = proximity,
            bbox = region?.mapToCore(),
            countries = options.countries?.map { it.code },
            language = listOf(options.language.code),
            limit = options.limit,
            types = generateCoreTypes(options.types),
            navProfile = options.navigationProfile?.rawName,
            etaType = DEFAULT_ETA_TYPE,
            ignoreUR = false,
        )

        return searchEngine.search(query, coreOptions).suspendFlatMap { (rawSuggestions, _) ->
            createSuggestions(rawSuggestions)
        }
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

    override suspend fun select(suggestion: PlaceAutocompleteSuggestion): Expected<Exception, PlaceAutocompleteResult> {
        activityReporter.reportActivity("place-autocomplete-suggestion-select")

        return when (val underlying = suggestion.underlying) {
            is Underlying.Suggestion -> selectRaw(underlying.suggestion).flatMap {
                resultFactory.createPlaceAutocompleteResultOrError(it)
            }
            is Underlying.Result -> resultFactory.createPlaceAutocompleteResultOrError(underlying.result)
        }
    }

    private suspend fun createSuggestions(
        rawSuggestions: List<BaseSearchSuggestion>
    ): Expected<Exception, List<PlaceAutocompleteSuggestion>> {
        // In some cases searchEngine.resolveAll() can be significantly faster than suggestions resolving one by one,
        if (rawSuggestions.all { it.coordinate == null }) {
            return searchEngine.resolveAll(rawSuggestions, allowCategorySuggestions = false).mapValue {
                resultFactory.createPlaceAutocompleteSuggestions(it)
            }
        }

        val suggestions = rawSuggestions
            .mapNotNull { suggestion ->
                val type = when (val suggestionType = suggestion.type) {
                    is BaseSearchSuggestionType.SearchResultSuggestion -> suggestionType.types
                    is BaseSearchSuggestionType.IndexableRecordItem -> {
                        failDebug {
                            "Suggestion of IndexableRecordItem type returned while indexable records was not requested"
                        }
                        null
                    }
                    is BaseSearchSuggestionType.Category,
                    is BaseSearchSuggestionType.Brand,
                    is BaseSearchSuggestionType.Query -> null
                }?.firstNotNullOfOrNull { PlaceAutocompleteType.createFromBaseType(it) } ?: return@mapNotNull null

                val coordinate = suggestion.coordinate
                if (coordinate != null) {
                    ExpectedFactory.createValue(
                        resultFactory.createPlaceAutocompleteSuggestion(coordinate, type, suggestion)
                    )
                } else {
                    selectRaw(suggestion).mapValue {
                        resultFactory.createPlaceAutocompleteSuggestion(type, it)
                    }
                }
            }

        // If at least one response completed successfully, return it.
        return if (suggestions.isNotEmpty() && suggestions.all { it.isError }) {
            ExpectedFactory.createError(requireNotNull(suggestions.first().error))
        } else {
            suggestions.asSequence()
                .mapNotNull { it.value }
                .toList()
                .let {
                    ExpectedFactory.createValue(it)
                }
        }
    }

    private suspend fun selectRaw(suggestion: BaseSearchSuggestion): Expected<Exception, BaseSearchResult> {
        return searchEngine.select(suggestion).flatMap {
            when (it) {
                is SearchSelectionResponse.Result -> ExpectedFactory.createValue(it.result)
                else -> {
                    // Shouldn't happen because we don't allow suggestions of type Category and Query
                    ExpectedFactory.createError(Exception("Unsupported suggestion type: $suggestion"))
                }
            }
        }
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

        private const val DEFAULT_ETA_TYPE = "navigation"

        private val ALL_TYPES = PlaceAutocompleteType.ALL_DECLARED_TYPES.map { it.coreType }

        private val DEFAULT_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "Place Autocomplete executor")
        }

        fun create(
            accessToken: String,
            app: Application,
            locationEngine: LocationEngine,
        ): PlaceAutocompleteImpl {
            val apiType = CoreApiType.SEARCH_BOX

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
