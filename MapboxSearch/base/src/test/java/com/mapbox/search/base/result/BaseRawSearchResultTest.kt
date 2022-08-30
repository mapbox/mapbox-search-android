package com.mapbox.search.base.result

import com.mapbox.geojson.Point
import com.mapbox.search.base.BuildConfig
import com.mapbox.search.base.core.CoreResultAccuracy
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.CoreRoutablePoint
import com.mapbox.search.base.core.CoreSearchResult
import com.mapbox.search.base.core.CoreSuggestAction
import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import com.mapbox.search.common.TestConstants.ASSERTIONS_KT_CLASS_NAME
import com.mapbox.search.common.catchThrowable
import com.mapbox.search.common.createCoreSearchAddress
import com.mapbox.search.internal.bindgen.ResultType
import com.mapbox.test.dsl.TestCase
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestFactory

internal class BaseRawSearchResultTest {

    @TestFactory
    fun `Check mapping CoreSearchResult to base`() = TestCase {
        Given("CoreSearchResult extension") {
            When("Convert empty core search result to base") {
                Then(
                    "Converted value should be as expected",
                    BASE_EMPTY_SEARCH_RESULT,
                    CORE_EMPTY_SEARCH_RESULT.mapToBase()
                )
            }

            When("Convert filled core search result to base") {
                Then(
                    "Converted value should be as expected",
                    BASE_FILLED_SEARCH_RESULT,
                    CORE_FILLED_SEARCH_RESULT.mapToBase()
                )
            }

            RESULT_TYPES_MAP.entries.forEach { (resultTypes, expectedValid) ->
                When("Convert filled core search result with $resultTypes types") {
                    val coreSearchResult = createCoreSearchResult(resultTypes.map { it.mapToCore() })
                    val actualValid = catchThrowable<IllegalStateException> {
                        coreSearchResult.mapToBase()
                    } == null

                    if (BuildConfig.DEBUG) {
                        Then("Result is valid should be $expectedValid", expectedValid, actualValid)
                    } else {
                        Then("Result is valid should be true", true, actualValid)
                    }
                }
            }
        }
    }

    @TestFactory
    fun `Check mapping BaseRawSearchResult to core`() = TestCase {
        Given("BaseRawSearchResult extension") {
            When("Convert empty core search result to base") {
                Then(
                    "Converted value should be as expected",
                    CORE_EMPTY_SEARCH_RESULT,
                    BASE_EMPTY_SEARCH_RESULT.mapToCore()
                )
            }

            When("Convert filled core search result to base") {
                Then(
                    "Converted value should be as expected",
                    CORE_FILLED_SEARCH_RESULT,
                    BASE_FILLED_SEARCH_RESULT.mapToCore()
                )
            }
        }
    }

