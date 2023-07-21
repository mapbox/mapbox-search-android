package com.mapbox.search.tests_support

import com.mapbox.geojson.Point
import com.mapbox.search.RequestOptions
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreRequestOptions
import com.mapbox.search.base.result.BaseIndexableRecordSearchResultImpl
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.result.BaseSearchAddress
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.result.BaseServerSearchResultImpl
import com.mapbox.search.base.result.BaseServerSearchSuggestion
import com.mapbox.search.base.result.BaseSuggestAction
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.common.tests.createTestCoreRequestOptions
import com.mapbox.search.mapToBase
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.record.mapToBase
import com.mapbox.search.result.ResultAccuracy
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.result.mapToBase
import com.mapbox.search.result.mapToCore

@Suppress("LongParameterList")
internal fun createTestBaseRawSearchResult(
    id: String = "id_test_search_result",
    mapboxId: String? = null,
    types: List<BaseRawResultType> = listOf(BaseRawResultType.POI),
    names: List<String> = listOf("Test Search Result"),
    languages: List<String> = listOf("def"),
    addresses: List<SearchAddress>? = null,
    descriptionAddress: String? = null,
    fullAddress: String? = null,
    distanceMeters: Double? = null,
    center: Point? = null,
    accuracy: ResultAccuracy? = null,
    routablePoints: List<RoutablePoint>? = null,
    categories: List<String>? = null,
    categoryIds: List<String>? = null,
    brand: List<String>? = null,
    brandId: String? = null,
    icon: String? = null,
    metadata: SearchResultMetadata? = null,
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
    languages = languages,
    addresses = addresses?.map { it.mapToBase() },
    descriptionAddress = descriptionAddress,
    fullAddress = fullAddress,
    distanceMeters = distanceMeters,
    center = center,
    accuracy = accuracy?.mapToCore(),
    routablePoints = routablePoints?.map { it.mapToCore() },
    categories = categories,
    categoryIds = categoryIds,
    brand = brand,
    brandId = brandId,
    icon = icon,
    metadata = metadata?.coreMetadata,
    externalIDs = externalIDs,
    layerId = layerId,
    userRecordId = userRecordId,
    userRecordPriority = userRecordPriority,
    action = action,
    serverIndex = serverIndex,
    etaMinutes = etaMinutes
)

@Suppress("LongParameterList")
internal fun createTestRequestOptions(
    query: String = "",
    endpoint: String = "suggest",
    options: SearchOptions = SearchOptions.Builder().build(),
    proximityRewritten: Boolean = false,
    originRewritten: Boolean = false,
    sessionID: String = "test-session-id",
    requestContext: SearchRequestContext = SearchRequestContext(CoreApiType.SEARCH_BOX),
) = RequestOptions(
    query = query,
    endpoint = endpoint,
    options = options,
    proximityRewritten = proximityRewritten,
    originRewritten = originRewritten,
    sessionID = sessionID,
    requestContext = requestContext
)

internal fun createTestBaseRequestOptions(
    core: CoreRequestOptions = createTestCoreRequestOptions(),
    requestContext: SearchRequestContext = SearchRequestContext(CoreApiType.SEARCH_BOX),
) = BaseRequestOptions(
    core = core,
    requestContext = requestContext
)

internal fun createTestBaseSuggestAction(
    endpoint: String = "test-endpoint",
    path: String = "test-path",
    query: String? = "test-query",
    body: ByteArray? = null,
): BaseSuggestAction {
    return BaseSuggestAction(
        endpoint = endpoint,
        path = path,
        query = query,
        body = body
    )
}

internal fun createTestBaseSearchSuggestion(
    rawSearchResult: BaseRawSearchResult = createTestBaseRawSearchResult(
        action = createTestBaseSuggestAction()
    ),
    requestOptions: BaseRequestOptions = createTestBaseRequestOptions()
): BaseSearchSuggestion {
    return BaseServerSearchSuggestion(
        rawSearchResult = rawSearchResult,
        requestOptions = requestOptions
    )
}

