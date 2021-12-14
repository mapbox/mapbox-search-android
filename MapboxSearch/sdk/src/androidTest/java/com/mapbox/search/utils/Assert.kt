package com.mapbox.search.utils

import org.junit.Assert

internal fun assertEqualsButNotSame(expected: Any?, actual: Any?) {
    Assert.assertNotSame(expected, actual)
    Assert.assertEquals(expected, actual)
}

internal fun assertEqualsIgnoreCase(expected: String, actual: String) {
    Assert.assertEquals(expected.toLowerCase(), actual.toLowerCase())
}
