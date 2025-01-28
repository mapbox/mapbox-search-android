package com.mapbox.search.record

import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreRoutablePoint
import com.mapbox.search.base.core.createCoreResultMetadata
import com.mapbox.search.base.result.mapToBase
import com.mapbox.search.base.utils.TimeProvider
import com.mapbox.search.base.utils.extension.mapToPlatform
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.common.concurrent.MainThreadWorker
import com.mapbox.search.common.tests.TestExecutor
import com.mapbox.search.common.tests.TestThreadExecutorService
import com.mapbox.search.common.tests.createTestCoreSearchResult
import com.mapbox.search.internal.bindgen.ResultType
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.tests_support.BlockingCompletionCallback
import com.mapbox.search.tests_support.TestDataProviderEngine
import com.mapbox.search.tests_support.TestMainThreadWorker
import com.mapbox.search.tests_support.assertEqualsJsonify
import com.mapbox.search.tests_support.createTestHistoryRecord
import com.mapbox.search.tests_support.createTestIndexableRecordSearchResult
import com.mapbox.search.tests_support.createTestRequestOptions
import com.mapbox.search.tests_support.createTestServerSearchResult
import com.mapbox.search.tests_support.record.addToHistoryIfNeededBlocking
import com.mapbox.search.tests_support.record.getAllBlocking
import com.mapbox.search.tests_support.record.getBlocking
import com.mapbox.search.tests_support.record.getSizeBlocking
import com.mapbox.search.tests_support.record.registerIndexableDataProviderEngineBlocking
import com.mapbox.search.tests_support.record.upsertAllBlocking
import com.mapbox.search.tests_support.record.upsertBlocking
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

@Suppress("LargeClass")
internal class HistoryDataProviderTest {

    private lateinit var testDataProviderEngine: TestDataProviderEngine<HistoryRecord>
    private lateinit var recordsStorage: RecordsFileStorage<HistoryRecord>
    private lateinit var executorService: ExecutorService
    private lateinit var timeProvider: TimeProvider
    private lateinit var mainThreadWorker: MainThreadWorker
    private lateinit var executor: Executor

    private lateinit var historyDataProvider: HistoryDataProviderImpl

    @BeforeEach
    fun setUp() {
        testDataProviderEngine = TestDataProviderEngine()

        recordsStorage = mockk(relaxed = true)
        every { recordsStorage.load() } returns emptyList()

        executorService = spyk(TestThreadExecutorService())

        timeProvider = mockk()
        every { timeProvider.currentTimeMillis() } returns TEST_LOCAL_TIME_MILLIS

        mainThreadWorker = spyk(TestMainThreadWorker())

        executor = spyk(TestExecutor())

        historyDataProvider = HistoryDataProviderImpl(
            recordsStorage, executorService, timeProvider
        )
        historyDataProvider.registerIndexableDataProviderEngineBlocking(testDataProviderEngine, executor)
    }

    @TestFactory
    fun `Check history provider name`() = TestCase {
        Given("HistoryDataProviderImpl with mocked dependencies") {
            When("get dataProviderName") {
                Then(
                    "Provider name should be ${HistoryDataProvider.PROVIDER_NAME}",
                    HistoryDataProvider.PROVIDER_NAME,
                    historyDataProvider.dataProviderName
                )
            }
        }
    }

