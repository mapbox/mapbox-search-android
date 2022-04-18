package com.mapbox.search

import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.record.DataProviderEngineRegistrationService
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableDataProviderEngineImpl
import com.mapbox.search.tests_support.TestExecutor
import com.mapbox.search.tests_support.TestSyncLocker
import com.mapbox.search.tests_support.TestThreadExecutorService
import com.mapbox.search.tests_support.equalsTo
import com.mapbox.search.tests_support.record.TestDataProvider
import com.mapbox.search.utils.SyncLocker
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import org.junit.Assert.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import java.util.concurrent.ExecutorService

@Suppress("LargeClass")
internal class IndexableDataProvidersRegistryTest {

    private lateinit var syncLocker: SyncLocker
    private lateinit var registrationService: DataProviderEngineRegistrationService
    private lateinit var executorService: ExecutorService

    private lateinit var dataProviderEngine1: IndexableDataProviderEngineImpl
    private lateinit var dataProvider1: IndexableDataProvider<HistoryRecord>

    private lateinit var dataProviderEngine2: IndexableDataProviderEngineImpl
    private lateinit var dataProvider2: IndexableDataProvider<HistoryRecord>

    private lateinit var searchEngine1: CoreSearchEngineInterface
    private lateinit var searchEngine2: CoreSearchEngineInterface

    private lateinit var registry: IndexableDataProvidersRegistry

    @BeforeEach
    fun setUp() {
        syncLocker = spyk(TestSyncLocker())
        registrationService = mockk(relaxed = true)
        executorService = spyk(TestThreadExecutorService())

        dataProviderEngine1 = IndexableDataProviderEngineImpl(
            IndexableDataProviderEngineImpl.CoreLayerContext(
                mockk(relaxed = true), syncLocker
            )
        )
        dataProvider1 = spyk(
            TestDataProvider(
                dataProviderName = "Test data provider 1",
                executorService = executorService
            )
        )

        dataProviderEngine2 = IndexableDataProviderEngineImpl(
            IndexableDataProviderEngineImpl.CoreLayerContext(
                mockk(relaxed = true), syncLocker
            )
        )
        dataProvider2 = spyk(
            TestDataProvider(
                dataProviderName = "Test data provider 2",
                executorService = executorService
            )
        )

        searchEngine1 = mockk(relaxed = true)
        searchEngine2 = mockk(relaxed = true)

        registry = IndexableDataProvidersRegistryImpl(syncLocker, registrationService)
    }

    private fun mockDataProviderRegistration(
        dataProvider: IndexableDataProvider<*> = dataProvider1,
        dataProviderEngine: IndexableDataProviderEngineImpl = dataProviderEngine1
    ): AsyncOperationTask {
        val task: AsyncOperationTask = mockk(relaxed = true)
        val callbackSlot = slot<CompletionCallback<IndexableDataProviderEngineImpl>>()
        every { registrationService.register(dataProvider, capture(callbackSlot)) } answers {
            callbackSlot.captured.onComplete(dataProviderEngine)
            task
        }
        return task
    }

    @TestFactory
    fun `Check successful data provider registration`() = TestCase {
        Given("IndexableDataProvidersRegistry with mocked dependencies") {
            mockDataProviderRegistration()

            When("Register provider for the first time") {
                val callbackExecutor = spyk(TestExecutor())
                val callback: CompletionCallback<Unit> = mockk(relaxed = true)

                val task = registry.register(dataProvider1, searchEngine1, callbackExecutor, callback)

                VerifyOnce("Data provider registered in the registration service") {
                    registrationService.register(dataProvider1, any())
                }

                VerifyOnce("SyncLocker triggered") {
                    syncLocker.executeInSync(any())
                }

                VerifyOnce("CoreUserRecordsLayer returned from the service added to the SearchEngine") {
                    searchEngine1.addUserLayer(dataProviderEngine1.coreLayerContext.coreLayer)
                }

                VerifyOnce("Callback executor triggered") {
                    callbackExecutor.execute(any())
                }

                VerifyOnce("Completion result passed to callback") {
                    callback.onComplete(Unit)
                }

                Then("Task completes", true, task.isDone)
            }
        }
    }

