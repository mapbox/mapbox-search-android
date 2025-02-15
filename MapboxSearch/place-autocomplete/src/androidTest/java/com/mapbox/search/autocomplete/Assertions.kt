package com.mapbox.search.autocomplete

import com.mapbox.bindgen.Expected
import com.mapbox.search.base.utils.extension.equalsTo
import com.mapbox.search.base.utils.extension.toPrettyString
import org.junit.Assert.fail

internal fun <E, V> assertEqualsExpected(expected: Expected<E, V>, actual: Expected<E, V>) {
    if (!expected.equalsTo(actual)) {
        fail("Expected ${expected.toPrettyString()}, but was ${actual.toPrettyString()}")
    }
}
