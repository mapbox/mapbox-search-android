package com.mapbox.search.offline

import com.mapbox.common.TileStore
import com.mapbox.common.location.LocationProvider
import com.mapbox.search.common.tests.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.test.dsl.TestCase
import io.mockk.mockk
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory
import java.net.URI

internal class OfflineSearchEngineSettingsTest {

    @TestFactory
    fun `Test equals(), hashCode(), and toString() methods`() = TestCase {
        Given("OfflineSearchEngineSettings class") {
            When("equals(), hashCode(), and toString() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(OfflineSearchEngineSettings::class.java).verify()
                }

                Then("toString() function should use every declared property") {
                    ToStringVerifier(
                        clazz = OfflineSearchEngineSettings::class,
                        objectsFactory = REFLECTION_OBJECT_FACTORY,
                        includeAllProperties = false
                    ).verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check OfflineSearchSettings default settings`() = TestCase {
        Given("OfflineSearchSettings object") {
            When("Values accessed") {
                Then(
                    "Default url should be as expected",
                    URI.create("https://api.mapbox.com"),
                    OfflineSearchEngineSettings.DEFAULT_ENDPOINT_URI
                )

                Then(
                    "Default dataset should be as expected",
                    "mbx-gen2",
                    OfflineSearchEngineSettings.DEFAULT_DATASET
                )

                Then(
                    "Default dataset version should be as expected",
                    "",
                    OfflineSearchEngineSettings.DEFAULT_VERSION
                )
            }
        }
    }

    @TestFactory
    fun `Check OfflineSearchSettings default builder`() = TestCase {
        Given("OfflineSearchSettings builder") {
            When("Build new settings with default values") {
                val actual = OfflineSearchEngineSettings.Builder()
                    .locationProvider(TEST_MOCKED_LOCATION_ENGINE)
                    .tileStore(TEST_MOCKED_TILE_STORE)
                    .build()

                val expected = OfflineSearchEngineSettings(
                    tileStore = TEST_MOCKED_TILE_STORE,
                    locationProvider = TEST_MOCKED_LOCATION_ENGINE
                )

                Then("Settings should be equal", expected, actual)
            }
        }
    }

    @TestFactory
    fun `Check OfflineSearchSettings builder with all values set`() = TestCase {
        Given("OfflineSearchSettings builder") {
            When("Build new settings with test values") {
                val actual = OfflineSearchEngineSettings.Builder()
                    .locationProvider(TEST_MOCKED_LOCATION_ENGINE)
                    .tileStore(TEST_MOCKED_TILE_STORE)
                    .tilesBaseUri(TEST_DEFAULT_ENDPOINT_URI)
                    .build()

                val expected = OfflineSearchEngineSettings(
                    tileStore = TEST_MOCKED_TILE_STORE,
                    tilesBaseUri = TEST_DEFAULT_ENDPOINT_URI,
                    locationProvider = TEST_MOCKED_LOCATION_ENGINE,
                )

                Then("Settings should be equal", expected, actual)
            }
        }
    }

    @TestFactory
    fun `Check OfflineSearchSettings toBuilder function`() = TestCase {
        Given("OfflineSearchSettings builder") {
            When("Object created with toBuilder()") {
                val settings = OfflineSearchEngineSettings(
                    tileStore = TEST_MOCKED_TILE_STORE,
                    tilesBaseUri = TEST_DEFAULT_ENDPOINT_URI,
                    locationProvider = TEST_MOCKED_LOCATION_ENGINE,
                )

                Then("Settings should be equal", settings, settings.toBuilder().build())
            }
        }
    }

    private companion object {
        val TEST_DEFAULT_ENDPOINT_URI: URI = URI.create("https://cloudfront-staging.tilestream.net")
        val TEST_MOCKED_LOCATION_ENGINE: LocationProvider = mockk(relaxed = true)
        val TEST_MOCKED_TILE_STORE: TileStore = mockk(relaxed = true)

        val LOCATION_ENGINE_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(LocationProvider::class) { mode ->
            listOf(
                mockk<LocationProvider>(relaxed = true), mockk<LocationProvider>(relaxed = true),
            )[mode.ordinal]
        }

        private val REFLECTION_OBJECT_FACTORY = ReflectionObjectsFactory(
            extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS + LOCATION_ENGINE_OBJECT_CREATOR
        )
    }
}
