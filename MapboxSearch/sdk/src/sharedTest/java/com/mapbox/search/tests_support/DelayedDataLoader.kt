package com.mapbox.search.tests_support

import com.mapbox.search.utils.loader.DataLoader

internal class DelayedDataLoader(
    private val delayInMillis: Long
) : DataLoader<ByteArray> {
    override fun load(relativeDir: String, fileName: String): ByteArray {
        Thread.sleep(delayInMillis)
        return byteArrayOf()
    }

    override fun save(relativeDir: String, fileName: String, data: ByteArray) {
        Thread.sleep(delayInMillis)
    }
}