internal fun createTestSearchSuggestion(id: String = "id_test_search_result"): SearchSuggestion {
    return SearchSuggestion(
        createTestBaseSearchSuggestion(
            createTestBaseRawSearchResult(
                id = id,
                action = createTestBaseSuggestAction(),
            )
        )
    )
}

internal fun createTestSearchResult(
    id: String = "id_test_search_result",
    center: Point = Point.fromLngLat(10.0, 11.123456)
): SearchResult = createTestServerSearchResult(
    types = listOf(SearchResultType.POI),
    rawSearchResult = createTestBaseRawSearchResult(
        id = id,
        types = listOf(BaseRawResultType.POI),
        center = center
    ),
    requestOptions = createTestRequestOptions()
)

internal fun createTestFavoriteRecord(
    id: String = "test id",
    name: String = "test name",
    coordinate: Point = Point.fromLngLat(.0, .1),
    descriptionText: String? = "Test description text",
    address: SearchAddress? = SearchAddress(country = "Belarus"),
    searchResultType: SearchResultType = SearchResultType.POI,
    makiIcon: String? = "test maki",
    categories: List<String>? = listOf("test"),
    routablePoints: List<RoutablePoint>? = null,
    metadata: SearchResultMetadata? = null,
) = FavoriteRecord(
    id = id,
    name = name,
    coordinate = coordinate,
    descriptionText = descriptionText,
    address = address,
    type = searchResultType,
    makiIcon = makiIcon,
    categories = categories,
    routablePoints = routablePoints,
    metadata = metadata
)

@Suppress("LongParameterList")
internal fun createTestHistoryRecord(
    id: String = "test_history_record_id",
    name: String = "Test history record",
    coordinate: Point = Point.fromLngLat(10.0, 20.0),
    descriptionText: String? = null,
    address: SearchAddress? = SearchAddress(),
    timestamp: Long = 123L,
    searchResultType: SearchResultType = SearchResultType.POI,
    routablePoints: List<RoutablePoint>? = null,
    metadata: SearchResultMetadata? = null,
    categories: List<String>? = null,
    makiIcon: String? = null
) = HistoryRecord(
    id = id,
    name = name,
    coordinate = coordinate,
    descriptionText = descriptionText,
    address = address,
    timestamp = timestamp,
    type = searchResultType,
    routablePoints = routablePoints,
    metadata = metadata,
    makiIcon = makiIcon,
    categories = categories,
)

internal fun createHistoryRecord(searchResult: SearchResult, timestamp: Long): HistoryRecord {
    return HistoryRecord(
        id = searchResult.id,
        name = searchResult.name,
        coordinate = searchResult.coordinate,
        descriptionText = searchResult.descriptionText,
        address = searchResult.address,
        timestamp = timestamp,
        type = searchResult.types.first(),
        routablePoints = searchResult.routablePoints,
        metadata = searchResult.metadata,
        makiIcon = searchResult.makiIcon,
        categories = searchResult.categories,
    )
}

@Suppress("LongParameterList")
internal fun createSearchAddress(
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
) = SearchAddress(
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

@JvmSynthetic
internal fun createTestServerSearchResult(
    types: List<SearchResultType>,
    rawSearchResult: BaseRawSearchResult,
    requestOptions: RequestOptions
): SearchResult {
    val base = BaseServerSearchResultImpl(
        types = types.map { it.mapToBase() },
        rawSearchResult = rawSearchResult,
        requestOptions = requestOptions.mapToBase()
    )
    return SearchResult(base)
}

@JvmSynthetic
internal fun createTestIndexableRecordSearchResult(
    record: IndexableRecord,
    rawSearchResult: BaseRawSearchResult,
    requestOptions: RequestOptions
): SearchResult {
    val base = BaseIndexableRecordSearchResultImpl(
        record = record.mapToBase(),
        rawSearchResult = rawSearchResult,
        requestOptions = requestOptions.mapToBase()
    )
    return SearchResult(base)
}
