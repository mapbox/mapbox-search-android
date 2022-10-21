package com.mapbox.search.autofill

import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

internal class QueryTest {

    @TestFactory
    fun `Check Query's min length`() = TestCase {
        Given("${Query::class.java.simpleName} class") {
            When("MIN_QUERY_LENGTH accessed") {
                Then("MIN_QUERY_LENGTH should be 2", 2, Query.MIN_QUERY_LENGTH)
            }
        }
    }

    @TestFactory
    fun `Check Query's create() function`() = TestCase {
        Given("${Query::class.java.simpleName} class") {
            (0..Query.MIN_QUERY_LENGTH)
                .map { length -> "a".repeat(length) }
                .forEach { query ->
                    When("create($query) called") {
                        val value = if (query.length < 2) {
                            null
                        } else {
                            Query(query)
                        }
                        Then("Created value should be $value", value, Query.create(query))
                    }
                }
        }
    }

    @TestFactory
    fun `Check Query's equals() and hashCode()`() = TestCase {
        Given("${Query::class.java.simpleName} class") {
            When("Call equals() and hashCode()") {
                Then("equals() and hashCode() should be implemented correctly") {
                    EqualsVerifier.forClass(Query::class.java).verify()
                }
            }
        }
    }

    @TestFactory
    fun `Check Query's toString()`() = TestCase {
        Given("${Query::class.java.simpleName} class") {
            When("Call toString()") {
                Then("toString() function should be implemented correctly") {
                    ToStringVerifier(clazz = Query::class).verify()
                }
            }
        }
    }
}
