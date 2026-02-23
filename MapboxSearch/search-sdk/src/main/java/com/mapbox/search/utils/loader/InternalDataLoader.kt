package com.mapbox.search.utils.loader

import android.content.Context
import android.util.AtomicFile
import androidx.annotation.WorkerThread
import com.mapbox.search.base.logger.logd
import com.mapbox.search.utils.file.FileSystem
import java.io.File
import java.io.FileOutputStream

@WorkerThread
internal class InternalDataLoader(
    private val context: Context,
    private val fileHelper: FileSystem,
    private val atomicFileFactory: (File) -> AtomicFile = { AtomicFile(it) }
) : DataLoader<ByteArray> {

    override fun load(relativeDir: String, fileName: String): ByteArray {
        logd("Loading file $relativeDir/$fileName")

        val dir = fileHelper.getAppRelativeDir(context, relativeDir)
        val file = fileHelper.createFile(dir, fileName)
        return if (!file.exists()) {
            ByteArray(0)
        } else {
            file.readBytes()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override fun save(relativeDir: String, fileName: String, data: ByteArray) {
        logd("Saving file $relativeDir/$fileName")

        val dir = fileHelper.getAppRelativeDir(context, relativeDir)
        val file = fileHelper.createFile(dir, fileName)

        val atomicFile = atomicFileFactory(file)
        var fos: FileOutputStream? = null

        try {
            fos = atomicFile.startWrite()
            fos.write(data)
            fos.flush()
            atomicFile.finishWrite(fos)
        } catch (t: Throwable) {
            atomicFile.failWrite(fos)
            throw t
        }
    }
}
