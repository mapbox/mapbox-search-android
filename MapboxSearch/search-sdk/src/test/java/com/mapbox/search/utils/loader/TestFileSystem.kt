package com.mapbox.search.utils.loader

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.mapbox.search.utils.file.FileSystem
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files

internal class TestFileSystem : FileSystem {

    private val temporaryFolder = TemporaryFolder()
    private var throwException: Boolean = false

    fun init(throwException: Boolean = false) {
        temporaryFolder.create()
        this.throwException = throwException
    }

    fun destroy() {
        temporaryFolder.delete()
    }

    fun createDir(vararg folderNames: String): File {
        var dir = getRoot()
        folderNames.forEach { folderName ->
            dir = File(dir, folderName)
            dir.mkdir()
        }
        return dir
    }

    fun writeBytesToFile(vararg folderNames: String, fileName: String, byteArray: ByteArray) {
        val dir = createDir(*folderNames)
        val file = createFile(dir, fileName)
        file.writeBytes(byteArray)
    }

    fun getBytesFromFile(vararg folderNames: String, fileName: String): ByteArray {
        val dir = createDir(*folderNames)
        val file = createFile(dir, fileName)
        return file.readBytes()
    }

    fun getRoot(): File {
        return temporaryFolder.root
    }

    fun createOutputStream(vararg folderNames: String, fileName: String): FileOutputStream {
        val dir = createDir(*folderNames)
        val file = createFile(dir, fileName)
        return FileOutputStream(file)
    }

    // TODO refactor InternalFileSystem. Join getAppRelativeDir and getAbsoluteDir
    override fun getAbsoluteDir(absoluteDir: String): File {
        val dir = File(absoluteDir)
        if ((!dir.exists() && !dir.mkdirs()) || throwException) {
            throw IllegalStateException("Can not create dir at ${dir.path}")
        }
        return dir
    }

    override fun getAppRelativeDir(context: Context, relativeDir: String): File {
        val dir = File(temporaryFolder.root, relativeDir)
        return getAbsoluteDir(dir.absolutePath)
    }

    override fun createFile(pathName: String) = File(pathName)

    override fun createFile(parent: File, child: String) = File(parent, child)

    override fun createDirectory(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("NewApi")
            if (!file.exists()) {
                Files.createDirectory(file.toPath())
            }
        } else {
            if (!file.exists() && !file.mkdirs()) {
                throw IllegalStateException("Can not create dir at ${file.path}")
            }
        }
    }
}
