package com.mapbox.search.utils

internal interface SyncLocker {
    fun <T> executeInSync(action: () -> T): T
}

internal class SyncLockerImpl(
    private val lock: Any = Any(),
) : SyncLocker {

    override fun <T> executeInSync(action: () -> T): T {
        return synchronized(lock) {
            action()
        }
    }
}
