package com.mapbox.search.utils

import org.junit.Assert
import java.util.Locale

internal fun assertEqualsButNotSame(expected: Any?, actual: Any?) {
    Assert.assertNotSame(expected, actual)
    Assert.assertEquals(expected, actual)
}

internal fun assertEqualsIgnoreCase(expected: String, actual: String) {
    Assert.assertEquals(expected.lowercase(Locale.getDefault()), actual.lowercase(Locale.getDefault()))
}
