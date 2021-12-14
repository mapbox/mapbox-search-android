package com.mapbox.search.utils.serialization

internal interface DataAccessObject<T> {

    val isValid: Boolean

    fun createData(): T
}
