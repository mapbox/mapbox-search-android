package com.mapbox.search.common

// https://jqno.nl/equalsverifier/errormessages/double-equals-doesnt-use-doublecompare-for-field-foo/

@JvmSynthetic
internal fun Float?.safeCompareTo(other: Float?): Boolean {
    return when {
        this == null -> other == null
        other == null -> false
        else -> compareTo(other) == 0
    }
}

@JvmSynthetic
internal fun Double?.safeCompareTo(other: Double?): Boolean {
    return when {
        this == null -> other == null
        other == null -> false
        else -> compareTo(other) == 0
    }
}
