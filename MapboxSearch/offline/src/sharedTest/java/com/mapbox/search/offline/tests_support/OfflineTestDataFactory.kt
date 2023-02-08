package com.mapbox.search.offline.tests_support

import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreResultAccuracy
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.CoreRoutablePoint
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.result.BaseSearchAddress
import com.mapbox.search.base.result.BaseSuggestAction

internal fun createTestBaseRawSearchResult(
    id: String = "id_test_search_result",
    types: List<BaseRawResultType> = listOf(BaseRawResultType.POI),
    names: List<String> = listOf("Test Search Result"),
    languages: List<String> = listOf("def"),
    addresses: List<BaseSearchAddress>? = null,
    descriptionAddress: String? = null,
    matchingName: String? = null,
    fullAddress: String? = null,
    distanceMeters: Double? = null,
    center: Point? = null,
    accuracy: CoreResultAccuracy? = null,
    routablePoints: List<CoreRoutablePoint>? = null,
    categories: List<String>? = null,
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
    types = types,
    names = names,
    languages = languages,
    addresses = addresses,
    descriptionAddress = descriptionAddress,
    distanceMeters = distanceMeters,
    matchingName = matchingName,
    fullAddress = fullAddress,
    center = center,
    accuracy = accuracy,
    routablePoints = routablePoints,
    categories = categories,
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

@Suppress("LongParameterList")
internal fun createTestBaseSearchAddress(
    defaultValue: String? = null,
    houseNumber: String? = defaultValue,
    street: String? = defaultValue,
    neighborhood: String? = defaultValue,
    locality: String? = defaultValue,
    postcode: String? = defaultValue,
    place: String? = defaultValue,
    district: String? = defaultValue,
    region: String? = defaultValue,
    country: String? = defaultValue,
) = BaseSearchAddress(
    houseNumber = houseNumber,
    street = street,
    neighborhood = neighborhood,
    locality = locality,
    postcode = postcode,
    place = place,
    district = district,
    region = region,
    country = country
)
