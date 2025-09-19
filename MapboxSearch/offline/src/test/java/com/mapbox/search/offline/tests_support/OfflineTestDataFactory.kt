package com.mapbox.search.offline.tests_support

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreResultAccuracy
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.CoreResultType
import com.mapbox.search.base.core.CoreRoutablePoint
import com.mapbox.search.base.core.CoreSearchAddress
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.result.BaseSuggestAction

internal fun createTestBaseRawSearchResult(
    id: String = "id_test_search_result",
    mapboxId: String? = null,
    types: List<CoreResultType> = listOf(CoreResultType.POI),
    names: List<String> = listOf("Test Search Result"),
    namePreferred: String? = null,
    languages: List<String> = listOf("def"),
    addresses: List<CoreSearchAddress>? = null,
    descriptionAddress: String? = null,
    matchingName: String? = null,
    fullAddress: String? = null,
    distanceMeters: Double? = null,
    center: Point? = null,
    accuracy: CoreResultAccuracy? = null,
    routablePoints: List<CoreRoutablePoint>? = null,
    bbox: BoundingBox? = null,
    categories: List<String>? = null,
    categoryIds: List<String>? = null,
    brand: List<String>? = null,
    brandId: String? = null,
    icon: String? = null,
    metadata: CoreResultMetadata? = null,
    externalIDs: Map<String, String>? = null,
    layerId: String? = null,
    userRecordId: String? = null,
    userRecordPriority: Int = -1,
    action: BaseSuggestAction? = null,
    serverIndex: Int? = 0,
    etaMinutes: Double? = null,
) = BaseRawSearchResult(
    id = id,
    mapboxId = mapboxId,
    types = types,
    names = names,
    namePreferred = namePreferred,
    languages = languages,
    addresses = addresses,
    descriptionAddress = descriptionAddress,
    distanceMeters = distanceMeters,
    matchingName = matchingName,
    fullAddress = fullAddress,
    center = center,
    accuracy = accuracy,
    routablePoints = routablePoints,
    bbox = bbox,
    categories = categories,
    categoryIds = categoryIds,
    brand = brand,
    brandId = brandId,
    icon = icon,
    metadata = metadata,
    externalIDs = externalIDs,
    layerId = layerId,
    userRecordId = userRecordId,
    userRecordPriority = userRecordPriority,
    action = action,
    serverIndex = serverIndex,
    etaMinutes = etaMinutes
)
