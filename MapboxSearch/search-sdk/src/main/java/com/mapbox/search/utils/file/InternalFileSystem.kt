package com.mapbox.search.utils.file

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.WorkerThread
import java.io.File
import java.nio.file.Files

@WorkerThread
internal class InternalFileSystem(
    val sdkVersionProvider: () -> Int = { Build.VERSION.SDK_INT }
) : FileSystem {

    override fun getAbsoluteDir(absoluteDir: String): File {
        return createFile(absoluteDir).apply {
            createDirectory(this)
        }
    }

    override fun getAppRelativeDir(context: Context, relativeDir: String): File {
        return createFile(context.filesDir, relativeDir).apply {
            createDirectory(this)
        }
    }

    override fun createFile(pathName: String) = File(pathName)

    override fun createFile(parent: File, child: String) = File(parent, child)

    override fun createDirectory(file: File) {
        if (sdkVersionProvider() >= Build.VERSION_CODES.O) {
            @SuppressLint("NewApi")
            if (!file.exists()) {
                try {
                    Files.createDirectory(file.toPath())
                } catch (_: FileAlreadyExistsException) {
                    // Race condition, the directory was created in another thread, do nothing
                }
            }
        } else {
            if (!file.exists() && !file.mkdirs()) {
                if (!file.exists()) {
                    throw IllegalStateException("Can not create dir at ${file.path}")
                }
                // Otherwise, race condition, the directory was created in another thread, do nothing
            }
        }
    }
}
