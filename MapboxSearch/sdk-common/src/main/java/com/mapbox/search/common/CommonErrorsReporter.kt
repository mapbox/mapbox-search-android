package com.mapbox.search.common

object CommonErrorsReporter {
    var reporter: ((Throwable) -> Unit)? = null
}
