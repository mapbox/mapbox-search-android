package com.mapbox.search

import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.tests_support.SdkCustomTypeObjectCreators
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class SearchRequestExceptionTest {

    @TestFactory
    fun `Check SearchRequestException functions()`() = TestCase {
        Given("SearchRequestException") {
            When("Exception created with code 400") {
                val e = SearchRequestException(TEST_MESSAGE, 400)

                Then("isClientError() == true", true, e.isClientError())
                Then("isServerError() == false", false, e.isServerError())
            }

            When("Exception created with code 500") {
                val e = SearchRequestException(TEST_MESSAGE, 500)

                Then("isClientError() == false", false, e.isClientError())
                Then("isServerError() == true", true, e.isServerError())
            }
        }
    }

    @TestFactory
    fun `Check SearchRequestException toString()`() = TestCase {
        Given("SearchRequestException class") {
            When("toString() called") {
                Then("toString() function should include every declared property") {
                    ToStringVerifier(
                        clazz = SearchRequestException::class,
                        objectsFactory = ReflectionObjectsFactory(SdkCustomTypeObjectCreators.ALL_CREATORS)
                    ).verify()
                }
            }
        }
    }

    private companion object {
        const val TEST_MESSAGE = "Test message"
    }
}
