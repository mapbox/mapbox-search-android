package com.mapbox.search.tests_support

import com.mapbox.search.utils.SyncLocker

internal class TestSyncLocker : SyncLocker {
    override fun <T> executeInSync(action: () -> T): T {
        return action()
    }
}
