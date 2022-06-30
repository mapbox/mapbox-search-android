package com.mapbox.search

import android.content.Context
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.search.common.tests.CopyVerifier
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.tests_support.MockedTypesObjectCreators
import com.mapbox.search.tests_support.SdkCustomTypeObjectCreators
import com.mapbox.test.dsl.TestCase
import io.mockk.mockk
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class SearchEngineSettingsTest {

    private val reflectionObjectFactory = ReflectionObjectsFactory(
        extraCreators = SdkCustomTypeObjectCreators.ALL_CREATORS + MockedTypesObjectCreators.ALL_CREATORS
    )

    @TestFactory
    fun `Test generated equals(), hashCode(), copy(), and toString() methods`() = TestCase {
        Given("SearchEngineSettings class") {
            When("equals(), hashCode(), copy(), and toString() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(SearchEngineSettings::class.java).verify()
                }

                Then("toString() function should use every declared property") {
                    ToStringVerifier(
                        clazz = SearchEngineSettings::class,
                        objectsFactory = reflectionObjectFactory,
                        includeAllProperties = false
                    ).verify()
                }

                Then("copy() function should use every declared property") {
                    CopyVerifier(
                        clazz = SearchEngineSettings::class,
                        objectsFactory = reflectionObjectFactory,
                    ).verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check SearchEngineSettings default settings`() = TestCase {
        Given("SearchEngineSettings object") {
            When("Values accessed") {
                Then(
                    "Default geocoding endpoint should be as expected",
                    "https://api.mapbox.com",
                    SearchEngineSettings.DEFAULT_ENDPOINT_GEOCODING
                )
            }
        }
    }

    @TestFactory
    fun `Check SearchEngineSettings default builder`() = TestCase {
        Given("SearchEngineSettings builder") {
            When("Build new settings with default values") {
                val actual = SearchEngineSettings.Builder(TEST_MOCKED_CONTEXT, TEST_ACCESS_TOKEN)
                    .locationEngine(TEST_MOCKED_LOCATION_ENGINE)
                    .build()

                val expected = SearchEngineSettings(
                    applicationContext = TEST_MOCKED_CONTEXT,
                    accessToken = TEST_ACCESS_TOKEN,
                    locationEngine = TEST_MOCKED_LOCATION_ENGINE,
                )

                Then("Settings should be equal", expected, actual)
            }
        }
    }

    @TestFactory
    fun `Check SearchEngineSettings builder with all values set`() = TestCase {
        Given("SearchEngineSettings builder") {
            When("Build new settings with test values") {
                val actual = SearchEngineSettings.Builder(TEST_MOCKED_CONTEXT, TEST_ACCESS_TOKEN)
                    .locationEngine(TEST_MOCKED_LOCATION_ENGINE)
                    .viewportProvider(TEST_MOCKED_VIEWPORT_PROVIDER)
                    .geocodingEndpointBaseUrl(TEST_V5_ENDPOINT)
                    .singleBoxSearchBaseUrl(TEST_SBS_ENDPOINT)
                    .build()

                val expected = SearchEngineSettings(
                    applicationContext = TEST_MOCKED_CONTEXT,
                    accessToken = TEST_ACCESS_TOKEN,
                    locationEngine = TEST_MOCKED_LOCATION_ENGINE,
                    geocodingEndpointBaseUrl = TEST_V5_ENDPOINT,
                    singleBoxSearchBaseUrl = TEST_SBS_ENDPOINT,
                    viewportProvider = TEST_MOCKED_VIEWPORT_PROVIDER,
                )

                Then("Settings should be equal", expected, actual)
            }
        }
    }

    @TestFactory
    fun `Check SearchEngineSettings toBuilder function`() = TestCase {
        Given("SearchEngineSettings builder") {
            When("Object created with toBuilder()") {
                val settings = SearchEngineSettings(
                    applicationContext = TEST_MOCKED_CONTEXT,
                    accessToken = TEST_ACCESS_TOKEN,
                    locationEngine = TEST_MOCKED_LOCATION_ENGINE,
                    geocodingEndpointBaseUrl = TEST_V5_ENDPOINT,
                    singleBoxSearchBaseUrl = TEST_SBS_ENDPOINT,
                    viewportProvider = TEST_MOCKED_VIEWPORT_PROVIDER,
                )

                Then("Settings should be equal", settings, settings.toBuilder().build())
            }
        }
    }

    private companion object {
        const val TEST_ACCESS_TOKEN = "test token"
        const val TEST_V5_ENDPOINT = "https://test-v5.mapbox.com"
        const val TEST_SBS_ENDPOINT = "https://test-sbs.mapbox.com"
        val TEST_MOCKED_CONTEXT: Context = mockk(relaxed = true)
        val TEST_MOCKED_LOCATION_ENGINE: LocationEngine = mockk(relaxed = true)
        val TEST_MOCKED_VIEWPORT_PROVIDER: ViewportProvider = mockk(relaxed = true)
    }
}
