package com.mapbox.search.common

import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException

internal class SearchRequestExceptionTest {

    @Test
    fun `Check client error SearchRequestException()`() {
        val e = SearchRequestException(TEST_MESSAGE, 400)

        assertEquals(true, e.isClientError())
        assertEquals(false, e.isServerError())
    }

    @Test
    fun `Check server error SearchRequestException()`() {
        val e = SearchRequestException(TEST_MESSAGE, 500)

        assertEquals(false, e.isClientError())
        assertEquals(true, e.isServerError())
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = SearchRequestException::class,
            objectsFactory = ReflectionObjectsFactory(listOf(EXCEPTION_CREATOR))
        ).verify()
    }

    private companion object {
        const val TEST_MESSAGE = "Test message"

        val EXCEPTION_CREATOR = CustomTypeObjectCreatorImpl(Exception::class) { mode ->
            listOf(Exception(), IOException())[mode.ordinal]
        }
    }
}
