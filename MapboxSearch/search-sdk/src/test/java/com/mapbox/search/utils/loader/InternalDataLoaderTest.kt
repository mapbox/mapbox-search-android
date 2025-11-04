package com.mapbox.search.utils.loader

import android.content.Context
import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import com.mapbox.test.dsl.TestCase
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestFactory

internal class InternalDataLoaderTest {

    private lateinit var dataLoader: InternalDataLoader
    private lateinit var byteArray: ByteArray

    @TestFactory
    fun `Check InternalDataLoader load`() = TestCase {
        Given("Empty file system (without file)") {
            val fileSystem = spyk<TestFileSystem>()
            val context = mockContext()

            Before {
                fileSystem.init()
                dataLoader = InternalDataLoader(context, fileSystem)
                byteArray = dataLoader.load(RELATIVE_DIR, FILE_NAME)
            }
            After {
                fileSystem.destroy()
            }

            When("Call load") {
                Then("Should load empty array") {
                    Assertions.assertArrayEquals(byteArray, EMPTY_ARRAY)
                }
                Verify("Called fileSystem.getAppRelativeDir") {
                    fileSystem.getAppRelativeDir(context, RELATIVE_DIR)
                }
                Verify("Called fileSystem.createFile") {
                    fileSystem.createFile(any(), FILE_NAME)
                }
            }
        }

        Given("File system with file") {
            val fileSystem = spyk<TestFileSystem>()
            val context = mockContext()

            Before {
                fileSystem.init()
                fileSystem.writeBytesToFile(
                    fileName = FILE_NAME,
                    byteArray = TEST_ARRAY,
                    folderNames = arrayOf(RELATIVE_DIR)
                )
                dataLoader = InternalDataLoader(context, fileSystem)
                byteArray = dataLoader.load(RELATIVE_DIR, FILE_NAME)
            }
            After {
                fileSystem.destroy()
            }

            When("Call load") {
                Then("Should read test array") {
                    Assertions.assertArrayEquals(byteArray, TEST_ARRAY)
                }
                Verify("Called fileSystem.getAppRelativeDir") {
                    fileSystem.getAppRelativeDir(context, RELATIVE_DIR)
                }
                Verify("Called fileSystem.createFile") {
                    fileSystem.createFile(any(), FILE_NAME)
                }
            }
        }

        Given("File system with error") {
            val fileSystem = spyk<TestFileSystem>()
            val context = mockContext()

            Before {
                fileSystem.init(throwException = true)
                dataLoader = InternalDataLoader(context, fileSystem)
            }
            After {
                fileSystem.destroy()
            }

            WhenThrows("Can't open file", IllegalStateException::class) {
                byteArray = dataLoader.load(RELATIVE_DIR, FILE_NAME)
            }
        }
    }

    @TestFactory
    fun `Check InternalDataLoader save`() = TestCase {
        Given("Empty file system (without file)") {
            val fileSystem = spyk<TestFileSystem>()
            val context = mockContext()

            Before {
                fileSystem.init()
                dataLoader = InternalDataLoader(context, fileSystem)
                dataLoader.save(RELATIVE_DIR, FILE_NAME, TEST_ARRAY)
                byteArray = fileSystem.getBytesFromFile(RELATIVE_DIR, fileName = FILE_NAME)
            }
            After {
                fileSystem.destroy()
            }

            When("Call import") {
                Then("test") {
                    Assertions.assertArrayEquals(TEST_ARRAY, byteArray)
                }
                Verify("Called fileSystem.getAppRelativeDir") {
                    fileSystem.getAppRelativeDir(context, RELATIVE_DIR)
                }
                Verify("Called fileSystem.createFile") {
                    fileSystem.createFile(any(), FILE_NAME)
                }
            }
        }

        Given("File with some data") {
            val fileSystem = spyk<TestFileSystem>()
            val context = mockContext()

            Before {
                fileSystem.init()
                dataLoader = InternalDataLoader(context, fileSystem)
                fileSystem.writeBytesToFile(
                    fileName = FILE_NAME,
                    byteArray = TEST_ARRAY.reversedArray(),
                    folderNames = arrayOf(RELATIVE_DIR)
                )
                dataLoader.save(RELATIVE_DIR, FILE_NAME, TEST_ARRAY)
                byteArray = fileSystem.getBytesFromFile(RELATIVE_DIR, fileName = FILE_NAME)
            }
            After {
                fileSystem.destroy()
            }

            When("Call save") {
                Then("Should read from file new data") {
                    Assertions.assertArrayEquals(TEST_ARRAY, byteArray)
                }
                Verify("Called fileSystem.getAppRelativeDir") {
                    fileSystem.getAppRelativeDir(context, RELATIVE_DIR)
                }
                Verify("Called fileSystem.createFile") {
                    fileSystem.createFile(any(), FILE_NAME)
                }
            }
        }
    }

    private fun mockContext() = mockk<Context>()

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