    @TestFactory
    fun `Check load initial data at provider creation`() = TestCase {
        Given("HistoryDataProviderImpl with mocked dependencies") {
            recordsStorage = mockk()
            every { recordsStorage.load() } returns listOf(TEST_HISTORY_RECORD_1, TEST_HISTORY_RECORD_2)

            When("HistoryDataProviderImpl created") {
                historyDataProvider = HistoryDataProviderImpl(
                    recordsStorage,
                    executorService,
                    timeProvider
                )
                historyDataProvider.registerIndexableDataProviderEngineBlocking(testDataProviderEngine, executor)

                Verify("Data loaded from storage", exactly = 1) {
                    recordsStorage.load()
                }

                Verify("Initial data didn't save to storage", exactly = 0) {
                    recordsStorage.save(any())
                }

                Then("Layer should have initial data") {
                    assertEquals(
                        listOf(TEST_HISTORY_RECORD_1, TEST_HISTORY_RECORD_2),
                        historyDataProvider.getAllBlocking(executor)
                    )
                }

                Then("TestDataProviderEngine should have initial data") {
                    assertEqualsJsonify(
                        listOf(TEST_HISTORY_RECORD_1, TEST_HISTORY_RECORD_2),
                        testDataProviderEngine.records.sortedBy { it.timestamp }
                    )
                }
            }
        }
    }

    @TestFactory
    fun `Check initial load when storage has more records than max allowed records amount`() = TestCase {
        Given("HistoryDataProviderImpl with mocked dependencies") {
            recordsStorage = mockk()
            val testRecords = (1..100).map { index ->
                createTestHistoryRecord(
                    id = "test-history-record-$index",
                    name = "Test History #$index",
                    timestamp = index * 100L
                )
            }.shuffled()
            every { recordsStorage.load() } returns testRecords

            val maxRecordsAmount = 30
            When("HistoryDataProviderImpl with max records amount = $maxRecordsAmount created") {
                historyDataProvider = HistoryDataProviderImpl(
                    recordsStorage,
                    executorService,
                    timeProvider,
                    maxRecordsAmount
                )
                historyDataProvider.registerIndexableDataProviderEngineBlocking(testDataProviderEngine, executor)

                val expectedRecords = testRecords.sortedBy { it.timestamp }.takeLast(maxRecordsAmount)

                Then("HistoryDataProvider returns only $maxRecordsAmount records with the latest timestamp") {
                    assertEqualsJsonify(
                        expectedRecords,
                        historyDataProvider.getAllBlocking(executor).sortedBy { it.timestamp }
                    )
                }

                Then("TestDataProviderEngine returns only $maxRecordsAmount records with the latest timestamp") {
                    assertEqualsJsonify(
                        expectedRecords,
                        testDataProviderEngine.records.sortedBy { it.timestamp }
                    )
                }
            }
        }
    }

    @TestFactory
    fun `Check history search result not added`() = TestCase {
        Given("HistoryDataProviderImpl with mocked dependencies") {
            When("History suggestion tried to be added") {
                historyDataProvider = HistoryDataProviderImpl(recordsStorage, executorService, timeProvider)
                historyDataProvider.registerIndexableDataProviderEngineBlocking(testDataProviderEngine, executor)

                historyDataProvider.addToHistoryIfNeededBlocking(TEST_HISTORY_SEARCH_RESULT, executor)

                val allRecords = historyDataProvider.getAllBlocking(executor)
                Then("Provider still should be empty", 0, allRecords.size)

                Then("No new records added", 0, testDataProviderEngine.records.size)

                Verify("No data saved", exactly = 0) {
                    recordsStorage.save(any())
                }
            }
        }
    }