    @TestFactory
    fun `Check successful data provider registration when another data provider registered in the search engine`() = TestCase {
        Given("IndexableDataProvidersRegistry with mocked dependencies") {
            mockDataProviderRegistration(dataProvider1, dataProviderEngine1)
            mockDataProviderRegistration(dataProvider2, dataProviderEngine2)

            When("Another data provider registered in the search engine") {
                registry.register(dataProvider1, searchEngine1, TestExecutor(), mockk(relaxed = true))

                val callbackExecutor = spyk(TestExecutor())
                val callback: CompletionCallback<Unit> = mockk(relaxed = true)

                val task = registry.register(dataProvider2, searchEngine1, callbackExecutor, callback)

                VerifyOnce("New data provider registered in the registration service") {
                    registrationService.register(dataProvider2, any())
                }

                Verify("SyncLocker triggered for each CoreSearchEngine.addUserLayer()", exactly = 2) {
                    syncLocker.executeInSync(any())
                }

                VerifyOnce("New CoreUserRecordsLayer added to the SearchEngine") {
                    searchEngine1.addUserLayer(dataProviderEngine2.coreLayerContext.coreLayer)
                }

                VerifyOnce("Callback executor triggered") {
                    callbackExecutor.execute(any())
                }

                VerifyOnce("Completion result passed to callback") {
                    callback.onComplete(Unit)
                }

                Then("Task completes", true, task.isDone)
            }
        }
    }

    @TestFactory
    fun `Check already registered data provider in the same search engine`() = TestCase {
        Given("IndexableDataProvidersRegistry with mocked dependencies") {
            mockDataProviderRegistration()

            When("Data provider registered in the search engine twice") {
                registry.register(dataProvider1, searchEngine1, TestExecutor(), mockk(relaxed = true))

                val callbackExecutor = spyk(TestExecutor())
                val callback: CompletionCallback<Unit> = mockk(relaxed = true)
                val errorSlot = slot<Exception>()
                every { callback.onError(capture(errorSlot)) } returns Unit

                val task = registry.register(dataProvider1, searchEngine1, callbackExecutor, callback)

                VerifyOnce("Data provider is not registered again") {
                    registrationService.register(dataProvider1, any())
                }

                VerifyOnce("SyncLocker is not triggered again") {
                    syncLocker.executeInSync(any())
                }

                VerifyOnce("CoreUserRecordsLayer is not added to the SearchEngine again") {
                    searchEngine1.addUserLayer(dataProviderEngine1.coreLayerContext.coreLayer)
                }

                VerifyOnce("Callback executor triggered") {
                    callbackExecutor.execute(any())
                }

                Verify("Completion error passed to callback") {
                    callback.onError(any())
                }

                val expectedError = IllegalStateException(
                    "${dataProvider1.dataProviderName} has already been registered in the provided search engine"
                )
                Then(
                    "Registry returns correct error",
                    true,
                    expectedError.equalsTo(errorSlot.captured)
                )

                Then("Registry returns completed task") {
                    assertSame(CompletedAsyncOperationTask, task)
                }
            }
        }
    }

    @TestFactory
    fun `Check registered in another search engine data provider registration`() = TestCase {
        Given("IndexableDataProvidersRegistry with mocked dependencies") {
            val originalServiceTask1: AsyncOperationTask = mockk(relaxed = true)
            val callbackSlot1 = slot<CompletionCallback<IndexableDataProviderEngineImpl>>()
            every { registrationService.register(dataProvider1, capture(callbackSlot1)) } answers {
                callbackSlot1.captured.onComplete(dataProviderEngine1)
                originalServiceTask1
            }

            When("Data provider registered in another search engine") {
                registry.register(dataProvider1, searchEngine1, TestExecutor(), mockk(relaxed = true))

                val callbackExecutor = spyk(TestExecutor())
                val callback: CompletionCallback<Unit> = mockk(relaxed = true)

                val task = registry.register(dataProvider1, searchEngine2, callbackExecutor, callback)

                VerifyOnce("Data provider is not registered again") {
                    registrationService.register(dataProvider1, any())
                }

                Verify("SyncLocker triggered for each CoreSearchEngine.addUserLayer()", exactly = 2) {
                    syncLocker.executeInSync(any())
                }

                VerifyOnce("CoreUserRecordsLayer returned from the service added to the SearchEngine") {
                    searchEngine2.addUserLayer(dataProviderEngine1.coreLayerContext.coreLayer)
                }

                VerifyOnce("Callback executor triggered") {
                    callbackExecutor.execute(any())
                }

                VerifyOnce("Completion result passed to callback") {
                    callback.onComplete(Unit)
                }

                Then("Registry returns completed task") {
                    assertSame(CompletedAsyncOperationTask, task)
                }
            }
        }
    }

