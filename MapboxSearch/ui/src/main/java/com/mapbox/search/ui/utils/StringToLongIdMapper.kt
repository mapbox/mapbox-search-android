package com.mapbox.search.ui.utils

internal class StringToLongIdMapper {

    private val map = mutableMapOf<String, Long>()
    private var currentId = 0L

    fun getId(string: String): Long {
        return map.getOrPut(string) {
            currentId++
        }
    }
}
