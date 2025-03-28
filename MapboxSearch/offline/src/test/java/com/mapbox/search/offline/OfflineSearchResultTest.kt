@file:Suppress("DEPRECATION")

package com.mapbox.search.offline

import com.mapbox.geojson.Point
import com.mapbox.search.base.SdkAssertion
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.utils.extension.mapToPlatform
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.catchThrowable
import com.mapbox.search.common.tests.createTestCoreRoutablePoint
import com.mapbox.search.common.tests.equalsTo
import com.mapbox.search.offline.tests_support.createTestBaseRawSearchResult
import com.mapbox.search.offline.tests_support.createTestBaseSearchAddress
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory

internal class OfflineSearchResultTest {

    private val sdkAssertion = mockk<SdkAssertion>(relaxed = true)

    @BeforeEach
    fun setUp() {
        mockkObject(SdkAssertion.Companion)
        every { SdkAssertion.IMPL } returns sdkAssertion
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SdkAssertion.Companion)
    }

    @TestFactory
    fun `Check OfflineSearchResult equals(), hashCode(), and toString()`() = TestCase {
        Given("${OfflineSearchResult::class.java.simpleName} class") {
            When("equals(), hashCode(), toString() called") {
                Then("equals() and hashCode() should be implemented correctly") {
                    EqualsVerifier.forClass(OfflineSearchResult::class.java)
                        .withIgnoredFields("offlineType")
                        .withPrefabValues(
                            BaseRawSearchResult::class.java,
                            TEST_BASE_RAW_RESULT_1,
                            TEST_BASE_RAW_RESULT_2
                        )
                        .verify()
                }

                val objectCreator = CustomTypeObjectCreatorImpl(BaseRawSearchResult::class) { mode ->
                    listOf(TEST_BASE_RAW_RESULT_1, TEST_BASE_RAW_RESULT_2)[mode.ordinal]
                }

                Then("toString() function should use every declared property") {
                    ToStringVerifier(
                        clazz = OfflineSearchResult::class,
                        ignoredProperties = listOf("rawSearchResult", "offlineType"),
                        objectsFactory = ReflectionObjectsFactory(listOf(objectCreator)),
                        includeAllProperties = false,
                    ).verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check OfflineSearchResult instantiation with a null coordinate`() = TestCase {
        Given("OfflineSearchResult constructor") {
            When("OfflineSearchResult instantiated with a null coordinate") {
                val e = catchThrowable<IllegalStateException> {
                    OfflineSearchResult(createTestBaseRawSearchResult(center = null))
                }

                Then(
                    "Exception should be thrown",
                    true,
                    IllegalStateException("Offline search result must have a coordinate").equalsTo(e)
                )
            }
        }
    }

    @TestFactory
    fun `Check OfflineSearchResult instantiation with unsupported type`() = TestCase {
        Given("OfflineSearchResult constructor") {
            When("OfflineSearchResult instantiated with ${BaseRawResultType.LOCALITY} type") {
                val baseResult = TEST_BASE_RAW_RESULT_1.copy(types = listOf(BaseRawResultType.LOCALITY))
                val offlineSearchResult = OfflineSearchResult(baseResult)

                Then(
                    "newType should be ${NewOfflineSearchResultType.FALLBACK_TYPE}",
                    NewOfflineSearchResultType.FALLBACK_TYPE,
                    offlineSearchResult.newType,
                )

                Then(
                    "type should be ${OfflineSearchResultType.DEFAULT}",
                    OfflineSearchResultType.DEFAULT,
                    offlineSearchResult.type,
                )

                Verify("assertDebug() called", atLeast = 1) {
                    sdkAssertion.assertDebug(any(), any())
                }
            }
        }
    }

    @TestFactory
    fun `Check OfflineSearchResult properties`() = TestCase {
        Given("OfflineSearchResult") {
            val base = mockk<BaseRawSearchResult>().apply {
                every { id } returns TEST_ID
                every { names } returns listOf(TEST_NAME)
                every { descriptionAddress } returns TEST_DESCRIPTION_TEXT
                every { addresses } returns listOf(TEST_BASE_ADDRESS)
                every { types } returns listOf(BaseRawResultType.ADDRESS)
                every { center } returns TEST_COORDINATE
                every { routablePoints } returns listOf(TEST_CORE_ROUTABLE_POINT)
                every { types } returns listOf(TEST_BASE_TYPE)
                every { distanceMeters } returns TEST_DISTANCE_METERS
            }

            val searchResult = OfflineSearchResult(base)

            When("OfflineSearchResult instantiated with a null coordinate") {
                Then("id should be $TEST_ID", TEST_ID, searchResult.id)
                VerifyOnce("base.id called") {
                    base.id
                }

                Then("name should be $TEST_NAME", TEST_NAME, searchResult.name)
                VerifyOnce("base.names called") {
                    base.names
                }

                Then("descriptionText should be $TEST_DESCRIPTION_TEXT", TEST_DESCRIPTION_TEXT, searchResult.descriptionText)
                VerifyOnce("base.descriptionAddress called") {
                    base.descriptionAddress
                }

                Then("address should be $TEST_ADDRESS", TEST_ADDRESS, searchResult.address)
                VerifyOnce("base.addresses called") {
                    base.addresses
                }

                Then("coordinate should be $TEST_COORDINATE", TEST_COORDINATE, searchResult.coordinate)
                Verify("base.center called", exactly = 2) {
                    base.center
                }

                val points = listOf(TEST_ROUTABLE_POINT)
                Then("routablePoints should be $points", points, searchResult.routablePoints)
                VerifyOnce("base.routablePoints called") {
                    base.routablePoints
                }

                val oldType = NewOfflineSearchResultType.toOldResultType(searchResult.newType)
                Then("type should be $oldType", oldType, searchResult.type)
                Then("newType should be $TEST_TYPE", TEST_TYPE, searchResult.newType)
                VerifyOnce("base.types called") {
                    base.types
                }

                Then("type should be $TEST_DISTANCE_METERS", TEST_DISTANCE_METERS, searchResult.distanceMeters)
                VerifyOnce("base.distanceMeters called") {
                    base.distanceMeters
                }
            }
        }
    }

    private companion object {

        const val TEST_ID = "test-id-1"
        const val TEST_NAME = "test-name"
        const val TEST_DESCRIPTION_TEXT = "test-description-text"
        val TEST_BASE_ADDRESS = createTestBaseSearchAddress(houseNumber = "1", street = "street")
        val TEST_ADDRESS = TEST_BASE_ADDRESS.mapToOfflineSdkType()
        val TEST_COORDINATE: Point = Point.fromLngLat(10.0, 20.0)
        val TEST_CORE_ROUTABLE_POINT = createTestCoreRoutablePoint()
        val TEST_ROUTABLE_POINT = TEST_CORE_ROUTABLE_POINT.mapToPlatform()
        val TEST_BASE_TYPE = BaseRawResultType.ADDRESS
        val TEST_TYPE = NewOfflineSearchResultType.createFromRawResultType(TEST_BASE_TYPE)
        const val TEST_DISTANCE_METERS = 123.456

        val TEST_BASE_RAW_RESULT_1 = createTestBaseRawSearchResult(
            id = TEST_ID,
            names = listOf(TEST_NAME),
            descriptionAddress = TEST_DESCRIPTION_TEXT,
            center = TEST_COORDINATE,
            types = listOf(TEST_BASE_TYPE)
        )

        val TEST_BASE_RAW_RESULT_2 = createTestBaseRawSearchResult(
            id = "id2",
            center = Point.fromLngLat(30.0, 50.0),
            types = listOf(BaseRawResultType.STREET)
        )
    }
}
