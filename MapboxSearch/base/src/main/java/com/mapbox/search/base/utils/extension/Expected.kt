package com.mapbox.search.base.utils.extension

import com.mapbox.bindgen.Expected

fun <E, V> Expected<E, V>.realToString(): String {
    return if (isValue) {
        "Value: $value"
    } else {
        "Error: $error"
    }
}