    @TestFactory
    fun `Check history records do not exceed max allowed records amount`() = TestCase {
        var maxRecordsAmount = 3
        Given("HistoryDataProviderImpl with max allowed records amount = $maxRecordsAmount") {
            val testRecords = listOf(
                createTestHistoryRecord(id = "test-id-1", timestamp = 400L),
                createTestHistoryRecord(id = "test-id-2", timestamp = 400L),
                createTestHistoryRecord(id = "test-id-3", timestamp = 500L),
            )
            every { recordsStorage.load() } returns testRecords
            historyDataProvider = HistoryDataProviderImpl(recordsStorage, executorService, timeProvider, maxRecordsAmount)
            val testEngine = TestDataProviderEngine<HistoryRecord>()
            historyDataProvider.registerIndexableDataProviderEngineBlocking(testEngine, executor)

            When("New records with same timestamp added") {
                val anotherTestRecords = listOf(
                    createTestHistoryRecord(id = "test-id-4", timestamp = 300L),
                    createTestHistoryRecord(id = "test-id-5", timestamp = 400L),
                    createTestHistoryRecord(id = "test-id-6", timestamp = 400L),
                    createTestHistoryRecord(id = "test-id-7", timestamp = 400L),
                    createTestHistoryRecord(id = "test-id-8", timestamp = 400L),
                    createTestHistoryRecord(id = "test-id-9", timestamp = 400L),
                )
                historyDataProvider.upsertAllBlocking(anotherTestRecords, executor)

                val allRecords = historyDataProvider.getAllBlocking(executor)
                val allEngineRecords = testEngine.records

                Then("Amount of all records equals to $maxRecordsAmount", maxRecordsAmount, allRecords.size)

                val expectedRecords = listOf(
                    createTestHistoryRecord(id = "test-id-8", timestamp = 400L),
                    createTestHistoryRecord(id = "test-id-9", timestamp = 400L),
                    createTestHistoryRecord(id = "test-id-3", timestamp = 500L),
                )

                Then("Only records with latest timestamps are present") {
                    assertEqualsJsonify(
                        expectedValue = expectedRecords,
                        actualValue = allRecords.sortedBy { it.timestamp }
                    )
                }

                Then("Only records with latest timestamps are present in engine layer") {
                    assertEqualsJsonify(
                        expectedValue = expectedRecords,
                        actualValue = allEngineRecords.sortedBy { it.timestamp }
                    )
                }
            }

            When("One record added") {
                val singleRecord =
                    createTestHistoryRecord(id = "test-id-10", timestamp = 1000L)
                historyDataProvider.upsertBlocking(singleRecord, executor)

                val allRecords = historyDataProvider.getAllBlocking(executor)
                val allEngineRecords = testEngine.records

                Then("Amount of all records equals to $maxRecordsAmount", maxRecordsAmount, allRecords.size)

                val expectedRecords = listOf(
                    createTestHistoryRecord(id = "test-id-9", timestamp = 400L),
                    createTestHistoryRecord(id = "test-id-3", timestamp = 500L),
                    createTestHistoryRecord(id = "test-id-10", timestamp = 1000L),
                )

                Then("Only records with latest timestamps are present") {
                    assertEqualsJsonify(
                        expectedValue = expectedRecords,
                        actualValue = allRecords.sortedBy { it.timestamp }
                    )
                }

                Then("Only records with latest timestamps are present in engine layer") {
                    assertEqualsJsonify(
                        expectedValue = expectedRecords,
                        actualValue = allEngineRecords.sortedBy { it.timestamp }
                    )
                }
            }
        }

        maxRecordsAmount = 5
        Given("HistoryDataProviderImpl with max allowed records amount = $maxRecordsAmount") {
            val testRecords = (1..5).map { index ->
                createTestHistoryRecord(
                    id = "test-history-record-$index",
                    name = "Test History #$index",
                    timestamp = index * 100L // [100, 200, 300, 400, | 500]
                )
            }
            every { recordsStorage.load() } returns testRecords
            historyDataProvider = HistoryDataProviderImpl(recordsStorage, executorService, timeProvider, maxRecordsAmount)
            val testEngine = TestDataProviderEngine<HistoryRecord>()
            historyDataProvider.registerIndexableDataProviderEngineBlocking(testEngine, executor)

            When("New records added") {
                val anotherTestRecords = (6..10).map { index ->
                    createTestHistoryRecord(
                        id = "test-history-record-$index",
                        name = "Test History #$index",
                        timestamp = 100L + (index - 5) * 150L // [250, | 400, 550, 700, 850]
                    )
                }
                historyDataProvider.upsertAllBlocking(anotherTestRecords, executor)

                val allRecords = historyDataProvider.getAllBlocking(executor)
                val allEngineRecords = testEngine.records

                Then("Amount of all records equals to $maxRecordsAmount", maxRecordsAmount, allRecords.size)

                fun List<HistoryRecord>.withTimestamp(timestamp: Long) = find { it.timestamp == timestamp }

                // 400, 500, 550, 700, 850
                val expectedRecords = listOf(
                    anotherTestRecords.withTimestamp(400),
                    testRecords.withTimestamp(500),
                    anotherTestRecords.withTimestamp(550),
                    anotherTestRecords.withTimestamp(700),
                    anotherTestRecords.withTimestamp(850),
                )

                Then("Only records with latest timestamps are present") {
                    assertEqualsJsonify(
                        expectedValue = expectedRecords,
                        actualValue = allRecords.sortedBy { it.timestamp }
                    )
                }

                Then("Only records with latest timestamps are present in engine layer") {
                    assertEqualsJsonify(
                        expectedValue = expectedRecords,
                        actualValue = allEngineRecords.sortedBy { it.timestamp }
                    )
                }
            }
        }

        maxRecordsAmount = 100_000
        Given("HistoryDataProviderImpl with max allowed records amount = $maxRecordsAmount") {
            val testRecords = (1..200_000L).map { index ->
                createTestHistoryRecord(
                    id = "test-history-record-$index",
                    name = "Test History #$index",
                    timestamp = index
                )
            }.shuffled()
            every { recordsStorage.load() } returns testRecords
            historyDataProvider = HistoryDataProviderImpl(recordsStorage, executorService, timeProvider, maxRecordsAmount)
            val testEngine = TestDataProviderEngine<HistoryRecord>()
            historyDataProvider.registerIndexableDataProviderEngineBlocking(testEngine, executor)

            When("New records added") {
                val anotherTestRecords = (150_000..200_000L).map { index ->
                    createTestHistoryRecord(
                        id = "another-test-history-record-$index",
                        name = "Another Test History #$index",
                        timestamp = index
                    )
                }.shuffled()
                historyDataProvider.upsertAllBlocking(anotherTestRecords, executor)

                val allRecords = historyDataProvider.getAllBlocking(executor)
                val allEngineRecords = testEngine.records

                Then("Amount of all records equals to $maxRecordsAmount", maxRecordsAmount, allRecords.size)

                val expectedRecords = (testRecords.filter { it.timestamp > 150_000L } +
                        anotherTestRecords.filter { it.timestamp > 150_000L }).sortedBy { it.timestamp }

                Then("Only records with latest timestamps are present") {
                    assertEqualsJsonify(
                        expectedValue = expectedRecords,
                        actualValue = allRecords.sortedBy { it.timestamp }
                    )
                }

                Then("Only records with latest timestamps are present in engine layer") {
                    assertEqualsJsonify(
                        expectedValue = expectedRecords,
                        actualValue = allEngineRecords.sortedBy { it.timestamp }
                    )
                }
            }
        }
    }

