package com.mapbox.search

import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory
import java.net.URI

internal class OfflineSearchSettingsTest {

    @TestFactory
    fun `Check OfflineSearchSettings default settings`() = TestCase {
        Given("OfflineSearchSettings object") {
            When("Values accessed") {
                Then(
                    "Default url should be as expected",
                    URI.create("https://api-offline-search-staging.tilestream.net"),
                    OfflineSearchSettings.DEFAULT_ENDPOINT_URI
                )

                Then(
                    "Default dataset should be as expected",
                    "test-dataset",
                    OfflineSearchSettings.DEFAULT_DATASET
                )

                Then(
                    "Default dataset version should be as expected",
                    "",
                    OfflineSearchSettings.DEFAULT_VERSION
                )
            }
        }
    }

    @TestFactory
    fun `Check OfflineSearchSettings default builder`() = TestCase {
        Given("OfflineSearchSettings builder") {
            When("Build new settings with default values") {
                val actual = OfflineSearchSettings.Builder().build()
                val expected = OfflineSearchSettings()

                Then("Settings should be equal", expected, actual)
            }
        }
    }

    @TestFactory
    fun `Check OfflineSearchSettings builder with all values set`() = TestCase {
        Given("OfflineSearchSettings builder") {
            When("Build new settings with test values") {
                val actual = OfflineSearchSettings.Builder().run {
                    tilesBaseUri(TEST_DEFAULT_ENDPOINT_URI)
                    build()
                }

                val expected = OfflineSearchSettings(
                    tilesBaseUri = TEST_DEFAULT_ENDPOINT_URI,
                )

                Then("Settings should be equal", expected, actual)
            }
        }
    }

    @TestFactory
    fun `Check OfflineSearchSettings toBuilder function`() = TestCase {
        Given("OfflineSearchSettings builder") {
            When("Object created with toBuilder()") {
                val settings = OfflineSearchSettings(
                    tilesBaseUri = TEST_DEFAULT_ENDPOINT_URI,
                )

                Then("Settings should be equal", settings, settings.toBuilder().build())
            }
        }
    }

    private companion object {
        val TEST_DEFAULT_ENDPOINT_URI: URI = URI.create("https://api-offline-search-staging.tilestream.net")
    }
}
