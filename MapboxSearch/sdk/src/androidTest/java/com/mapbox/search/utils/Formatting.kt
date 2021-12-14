package com.mapbox.search.utils

internal fun String.removeJsonPrettyPrinting(): String {
    return replace("\\s\\s+".toRegex(), "")
        .replace(": ", ":")
        .replace("\n", "")
}