    @TestFactory
    fun `Check external record search result added`() = TestCase {
        Given("HistoryDataProviderImpl with mocked dependencies") {
            When("External record suggestion tried to be added") {
                historyDataProvider.addToHistoryIfNeededBlocking(TEST_FAVORITE_RECORD_SEARCH_RESULT, executor)
                val testEngine = TestDataProviderEngine<HistoryRecord>()
                historyDataProvider.registerIndexableDataProviderEngineBlocking(testEngine, executor)

                val addedRecord = historyDataProvider.getBlocking(TEST_FAVORITE_RECORD_SEARCH_RESULT.id, executor)

                val allRecords = historyDataProvider.getAllBlocking(executor)

                Then("Suggestion added") {
                    Assertions.assertNotNull(addedRecord)
                    assertEquals(1, allRecords.size)
                }
                checkNotNull(addedRecord)

                val expectedRecord = HistoryRecord(
                    id = TEST_FAVORITE_RECORD_SEARCH_RESULT.id,
                    name = TEST_FAVORITE_RECORD_SEARCH_RESULT.name,
                    coordinate = TEST_FAVORITE_RECORD_SEARCH_RESULT.coordinate,
                    descriptionText = TEST_FAVORITE_RECORD_SEARCH_RESULT.descriptionText,
                    address = TEST_FAVORITE_RECORD_SEARCH_RESULT.address,
                    timestamp = TEST_LOCAL_TIME_MILLIS,
                    type = TEST_FAVORITE_RECORD_SEARCH_RESULT.types.first(),
                    routablePoints = TEST_FAVORITE_RECORD_SEARCH_RESULT.routablePoints,
                    metadata = TEST_FAVORITE_RECORD_SEARCH_RESULT.metadata,
                    makiIcon = TEST_FAVORITE_RECORD_SEARCH_RESULT.makiIcon,
                    categories = TEST_FAVORITE_RECORD_SEARCH_RESULT.categories
                )

                Then("Added record is $expectedRecord", expectedRecord, addedRecord)

                Verify("History record timestamp is a local time") {
                    timeProvider.currentTimeMillis()
                    assertEquals(TEST_LOCAL_TIME_MILLIS, addedRecord.timestamp)
                }

                Then(
                    "New record added to core layer",
                    listOf(expectedRecord),
                    testEngine.records,
                )
            }
        }
    }

