@file:Suppress("NoMockkVerifyImport")
package com.mapbox.search.record

import com.mapbox.search.AsyncOperationTask
import com.mapbox.search.tests_support.BlockingCompletionCallback
import com.mapbox.search.tests_support.BlockingCompletionCallback.CompletionCallbackResult
import com.mapbox.search.tests_support.TestDataProviderEngineLayer
import com.mapbox.search.tests_support.TestExecutor
import com.mapbox.search.tests_support.TestThreadExecutorService
import com.mapbox.search.tests_support.assertEqualsJsonify
import com.mapbox.search.tests_support.createTestHistoryRecord
import com.mapbox.search.tests_support.record.getAllBlocking
import com.mapbox.test.dsl.TestCase
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

internal class LocalDataProviderTest {

    private lateinit var recordsStorage: RecordsStorage<HistoryRecord>
    private lateinit var executorService: ExecutorService
    private lateinit var executor: Executor

    private lateinit var dataProvider: TestDataProvider

    @BeforeEach
    fun setUp() {
        recordsStorage = mockk(relaxed = true)
        every { recordsStorage.load() } returns emptyList()

        executorService = spyk(TestThreadExecutorService())

        executor = spyk(TestExecutor())

        dataProvider = TestDataProvider(recordsStorage, executorService)
    }

    @TestFactory
    fun `Check initial load when storage has more records than max allowed records amount`() = TestCase {
        Given("TestDataProvider with mocked dependencies") {
            val testRecords = (1..100).map { index ->
                createTestHistoryRecord(
                    id = "test-history-record-$index",
                    name = "Test History #$index",
                    timestamp = index * 100L
                )
            }.shuffled()
            every { recordsStorage.load() } returns testRecords

            val maxRecordsAmount = 30
            When("TestDataProvider with max records amount = $maxRecordsAmount created") {
                val testDataProviderEngineLayer = TestDataProviderEngineLayer<HistoryRecord>()
                TestDataProvider(recordsStorage, executorService, listOf(testDataProviderEngineLayer), maxRecordsAmount)

                Then("TestDataProviderEngineLayer returns only $maxRecordsAmount records") {
                    assertEqualsJsonify(
                        testRecords.takeLast(maxRecordsAmount),
                        testDataProviderEngineLayer.records
                    )
                }
            }
        }
    }

    @TestFactory
    fun `LocalDataProvider with crashing data loading`() = TestCase {
        val testCases = mapOf<String, (TestDataProvider) -> Pair<CompletionCallbackResult<*>, AsyncOperationTask>>(
            "getAll()" to { dataProvider ->
                val callback = BlockingCompletionCallback<List<HistoryRecord>>()
                val task = dataProvider.getAll(executor, callback)
                callback.getResultBlocking() to task
            },
            "get()" to { dataProvider ->
                val callback = BlockingCompletionCallback<HistoryRecord?>()
                val task = dataProvider.get("test id", executor, callback)
                callback.getResultBlocking() to task
            },
            "contains()" to { dataProvider ->
                val callback = BlockingCompletionCallback<Boolean>()
                val task = dataProvider.contains("test id", executor, callback)
                callback.getResultBlocking() to task
            },
            "add()" to { dataProvider ->
                val callback = BlockingCompletionCallback<Unit>()
                val task = dataProvider.add(mockk(), executor, callback)
                callback.getResultBlocking() to task
            },
            "addAll()" to { dataProvider ->
                val callback = BlockingCompletionCallback<Unit>()
                val task = dataProvider.addAll(mockk(), executor, callback)
                callback.getResultBlocking() to task
            },
            "update" to { dataProvider ->
                val callback = BlockingCompletionCallback<Unit>()
                val task = dataProvider.update(mockk(), executor, callback)
                callback.getResultBlocking() to task
            },
            "remove()" to { dataProvider ->
                val callback = BlockingCompletionCallback<Boolean>()
                val task = dataProvider.remove("test-id", executor, callback)
                callback.getResultBlocking() to task
            },
            "clear()" to { dataProvider ->
                val callback = BlockingCompletionCallback<Unit>()
                val task = dataProvider.clear(executor, callback)
                callback.getResultBlocking() to task
            },
        )

        testCases.forEach { (functionName, testFunction) ->
            Given("LocalDataProvider with crashing data loading for $functionName test case") {
                clearAllMocks()

                val storageError = IOException("Can't load data")

                recordsStorage = mockk(relaxed = true)
                every { recordsStorage.load() } throws storageError

                val listener = mockk<LocalDataProvider.OnDataChangedListener<HistoryRecord>>(relaxed = true)

                When("LocalDataProvider.$functionName called") {
                    dataProvider = TestDataProvider(recordsStorage, executorService)

                    dataProvider.addOnDataChangedListener(listener)

                    Verify("Work completed on a background thread", exactly = 2) {
                        executorService.submit(any())
                    }

                    VerifyOnce("Storage.load() called") {
                        recordsStorage.load()
                    }

                    VerifyNo("Initial data didn't save to storage") {
                        recordsStorage.save(any())
                    }

                    VerifyOnce("Callbacks called inside executor") {
                        executor.execute(any())
                    }

                    VerifyNo("No change events sent to listener") {
                        listener.onDataChanged(any())
                    }

                    val (result, task) = testFunction(dataProvider)

                    Then(
                        "$functionName should return error",
                        true,
                        result is CompletionCallbackResult.Error
                    )

                    Then(
                        "Error should be from storage",
                        storageError,
                        (result as CompletionCallbackResult.Error).e
                    )

                    Then("Task is done", true, task.isDone)

                    Then("Task is not cancelled", false, task.isCancelled)
                }
            }
        }
    }

