package com.mapbox.search.utils.loader

import androidx.annotation.WorkerThread

@WorkerThread
internal interface DataLoader<T> {

    fun load(relativeDir: String, fileName: String): T

    fun save(relativeDir: String, fileName: String, data: T)
}
