package com.mapbox.search.utils.file

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import java.io.File
import java.nio.file.Files

@WorkerThread
internal class InternalFileSystem(
    val sdkVersionProvider: () -> Int = { Build.VERSION.SDK_INT }
) : FileSystem {

    @Synchronized
    override fun getAbsoluteDir(absoluteDir: String): File {
        val dir = createFile(absoluteDir)

        if (sdkVersionProvider() >= Build.VERSION_CODES.O) {
            @RequiresApi(Build.VERSION_CODES.O)
            if (!dir.exists()) {
                Files.createDirectory(dir.toPath())
            }
        } else {
            if (!dir.exists() && !dir.mkdirs()) {
                throw IllegalStateException("Can not create dir at ${dir.path}")
            }
        }
        return dir
    }

    override fun getAppRelativeDir(context: Context, relativeDir: String): File {
        val dir = createFile(context.filesDir, relativeDir)
        return getAbsoluteDir(dir.absolutePath)
    }

    override fun createFile(pathName: String) = File(pathName)

    override fun createFile(parent: File, child: String) = File(parent, child)
}
