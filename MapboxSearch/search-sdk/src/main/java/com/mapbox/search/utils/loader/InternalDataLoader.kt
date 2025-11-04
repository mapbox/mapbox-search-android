package com.mapbox.search.utils.loader

import android.content.Context
import androidx.annotation.WorkerThread
import com.mapbox.search.base.logger.logd
import com.mapbox.search.utils.file.FileSystem

@WorkerThread
internal class InternalDataLoader(
    private val context: Context,
    private val fileHelper: FileSystem
) : DataLoader<ByteArray> {

    override fun load(relativeDir: String, fileName: String): ByteArray {
        logd("Loading file $fileName from $relativeDir")

        val dir = fileHelper.getAppRelativeDir(context, relativeDir)
        val file = fileHelper.createFile(dir, fileName)
        return if (!file.exists()) {
            ByteArray(0)
        } else {
            file.readBytes()
        }
    }

    override fun save(relativeDir: String, fileName: String, data: ByteArray) {
        val dir = fileHelper.getAppRelativeDir(context, relativeDir)
        val file = fileHelper.createFile(dir, fileName)
        file.writeBytes(data)
    }
}
