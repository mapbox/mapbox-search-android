package com.mapbox.search.tests_support.offline

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import java.io.File
import java.io.InputStream

internal object AssetsUnpackUtils {

    @WorkerThread
    fun unpack(
        context: Context,
        baseDirPath: String,
        inputOutputDirs: List<Pair<String, String>>,
    ) {
        val baseDirFile = File(baseDirPath)
        baseDirFile.deleteRecursively()

        inputOutputDirs.forEach { (assetSourceDir, destinationDir) ->
            unpack(context, assetSourceDir, destinationDir)
        }
    }

    @WorkerThread
    private fun unpack(
        context: Context,
        assetSourceDir: String,
        destinationDir: String,
    ) {
        val destinationFile = File(destinationDir)
        destinationFile.deleteRecursively()

        if (!destinationFile.exists() && !destinationFile.mkdirs()) {
            throw IllegalStateException("Can't create dir ${destinationFile.path}")
        }

        val assets = context.assets
        val filesToCopy = assets.list(assetSourceDir) ?: emptyArray()

        Log.d("AssetsUnpackUtils",
            "Copying ${filesToCopy.size} files from assets $assetSourceDir to $destinationFile"
        )

        filesToCopy.forEach { fileName ->
            val assetFile = File(assetSourceDir, fileName)
            val f = File(destinationFile, fileName)
            f.createNewFile()
            assets.open(assetFile.path).use { assetFileStream ->
                f.copyInputStreamToFile(assetFileStream)
            }
        }

        Log.d("AssetsUnpackUtils", "Files in destination dir: ${destinationFile.list()?.toList()}")
    }

    private fun File.copyInputStreamToFile(inputStream: InputStream) {
        this.outputStream().use { fileOut ->
            inputStream.copyTo(fileOut)
        }
    }
}
