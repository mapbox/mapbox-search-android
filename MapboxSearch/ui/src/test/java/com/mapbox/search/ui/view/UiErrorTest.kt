package com.mapbox.search.ui.view

import com.mapbox.search.common.SearchRequestException
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.isEnum
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.junit.jupiter.api.TestFactory
import java.io.IOException

internal class UiErrorTest {

    @TestFactory
    fun `Check UiError fromException()`() = TestCase {
        listOf(
            IOException(),
            SearchRequestException("Server error", 500),
            SearchRequestException("Client error", 400),
            IllegalStateException()
        ).forEach { e ->
            Given("Exception: $e") {
                When("UiError.fromException() called") {
                    val uiError = when {
                        e is IOException -> UiError.NoInternetConnectionError
                        e is SearchRequestException && e.isServerError() -> UiError.ServerError
                        e is SearchRequestException && e.isClientError() -> UiError.ClientError
                        else -> UiError.UnknownError
                    }

                    Then("UiError shuld be $uiError", uiError, UiError.createFromException(e))
                }
            }
        }
    }

    @TestFactory
    fun `Test generated equals(), hashCode() and toString() methods`() = TestCase {
        listOf(
            UiError.NoInternetConnectionError::class,
            UiError.ServerError::class,
            UiError.ClientError::class,
            UiError.UnknownError::class,
        )
            .forEach { clazz ->
                Given("${clazz.java.canonicalName} class") {
                    When("${clazz.java.canonicalName} class") {
                        Then("equals() and hashCode() functions should use every declared property") {
                            EqualsVerifier.forClass(clazz.java)
                                .suppress(Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                                .verify()
                        }

                        if (!isEnum(clazz)) {
                            Then("toString() function should use every declared property") {
                                ToStringVerifier(clazz = clazz).verify()
                            }
                        }
                    }
                }
            }
    }
}
