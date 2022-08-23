package com.mapbox.search.base.tests_support

import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Assertions
import org.opentest4j.AssertionFailedError
import java.lang.reflect.Modifier

private val GSON = GsonBuilder()
    .setPrettyPrinting()
    .disableHtmlEscaping()
    .setExclusionStrategies()
    .excludeFieldsWithModifiers(Modifier.STATIC) // serialize transient fields also
    .create()

/**
 * Same as [Assertions.assertEquals], but in case of error we show diff
 * between serialized JSON representation of objects, rather than diff
 * between object's [toString] Strings.
 */
internal fun assertEqualsJsonify(expectedValue: Any?, actualValue: Any?) {
    try {
        Assertions.assertEquals(expectedValue, actualValue)
    } catch (ex: AssertionFailedError) {
        val expectedJson = if (expectedValue != null) GSON.toJson(expectedValue) else null
        val actualJson = if (actualValue != null) GSON.toJson(actualValue) else null
        throw AssertionFailedError(ex.message, expectedJson, actualJson)
    }
}
