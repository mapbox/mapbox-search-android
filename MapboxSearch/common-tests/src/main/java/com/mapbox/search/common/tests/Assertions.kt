package com.mapbox.search.common.tests

import com.mapbox.bindgen.Expected
import org.junit.Assert.fail

private fun <E, V> Expected<E, V>.toPrettyString(): String {
    return if (isValue) {
        "Value: $value"
    } else {
        "Error: $error"
    }
}

private fun <E, V> Expected<E, V>.equalsTo(e: Expected<E, V>): Boolean {
    return if (isValue && e.isValue) {
        value == e.value
    } else if (isError && e.isError) {
        error == e.error
    } else {
        false
    }
}

fun <E, V> assertEqualsExpected(expected: Expected<E, V>, actual: Expected<E, V>) {
    if (!expected.equalsTo(actual)) {
        fail("Expected ${expected.toPrettyString()}, but was ${actual.toPrettyString()}")
    }
}
