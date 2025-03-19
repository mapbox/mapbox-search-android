package com.mapbox.search.common.tests

import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.geojson.Point
import com.mapbox.search.internal.bindgen.AttributeSet
import com.mapbox.search.internal.bindgen.ConnectionError
import com.mapbox.search.internal.bindgen.EvSearchOptions
import com.mapbox.search.internal.bindgen.HttpError
import com.mapbox.search.internal.bindgen.InternalError
import com.mapbox.search.internal.bindgen.LonLatBBox
import com.mapbox.search.internal.bindgen.QueryType
import com.mapbox.search.internal.bindgen.RequestCancelled
import com.mapbox.search.internal.bindgen.RequestOptions
import com.mapbox.search.internal.bindgen.ResultAccuracy
import com.mapbox.search.internal.bindgen.ResultMetadata
import com.mapbox.search.internal.bindgen.ResultType
import com.mapbox.search.internal.bindgen.ReverseGeoOptions
import com.mapbox.search.internal.bindgen.ReverseMode
import com.mapbox.search.internal.bindgen.RoutablePoint
import com.mapbox.search.internal.bindgen.SearchAddress
import com.mapbox.search.internal.bindgen.SearchAddressCountry
import com.mapbox.search.internal.bindgen.SearchAddressRegion
import com.mapbox.search.internal.bindgen.SearchOptions
import com.mapbox.search.internal.bindgen.SearchResponse
import com.mapbox.search.internal.bindgen.SearchResult
import com.mapbox.search.internal.bindgen.SuggestAction

@Suppress("LongParameterList")
fun createTestCoreSearchOptions(
    proximity: Point? = null,
    origin: Point? = null,
    navProfile: String? = null,
    etaType: String? = null,
    bbox: LonLatBBox? = null,
    countries: List<String>? = null,
    fuzzyMatch: Boolean? = null,
    language: List<String>? = null,
    limit: Int? = null,
    types: List<QueryType>? = null,
    ignoreUR: Boolean = false,
    urDistanceThreshold: Double? = null,
    requestDebounce: Int? = null,
    route: List<Point>? = null,
    sarType: String? = null,
    timeDeviation: Double? = null,
    addonAPI: Map<String, String>? = null,
    offlineSearchPlacesOutsideBbox: Boolean = false,
    ensureResultsPerCategory: Boolean? = null,
    attributeSets: List<AttributeSet>? = null,
    evOptions: EvSearchOptions? = null,
): SearchOptions = SearchOptions(
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
    addonAPI?.let { it as? HashMap<String, String> ?: HashMap(it) },
    offlineSearchPlacesOutsideBbox,
    ensureResultsPerCategory,
    attributeSets,
    evOptions,
)

@Suppress("LongParameterList")
fun createTestCoreReverseGeoOptions(
    point: Point,
    reverseMode: ReverseMode? = null,
    countries: List<String>? = null,
    language: List<String>? = null,
    limit: Int? = null,
    types: List<QueryType>? = null,
): ReverseGeoOptions = ReverseGeoOptions(
    point,
    reverseMode,
    countries,
    language,
    limit,
    types,
)

@Suppress("LongParameterList")
fun createTestCoreSuggestAction(
    endpoint: String = "test_endpoint",
    path: String = "test_path",
    query: String? = null,
    body: ByteArray? = null,
    multiRetrievable: Boolean = false,
) = SuggestAction(
    endpoint, path, query, body, multiRetrievable
)

fun createTestCoreSearchResponseSuccess(
    request: RequestOptions = createTestCoreRequestOptions(),
    results: List<SearchResult> = emptyList(),
    responseUUID: String = "test-response-uuid"
) = SearchResponse(
    request, ExpectedFactory.createValue(results), responseUUID
)

fun createTestCoreSearchResponseHttpError(
    httpCode: Int = 400,
    message: String = "error",
    request: RequestOptions = createTestCoreRequestOptions(),
    responseUUID: String = "test-response-uuid",
) = SearchResponse(
    request, ExpectedFactory.createError(com.mapbox.search.internal.bindgen.Error(HttpError(httpCode, message))), responseUUID
)

fun createTestCoreSearchResponseInternalError(
    message: String = "Test internal error",
    request: RequestOptions = createTestCoreRequestOptions(),
    responseUUID: String = "test-response-uuid",
) = SearchResponse(
    request, ExpectedFactory.createError(com.mapbox.search.internal.bindgen.Error(InternalError(message))), responseUUID
)

fun createTestCoreSearchResponseConnectionError(
    message: String = "Test connection error",
    request: RequestOptions = createTestCoreRequestOptions(),
    responseUUID: String = "test-response-uuid",
) = SearchResponse(
    request, ExpectedFactory.createError(com.mapbox.search.internal.bindgen.Error(ConnectionError(message))), responseUUID
)

fun createTestCoreSearchResponseCancelled(
    reason: String = "Request cancelled",
    request: RequestOptions = createTestCoreRequestOptions(),
    responseUUID: String = "test-response-uuid",
) = SearchResponse(
    request, ExpectedFactory.createError(com.mapbox.search.internal.bindgen.Error(RequestCancelled(reason))), responseUUID
)

