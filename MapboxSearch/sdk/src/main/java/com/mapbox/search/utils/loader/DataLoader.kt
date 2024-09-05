package com.mapbox.search.utils.loader

import androidx.annotation.WorkerThread

@WorkerThread
public interface DataLoader<T> {

    public fun load(relativeDir: String, fileName: String): T

    public fun save(relativeDir: String, fileName: String, data: T)
}
