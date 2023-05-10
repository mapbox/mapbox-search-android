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
import com.mapbox.search.base.engine.TwoStepsToOneStepSearchEngineAdapter
import com.mapbox.search.base.location.LocationEngineAdapter
import com.mapbox.search.base.location.WrapperLocationProvider
import com.mapbox.search.base.record.IndexableRecordResolver
import com.mapbox.search.base.record.SearchHistoryService
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.utils.UserAgentProvider
import com.mapbox.search.internal.bindgen.UserActivityReporterInterface
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Temporary implementation of the [AddressAutofill] based on the two-step search.
 */
internal class AddressAutofillImpl(
    override val accessToken: String,
    private val searchEngine: TwoStepsToOneStepSearchEngineAdapter,
    private val activityReporter: UserActivityReporterInterface
) : AddressAutofill, MapboxApiClient {

    override suspend fun suggestions(
        point: Point,
        options: AddressAutofillOptions
    ): Expected<Exception, List<AddressAutofillSuggestion>> {
        activityReporter.reportActivity("address-autofill-reverse-geocoding")

        val coreOptions = createCoreReverseGeoOptions(
            point = point,
            countries = options.countries?.map { it.code },
            language = options.language?.let { listOf(it.code) },
        )

        return searchEngine.reverseGeocoding(coreOptions).mapValue { (results, _) ->
            results.toAddressAutofillSuggestions()
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
        )
        return searchEngine.searchResolveImmediately(query.query, coreOptions).mapValue {
            it.toAddressAutofillSuggestions()
        }
    }

    override suspend fun select(
        suggestion: AddressAutofillSuggestion
    ): Expected<Exception, AddressAutofillResult> {
        activityReporter.reportActivity("address-autofill-suggestion-select")

        return ExpectedFactory.createValue(AddressAutofillResult(suggestion, suggestion.address))
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

            val engine = TwoStepsToOneStepSearchEngineAdapter(
                apiType = CoreApiType.AUTOFILL,
                coreEngine = coreEngine,
                requestContextProvider = SearchRequestContextProvider(app),
                historyService = SearchHistoryService.STUB,
                searchResultFactory = SearchResultFactory(IndexableRecordResolver.EMPTY),
                engineExecutorService = DEFAULT_EXECUTOR
            )

            return AddressAutofillImpl(
                accessToken = accessToken,
                searchEngine = engine,
                activityReporter = getUserActivityReporter(accessToken)
            )
        }

        private fun List<BaseSearchResult>.toAddressAutofillSuggestions() = mapNotNull { it.toAddressAutofillSuggestion() }

        private fun BaseSearchResult.toAddressAutofillSuggestion(): AddressAutofillSuggestion? {
            // Filtering incomplete results
            val autofillAddress = AddressComponents.fromCoreSdkAddress(address, metadata) ?: return null

            return AddressAutofillSuggestion(
                name = name,
                formattedAddress = fullAddress ?: autofillAddress.formattedAddress(),
                address = autofillAddress,
                coordinate = coordinate,
            )
        }
    }
}