@Suppress("LongParameterList")
fun createTestCoreSearchResult(
    id: String = "id_test_core_search_result",
    mapboxId: String? = null,
    types: List<ResultType> = listOf(ResultType.POI),
    names: List<String> = listOf("Test Search Result"),
    namePreferred: String? = "Test name preferred",
    languages: List<String> = listOf("default"),
    addresses: List<SearchAddress>? = null,
    descriptionAddress: String? = null,
    matchingName: String? = null,
    fullAddress: String? = null,
    distanceMeters: Double? = null,
    etaMinutes: Double? = null,
    center: Point? = null,
    accuracy: ResultAccuracy? = null,
    routablePoints: List<RoutablePoint>? = null,
    categories: List<String>? = null,
    categoryIds: List<String>? = null,
    brand: List<String>? = null,
    brandId: String? = null,
    icon: String? = null,
    metadata: ResultMetadata? = null,
    externalIDs: Map<String, String>? = null,
    layerId: String? = null,
    userRecordId: String? = null,
    userRecordPriority: Int = 0,
    action: SuggestAction? = null,
    serverIndex: Int? = 0,
) = SearchResult(
    id,
    mapboxId,
    types,
    names,
    namePreferred,
    languages,
    addresses,
    descriptionAddress,
    matchingName,
    fullAddress,
    distanceMeters,
    etaMinutes,
    center,
    accuracy,
    routablePoints,
    categories,
    categoryIds,
    brand,
    brandId,
    icon,
    metadata,
    externalIDs?.let { (it as? HashMap<String, String>) ?: HashMap(it) },
    layerId,
    userRecordId,
    userRecordPriority,
    action,
    serverIndex,
    null,
)

@Suppress("LongParameterList")
fun SearchResult.copy(
    id: String = this.id,
    mapboxId: String? = this.mapboxId,
    types: List<ResultType> = this.types,
    names: List<String> = this.names,
    namePreferred: String? = this.namePreferred,
    languages: List<String> = this.languages,
    addresses: List<SearchAddress>? = this.addresses,
    descriptionAddress: String? = this.descrAddress,
    matchingName: String? = this.matchingName,
    fullAddress: String? = this.fullAddress,
    distanceMeters: Double? = this.distance,
    etaMinutes: Double? = this.eta,
    center: Point? = this.center,
    accuracy: ResultAccuracy? = this.accuracy,
    routablePoints: List<RoutablePoint>? = this.routablePoints,
    categories: List<String>? = this.categories,
    categoryIds: List<String>? = this.categoryIDs,
    brand: List<String>? = this.brand,
    brandId: String? = this.brandID,
    icon: String? = this.icon,
    metadata: ResultMetadata? = this.metadata,
    externalIDs: Map<String, String>? = this.externalIDs,
    layerId: String? = this.layer,
    userRecordId: String? = this.userRecordID,
    userRecordPriority: Int = this.userRecordPriority,
    action: SuggestAction? = this.action,
    serverIndex: Int? = this.serverIndex,
) = SearchResult(
    id,
    mapboxId,
    types,
    names,
    namePreferred,
    languages,
    addresses,
    descriptionAddress,
    matchingName,
    fullAddress,
    distanceMeters,
    etaMinutes,
    center,
    accuracy,
    routablePoints,
    categories,
    categoryIds,
    brand,
    brandId,
    icon,
    metadata,
    externalIDs?.let { (it as? HashMap<String, String>) ?: HashMap(it) },
    layerId,
    userRecordId,
    userRecordPriority,
    action,
    serverIndex,
    null,
)

fun createCoreSearchAddressRegion(
    name: String,
    regionCode: String? = null,
    regionCodeFull: String? = null
) = SearchAddressRegion(
    name, regionCode, regionCodeFull
)

fun createCoreSearchAddressCountry(
    name: String,
    countryCode: String? = null,
    countryCodeAlpha3: String? = null
) = SearchAddressCountry(
    name, countryCode, countryCodeAlpha3
)

@Suppress("LongParameterList")
fun createCoreSearchAddress(
    defaultValue: String? = null,
    houseNumber: String? = defaultValue,
    street: String? = defaultValue,
    neighborhood: String? = defaultValue,
    locality: String? = defaultValue,
    postcode: String? = defaultValue,
    place: String? = defaultValue,
    district: String? = defaultValue,
    region: SearchAddressRegion? = null,
    country: SearchAddressCountry? = null,
) = SearchAddress(
    houseNumber,
    street,
    neighborhood,
    locality,
    postcode,
    place,
    district,
    region ?: defaultValue?.let { createCoreSearchAddressRegion(name = it) },
    country ?: defaultValue?.let { createCoreSearchAddressCountry(name = it) }
)

fun createTestCoreRequestOptions(
    query: String = "",
    endpoint: String = "suggest",
    options: SearchOptions = createTestCoreSearchOptions(),
    proximityRewritten: Boolean = false,
    originRewritten: Boolean = false,
    sessionID: String = "test-session-id",
) = RequestOptions(
    query,
    endpoint,
    options,
    proximityRewritten,
    originRewritten,
    sessionID,
)

fun createTestCoreRoutablePoint(
    point: Point = Point.fromLngLat(10.0, 20.0),
    name: String = "test-routable-point",
) = RoutablePoint(
    point, name
)
