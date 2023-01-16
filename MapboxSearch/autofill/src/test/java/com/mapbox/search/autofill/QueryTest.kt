package com.mapbox.search.autofill

import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class QueryTest {

    @Test
    fun `Check Query's min length`() {
        assertEquals(2, Query.MIN_QUERY_LENGTH)
    }

    @Test
    fun `Check Query's create() function`() {
        (0..Query.MIN_QUERY_LENGTH)
            .map { length -> "a".repeat(length) }
            .forEach { query ->
                val value = if (query.length < 2) {
                    null
                } else {
                    Query(query)
                }
                assertEquals(value, Query.create(query))
            }
    }

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(Query::class.java).verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(clazz = Query::class).verify()
    }
}
