package com.mapbox.search.base.tests_support

import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreBoundingBox
import com.mapbox.search.base.core.CoreQueryType
import com.mapbox.search.base.core.CoreRequestOptions
import com.mapbox.search.base.core.CoreResultAccuracy
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.CoreResultType
import com.mapbox.search.base.core.CoreReverseGeoOptions
import com.mapbox.search.base.core.CoreReverseMode
import com.mapbox.search.base.core.CoreRoutablePoint
import com.mapbox.search.base.core.CoreSearchAddress
import com.mapbox.search.base.core.CoreSearchOptions
import com.mapbox.search.base.core.CoreSearchResponse
import com.mapbox.search.base.core.CoreSearchResponseError
import com.mapbox.search.base.core.CoreSearchResult
import com.mapbox.search.base.core.CoreSuggestAction
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.result.BaseSearchAddress
import com.mapbox.search.base.result.BaseSuggestAction
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.internal.bindgen.ConnectionError
import com.mapbox.search.internal.bindgen.HttpError
import com.mapbox.search.internal.bindgen.InternalError
import com.mapbox.search.internal.bindgen.RequestCancelled
import com.mapbox.search.internal.bindgen.ResultType
import java.util.HashMap

@Suppress("LongParameterList")
internal fun createTestBaseRawSearchResult(
    id: String = "id_test_search_result",
    types: List<BaseRawResultType> = listOf(BaseRawResultType.POI),
    names: List<String> = listOf("Test Search Result"),
    languages: List<String> = listOf("def"),
    addresses: List<BaseSearchAddress>? = null,
    descriptionAddress: String? = null,
    matchingName: String? = null,
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

internal fun createTestCoreSearchOptions(): CoreSearchOptions {
    return CoreSearchOptions(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        null,
        null,
        null,
        null,
        null,
        null,
    )
}

@Suppress("LongParameterList")
internal fun createTestCoreRequestOptions(
    query: String = "",
    endpoint: String = "suggest",
    options: CoreSearchOptions = createTestCoreSearchOptions(),
    proximityRewritten: Boolean = false,
    originRewritten: Boolean = false,
    sessionID: String = "test-session-id",
) = CoreRequestOptions(
    query = query,
    endpoint = endpoint,
    options = options,
    proximityRewritten = proximityRewritten,
    originRewritten = originRewritten,
    sessionID = sessionID,
)

@Suppress("LongParameterList")
internal fun createTestBaseRequestOptions(
    core: CoreRequestOptions = createTestCoreRequestOptions(),
    requestContext: SearchRequestContext = SearchRequestContext(CoreApiType.SBS),
) = BaseRequestOptions(
    core = core,
    requestContext = requestContext
)

@Suppress("LongParameterList")
internal fun createCoreSearchAddress(
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
) = CoreSearchAddress(
    houseNumber, street, neighborhood, locality, postcode, place, district, region, country
)

@Suppress("LongParameterList")
internal fun createBaseSearchAddress(
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
    houseNumber, street, neighborhood, locality, postcode, place, district, region, country
)

internal fun createTestCoreSearchResponseSuccess(
    request: CoreRequestOptions = createTestBaseRequestOptions().core,
    results: List<CoreSearchResult> = emptyList(),
    responseUUID: String = "test-response-uuid"
) = CoreSearchResponse(
    request, ExpectedFactory.createValue(results), responseUUID
)

internal fun createTestCoreSearchResponseHttpError(
    httpCode: Int = 400,
    message: String = "error",
    request: CoreRequestOptions = createTestBaseRequestOptions().core,
    responseUUID: String = "test-response-uuid",
) = CoreSearchResponse(
    request, ExpectedFactory.createError(CoreSearchResponseError(HttpError(httpCode, message))), responseUUID
)

internal fun createTestCoreSearchResponseInternalError(
    message: String = "Test internal error",
    request: CoreRequestOptions = createTestBaseRequestOptions().core,
    responseUUID: String = "test-response-uuid",
) = CoreSearchResponse(
    request, ExpectedFactory.createError(CoreSearchResponseError(InternalError(message))), responseUUID
)

internal fun createTestCoreSearchResponseConnectionError(
    message: String = "Test connection error",
    request: CoreRequestOptions = createTestBaseRequestOptions().core,
    responseUUID: String = "test-response-uuid",
) = CoreSearchResponse(
    request, ExpectedFactory.createError(CoreSearchResponseError(ConnectionError(message))), responseUUID
)

internal fun createTestCoreSearchResponseCancelled(
    reason: String = "Request cancelled",
    request: CoreRequestOptions = createTestBaseRequestOptions().core,
    responseUUID: String = "test-response-uuid",
) = CoreSearchResponse(
    request, ExpectedFactory.createError(CoreSearchResponseError(RequestCancelled(reason))), responseUUID
)

@Suppress("LongParameterList")
internal fun createTestCoreSearchResult(
    id: String = "id_test_core_search_result",
    types: List<CoreResultType> = listOf(ResultType.POI),
    names: List<String> = listOf("Test Search Result"),
    languages: List<String> = listOf("default"),
    addresses: List<CoreSearchAddress>? = null,
    descriptionAddress: String? = null,
    matchingName: String? = null,
    distanceMeters: Double? = null,
    etaMinutes: Double? = null,
    center: Point? = null,
    accuracy: CoreResultAccuracy? = null,
    routablePoints: List<CoreRoutablePoint>? = null,
    categories: List<String>? = null,
    icon: String? = null,
    metadata: CoreResultMetadata? = null,
    externalIDs: Map<String, String>? = null,
    layerId: String? = null,
    userRecordId: String? = null,
    userRecordPriority: Int = 0,
    action: CoreSuggestAction? = null,
    serverIndex: Int? = 0,
) = CoreSearchResult(
    id,
    types,
    names,
    languages,
    addresses,
    descriptionAddress,
    matchingName,
    distanceMeters,
    etaMinutes,
    center,
    accuracy,
    routablePoints,
    categories,
    icon,
    metadata,
    externalIDs?.let { (it as? HashMap<String, String>) ?: HashMap(it) },
    layerId,
    userRecordId,
    userRecordPriority,
    action,
    serverIndex
)

@Suppress("LongParameterList")
internal fun createTestCoreSuggestAction(
    endpoint: String = "test_endpoint",
    path: String = "test_path",
    query: String? = null,
    body: ByteArray? = null,
    multiRetrievable: Boolean = false,
) = CoreSuggestAction(
    endpoint, path, query, body, multiRetrievable
)

@Suppress("LongParameterList")
internal fun createTestBaseSuggestAction(
    endpoint: String = "test_endpoint",
    path: String = "test_path",
    query: String? = null,
    body: ByteArray? = null,
    multiRetrievable: Boolean = false,
) = BaseSuggestAction(
    endpoint, path, query, body, multiRetrievable
)

@Suppress("LongParameterList")
internal fun createTestCoreSearchOptions(
    proximity: Point? = null,
    origin: Point? = null,
    navProfile: String? = null,
    etaType: String? = null,
    bbox: CoreBoundingBox? = null,
    countries: List<String>? = null,
    fuzzyMatch: Boolean? = null,
    language: List<String>? = null,
    limit: Int? = null,
    types: List<CoreQueryType>? = null,
    ignoreUR: Boolean = false,
    urDistanceThreshold: Double? = null,
    requestDebounce: Int? = null,
    route: List<Point>? = null,
    sarType: String? = null,
    timeDeviation: Double? = null,
    addonAPI: Map<String, String>? = null,
): CoreSearchOptions = CoreSearchOptions(
    proximity,
    origin,
    navProfile,
    etaType,
    bbox,
    countries,
    fuzzyMatch,
    language,
    limit,
    types,
    ignoreUR,
    urDistanceThreshold,
    requestDebounce,
    route,
    sarType,
    timeDeviation,
    addonAPI?.let { it as? HashMap<String, String> ?: HashMap(it) }
)

@Suppress("LongParameterList")
internal fun createTestCoreReverseGeoOptions(
    point: Point,
    reverseMode: CoreReverseMode? = null,
    countries: List<String>? = null,
    language: List<String>? = null,
    limit: Int? = null,
    types: List<CoreQueryType>? = null,
): CoreReverseGeoOptions = CoreReverseGeoOptions(
    point,
    reverseMode,
    countries,
    language,
    limit,
    types,
)

@Suppress("LongParameterList")
internal fun createTestCoreSearchAddress(
    houseNumber: String? = null,
    street: String? = null,
    neighborhood: String? = null,
    locality: String? = null,
    postcode: String? = null,
    place: String? = null,
    district: String? = null,
    region: String? = null,
    country: String? = null,
): CoreSearchAddress = CoreSearchAddress(
    houseNumber,
    street,
    neighborhood,
    locality,
    postcode,
    place,
    district,
    region,
    country
)