    @TestFactory
    fun `Check server search result added`() = TestCase {
        Given("HistoryDataProviderImpl with mocked dependencies") {
            When("External record suggestion tried to be added") {
                historyDataProvider.addToHistoryIfNeededBlocking(TEST_SERVER_SEARCH_RESULT, executor)
                val testEngine = TestDataProviderEngine<HistoryRecord>()
                historyDataProvider.registerIndexableDataProviderEngineBlocking(testEngine, executor)

                val blockingCompletionCallback = BlockingCompletionCallback<HistoryRecord?>()
                historyDataProvider.get(TEST_SERVER_SEARCH_RESULT.id, executor, blockingCompletionCallback)

                val callbackResult = blockingCompletionCallback.getResultBlocking()
                val addedRecord = (callbackResult as BlockingCompletionCallback.CompletionCallbackResult.Result).result

                Then("Suggestion added") {
                    Assertions.assertNotNull(addedRecord)
                    assertEquals(1, historyDataProvider.getSizeBlocking(executor))
                }
                checkNotNull(addedRecord)

                val expectedRecord = HistoryRecord(
                    id = TEST_SERVER_SEARCH_RESULT.id,
                    name = TEST_SERVER_SEARCH_RESULT.name,
                    coordinate = TEST_SERVER_SEARCH_RESULT.coordinate,
                    descriptionText = TEST_SERVER_SEARCH_RESULT.descriptionText,
                    address = TEST_SERVER_SEARCH_RESULT.address,
                    timestamp = TEST_LOCAL_TIME_MILLIS,
                    type = TEST_SERVER_SEARCH_RESULT.types.first(),
                    routablePoints = TEST_SERVER_SEARCH_RESULT.routablePoints,
                    metadata = TEST_SERVER_SEARCH_RESULT.metadata,
                    makiIcon = TEST_SERVER_SEARCH_RESULT.makiIcon,
                    categories = TEST_SERVER_SEARCH_RESULT.categories
                )

                Then("Added record is $expectedRecord", expectedRecord, addedRecord)

                Verify("History record timestamp is a local time") {
                    timeProvider.currentTimeMillis()
                    assertEquals(TEST_LOCAL_TIME_MILLIS, addedRecord.timestamp)
                }

                Then(
                    "New record added to core layer",
                    listOf(expectedRecord),
                    testEngine.records,
                )
            }
        }
    }

