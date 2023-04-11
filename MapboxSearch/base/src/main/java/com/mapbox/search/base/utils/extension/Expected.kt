package com.mapbox.search.base.utils.extension

import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory

fun <E, V, V1> Expected<E, V>.flatMap(transformer: (V) -> Expected<E, V1>): Expected<E, V1> {
    return if (isValue) {
        transformer(requireNotNull(value))
    } else {
        ExpectedFactory.createError(requireNotNull(error))
    }
}

suspend fun <E, V, V1> Expected<E, V>.suspendFlatMap(transformer: suspend (V) -> Expected<E, V1>): Expected<E, V1> {
    return if (isValue) {
        transformer(requireNotNull(value))
    } else {
        ExpectedFactory.createError(requireNotNull(error))
    }
}

fun <E, V> Expected<E, V>.realToString(): String {
    return if (isValue) {
        "Value: $value"
    } else {
        "Error: $error"
    }
}
