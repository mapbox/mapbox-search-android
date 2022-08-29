package com.mapbox.search.offline

import com.mapbox.search.base.result.BaseSearchAddress
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class OfflineSearchAddressTest {

    @TestFactory
    fun `Test equals(), hashCode() and toString() methods`() = TestCase {
        Given("${OfflineSearchAddress::class.java.simpleName} class") {
            When("equals(), hashCode(), toString() called") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(OfflineSearchAddress::class.java).verify()
                }

                Then("toString() function should use every declared property") {
                    ToStringVerifier(
                        clazz = OfflineSearchAddress::class,
                        includeAllProperties = false
                    ).verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check BaseSearchAddress mapToOfflineSdkType() function`() = TestCase {
        Given("BaseSearchAddress instance") {
            val baseAddress = BaseSearchAddress(
                houseNumber = "test houseNumber",
                street = "test street",
                neighborhood = "test neighborhood",
                locality = "test locality",
                postcode = "test postcode",
                place = "test place",
                district = "test district",
                region = "test region",
                country = "test country"
            )

            When("mapToOfflineSdkType() called") {
                val offlineSearchAddress = OfflineSearchAddress(
                    houseNumber = "test houseNumber",
                    street = "test street",
                    neighborhood = "test neighborhood",
                    locality = "test locality",
                    place = "test place",
                    region = "test region",
                    country = "test country"
                )

                Then("Mapped object should be correct", offlineSearchAddress, baseAddress.mapToOfflineSdkType())
            }
        }
    }
}
