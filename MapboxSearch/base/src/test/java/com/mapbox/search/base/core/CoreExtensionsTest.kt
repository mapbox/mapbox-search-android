package com.mapbox.search.base.core

import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.TestFactory

internal class CoreExtensionsTest {

    @TestFactory
    fun `Check CoreResultMetadata extensions`() = TestCase {
        Given("CoreResultMetadata instance") {
            val testIso1 = "Test ISO 1"
            val testIso2 = "Test ISO 2"

            val dataKeySlot = slot<String>()
            val data = mockk<HashMap<String, String>>()
            every { data[capture(dataKeySlot)] } answers {
                when (dataKeySlot.captured) {
                    "iso_3166_1" -> testIso1
                    "iso_3166_2" -> testIso2
                    else -> "unknown"
                }
            }

            val meta = createCoreResultMetadata(data = data)

            When("CoreResultMetadata.countryIso1 called") {
                Then("countryIso1 should be equal to `$testIso1`", testIso1, meta.countryIso1)

                VerifyOnce("Data accessed with key `iso_3166_1`") {
                    data["iso_3166_1"]
                }
            }

            When("CoreResultMetadata.countryIso2 called") {
                Then("countryIso2 should be equal to `$testIso2`", testIso2, meta.countryIso2)

                VerifyOnce("Data accessed with key `iso_3166_2`") {
                    data["iso_3166_2"]
                }
            }
        }
    }
}
