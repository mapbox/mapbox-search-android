package com.mapbox.search

import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.record.IndexableDataProviderEngineLayerImpl
import com.mapbox.search.tests_support.StubIndexableRecord
import com.mapbox.search.tests_support.TestExecutor
import com.mapbox.search.tests_support.TestSyncLocker
import com.mapbox.search.tests_support.TestThreadExecutorService
import com.mapbox.search.tests_support.record.TestDataProvider
import com.mapbox.search.tests_support.record.TestDataProvider.Mode.Fail
import com.mapbox.search.utils.SyncLocker
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import java.util.concurrent.Executor
import kotlin.RuntimeException

internal class DefaultIndexableDataProvidersRegistryTest {

    private lateinit var executor: Executor
    private lateinit var registryExecutor: Executor
    private lateinit var syncLocker: SyncLocker
    private lateinit var coreSearchEngine: CoreSearchEngineInterface
    private lateinit var registry: DefaultIndexableDataProvidersRegistry

    @BeforeEach
    fun setUp() {
        executor = spyk(TestExecutor())
        registryExecutor = spyk(TestExecutor())
        syncLocker = spyk(TestSyncLocker())
        coreSearchEngine = mockk(relaxed = true)
        registry = DefaultIndexableDataProvidersRegistry(
            registryExecutor = registryExecutor,
            syncLocker = syncLocker,
        ) { _, _ -> mockk(relaxed = true) }

        every { IndexableDataProviderEngineLayerImpl.create(any()) } answers { mockk(relaxed = true) }
    }

    @TestFactory
    fun `Check new data provider registration`() = TestCase {
        Given("DefaultIndexableDataProvidersRegistry with mocked dependencies") {
            registry.addCoreSearchEngine(coreSearchEngine)

            val dataProvider = TestDataProvider<StubIndexableRecord>(
                executorService = TestThreadExecutorService(),
            )

            When("Register new data provider") {
                val callback = mockk<IndexableDataProvidersRegistry.Callback>(relaxed = true)
                registry.register(dataProvider, 100, executor, callback)

                VerifyOnce("Core layer was added to all core search engines") {
                    coreSearchEngine.addUserLayer(any())
                }
                VerifyOnce("onSuccess() was called") {
                    callback.onSuccess()
                }
                VerifyOnce("Sync locker was triggered") {
                    syncLocker.executeInSync(any())
                }
                VerifyOnce("Registry executor was triggered") {
                    registryExecutor.execute(any())
                }
                VerifyOnce("Provided executor was triggered") {
                    executor.execute(any())
                }
            }
        }
    }

    @TestFactory
    fun `Check new data provider double registration`() = TestCase {
        Given("DefaultIndexableDataProvidersRegistry with mocked dependencies") {
            registry.addCoreSearchEngine(coreSearchEngine)

            val dataProvider = TestDataProvider<StubIndexableRecord>(
                executorService = TestThreadExecutorService(),
            )

            When("Register new data provider twice") {
                val callback = mockk<IndexableDataProvidersRegistry.Callback>(relaxed = true)
                registry.register(dataProvider, 100, executor, callback)
                registry.register(dataProvider, 100, executor, callback)

                VerifyOnce("Core layer was added to all core search engines only the first time") {
                    coreSearchEngine.addUserLayer(any())
                }
                Verify("onSuccess() was called") {
                    callback.onSuccess()
                }
                VerifyOnce("Sync locker was triggered") {
                    syncLocker.executeInSync(any())
                }
                VerifyOnce("Registry executor was triggered") {
                    registryExecutor.execute(any())
                }
                Verify("Provided executor was triggered") {
                    executor.execute(any())
                }
            }
        }
    }

    @TestFactory
    fun `Check new core engine addotion`() = TestCase {
        Given("DefaultIndexableDataProvidersRegistry with mocked dependencies") {
            val dataProvider = TestDataProvider<StubIndexableRecord>(
                executorService = TestThreadExecutorService(),
            )
            val callback = mockk<IndexableDataProvidersRegistry.Callback>(relaxed = true)
            registry.register(dataProvider, 100, executor, callback)

            When("Add new core engine") {
                registry.addCoreSearchEngine(coreSearchEngine)

                VerifyOnce("Core layer was added to all core search engines") {
                    coreSearchEngine.addUserLayer(any())
                }
            }

            When("Add same core engine once again") {
                registry.addCoreSearchEngine(coreSearchEngine)

                VerifyOnce("Core layer was added to all core search engines only the first time") {
                    coreSearchEngine.addUserLayer(any())
                }
            }
        }
    }

    @TestFactory
    fun `Check new data provider registration with internal error`() = TestCase {
        Given("DefaultIndexableDataProvidersRegistry with mocked dependencies") {
            registry.addCoreSearchEngine(coreSearchEngine)

            val internalError = RuntimeException()
            val brokenDataProvider = TestDataProvider<StubIndexableRecord>(
                dataProviderName = "BROKEN_PROVIDER",
                executorService = TestThreadExecutorService(),
            ).apply {
                mode = Fail(internalError)
            }

            When("Register new data provider with internal error") {
                val callback = mockk<IndexableDataProvidersRegistry.Callback>(relaxed = true)
                registry.register(brokenDataProvider, 100, executor, callback)

                VerifyNo("Core layer wasn't added to any of core search engines") {
                    coreSearchEngine.addUserLayer(any())
                }
                Verify("onError() was called") {
                    callback.onError(internalError)
                }
                VerifyNo("Sync locker wasn't triggered") {
                    syncLocker.executeInSync(any())
                }
                Verify("Provided executor was triggered") {
                    executor.execute(any())
                }
            }
        }
    }

    private companion object {

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            mockkObject(IndexableDataProviderEngineLayerImpl)
        }

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            unmockkObject(IndexableDataProviderEngineLayerImpl)
        }
    }
}
