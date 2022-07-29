package com.mapbox.search.base.utils.extension

fun String?.nullIfEmpty(): String? {
    return if (isNullOrEmpty()) {
        null
    } else {
        this
    }
}
