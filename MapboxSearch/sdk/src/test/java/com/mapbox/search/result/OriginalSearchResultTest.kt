package com.mapbox.search.result

import com.mapbox.geojson.Point
import com.mapbox.search.BuildConfig
import com.mapbox.search.Language
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.TestConstants.ASSERTIONS_KT_CLASS_NAME
import com.mapbox.search.common.reportError
import com.mapbox.search.core.CoreResultMetadata
import com.mapbox.search.core.CoreRoutablePoint
import com.mapbox.search.core.CoreSearchResult
import com.mapbox.search.core.CoreSuggestAction
import com.mapbox.search.internal.bindgen.ResultType
import com.mapbox.search.tests_support.catchThrowable
import com.mapbox.search.tests_support.createCoreSearchAddress
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestFactory

internal class OriginalSearchResultTest {

    @TestFactory
    fun `Check mapping CoreSearchResult to platform`() = TestCase {
        Given("CoreSearchResult extension") {
            When("Convert empty core search result to platform") {
                Then(
                    "Converted value should be as expected",
                    PLATFORM_EMPTY_SEARCH_RESULT,
                    CORE_EMPTY_SEARCH_RESULT.mapToPlatform()
                )
            }

            When("Convert filled core search result to platform") {
                Then(
                    "Converted value should be as expected",
                    PLATFORM_FILLED_SEARCH_RESULT,
                    CORE_FILLED_SEARCH_RESULT.mapToPlatform()
                )
            }

            RESULT_TYPES_MAP.entries.forEach { (resultTypes, expectedValid) ->
                When("Convert filled core search result with $resultTypes types") {
                    val coreSearchResult = createCoreSearchResult(resultTypes.map { it.mapToCore() })
                    val actualValid = catchThrowable<IllegalStateException> {
                        coreSearchResult.mapToPlatform()
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
    fun `Check mapping OriginalSearchResult to core`() = TestCase {
        Given("OriginalSearchResult extension") {
            When("Convert empty core search result to platform") {
                Then(
                    "Converted value should be as expected",
                    CORE_EMPTY_SEARCH_RESULT,
                    PLATFORM_EMPTY_SEARCH_RESULT.mapToCore()
                )
            }

            When("Convert filled core search result to platform") {
                Then(
                    "Converted value should be as expected",
                    CORE_FILLED_SEARCH_RESULT,
                    PLATFORM_FILLED_SEARCH_RESULT.mapToCore()
                )
            }
        }
    }

    private companion object {

        val RESULT_TYPES_MAP: Map<List<OriginalResultType>, Boolean> = mapOf(
            listOf(OriginalResultType.COUNTRY, OriginalResultType.REGION) to true,
            listOf(OriginalResultType.REGION, OriginalResultType.PLACE) to true,
            listOf(OriginalResultType.COUNTRY, OriginalResultType.REGION) to true,
            listOf(OriginalResultType.COUNTRY, OriginalResultType.POI) to false,
            listOf(OriginalResultType.ADDRESS, OriginalResultType.POI) to false,
            listOf(OriginalResultType.REGION, OriginalResultType.ADDRESS, OriginalResultType.POI) to false,
            emptyList<OriginalResultType>() to false,
            listOf(OriginalResultType.PLACE, OriginalResultType.CATEGORY) to false,
        ) + OriginalResultType.values().map { listOf(it) to true }.toMap()

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
            null
        )

        val PLATFORM_EMPTY_SEARCH_RESULT = OriginalSearchResult(
            id = CORE_EMPTY_SEARCH_RESULT.id,
            types = CORE_EMPTY_SEARCH_RESULT.types.map { it.mapToPlatform() },
            names = CORE_EMPTY_SEARCH_RESULT.names,
            languages = CORE_EMPTY_SEARCH_RESULT.languages,
            addresses = CORE_EMPTY_SEARCH_RESULT.addresses?.map { it.mapToPlatform() },
            descriptionAddress = CORE_EMPTY_SEARCH_RESULT.descrAddress,
            matchingName = CORE_EMPTY_SEARCH_RESULT.matchingName,
            distanceMeters = CORE_EMPTY_SEARCH_RESULT.distance,
            center = CORE_EMPTY_SEARCH_RESULT.center,
            routablePoints = CORE_EMPTY_SEARCH_RESULT.routablePoints?.map { it.mapToPlatform() },
            categories = CORE_EMPTY_SEARCH_RESULT.categories,
            icon = CORE_EMPTY_SEARCH_RESULT.icon,
            metadata = CORE_EMPTY_SEARCH_RESULT.metadata?.let { SearchResultMetadata(it) },
            externalIDs = CORE_EMPTY_SEARCH_RESULT.externalIDs,
            layerId = CORE_EMPTY_SEARCH_RESULT.layer,
            userRecordId = CORE_EMPTY_SEARCH_RESULT.userRecordID,
            action = CORE_EMPTY_SEARCH_RESULT.action?.mapToPlatform(),
            serverIndex = CORE_EMPTY_SEARCH_RESULT.serverIndex,
            etaMinutes = CORE_EMPTY_SEARCH_RESULT.eta
        )

        val CORE_FILLED_SEARCH_RESULT = createCoreSearchResult()

        val PLATFORM_FILLED_SEARCH_RESULT = OriginalSearchResult(
            id = CORE_FILLED_SEARCH_RESULT.id,
            types = CORE_FILLED_SEARCH_RESULT.types.map { it.mapToPlatform() },
            names = CORE_FILLED_SEARCH_RESULT.names,
            languages = CORE_FILLED_SEARCH_RESULT.languages,
            addresses = CORE_FILLED_SEARCH_RESULT.addresses?.map { it.mapToPlatform() },
            descriptionAddress = CORE_FILLED_SEARCH_RESULT.descrAddress,
            matchingName = CORE_FILLED_SEARCH_RESULT.matchingName,
            distanceMeters = CORE_FILLED_SEARCH_RESULT.distance,
            center = CORE_FILLED_SEARCH_RESULT.center,
            routablePoints = CORE_FILLED_SEARCH_RESULT.routablePoints?.map { it.mapToPlatform() },
            categories = CORE_FILLED_SEARCH_RESULT.categories,
            icon = CORE_FILLED_SEARCH_RESULT.icon,
            metadata = CORE_FILLED_SEARCH_RESULT.metadata?.let { SearchResultMetadata(it) },
            externalIDs = CORE_FILLED_SEARCH_RESULT.externalIDs,
            layerId = CORE_FILLED_SEARCH_RESULT.layer,
            userRecordId = CORE_FILLED_SEARCH_RESULT.userRecordID,
            action = CORE_FILLED_SEARCH_RESULT.action?.mapToPlatform(),
            serverIndex = CORE_FILLED_SEARCH_RESULT.serverIndex,
            etaMinutes = CORE_FILLED_SEARCH_RESULT.eta
        )

        fun createCoreSearchResult(types: List<ResultType> = listOf(ResultType.POI)): CoreSearchResult {
            return CoreSearchResult(
                "test poi result id",
                types,
                listOf("Test filled search result"),
                listOf(Language.ENGLISH.code, Language.FRENCH.code, Language.GERMAN.code),
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
                CoreSuggestAction("test endpoint", "test path", "test query", byteArrayOf(1, 2, 3), true),
                123
            )
        }

        @Suppress("DEPRECATION", "JVM_STATIC_IN_PRIVATE_COMPANION")
        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            mockkStatic(ASSERTIONS_KT_CLASS_NAME)
            every { reportError(any()) } returns Unit
        }

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            unmockkStatic(ASSERTIONS_KT_CLASS_NAME)
        }
    }
}
