package com.mapbox.search.discover

import com.mapbox.bindgen.Expected
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreSearchOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.utils.extension.mapToCore

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

    private suspend fun search(
        query: DiscoverQuery,
        options: CoreSearchOptions
    ): Expected<Exception, List<DiscoverResult>> {
        return engine.search(query.canonicalName, options).mapValue { value ->
            value.first.map { DiscoverResult.createFromSearchResult(it) }
        }
    }
}