    private companion object {

        val RESULT_TYPES_MAP: Map<List<BaseRawResultType>, Boolean> = mapOf(
            listOf(BaseRawResultType.COUNTRY, BaseRawResultType.REGION) to true,
            listOf(BaseRawResultType.REGION, BaseRawResultType.PLACE) to true,
            listOf(BaseRawResultType.COUNTRY, BaseRawResultType.REGION) to true,
            listOf(BaseRawResultType.COUNTRY, BaseRawResultType.POI) to false,
            listOf(BaseRawResultType.ADDRESS, BaseRawResultType.POI) to false,
            listOf(BaseRawResultType.REGION, BaseRawResultType.ADDRESS, BaseRawResultType.POI) to false,
            emptyList<BaseRawResultType>() to false,
            listOf(BaseRawResultType.PLACE, BaseRawResultType.CATEGORY) to false,
        ) + BaseRawResultType.values().associate { listOf(it) to true }

        val CORE_EMPTY_SEARCH_RESULT = CoreSearchResult(
            "Empty result id",
            listOf(ResultType.PLACE, ResultType.REGION),
            listOf("Result name"),
            listOf("Default"),
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
            null,
            null,
            null,
            null,
            -1,
            null,
            null
        )

        val BASE_EMPTY_SEARCH_RESULT = BaseRawSearchResult(
            id = CORE_EMPTY_SEARCH_RESULT.id,
            types = CORE_EMPTY_SEARCH_RESULT.types.map { it.mapToBase() },
            names = CORE_EMPTY_SEARCH_RESULT.names,
            languages = CORE_EMPTY_SEARCH_RESULT.languages,
            addresses = CORE_EMPTY_SEARCH_RESULT.addresses?.map { it.mapToBaseSearchAddress() },
            descriptionAddress = CORE_EMPTY_SEARCH_RESULT.descrAddress,
            matchingName = CORE_EMPTY_SEARCH_RESULT.matchingName,
            distanceMeters = CORE_EMPTY_SEARCH_RESULT.distance,
            center = CORE_EMPTY_SEARCH_RESULT.center,
            accuracy = CORE_EMPTY_SEARCH_RESULT.accuracy,
            routablePoints = CORE_EMPTY_SEARCH_RESULT.routablePoints,
            categories = CORE_EMPTY_SEARCH_RESULT.categories,
            icon = CORE_EMPTY_SEARCH_RESULT.icon,
            metadata = CORE_EMPTY_SEARCH_RESULT.metadata,
            externalIDs = CORE_EMPTY_SEARCH_RESULT.externalIDs,
            layerId = CORE_EMPTY_SEARCH_RESULT.layer,
            userRecordId = CORE_EMPTY_SEARCH_RESULT.userRecordID,
            userRecordPriority = CORE_EMPTY_SEARCH_RESULT.userRecordPriority,
            action = CORE_EMPTY_SEARCH_RESULT.action?.mapToBase(),
            serverIndex = CORE_EMPTY_SEARCH_RESULT.serverIndex,
            etaMinutes = CORE_EMPTY_SEARCH_RESULT.eta
        )

        val CORE_FILLED_SEARCH_RESULT = createCoreSearchResult()

        val BASE_FILLED_SEARCH_RESULT = BaseRawSearchResult(
            id = CORE_FILLED_SEARCH_RESULT.id,
            types = CORE_FILLED_SEARCH_RESULT.types.map { it.mapToBase() },
            names = CORE_FILLED_SEARCH_RESULT.names,
            languages = CORE_FILLED_SEARCH_RESULT.languages,
            addresses = CORE_FILLED_SEARCH_RESULT.addresses?.map { it.mapToBaseSearchAddress() },
            descriptionAddress = CORE_FILLED_SEARCH_RESULT.descrAddress,
            matchingName = CORE_FILLED_SEARCH_RESULT.matchingName,
            distanceMeters = CORE_FILLED_SEARCH_RESULT.distance,
            center = CORE_FILLED_SEARCH_RESULT.center,
            accuracy = CORE_FILLED_SEARCH_RESULT.accuracy,
            routablePoints = CORE_FILLED_SEARCH_RESULT.routablePoints,
            categories = CORE_FILLED_SEARCH_RESULT.categories,
            icon = CORE_FILLED_SEARCH_RESULT.icon,
            metadata = CORE_FILLED_SEARCH_RESULT.metadata,
            externalIDs = CORE_FILLED_SEARCH_RESULT.externalIDs,
            layerId = CORE_FILLED_SEARCH_RESULT.layer,
            userRecordId = CORE_FILLED_SEARCH_RESULT.userRecordID,
            userRecordPriority = CORE_FILLED_SEARCH_RESULT.userRecordPriority,
            action = CORE_FILLED_SEARCH_RESULT.action?.mapToBase(),
            serverIndex = CORE_FILLED_SEARCH_RESULT.serverIndex,
            etaMinutes = CORE_FILLED_SEARCH_RESULT.eta
        )

        fun createCoreSearchResult(types: List<ResultType> = listOf(ResultType.POI)): CoreSearchResult {
            return CoreSearchResult(
                "test poi result id",
                types,
                listOf("Test filled search result"),
                listOf("en", "fr", "de"),
                listOf(
                    createCoreSearchAddress(country = "country"),
                    createCoreSearchAddress(neighborhood = "neighborhood"),
                    createCoreSearchAddress(postcode = "postcode")
                ),
                "Test description address",
                "Test matching name",
                123.456,
                123.0,
                Point.fromLngLat(10.0, 11.0),
                CoreResultAccuracy.POINT,
                listOf(
                    CoreRoutablePoint(Point.fromLngLat(1.0, 2.0), "test point 1"),
                    CoreRoutablePoint(Point.fromLngLat(2.0, 3.0), "test point 2"),
                    CoreRoutablePoint(Point.fromLngLat(3.0, 4.0), "test point 3"),
                ),
                listOf("category 1", "category 2", "category 3"),
                "test maki",
                CoreResultMetadata(
                    3456,
                    "+902 10 70 77",
                    "https://www.museodelprado.es/en/visit-the-museum",
                    9.7,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    hashMapOf()
                ),
                hashMapOf("external id 1" to "123", "external id 2" to "456", "external id 3" to "789"),
                "test layer id",
                "test user record id",
                -1,
                CoreSuggestAction("test endpoint", "test path", "test query", byteArrayOf(1, 2, 3), true),
                123
            )
        }

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            resetLogImpl()
            mockkStatic(ASSERTIONS_KT_CLASS_NAME)
        }

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            reinitializeLogImpl()
            unmockkStatic(ASSERTIONS_KT_CLASS_NAME)
        }
    }
}