    @TestFactory
    fun `LocalDataProvider with crashing data saving`() = TestCase {
        val previouslySavedRecords = (1..3).map { id -> createTestHistoryRecord("saved record id-$id") }

        val testCases = mapOf<String, (TestDataProvider) -> Pair<CompletionCallbackResult<*>, AsyncOperationTask>>(
            "add()" to { dataProvider ->
                val callback = BlockingCompletionCallback<Unit>()
                val task = dataProvider.add(createTestHistoryRecord("new record id-1"), executor, callback)
                callback.getResultBlocking() to task
            },
            "addAll()" to { dataProvider ->
                val callback = BlockingCompletionCallback<Unit>()
                val task = dataProvider.addAll(listOf(createTestHistoryRecord("new record id-1")), executor, callback)
                callback.getResultBlocking() to task
            },
            "update" to { dataProvider ->
                val callback = BlockingCompletionCallback<Unit>()
                val task = dataProvider.update(createTestHistoryRecord("new record id-1"), executor, callback)
                callback.getResultBlocking() to task
            },
            "remove()" to { dataProvider ->
                val callback = BlockingCompletionCallback<Boolean>()
                val task = dataProvider.remove(previouslySavedRecords.first().id, executor, callback)
                callback.getResultBlocking() to task
            },
            "clear()" to { dataProvider ->
                val callback = BlockingCompletionCallback<Unit>()
                val task = dataProvider.clear(executor, callback)
                callback.getResultBlocking() to task
            },
        )

        testCases.forEach { (functionName, testFunction) ->
            Given("LocalDataProvider with crashing data saving for $functionName test case") {
                clearAllMocks()

                recordsStorage = mockk(relaxed = true)

                every { recordsStorage.load() } returns previouslySavedRecords

                val storageError = IOException("Can't save data")
                every { recordsStorage.save(any()) } throws storageError

                val listener = mockk<LocalDataProvider.OnDataChangedListener<HistoryRecord>>(relaxed = true)

                When("LocalDataProvider.$functionName called") {
                    dataProvider = TestDataProvider(
                        recordsStorage,
                        executorService,
                    )

                    dataProvider.addOnDataChangedListener(listener)

                    // Submit should be called 3 times:
                    // initial read + function itself + get all data to check test result
                    Verify("Work completed on a background thread", exactly = 3) {
                        executorService.submit(any())
                    }

                    VerifyOnce("Storage.load() called") {
                        recordsStorage.load()
                    }

                    Verify("Callbacks called inside executor") {
                        executor.execute(any())
                    }

                    VerifyNo("No change events sent to listener") {
                        listener.onDataChanged(any())
                    }

                    val (result, task) = testFunction(dataProvider)

                    Then(
                        "$functionName should return error",
                        true,
                        result is CompletionCallbackResult.Error
                    )

                    Then(
                        "Error should be from storage",
                        storageError,
                        (result as CompletionCallbackResult.Error).e
                    )

                    Then("Task is done", true, task.isDone)

                    Then("Task is not cancelled", false, task.isCancelled)

                    VerifyOnce("Storage.save()") {
                        recordsStorage.save(any())
                    }

                    Then(
                        "Saved records shouldn't change",
                        previouslySavedRecords,
                        dataProvider.getAllBlocking(executor)
                    )
                }
            }
        }
    }

    // TODO add other LocalDataProvider functions

    private class TestDataProvider(
        recordsStorage: RecordsStorage<HistoryRecord>,
        backgroundTaskExecutorService: ExecutorService,
        dataProviderEngineLayers: List<IndexableDataProviderEngineLayer> = emptyList(),
        maxRecordsAmount: Int = Int.MAX_VALUE,
    ) : LocalDataProviderImpl<HistoryRecord>(
        dataProviderName = "Test data provider",
        recordsStorage = recordsStorage,
        backgroundTaskExecutorService = backgroundTaskExecutorService,
        dataProviderEngineLayers = CopyOnWriteArrayList(dataProviderEngineLayers),
        maxRecordsAmount = maxRecordsAmount,
    ) {

        override fun MutableMap<String, HistoryRecord>.addAndTrimRecords(newRecords: List<HistoryRecord>): List<HistoryRecord> {
            putAll(newRecords.map { it.id to it })

            return if (size <= maxRecordsAmount) {
                 emptyList()
            } else {
                val entriesToRemove = entries.take(size - maxRecordsAmount)
                entriesToRemove.forEach { (key, _) ->
                    remove(key)
                }
                entriesToRemove.map { it.value }
            }
        }
    }
}