    private companion object {
        const val TEST_LOCAL_TIME_MILLIS = 12345L

        val TEST_USER_RECORD_SEARCH_RESULT = createTestCoreSearchResult(
            id = "result id 1",
            types = listOf(ResultType.USER_RECORD),
            names = listOf("Result name"),
            languages = listOf("Default"),
            distanceMeters = 123.0,
            center = Point.fromLngLat(20.0, 30.0),
            routablePoints = listOf(
                CoreRoutablePoint(
                    Point.fromLngLat(19.999999, 30.0001),
                    "Entrance 1"
                ),
                CoreRoutablePoint(
                    Point.fromLngLat(20.000001, 30.0),
                    "Entrance 2"
                )
            ),
            categories = emptyList(),
        )

        val TEST_POI_SEARCH_RESULT = createTestCoreSearchResult(
            id = "test poi result id",
            types = listOf(ResultType.POI),
            names = listOf("Test POI search result"),
            languages = listOf("Default"),
            distanceMeters = 100.0,
            center = Point.fromLngLat(10.0, 11.0),
            categories = emptyList(),
            metadata = createCoreResultMetadata(
                3456,
                "+902 10 70 77",
                "https://www.museodelprado.es/en/visit-the-museum",
                9.7,
                data = hashMapOf(),
            ),
        )

        val TEST_HISTORY_RECORD_1 = HistoryRecord(
            id = "history item 1",
            name = "history item 1",
            coordinate = Point.fromLngLat(10.0, 20.0),
            descriptionText = null,
            address = null,
            timestamp = 1L,
            type = SearchResultType.POI,
            routablePoints = null,
            metadata = null,
            makiIcon = null,
            categories = emptyList()
        )

        val TEST_HISTORY_RECORD_2 = HistoryRecord(
            id = "history item 2",
            name = "history item 2",
            coordinate = Point.fromLngLat(20.0, 30.0),
            descriptionText = null,
            address = null,
            timestamp = 3L,
            type = SearchResultType.POI,
            routablePoints = listOf(
                RoutablePoint(
                    point = Point.fromLngLat(19.999999, 30.0001),
                    name = "Entrance 1"
                ),
                RoutablePoint(
                    point = Point.fromLngLat(20.000001, 30.0),
                    name = "Entrance 2"
                )
            ),
            metadata = null,
            makiIcon = "test maki",
            categories = listOf("cafe")
        )

        val TEST_FAVORITE_RECORD = FavoriteRecord(
            id = "test favorite id",
            name = "test favorite",
            coordinate = TEST_USER_RECORD_SEARCH_RESULT.center!!,
            descriptionText = TEST_USER_RECORD_SEARCH_RESULT.descrAddress,
            address = null,
            type = SearchResultType.POI,
            makiIcon = null,
            categories = emptyList(),
            routablePoints = TEST_USER_RECORD_SEARCH_RESULT.routablePoints?.take(1)?.map { it.mapToPlatform() },
            metadata = null
        )

        val TEST_HISTORY_SEARCH_RESULT = createTestIndexableRecordSearchResult(
            record = TEST_HISTORY_RECORD_1,
            rawSearchResult = TEST_USER_RECORD_SEARCH_RESULT.mapToBase(),
            requestOptions = createTestRequestOptions("Test query")
        )

        val TEST_FAVORITE_RECORD_SEARCH_RESULT = createTestIndexableRecordSearchResult(
            record = TEST_FAVORITE_RECORD,
            rawSearchResult = TEST_USER_RECORD_SEARCH_RESULT.mapToBase(),
            requestOptions = createTestRequestOptions("Test query")
        )

        val TEST_SERVER_SEARCH_RESULT = createTestServerSearchResult(
            types = listOf(SearchResultType.POI),
            rawSearchResult = TEST_POI_SEARCH_RESULT.mapToBase(),
            requestOptions = createTestRequestOptions("Test query")
        )
    }
}
