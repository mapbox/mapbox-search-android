package com.mapbox.search.base.utils.extension

// https://jqno.nl/equalsverifier/errormessages/double-equals-doesnt-use-doublecompare-for-field-foo/
fun Float?.safeCompareTo(other: Float?): Boolean {
    return when {
        this == null -> other == null
        other == null -> false
        else -> compareTo(other) == 0
    }
}
