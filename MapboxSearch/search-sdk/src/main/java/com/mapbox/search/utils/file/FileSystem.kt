package com.mapbox.search.utils.file

import android.content.Context
import androidx.annotation.WorkerThread
import java.io.File

@WorkerThread
internal interface FileSystem {

    fun getAbsoluteDir(absoluteDir: String): File

    fun getAppRelativeDir(context: Context, relativeDir: String): File

    fun createFile(pathName: String): File

    fun createFile(parent: File, child: String): File
}
