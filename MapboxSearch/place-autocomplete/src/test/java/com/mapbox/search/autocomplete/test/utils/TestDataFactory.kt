package com.mapbox.search.autocomplete.test.utils

import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreRequestOptions
import com.mapbox.search.base.core.CoreResultAccuracy
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.CoreSearchAddress
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.result.BaseSearchResultType
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.result.BaseServerSearchResultImpl
import com.mapbox.search.base.result.BaseServerSearchSuggestion
import com.mapbox.search.base.result.BaseSuggestAction
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.base.result.mapToBaseSearchAddress
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.common.tests.createCoreSearchAddress
import com.mapbox.search.common.tests.createCoreSearchAddressCountry
import com.mapbox.search.common.tests.createCoreSearchAddressRegion
import com.mapbox.search.common.tests.createTestCoreRequestOptions
import com.mapbox.search.common.tests.createTestResultMetadata
import com.mapbox.search.internal.bindgen.ImageInfo
import com.mapbox.search.internal.bindgen.OpenHours
import com.mapbox.search.internal.bindgen.OpenMode

internal val filledTestCoreMetadata: CoreResultMetadata = createTestResultMetadata(
    reviewCount = 15,
    phone = "+123456789",
    website = "https://test.com",
    avRating = 4.9,
    description = "test-description",
    openHours = OpenHours(OpenMode.ALWAYS_OPEN, emptyList()),
    primaryPhoto = listOf(ImageInfo("https://test.com/img-primary.jpg", 150, 150)),
    otherPhoto = listOf(ImageInfo("https://test.com/img-other.jpg", 300, 150)),
    data = hashMapOf("iso_3166_1" to "test-iso-1", "iso_3166_2" to "test-iso-2"),
)

internal val filledCoreSearchAddress: CoreSearchAddress = createCoreSearchAddress(
    houseNumber = "test-house-number",
    street = "test-street",
    neighborhood = "test-neighborhood",
    locality = "test-locality",
    postcode = "test-postcode",
    place = "test-place",
    district = "test-district",
    region = createCoreSearchAddressRegion("test-region"),
    country = createCoreSearchAddressCountry("test-country"),
)

internal val testBaseRawSearchResult: BaseRawSearchResult = createTestBaseRawSearchResult(
    id = "test-id",
    names = listOf("test-suggestion"),
    descriptionAddress = "test-description",
    addresses = listOf(filledCoreSearchAddress),
    fullAddress = "test-full-address",
    center = Point.fromLngLat(10.0, 15.0),
    distanceMeters = 123.0,
    icon = "test-suggestion",
    etaMinutes = 5.0,
    types = listOf(BaseRawResultType.ADDRESS),
    action = null,
    metadata = filledTestCoreMetadata
)

internal val testBaseResult: BaseServerSearchResultImpl = BaseServerSearchResultImpl(
    types = listOf(BaseSearchResultType.ADDRESS),
    rawSearchResult = testBaseRawSearchResult,
    requestOptions = createTestBaseRequestOptions()
)

internal val testBaseRawSearchSuggestionWithCoordinates: BaseRawSearchResult = createTestBaseRawSearchResult(
    id = "test-suggestion-id",
    names = listOf("test-suggestion"),
    descriptionAddress = "test-description",
    addresses = listOf(filledCoreSearchAddress),
    fullAddress = "test-full-address",
    center = Point.fromLngLat(10.0, 15.0),
    routablePoints = listOf(RoutablePoint(Point.fromLngLat(11.0, 16.0), "test")),
    distanceMeters = 123.0,
    icon = "test-suggestion",
    etaMinutes = 5.0,
    types = listOf(BaseRawResultType.ADDRESS),
    action = createTestBaseSuggestAction()
)

internal val testBaseRawSearchSuggestionWithoutCoordinates: BaseRawSearchResult = createTestBaseRawSearchResult(
    id = "test-suggestion-id",
    names = listOf("test-suggestion"),
    descriptionAddress = "test-description",
    addresses = listOf(filledCoreSearchAddress),
    fullAddress = "test-full-address",
    distanceMeters = 123.0,
    icon = "test-suggestion",
    etaMinutes = 5.0,
    types = listOf(BaseRawResultType.ADDRESS),
    action = createTestBaseSuggestAction()
)

internal fun createTestBaseRequestOptions(
    core: CoreRequestOptions = createTestCoreRequestOptions(),
    requestContext: SearchRequestContext = SearchRequestContext(CoreApiType.SBS),
) = BaseRequestOptions(
    core = core,
    requestContext = requestContext
)

@Suppress("LongParameterList")
internal fun createTestBaseRawSearchResult(
    id: String = "id_test_search_result",
    types: List<BaseRawResultType> = listOf(BaseRawResultType.POI),
    names: List<String> = listOf("Test Search Result"),
    languages: List<String> = listOf("def"),
    addresses: List<CoreSearchAddress>? = null,
    descriptionAddress: String? = null,
    matchingName: String? = null,
    fullAddress: String? = null,
    distanceMeters: Double? = null,
    center: Point? = null,
    accuracy: CoreResultAccuracy? = null,
    routablePoints: List<RoutablePoint>? = null,
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
    types = types,
    names = names,
    languages = languages,
    addresses = addresses?.map { it.mapToBaseSearchAddress() },
    descriptionAddress = descriptionAddress,
    distanceMeters = distanceMeters,
    matchingName = matchingName,
    fullAddress = fullAddress,
    center = center,
    accuracy = accuracy,
    routablePoints = routablePoints?.map { it.mapToCore() },
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

internal fun createTestBaseSuggestAction(
    endpoint: String = "test-endpoint",
    path: String = "test-path",
    query: String? = "test-query",
    body: ByteArray? = null,
    multiRetrievable: Boolean = false,
): BaseSuggestAction {
    return BaseSuggestAction(
        endpoint = endpoint,
        path = path,
        query = query,
        body = body,
        multiRetrievable = multiRetrievable
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
