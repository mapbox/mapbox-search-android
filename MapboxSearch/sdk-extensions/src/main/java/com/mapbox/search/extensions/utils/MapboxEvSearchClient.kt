@file:OptIn(ExperimentalPreviewMapboxEvAPI::class)

package com.mapbox.search.extensions.utils

import com.mapbox.geojson.Point
import com.mapbox.navigation.ev.ExperimentalPreviewMapboxEvAPI
import com.mapbox.navigation.ev.model.ChargingStation
import com.mapbox.navigation.ev.search.MapboxEvSearchClient
import com.mapbox.navigation.ev.search.MapboxEvSearchOptions

@JvmSynthetic
internal suspend fun MapboxEvSearchClient.searchChargers(
    point: Point,
    options: MapboxEvSearchOptions,
): Result<List<ChargingStation>> = searchChargers(
    latitude = point.latitude(),
    longitude = point.longitude(),
    options = options,
)