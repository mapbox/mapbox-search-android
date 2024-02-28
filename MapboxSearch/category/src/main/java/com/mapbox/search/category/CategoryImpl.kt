package com.mapbox.search.category

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
import com.mapbox.search.internal.bindgen.UserActivityReporterInterface
import java.util.concurrent.Executor

internal class CategoryImpl(
    private val engine: CategorySearchEngine,
    private val activityReporter: UserActivityReporterInterface
) : Category {

    override suspend fun search(
        query: CategoryQuery,
        proximity: Point,
        options: CategoryOptions
    ): Expected<Exception, List<CategoryResult>> {
        activityReporter.reportActivity("category-search-nearby")

        val coreOptions = createCoreSearchOptions(
            proximity = proximity,
            limit = options.limit,
            language = listOf(options.language.code),
            ignoreUR = true,
        )
        return search(query, coreOptions)
    }

    override fun search(
        query: CategoryQuery,
        proximity: Point,
        options: CategoryOptions,
        executor: Executor,
        callback: CompletionCallback<List<CategoryResult>>
    ): AsyncOperationTask {
        activityReporter.reportActivity("category-search-nearby")

        val coreOptions = createCoreSearchOptions(
            proximity = proximity,
            limit = options.limit,
            language = listOf(options.language.code),
            ignoreUR = true,
        )
        return search(query, coreOptions, executor, callback)
    }

    override suspend fun search(
        query: CategoryQuery,
        region: BoundingBox,
        proximity: Point?,
        options: CategoryOptions
    ): Expected<Exception, List<CategoryResult>> {
        activityReporter.reportActivity("category-search-in-area")

        val coreOptions = createCoreSearchOptions(
            proximity = proximity,
            bbox = region.mapToCore(),
            limit = options.limit,
            language = listOf(options.language.code),
            ignoreUR = true,
        )
        return search(query, coreOptions)
    }

    override fun search(
        query: CategoryQuery,
        region: BoundingBox,
        proximity: Point?,
        options: CategoryOptions,
        executor: Executor,
        callback: CompletionCallback<List<CategoryResult>>
    ): AsyncOperationTask {
        activityReporter.reportActivity("category-search-in-area")

        val coreOptions = createCoreSearchOptions(
            proximity = proximity,
            bbox = region.mapToCore(),
            limit = options.limit,
            language = listOf(options.language.code),
            ignoreUR = true,
        )
        return search(query, coreOptions, executor, callback)
    }

    override suspend fun search(
        query: CategoryQuery,
        route: List<Point>,
        deviation: RouteDeviationOptions,
        options: CategoryOptions
    ): Expected<Exception, List<CategoryResult>> {
        activityReporter.reportActivity("category-search-along-the-route")

        val coreOptions = createCoreSearchOptions(
            limit = options.limit,
            language = listOf(options.language.code),
            route = route,
            sarType = deviation.sarType?.rawName,
            timeDeviation = deviation.timeDeviationMinutes,
            ignoreUR = true,
        )
        return search(query, coreOptions)
    }

    override fun search(
        query: CategoryQuery,
        route: List<Point>,
        deviation: RouteDeviationOptions,
        options: CategoryOptions,
        executor: Executor,
        callback: CompletionCallback<List<CategoryResult>>
    ): AsyncOperationTask {
        activityReporter.reportActivity("category-search-along-the-route")

        val coreOptions = createCoreSearchOptions(
            limit = options.limit,
            language = listOf(options.language.code),
            route = route,
            sarType = deviation.sarType?.rawName,
            timeDeviation = deviation.timeDeviationMinutes,
            ignoreUR = true,
        )
        return search(query, coreOptions, executor, callback)
    }

    private fun search(
        query: CategoryQuery,
        options: CoreSearchOptions,
        executor: Executor,
        callback: CompletionCallback<List<CategoryResult>>
    ): AsyncOperationTask {
        return engine.search(query.canonicalName, options, executor, object : BaseSearchCallback {
            override fun onResults(
                results: List<BaseSearchResult>,
                responseInfo: BaseResponseInfo
            ) {
                callback.onComplete(results.map { CategoryResult.createFromSearchResult(it) })
            }

            override fun onError(e: Exception) {
                callback.onError(e)
            }
        })
    }

    private suspend fun search(
        query: CategoryQuery,
        options: CoreSearchOptions
    ): Expected<Exception, List<CategoryResult>> {
        return engine.search(query.canonicalName, options).mapValue { value ->
            value.first.map { CategoryResult.createFromSearchResult(it) }
        }
    }
}
