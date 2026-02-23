@file:Suppress("NoMockkVerifyImport")
package com.mapbox.search.utils.loader

import android.content.Context
import android.util.AtomicFile
import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.FileOutputStream
import java.io.IOException

internal class InternalDataLoaderTest {

    private lateinit var dataLoader: InternalDataLoader

    private lateinit var fileSystem: TestFileSystem
    private lateinit var context: Context

    private lateinit var atomicFile: AtomicFile
    private lateinit var atomicFileFOS: FileOutputStream

    @BeforeEach
    fun setUp() {
        fileSystem = spyk<TestFileSystem>()
        fileSystem.init()

        atomicFileFOS = spyk(
            fileSystem.createOutputStream(
                RELATIVE_DIR,
                fileName = FILE_NAME,
            )
        )

        atomicFile = mockk<AtomicFile>(relaxed = true)
        every { atomicFile.startWrite() } returns atomicFileFOS

        context = mockk<Context>()

        dataLoader = InternalDataLoader(
            context,
            fileSystem,
            { atomicFile }
        )
    }

    @AfterEach
    fun tearDown() {
        fileSystem.destroy()
    }

    @Test
    fun testLoadFromEmptyFile() {
        val byteArray = dataLoader.load(RELATIVE_DIR, FILE_NAME)
        assertArrayEquals(byteArray, EMPTY_ARRAY)
        verify {
            fileSystem.getAppRelativeDir(context, RELATIVE_DIR)
        }
        verify {
            fileSystem.createFile(any(), FILE_NAME)
        }
    }

    @Test
    fun testLoadFromExistingFile() {
        fileSystem.writeBytesToFile(
            fileName = FILE_NAME,
            byteArray = TEST_ARRAY,
            folderNames = arrayOf(RELATIVE_DIR)
        )

        val byteArray = dataLoader.load(RELATIVE_DIR, FILE_NAME)
        assertArrayEquals(byteArray, TEST_ARRAY)

        verify {
            fileSystem.getAppRelativeDir(context, RELATIVE_DIR)
        }
        verify {
            fileSystem.createFile(any(), FILE_NAME)
        }
    }

    @Test
    fun testFileSystemWithError() {
        fileSystem.init(throwException = true)

        val thrown = assertThrows(IllegalStateException::class.java) {
            dataLoader.load(RELATIVE_DIR, FILE_NAME)
        }

        assertTrue(thrown?.message?.startsWith("Can not create dir at") == true)
    }

    @Test
    fun testSaveToEmptyFile() {
        dataLoader.save(RELATIVE_DIR, FILE_NAME, TEST_ARRAY)

        val byteArray = fileSystem.getBytesFromFile(RELATIVE_DIR, fileName = FILE_NAME)
        assertArrayEquals(TEST_ARRAY, byteArray)

        verify {
            fileSystem.getAppRelativeDir(context, RELATIVE_DIR)
            fileSystem.createFile(any(), FILE_NAME)
        }

        verify(exactly = 1) {
            atomicFile.finishWrite(any())
        }

        verify(exactly = 0) {
            atomicFile.failWrite(any())
        }

        verify(exactly = 1) {
            atomicFileFOS.write(eq(TEST_ARRAY))
            atomicFileFOS.flush()
        }
    }

    @Test
    fun testSaveToExistingFile() {
        fileSystem.writeBytesToFile(
            fileName = FILE_NAME,
            byteArray = TEST_ARRAY.reversedArray(),
            folderNames = arrayOf(RELATIVE_DIR)
        )

        dataLoader.save(RELATIVE_DIR, FILE_NAME, TEST_ARRAY)

        val byteArray = fileSystem.getBytesFromFile(RELATIVE_DIR, fileName = FILE_NAME)
        assertArrayEquals(TEST_ARRAY, byteArray)

        verify {
            fileSystem.getAppRelativeDir(context, RELATIVE_DIR)
            fileSystem.createFile(any(), FILE_NAME)
        }

        verify(exactly = 1) {
            atomicFile.finishWrite(any())
        }

        verify(exactly = 0) {
            atomicFile.failWrite(any())
        }

        verify(exactly = 1) {
            atomicFileFOS.write(eq(TEST_ARRAY))
            atomicFileFOS.flush()
        }
    }

    @Test
    fun testAtomicFileFailWriteOnError() {
        val error = IOException()
        every { atomicFile.startWrite() } throws error

        val thrown = assertThrows(error.javaClass) {
            dataLoader.save(RELATIVE_DIR, FILE_NAME, TEST_ARRAY)
        }

        verify(exactly = 1) {
            atomicFile.failWrite(any())
        }

        assertSame(error, thrown)
    }

    companion object {

        private const val RELATIVE_DIR = "dir"
        private const val FILE_NAME = "file.txt"
        private val EMPTY_ARRAY = ByteArray(0)
        private val TEST_ARRAY = byteArrayOf(52, 32, 53, 75, 77, 65, 98)

        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            resetLogImpl()
        }

        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            reinitializeLogImpl()
        }
    }
}
