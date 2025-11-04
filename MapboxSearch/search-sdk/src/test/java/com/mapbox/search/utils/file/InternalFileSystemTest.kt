package com.mapbox.search.utils.file

import android.content.Context
import android.os.Build
import com.mapbox.search.common.tests.catchThrowable
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

internal class InternalFileSystemTest {

    private lateinit var file: File
    private lateinit var mockedFileSystem: FileSystem
    private lateinit var mockedContext: Context
    private lateinit var temporaryFolder: TemporaryFolder
    private lateinit var fullPath: String

    @BeforeEach
    fun setUp() {
        temporaryFolder = TemporaryFolder().apply {
            create()
        }

        mockedFileSystem = spyk<InternalFileSystem>()
        mockedContext = mockContext(temporaryFolder)

        mockkStatic(Files::class)
        every { Files.createDirectory(any()) } throws IOException()
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Files::class)
        temporaryFolder.delete()
    }

    @TestFactory
    fun `Check InternalFileSystem`() = TestCase {
        Given("InternalFileSystem") {
            file = mockedFileSystem.getAppRelativeDir(mockedContext, TEST_RELATIVE_DIR)
            fullPath = temporaryFolder.root.absolutePath + "/" + TEST_RELATIVE_DIR

            When("Call getAppRelativeDir") {
                Verify("Called getAppRelativeDir for FileSystem") {
                    mockedFileSystem.getAppRelativeDir(mockedContext, TEST_RELATIVE_DIR)
                }
                Verify("Called createFile with File and <$TEST_RELATIVE_DIR>") {
                    mockedFileSystem.createFile(any(), TEST_RELATIVE_DIR)
                }
                Verify("Called filesDir for Context") {
                    mockedContext.filesDir
                }
                Then("Directory exists") {
                    Assertions.assertEquals(true, file.exists())
                }
                Then("it's directory") {
                    Assertions.assertEquals(true, file.isDirectory)
                }
            }
        }
    }

    @TestFactory
    fun `Check InternalFileSystem with readonly FS`() = TestCase {
        Given("InternalFileSystem") {
            val readOnlyDirMock = mockk<File>().apply {
                every { exists() } returns false
                every { mkdirs() } returns false
                every { path } returns "test path"
            }

            every { mockedFileSystem.createFile(any(), any()) } returns readOnlyDirMock

            WhenThrows("Can't access folder", IllegalStateException::class) {
                file = mockedFileSystem.getAppRelativeDir(mockedContext, TEST_RELATIVE_DIR)
            }
        }
    }

    @TestFactory
    fun `Check InternalFileSystem with readonly FS on Android O`() = TestCase {
        Given("InternalFileSystem") {
            When("getAppRelativeDir() called") {
                val pathMock = mockk<Path>()
                val readOnlyDirMock = mockk<File>().apply {
                    every { exists() } returns false
                    every { path } returns "test path"
                    every { toPath() } returns pathMock
                }

                mockedFileSystem = spyk(InternalFileSystem { Build.VERSION_CODES.O }).apply {
                    every { createFile(any(), any()) } returns readOnlyDirMock
                }

                val thrown = catchThrowable<IOException> {
                    file = mockedFileSystem.getAppRelativeDir(mockedContext, TEST_RELATIVE_DIR)
                }

                Then("Thrown exception is IOException", true, thrown is IOException)

                Verify("Files.createDirectory() called") {
                    Files.createDirectory(eq(pathMock))
                }

                Verify("File.exists() called") {
                    readOnlyDirMock.exists()
                }

                Verify("File.toPath() called") {
                    readOnlyDirMock.toPath()
                }
            }
        }
    }

    private companion object {

        const val TEST_RELATIVE_DIR = "test_dir"

        fun mockContext(temporaryFolder: TemporaryFolder): Context {
            return mockk<Context>().apply {
                every { filesDir } answers { temporaryFolder.root }
            }
        }
    }
}
