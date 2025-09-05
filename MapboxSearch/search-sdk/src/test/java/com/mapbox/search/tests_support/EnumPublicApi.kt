package com.mapbox.search.tests_support

import org.junit.jupiter.api.Assertions
import java.lang.Exception

internal fun <T : Enum<T>> checkEnumValues(expectedValues: List<String>, clazz: Class<T>) {
    check(clazz.isEnum)
    val enumValues = clazz.enumConstants!!

    Assertions.assertEquals(
        expectedValues.size,
        enumValues.size,
        "Size is not the same, expected ${expectedValues.size}"
    )

    expectedValues.forEachIndexed { index, value ->
        val enumValue: T
        try {
            enumValue = enumValues.find { it.name == value }!!
        } catch (e: Exception) {
            Assertions.fail<Unit>("${clazz.name} doesn't have $value value")
            return
        }
        Assertions.assertEquals(
            value,
            enumValue.name,
            "$enumValue name doesn't match, expected $value, but was ${enumValue.name}"
        )
        Assertions.assertEquals(
            index,
            enumValue.ordinal,
            "$enumValue order doesn't match, expected $index, but was ${enumValue.ordinal}"
        )
    }
}
