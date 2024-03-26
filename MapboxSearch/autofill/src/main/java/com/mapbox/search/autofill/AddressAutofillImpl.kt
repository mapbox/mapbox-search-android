package com.mapbox.search.autofill

import android.app.Application
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.geojson.Point
import com.mapbox.search.base.MapboxApiClient
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreEngineOptions
import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.core.createCoreReverseGeoOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.core.getUserActivityReporter
import com.mapbox.search.base.location.LocationEngineAdapter
import com.mapbox.search.base.location.WrapperLocationProvider
import com.mapbox.search.base.record.IndexableRecordResolver
import com.mapbox.search.base.record.SearchHistoryService
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.utils.UserAgentProvider
import com.mapbox.search.base.utils.extension.flatMap
import com.mapbox.search.internal.bindgen.UserActivityReporterInterface
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Temporary implementation of the [AddressAutofill] based on the two-step search.
 */
internal class AddressAutofillImpl(
    override val accessToken: String,
    private val autofillEngine: AutofillSearchEngine,
    private val activityReporter: UserActivityReporterInterface,
    private val resultFactory: AddressAutofillResultFactory = AddressAutofillResultFactory()
) : AddressAutofill, MapboxApiClient {

    override suspend fun reverseGeocoding(
        point: Point,
        options: AddressAutofillOptions
    ): Expected<Exception, List<AddressAutofillResult>> {
        activityReporter.reportActivity("address-autofill-reverse-geocoding")

        val coreOptions = createCoreReverseGeoOptions(
            point = point,
            countries = options.countries?.map { it.code },
            language = options.language?.let { listOf(it.code) },
        )

        return autofillEngine.search(coreOptions).mapValue { (results, _) ->
            results.mapNotNull {
                val expected = resultFactory.createAddressAutofillResultOrNull(it)
                if (expected.isValue) expected.value else null
            }
        }.let { result ->
            if (result.isValue && result.value.isNullOrEmpty()) {
                ExpectedFactory.createError(Exception("No results for point $point"))
            } else {
                result
            }
        }
    }

    override suspend fun suggestions(
        query: Query,
        options: AddressAutofillOptions
    ): Expected<Exception, List<AddressAutofillSuggestion>> {
        activityReporter.reportActivity("address-autofill-forward-geocoding")

        val coreOptions = createCoreSearchOptions(
            countries = options.countries?.map { it.code },
            language = options.language?.let { listOf(it.code) },
            limit = 10,
            ignoreUR = true,
            addonAPI = mapOf("types" to "address", "streets" to "true")
        )

        return autofillEngine.search(query.query, coreOptions).mapValue { (suggestions, _) ->
            resultFactory.createAddressAutofillSuggestions(suggestions)
        }
    }

    override suspend fun select(
        suggestion: AddressAutofillSuggestion
    ): Expected<Exception, AddressAutofillResult> {
        activityReporter.reportActivity("address-autofill-suggestion-select")

        return if (suggestion.underlying == null) {
            ExpectedFactory.createError(Exception("AddressAutofillSuggestion doesn't contain underlying suggestion"))
         } else {
            val baseResult = selectRaw(suggestion.underlying).value
            if (baseResult == null) {
                ExpectedFactory.createError(Exception("No results for suggestion $suggestion"))
            } else {
                resultFactory.createAddressAutofillResultOrNull(baseResult)
            }
        }
    }

    private suspend fun selectRaw(suggestion: BaseSearchSuggestion): Expected<Exception, BaseSearchResult> {
        return autofillEngine.select(suggestion).flatMap {
            when (it) {
                is AutofillSearchEngine.SearchSelectionResponse.Result -> ExpectedFactory.createValue(it.result)
                else -> {
                    // Shouldn't happen because we don't allow suggestions of type Category and Query
                    ExpectedFactory.createError(Exception("Unsupported suggestion type: $suggestion"))
                }
            }
        }
    }

    internal companion object {

        private val DEFAULT_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "AddressAutofill executor")
        }

        fun create(
            accessToken: String,
            app: Application,
            locationEngine: LocationEngine,
        ): AddressAutofillImpl {
            val coreEngine = CoreSearchEngine(
                CoreEngineOptions(
                    accessToken,
                    null,
                    CoreApiType.AUTOFILL,
                    UserAgentProvider.userAgent,
                    null
                ),
                WrapperLocationProvider(
                    LocationEngineAdapter(app, locationEngine),
                    null
                ),
            )

            val engine = AutofillSearchEngine(
                coreEngine = coreEngine,
                requestContextProvider = SearchRequestContextProvider(app),
                historyService = SearchHistoryService.STUB,
                searchResultFactory = SearchResultFactory(IndexableRecordResolver.EMPTY),
                engineExecutorService = DEFAULT_EXECUTOR
            )

            return AddressAutofillImpl(
                accessToken = accessToken,
                autofillEngine = engine,
                activityReporter = getUserActivityReporter(accessToken)
            )
        }
    }
}
