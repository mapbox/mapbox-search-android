package com.mapbox.search

import com.mapbox.search.analytics.AnalyticsService
import com.mapbox.search.base.BaseSearchSdkInitializer
import com.mapbox.search.base.StubCompletionCallback
import com.mapbox.search.base.core.CoreEngineOptions
import com.mapbox.search.base.core.CoreLocationProvider
import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.base.core.getUserActivityReporter
import com.mapbox.search.base.location.LocationEngineAdapter
import com.mapbox.search.base.location.WrapperLocationProvider
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.utils.CompoundCompletionCallback
import java.util.concurrent.Executor

internal class SearchEngineFactory {

    @JvmOverloads
    fun createSearchEngineWithBuiltInDataProviders(
        settings: SearchEngineSettings,
        executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
        callback: CompletionCallback<Unit> = StubCompletionCallback()
    ): SearchEngine {
        return createSearchEngineWithBuiltInDataProviders(ApiType.GEOCODING, settings, executor, callback)
    }

    @JvmOverloads
    fun createSearchEngineWithBuiltInDataProviders(
        apiType: ApiType,
        settings: SearchEngineSettings,
        executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
        callback: CompletionCallback<Unit> = StubCompletionCallback()
    ): SearchEngine {
        val coreEngine = createCoreEngineByApiType(apiType, settings)

        val searchEngine = createSearchEngine(
            apiType,
            settings,
            coreEngine,
            MapboxSearchSdk.createAnalyticsService(settings, coreEngine)
        )

        val compoundCallback = CompoundCompletionCallback(2, executor, callback)
        compoundCallback.addInnerTask(
            searchEngine.registerDataProvider(
                ServiceProvider.INSTANCE.historyDataProvider(),
                executor,
                compoundCallback
            )
        )
        compoundCallback.addInnerTask(
            searchEngine.registerDataProvider(
                ServiceProvider.INSTANCE.favoritesDataProvider(),
                executor,
                compoundCallback
            )
        )

        return searchEngine
    }

    fun createSearchEngine(
        apiType: ApiType,
        settings: SearchEngineSettings,
        coreEngine: CoreSearchEngineInterface = createCoreEngineByApiType(apiType, settings),
        analyticsService: AnalyticsService = MapboxSearchSdk.createAnalyticsService(settings, coreEngine),
    ): SearchEngine {
        return SearchEngineImpl(
            apiType,
            settings,
            analyticsService,
            coreEngine,
            getUserActivityReporter(),
            ServiceProvider.INTERNAL_INSTANCE.historyService(),
            MapboxSearchSdk.searchRequestContextProvider,
            MapboxSearchSdk.searchResultFactory,
            indexableDataProvidersRegistry = MapboxSearchSdk.indexableDataProvidersRegistry
        )
    }

    private fun createCoreEngineByApiType(
        apiType: ApiType,
        settings: SearchEngineSettings
    ): CoreSearchEngineInterface {
        val baseUrl = when (apiType) {
            ApiType.GEOCODING -> settings.geocodingEndpointBaseUrl
            ApiType.SBS -> settings.singleBoxSearchBaseUrl
            ApiType.SEARCH_BOX -> settings.searchBoxEndpointUrl
        }

        // Workaround for sync location provider in test environment.
        // Needed while https://github.com/mapbox/mapbox-search-sdk/issues/671 not fixed
        val coreLocationProvider = if (settings.locationProvider is CoreLocationProvider) {
            settings.locationProvider
        } else {
            WrapperLocationProvider(
                LocationEngineAdapter(
                    BaseSearchSdkInitializer.app,
                    settings.locationProvider
                )
            ) {
                settings.viewportProvider?.getViewport()?.mapToCore()
            }
        }

        return CoreSearchEngine(
            CoreEngineOptions(
                baseUrl = baseUrl,
                apiType = apiType.mapToCore(),
                sdkInformation = BaseSearchSdkInitializer.sdkInformation,
                eventsUrl = null,
            ),
            coreLocationProvider
        )
    }
}
