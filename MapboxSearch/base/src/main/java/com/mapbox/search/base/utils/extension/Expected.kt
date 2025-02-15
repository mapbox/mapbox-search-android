package com.mapbox.search.base.utils.extension

import androidx.annotation.RestrictTo
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun <E, V, V1> Expected<E, V>.flatMap(transformer: (V) -> Expected<E, V1>): Expected<E, V1> {
    return if (isValue) {
        transformer(requireNotNull(value))
    } else {
        ExpectedFactory.createError(requireNotNull(error))
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
suspend fun <E, V, V1> Expected<E, V>.suspendFlatMap(transformer: suspend (V) -> Expected<E, V1>): Expected<E, V1> {
    return if (isValue) {
        transformer(requireNotNull(value))
    } else {
        ExpectedFactory.createError(requireNotNull(error))
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun <E, V> Expected<E, V>.toPrettyString(): String {
    return if (isValue) {
        "Value: $value"
    } else {
        "Error: $error"
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun <E, V> Expected<E, V>.equalsTo(e: Expected<E, V>): Boolean {
    return if (isValue && e.isValue) {
        value == e.value
    } else if (isError && e.isError) {
        error == e.error
    } else {
        false
    }
}
