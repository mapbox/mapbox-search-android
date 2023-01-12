package com.mapbox.search.discover

import com.mapbox.bindgen.Expected
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreSearchOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.utils.extension.mapToCore

internal class DiscoverApiImpl(private val engine: DiscoverApiSearchEngine) : DiscoverApi {

    override suspend fun search(
        query: DiscoverApiQuery,
        proximity: Point,
        options: DiscoverApiOptions
    ): Expected<Exception, List<DiscoverApiResult>> {
        val coreOptions = createCoreSearchOptions(
            proximity = proximity,
            limit = options.limit,
            language = listOf(options.language.code)
        )
        return search(query, coreOptions)
    }

    override suspend fun search(
        query: DiscoverApiQuery,
        region: BoundingBox,
        proximity: Point?,
        options: DiscoverApiOptions
    ): Expected<Exception, List<DiscoverApiResult>> {
        val coreOptions = createCoreSearchOptions(
            proximity = proximity,
            bbox = region.mapToCore(),
            limit = options.limit,
            language = listOf(options.language.code)
        )
        return search(query, coreOptions)
    }

    override suspend fun search(
        query: DiscoverApiQuery,
        route: List<Point>,
        deviation: RouteDeviationOptions,
        options: DiscoverApiOptions
    ): Expected<Exception, List<DiscoverApiResult>> {
        val coreOptions = createCoreSearchOptions(
            limit = options.limit,
            language = listOf(options.language.code),
            route = route,
            sarType = deviation.sarType?.rawName,
            timeDeviation = deviation.timeDeviationMinutes,
        )
        return search(query, coreOptions)
    }

    private suspend fun search(
        query: DiscoverApiQuery,
        options: CoreSearchOptions
    ): Expected<Exception, List<DiscoverApiResult>> {
        return engine.search(query.canonicalName, options).mapValue { value ->
            value.first.map { DiscoverApiResult.createFromSearchResult(it) }
        }
    }
}