    @TestFactory
    fun `Check data provider registration when data provider can't be registered in the registration service`() = TestCase {
        Given("IndexableDataProvidersRegistry with mocked dependencies") {
            val serviceError = Exception("Can't register data provider")
            val originalServiceTask: AsyncOperationTask = mockk(relaxed = true)
            val callbackSlot = slot<CompletionCallback<IndexableDataProviderEngineImpl>>()
            every { registrationService.register(dataProvider1, capture(callbackSlot)) } answers {
                callbackSlot.captured.onError(serviceError)
                originalServiceTask
            }

            When("Register provider for the first time") {
                val callbackExecutor = spyk(TestExecutor())
                val callback: CompletionCallback<Unit> = mockk(relaxed = true)

                val task = registry.register(dataProvider1, searchEngine1, callbackExecutor, callback)

                VerifyOnce("Data provider registered in the registration service") {
                    registrationService.register(dataProvider1, any())
                }

                VerifyNo("SyncLocker is not triggered") {
                    syncLocker.executeInSync(any())
                }

                VerifyNo("No CoreUserRecordsLayer added to the SearchEngine") {
                    searchEngine1.addUserLayer(any())
                }

                VerifyOnce("Callback executor triggered") {
                    callbackExecutor.execute(any())
                }

                VerifyOnce("Error from the service passed to callback") {
                    callback.onError(serviceError)
                }

                Then("Task completes", true, task.isDone)
            }
        }
    }

    @TestFactory
    fun `Check data provider registration cancellation`() = TestCase {
        Given("IndexableDataProvidersRegistry with mocked dependencies") {
            val serviceTask: AsyncOperationTask = mockk(relaxed = true)
            every { registrationService.register(dataProvider1, any()) } answers {
                serviceTask
            }

            When("Register task cancelled") {
                val callbackExecutor = spyk(TestExecutor())
                val callback: CompletionCallback<Unit> = mockk(relaxed = true)

                val task = registry.register(dataProvider1, searchEngine1, callbackExecutor, callback)
                task.cancel()

                VerifyOnce("Register process started in the registration service") {
                    registrationService.register(dataProvider1, any())
                }

                VerifyNo("SyncLocker is not triggered") {
                    syncLocker.executeInSync(any())
                }

                VerifyNo("CoreUserRecordsLayer is not added to the SearchEngine") {
                    searchEngine1.addUserLayer(any())
                }

                VerifyNo("Callback executor is not triggered") {
                    callbackExecutor.execute(any())
                }

                VerifyNo("Callback not called") {
                    callback.onComplete(Unit)
                    callback.onError(any())
                }

                VerifyOnce("Root registration task from the registration service is cancelled") {
                    serviceTask.cancel()
                }
            }
        }
    }

    @TestFactory
    fun `Check registered data provider unregister`() = TestCase {
        Given("IndexableDataProvidersRegistry with registered data provider") {
            mockDataProviderRegistration()
            registry.register(dataProvider1, searchEngine1, TestExecutor(), mockk(relaxed = true))

            When("Data provider registered and then unregistered") {
                val callbackExecutor = spyk(TestExecutor())
                val callback: CompletionCallback<Unit> = mockk(relaxed = true)

                val task = registry.unregister(dataProvider1, searchEngine1, callbackExecutor, callback)

                VerifyOnce("Data provider is not registered again") {
                    registrationService.register(dataProvider1, any())
                }

                Verify("SyncLocker triggered for core user layer add and remove", exactly = 2) {
                    syncLocker.executeInSync(any())
                }

                VerifyOnce("Data provider removed from the SearchEngine") {
                    searchEngine1.removeUserLayer(eq("Test data provider 1"))
                }

                VerifyOnce("Callback executor triggered") {
                    callbackExecutor.execute(any())
                }

                VerifyOnce("Completion result passed to callback") {
                    callback.onComplete(Unit)
                }

                Then("Returned async task completes", true, task.isDone)
            }
        }
    }

    @TestFactory
    fun `Check not registered data provider unregister`() = TestCase {
        Given("IndexableDataProvidersRegistry with registered data provider") {
            mockDataProviderRegistration()

            When("Data provider registered and then unregistered") {
                val callbackExecutor = spyk(TestExecutor())
                val callback: CompletionCallback<Unit> = mockk(relaxed = true)
                val errorSlot = slot<Exception>()
                every { callback.onError(capture(errorSlot)) } returns Unit

                val task = registry.unregister(dataProvider1, searchEngine1, callbackExecutor, callback)

                VerifyNo("Nothing is registered in registration service") {
                    registrationService.register(dataProvider1, any())
                }

                VerifyNo("SyncLocker is not triggered") {
                    syncLocker.executeInSync(any())
                }

                VerifyNo("SearchEngine is not accessed") {
                    searchEngine1.addUserLayer(any())
                    searchEngine1.removeUserLayer(any())
                }

                VerifyOnce("Callback executor triggered") {
                    callbackExecutor.execute(any())
                }

                val expectedError = Exception(
                    "Data provider ${dataProvider1.dataProviderName} is not associated with this search engine"
                )
                Then(
                    "Registry returns correct error",
                    true,
                    expectedError.equalsTo(errorSlot.captured)
                )

                VerifyOnce("Completion result passed to callback") {
                    callback.onError(errorSlot.captured)
                }

                Then("Registry returns completed task") {
                    assertSame(CompletedAsyncOperationTask, task)
                }
            }
        }
    }
}
