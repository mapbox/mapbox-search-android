package com.mapbox.search.discover

import com.mapbox.bindgen.Expected
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.BaseSearchCallback
import com.mapbox.search.base.core.CoreSearchOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import java.util.concurrent.Executor

internal class DiscoverImpl(private val engine: DiscoverSearchEngine) : Discover {

    override suspend fun search(
        query: DiscoverQuery,
        proximity: Point,
        options: DiscoverOptions
    ): Expected<Exception, List<DiscoverResult>> {
        val coreOptions = createCoreSearchOptions(
            proximity = proximity,
            limit = options.limit,
            language = listOf(options.language.code)
        )
        return search(query, coreOptions)
    }

    override fun search(
        query: DiscoverQuery,
        proximity: Point,
        options: DiscoverOptions,
        executor: Executor,
        callback: CompletionCallback<List<DiscoverResult>>
    ): AsyncOperationTask {
        val coreOptions = createCoreSearchOptions(
            proximity = proximity,
            limit = options.limit,
            language = listOf(options.language.code)
        )
        return search(query, coreOptions, executor, callback)
    }

    override suspend fun search(
        query: DiscoverQuery,
        region: BoundingBox,
        proximity: Point?,
        options: DiscoverOptions
    ): Expected<Exception, List<DiscoverResult>> {
        val coreOptions = createCoreSearchOptions(
            proximity = proximity,
            bbox = region.mapToCore(),
            limit = options.limit,
            language = listOf(options.language.code)
        )
        return search(query, coreOptions)
    }

    override fun search(
        query: DiscoverQuery,
        region: BoundingBox,
        proximity: Point?,
        options: DiscoverOptions,
        executor: Executor,
        callback: CompletionCallback<List<DiscoverResult>>
    ): AsyncOperationTask {
        val coreOptions = createCoreSearchOptions(
            proximity = proximity,
            bbox = region.mapToCore(),
            limit = options.limit,
            language = listOf(options.language.code)
        )
        return search(query, coreOptions, executor, callback)
    }

    override suspend fun search(
        query: DiscoverQuery,
        route: List<Point>,
        deviation: RouteDeviationOptions,
        options: DiscoverOptions
    ): Expected<Exception, List<DiscoverResult>> {
        val coreOptions = createCoreSearchOptions(
            limit = options.limit,
            language = listOf(options.language.code),
            route = route,
            sarType = deviation.sarType?.rawName,
            timeDeviation = deviation.timeDeviationMinutes,
        )
        return search(query, coreOptions)
    }

    override fun search(
        query: DiscoverQuery,
        route: List<Point>,
        deviation: RouteDeviationOptions,
        options: DiscoverOptions,
        executor: Executor,
        callback: CompletionCallback<List<DiscoverResult>>
    ): AsyncOperationTask {
        val coreOptions = createCoreSearchOptions(
            limit = options.limit,
            language = listOf(options.language.code),
            route = route,
            sarType = deviation.sarType?.rawName,
            timeDeviation = deviation.timeDeviationMinutes,
        )
        return search(query, coreOptions, executor, callback)
    }

    private fun search(
        query: DiscoverQuery,
        options: CoreSearchOptions,
        executor: Executor,
        callback: CompletionCallback<List<DiscoverResult>>
    ): AsyncOperationTask {
        return engine.search(query.canonicalName, options, executor, object : BaseSearchCallback {
            override fun onResults(
                results: List<BaseSearchResult>,
                responseInfo: BaseResponseInfo
            ) {
                callback.onComplete(results.map { DiscoverResult.createFromSearchResult(it) })
            }

            override fun onError(e: Exception) {
                callback.onError(e)
            }
        })
    }

    private suspend fun search(
        query: DiscoverQuery,
        options: CoreSearchOptions
    ): Expected<Exception, List<DiscoverResult>> {
        return engine.search(query.canonicalName, options).mapValue { value ->
            value.first.map { DiscoverResult.createFromSearchResult(it) }
        }
    }
}
