package com.mapbox.search.common

// TODO should ErrorsReporter functionality be moved to the common-sdk module?
object CommonErrorsReporter {
    var reporter: ((Throwable) -> Unit)? = null
}
