package com.mapbox.search.offline

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreSearchCallback
import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.result.mapToBase
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.SearchCancellationException
import com.mapbox.search.common.concurrent.MainThreadWorker
import com.mapbox.search.common.concurrent.SearchSdkMainThreadWorker
import com.mapbox.search.common.ev.EvConnectorType
import com.mapbox.search.common.tests.TestConstants
import com.mapbox.search.common.tests.TestExecutor
import com.mapbox.search.common.tests.TestThreadExecutorService
import com.mapbox.search.common.tests.createCoreSearchAddress
import com.mapbox.search.common.tests.createTestCoreRequestOptions
import com.mapbox.search.common.tests.createTestCoreReverseGeoOptions
import com.mapbox.search.common.tests.createTestCoreSearchResponseCancelled
import com.mapbox.search.common.tests.createTestCoreSearchResponseHttpError
import com.mapbox.search.common.tests.createTestCoreSearchResponseSuccess
import com.mapbox.search.common.tests.createTestCoreSearchResult
import com.mapbox.search.internal.bindgen.ResultType
import com.mapbox.search.internal.bindgen.UserActivityReporterInterface
import com.mapbox.test.dsl.TestCase
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService

@OptIn(MapboxExperimental::class)
@Suppress("LargeClass")
internal class OfflineSearchEngineTest {

    private lateinit var coreEngine: CoreSearchEngineInterface
    private lateinit var activityReporter: UserActivityReporterInterface
    private lateinit var searchResultFactory: SearchResultFactory
    private lateinit var executorService: ExecutorService
    private lateinit var mainThreadWorker: MainThreadWorker
    private lateinit var executor: Executor
    private lateinit var testMainThreadWorker: MainThreadWorker

    private lateinit var requestContextProvider: SearchRequestContextProvider

    private lateinit var searchEngine: OfflineSearchEngine

    @BeforeEach
    fun setUp() {
        executorService = spyk(TestThreadExecutorService())
        mainThreadWorker = spyk(TestMainThreadWorker())
        executor = spyk(TestExecutor())

        testMainThreadWorker = spyk(TestMainThreadWorker()).apply {
            mainExecutor = executor
        }

        SearchSdkMainThreadWorker.delegate = testMainThreadWorker

        coreEngine = mockk(relaxed = true)

        activityReporter = mockk(relaxed = true)

        searchResultFactory = spyk(SearchResultFactory(mockk()))

        requestContextProvider = mockk()
        every { requestContextProvider.provide(CoreApiType.SBS) } returns TEST_SEARCH_REQUEST_CONTEXT

        createSearchEngine()
        mockDefaultSearchEngineFunctions()
    }

    @AfterEach
    fun tearDown() {
        SearchSdkMainThreadWorker.resetDelegate()
    }

    private fun createSearchEngine() {
        val settings = mockk<OfflineSearchEngineSettings>(relaxed = true)
        every { settings.tileStore } returns mockk()

        searchEngine = OfflineSearchEngineImpl(
            settings = settings,
            coreEngine = coreEngine,
            activityReporter = activityReporter,
            requestContextProvider = requestContextProvider,
            searchResultFactory = searchResultFactory,
            engineExecutorService = executorService
        )
    }

    private fun mockDefaultSearchEngineFunctions() {
        val reverseGeocodingOfflineSlotCallback = slot<CoreSearchCallback>()
        every { coreEngine.reverseGeocodingOffline(any(), capture(reverseGeocodingOfflineSlotCallback)) } answers {
            reverseGeocodingOfflineSlotCallback.captured.run(TEST_RETRIEVED_SUCCESSFUL_CORE_RESPONSE)
        }

        val getAddressesOfflineSlotCallback = slot<CoreSearchCallback>()
        every { coreEngine.getAddressesOffline(any(), any(), any(), capture(getAddressesOfflineSlotCallback)) } answers {
            getAddressesOfflineSlotCallback.captured.run(TEST_RETRIEVED_SUCCESSFUL_CORE_RESPONSE)
        }

        val searchOfflineSlotCallback = slot<CoreSearchCallback>()
        every { coreEngine.searchOffline(any(), any(), any(), capture(searchOfflineSlotCallback)) } answers {
            searchOfflineSlotCallback.captured.run(TEST_RETRIEVED_SUCCESSFUL_CORE_RESPONSE)
        }
    }

    @TestFactory
    fun `Check offline reverse geocoding search`() = TestCase {
        Given("OfflineSearchEngine with mocked dependencies") {
            When("reverseGeocoding() called") {
                val slotSearchCallback = slot<CoreSearchCallback>()

                every { coreEngine.reverseGeocodingOffline(any(), capture(slotSearchCallback)) } answers {
                    slotSearchCallback.captured.run(TEST_RETRIEVED_SUCCESSFUL_CORE_RESPONSE)
                }

                val callback = mockk<OfflineSearchCallback>(relaxed = true)

                val task = searchEngine.reverseGeocoding(
                    options = OfflineReverseGeoOptions(center = TEST_POINT),
                    executor = executor,
                    callback = callback,
                )

                Then("Task is executed", true, task.isDone)

                VerifyOnce("Callbacks called inside executor") {
                    executor.execute(any())
                }

                VerifyOnce("CoreSearchEngine.reverseGeocodingOffline() called") {
                    coreEngine.reverseGeocodingOffline(eq(createTestCoreReverseGeoOptions(point = TEST_POINT)), slotSearchCallback.captured)
                }

                VerifyOnce("Results passed to callback") {
                    callback.onResults(
                        listOf(TEST_SEARCH_RESULT),
                        TEST_OFFLINE_RESPONSE_INFO
                    )
                }

                VerifyNo("onError() wasn't called") {
                    callback.onError(any())
                }

                VerifyOnce("User activity reported") {
                    activityReporter.reportActivity(eq("offline-search-engine-reverse-geocoding"))
                }
            }
        }
    }

    @TestFactory
    fun `Check reverseGeocoding with default executor`() = TestCase {
        Given("OfflineSearchEngine with mocked dependencies") {
            When("reverseGeocoding() with default executor called") {
                searchEngine.reverseGeocoding(
                    options = OfflineReverseGeoOptions(center = TEST_POINT),
                    callback = mockk(relaxed = true)
                )

                VerifyOnce("SearchSdkMainThreadWorker.mainExecutor accessed") {
                    testMainThreadWorker.mainExecutor
                }

                VerifyOnce("Callback called inside executor") {
                    executor.execute(any())
                }

                VerifyOnce("User activity reported") {
                    activityReporter.reportActivity(eq("offline-search-engine-reverse-geocoding"))
                }
            }
        }
    }

    @TestFactory
    fun `Check successful offline address search`() = TestCase {
        Given("OfflineSearchEngine with mocked dependencies") {
            When("Core getAddressesOffline() returns successful response") {
                val testStreet = "Test street"
                val testRadius = 3.141
                val slotSearchCallback = slot<CoreSearchCallback>()

                every { coreEngine.getAddressesOffline(any(), any(), any(), capture(slotSearchCallback)) } answers {
                    slotSearchCallback.captured.run(TEST_RETRIEVED_SUCCESSFUL_CORE_RESPONSE)
                }

                val callback = mockk<OfflineSearchCallback>(relaxed = true)

                val task = searchEngine.searchAddressesNearby(
                    street = testStreet,
                    proximity = TEST_POINT,
                    radiusMeters = testRadius,
                    executor = executor,
                    callback = callback
                )

                Then("Task is executed", true, task.isDone)

                VerifyOnce("Callbacks called inside executor") {
                    executor.execute(any())
                }

                VerifyOnce("CoreSearchEngine.getAddressesOffline() called") {
                    coreEngine.getAddressesOffline(eq(testStreet), eq(TEST_POINT), eq(testRadius), slotSearchCallback.captured)
                }

                VerifyOnce("Results passed to callback") {
                    callback.onResults(
                        listOf(TEST_SEARCH_RESULT),
                        TEST_OFFLINE_RESPONSE_INFO
                    )
                }

                VerifyNo("onError() wasn't called") {
                    callback.onError(any())
                }

                VerifyOnce("User activity reported") {
                    activityReporter.reportActivity(eq("offline-search-engine-search-nearby-street"))
                }
            }
        }
    }

    @TestFactory
    fun `Check error offline address search`() = TestCase {
        Given("OfflineSearchEngine with mocked dependencies") {
            When("Core getAddressesOffline() returns error response") {
                val testStreet = "Test street"
                val testRadius = 3.141
                val slotSearchCallback = slot<CoreSearchCallback>()

                val coreErrorResponse = createTestCoreSearchResponseHttpError(
                    httpCode = 400,
                    message = "Unknown error",
                    request = TEST_CORE_REQUEST_OPTIONS,
                    responseUUID = TEST_RESPONSE_UUID
                )

                every { coreEngine.getAddressesOffline(any(), any(), any(), capture(slotSearchCallback)) } answers {
                    slotSearchCallback.captured.run(coreErrorResponse)
                }

                val callback = mockk<OfflineSearchCallback>(relaxed = true)

                val task = searchEngine.searchAddressesNearby(
                    street = testStreet,
                    proximity = TEST_POINT,
                    radiusMeters = testRadius,
                    executor = executor,
                    callback = callback,
                )

                Then("Task is executed", true, task.isDone)

                VerifyOnce("Callbacks called inside executor") {
                    executor.execute(any())
                }

                VerifyOnce("CoreSearchEngine.getAddressesOffline() called") {
                    coreEngine.getAddressesOffline(eq(testStreet), eq(TEST_POINT), eq(testRadius), slotSearchCallback.captured)
                }

                VerifyOnce("Error passed to callback") {
                    callback.onError(any())
                }

                VerifyNo("onResults() wasn't called") {
                    callback.onResults(any(), any())
                }

                VerifyOnce("User activity reported") {
                    activityReporter.reportActivity(eq("offline-search-engine-search-nearby-street"))
                }
            }
        }
    }

    @TestFactory
    fun `Check error offline address search with negative radius passed`() = TestCase {
        Given("OfflineSearchEngine with mocked dependencies") {
            When("searchAddressesNearby() called with negative radius") {
                val testStreet = "Test street"
                val testRadius = -10.0

                val callback = mockk<OfflineSearchCallback>(relaxed = true)
                val errorSlot = slot<Exception>()
                every { callback.onError(capture(errorSlot)) } returns Unit

                val task = searchEngine.searchAddressesNearby(
                    street = testStreet,
                    proximity = TEST_POINT,
                    radiusMeters = testRadius,
                    executor = executor,
                    callback = callback
                )

                Then("Task is executed", true, task.isDone)

                VerifyOnce("Callbacks called inside executor") {
                    executor.execute(any())
                }

                VerifyNo("CoreSearchEngine.getAddressesOffline() not called") {
                    coreEngine.getAddressesOffline(any(), any(), any(), any())
                }

                VerifyOnce("Error passed to callback") {
                    callback.onError(any())
                }

                Then("Passed error is correct") {
                    Assertions.assertTrue(errorSlot.captured is IllegalArgumentException)
                    Assertions.assertEquals("Negative or zero radius: -10.0", errorSlot.captured.message)
                }

                VerifyNo("onResults() wasn't called") {
                    callback.onResults(any(), any())
                }

                VerifyOnce("User activity reported") {
                    activityReporter.reportActivity(eq("offline-search-engine-search-nearby-street"))
                }
            }
        }
    }
    @TestFactory
    fun `Check error offline address search with zero radius passed`() = TestCase {
        Given("OfflineSearchEngine with mocked dependencies") {
            When("searchAddressesNearby() called with zero radius") {
                val testStreet = "Some test street"
                val testRadius = 0.0

                val callback = mockk<OfflineSearchCallback>(relaxed = true)
                val errorSlot = slot<Exception>()
                every { callback.onError(capture(errorSlot)) } returns Unit

                val task = searchEngine.searchAddressesNearby(
                    street = testStreet,
                    proximity = TEST_POINT,
                    radiusMeters = testRadius,
                    executor = executor,
                    callback = callback
                )

                Then("Task is executed", true, task.isDone)

                VerifyOnce("Callbacks called inside executor") {
                    executor.execute(any())
                }

                VerifyNo("CoreSearchEngine.getAddressesOffline() not called") {
                    coreEngine.getAddressesOffline(any(), any(), any(), any())
                }

                VerifyOnce("Error passed to callback") {
                    callback.onError(any())
                }

                Then("Passed error is correct") {
                    Assertions.assertTrue(errorSlot.captured is IllegalArgumentException)
                    Assertions.assertEquals("Negative or zero radius: 0.0", errorSlot.captured.message)
                }

                VerifyNo("onResults() wasn't called") {
                    callback.onResults(any(), any())
                }

                VerifyOnce("User activity reported") {
                    activityReporter.reportActivity(eq("offline-search-engine-search-nearby-street"))
                }
            }
        }
    }
    @TestFactory
    fun `Check error offline address search with invalid proximity`() = TestCase {
        Given("OfflineSearchEngine with mocked dependencies") {
            When("searchAddressesNearby() called with invalid proximity") {
                val testStreet = "Street with proximity"
                val testRadius = 600.0
                val testProximity = Point.fromLngLat(181.0, 0.0)

                val callback = mockk<OfflineSearchCallback>(relaxed = true)
                val errorSlot = slot<Exception>()
                every { callback.onError(capture(errorSlot)) } returns Unit

                val task = searchEngine.searchAddressesNearby(
                    street = testStreet,
                    proximity = testProximity,
                    radiusMeters = testRadius,
                    executor = executor,
                    callback = callback
                )

                Then("Task is executed", true, task.isDone)

                VerifyOnce("Callbacks called inside executor") {
                    executor.execute(any())
                }

                VerifyNo("CoreSearchEngine.getAddressesOffline() not called") {
                    coreEngine.getAddressesOffline(any(), any(), any(), any())
                }

                VerifyOnce("Error passed to callback") {
                    callback.onError(any())
                }

                Then("Passed error is correct") {
                    Assertions.assertTrue(errorSlot.captured is IllegalArgumentException)
                    Assertions.assertEquals("Invalid proximity(lon=181.0,lat=0.0)", errorSlot.captured.message)
                }

                VerifyNo("onResults() wasn't called") {
                    callback.onResults(any(), any())
                }

                VerifyOnce("User activity reported") {
                    activityReporter.reportActivity(eq("offline-search-engine-search-nearby-street"))
                }
            }
        }
    }

    @TestFactory
    fun `Check searchAddressesNearby with default executor`() = TestCase {
        Given("OfflineSearchEngine with mocked dependencies") {
            When("searchAddressesNearby() with default executor called") {
                searchEngine.searchAddressesNearby(
                    street = "Test street",
                    proximity = TEST_POINT,
                    radiusMeters = 3.141,
                    callback = mockk(relaxed = true)
                )

                VerifyOnce("SearchSdkMainThreadWorker.mainExecutor accessed") {
                    testMainThreadWorker.mainExecutor
                }

                VerifyOnce("Callback called inside executor") {
                    executor.execute(any())
                }

                VerifyOnce("User activity reported") {
                    activityReporter.reportActivity(eq("offline-search-engine-search-nearby-street"))
                }
            }
        }
    }

    @TestFactory
    fun `Check successful offline forward geocoding`() = TestCase {
        Given("OfflineSearchEngine with mocked dependencies") {
            val slotSearchCallback = slot<CoreSearchCallback>()
            every { coreEngine.searchOffline(any(), any(), any(), capture(slotSearchCallback)) } answers {
                slotSearchCallback.captured.run(TEST_RETRIEVED_SUCCESSFUL_CORE_RESPONSE)
            }

            When("Initial search called") {
                val callback = mockk<OfflineSearchCallback>(relaxed = true)

                val task = searchEngine.search(
                    query = TEST_QUERY,
                    options = OfflineSearchOptions(),
                    executor = executor,
                    callback = callback
                )

                Then("Task is executed", true, task.isDone)

                VerifyOnce("Callbacks called inside executor") {
                    executor.execute(any())
                }

                VerifyOnce("CoreSearchEngine.searchOffline() called") {
                    coreEngine.searchOffline(
                        eq(TEST_QUERY),
                        eq(emptyList()),
                        eq(OfflineSearchOptions().mapToCore()),
                        slotSearchCallback.captured
                    )
                }

                VerifyOnce("Results passed to callback") {
                    callback.onResults(
                        listOf(TEST_SEARCH_RESULT),
                        TEST_OFFLINE_RESPONSE_INFO,
                    )
                }

                VerifyNo("onError() wasn't called") {
                    callback.onError(any())
                }

                VerifyOnce("User activity reported") {
                    activityReporter.reportActivity(eq("offline-search-engine-forward-geocoding"))
                }
            }
        }
    }

    @TestFactory
    fun `Check offline forward geocoding with default executor`() = TestCase {
        Given("OfflineSearchEngine with mocked dependencies") {
            When("search() and select() with default executor called") {
                searchEngine.search(
                    query = TEST_QUERY,
                    options = OfflineSearchOptions(),
                    callback = mockk(relaxed = true)
                )

                Verify("SearchSdkMainThreadWorker.mainExecutor accessed", exactly = 1) {
                    testMainThreadWorker.mainExecutor
                }

                Verify("Callback called inside executor", exactly = 1) {
                    executor.execute(any())
                }

                VerifyOnce("User activity reported") {
                    activityReporter.reportActivity(eq("offline-search-engine-forward-geocoding"))
                }
            }
        }
    }

    @TestFactory
    fun `Check search call cancellation`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            val cancellationReason = "Request cancelled"

            val slotSearchCallback = slot<CoreSearchCallback>()
            every { coreEngine.searchOffline(eq(TEST_QUERY), any(), any(), capture(slotSearchCallback)) } answers {
                slotSearchCallback.captured.run(createTestCoreSearchResponseCancelled(cancellationReason))
            }

            When("Search request cancelled by the Search SDK") {
                val callback = mockk<OfflineSearchCallback>(relaxed = true)

                val task = searchEngine.search(TEST_QUERY, OfflineSearchOptions(), callback)

                Then("Task is cancelled", true, task.isCancelled)

                VerifyOnce("Callback called with cancellation error") {
                    callback.onError(eq(SearchCancellationException(cancellationReason)))
                }

                VerifyOnce("User activity reported") {
                    activityReporter.reportActivity(eq("offline-search-engine-forward-geocoding"))
                }
            }
        }
    }

    @TestFactory
    fun `Check successful retrieve`() = TestCase {
        Given("Feature from map click event") {
            val slotSearchCallback = slot<CoreSearchCallback>()
            every { coreEngine.searchOffline(any(), any(), any(), capture(slotSearchCallback)) } answers {
                slotSearchCallback.captured.run(TEST_RETRIEVED_SUCCESSFUL_CORE_RESPONSE)
            }

            When("Retrieve is called") {
                val callback = mockk<OfflineSearchResultCallback>(relaxed = true)

                val task = searchEngine.retrieve(
                    feature = TEST_RETRIEVE_FEATURE,
                    executor = executor,
                    callback = callback
                )

                Then("Task is executed", true, task.isDone)

                VerifyOnce("Callbacks called inside executor") {
                    executor.execute(any())
                }

                VerifyOnce("CoreSearchEngine.searchOffline() called") {
                    coreEngine.searchOffline(
                        eq(TEST_RETRIEVE_FEATURE.getStringProperty("name")),
                        eq(emptyList()),
                        eq(OfflineSearchOptions(
                            origin = TEST_RETRIEVE_FEATURE.geometry() as Point,
                            proximity = TEST_RETRIEVE_FEATURE.geometry() as Point,
                        ).mapToCore()),
                        slotSearchCallback.captured
                    )
                }

                VerifyOnce("Results passed to callback") {
                    callback.onResult(any(), any())
                }

                VerifyNo("onError() wasn't called") {
                    callback.onError(any())
                }
            }
        }
    }

    @TestFactory
    fun `Check offline search along route search`() = TestCase {
        Given("OfflineSearchEngine with mocked dependencies") {
            When("searchAlongRoute() called with explicitly initialized OfflineSearchAlongRouteOptions options") {
                val slotSearchCallback = slot<CoreSearchCallback>()

                every { coreEngine.searchOffline(any(), any(), any(), capture(slotSearchCallback)) } answers {
                    slotSearchCallback.captured.run(TEST_RETRIEVED_SUCCESSFUL_CORE_RESPONSE)
                }

                val callback = mockk<OfflineSearchCallback>(relaxed = true)

                val options = OfflineSearchAlongRouteOptions(
                    route = listOf(Point.fromLngLat(10.0, 20.0), Point.fromLngLat(20.0, 30.0)),
                    origin = Point.fromLngLat(40.0, 50.0),
                    limit = 15,
                    evSearchOptions = OfflineEvSearchOptions(connectorTypes = listOf(EvConnectorType.TESLA_S)),
                )

                val coreOptions = createCoreSearchOptions(
                    route = options.route,
                    proximity = options.route.first(),
                    origin = options.origin,
                    limit = options.limit,
                    evSearchOptions = options.evSearchOptions?.mapToCore(),
                )

                val task = searchEngine.searchAlongRoute(
                    query = TEST_QUERY,
                    options = options,
                    executor = executor,
                    callback = callback
                )

                Then("Task is executed", true, task.isDone)

                VerifyOnce("Callbacks called inside executor") {
                    executor.execute(any())
                }

                VerifyOnce("CoreSearchEngine.searchOffline() called with correct arguments") {
                    coreEngine.searchOffline(
                        eq(TEST_QUERY),
                        eq(emptyList()),
                        eq(coreOptions),
                        slotSearchCallback.captured
                    )
                }

                VerifyOnce("Results passed to callback") {
                    callback.onResults(
                        listOf(TEST_SEARCH_RESULT),
                        TEST_OFFLINE_RESPONSE_INFO
                    )
                }

                VerifyNo("onError() wasn't called") {
                    callback.onError(any())
                }

                VerifyOnce("User activity reported") {
                    activityReporter.reportActivity(eq("offline-search-engine-search-along-route"))
                }
            }
        }
    }

    @TestFactory
    fun `Check offline search along route search default parameters`() = TestCase {
        Given("OfflineSearchEngine with mocked dependencies") {
            When("searchAlongRoute() called with OfflineSearchAlongRouteOptions default parameters") {
                val slotSearchCallback = slot<CoreSearchCallback>()

                every { coreEngine.searchOffline(any(), any(), any(), capture(slotSearchCallback)) } answers {
                    slotSearchCallback.captured.run(TEST_RETRIEVED_SUCCESSFUL_CORE_RESPONSE)
                }

                val callback = mockk<OfflineSearchCallback>(relaxed = true)

                val options = OfflineSearchAlongRouteOptions(
                    route = listOf(Point.fromLngLat(10.0, 20.0), Point.fromLngLat(20.0, 30.0)),
                )

                val coreOptions = createCoreSearchOptions(
                    route = options.route,
                    proximity = options.route.first(),
                    origin = options.origin,
                    limit = null,
                    evSearchOptions = null,
                )

                searchEngine.searchAlongRoute(
                    query = TEST_QUERY,
                    options = options,
                    executor = executor,
                    callback = callback
                )

                VerifyOnce("CoreSearchEngine.searchOffline() called with correct arguments") {
                    coreEngine.searchOffline(
                        eq(TEST_QUERY),
                        eq(emptyList()),
                        eq(coreOptions),
                        slotSearchCallback.captured
                    )
                }
            }
        }
    }

    @Test
    fun `Check selectTileset() functions`() {
        searchEngine.selectTileset("test-dataset", "test-version")
        verify(exactly = 1) {
            coreEngine.selectTileset("test-dataset", "test-version")
        }

        val tilesetParameters = TilesetParameters.Builder(dataset = "test-dataset", version = "test-version")
            .worldview(IsoLanguageCode.ENGLISH, IsoCountryCode.MOROCCO)
            .build()

        clearMocks(coreEngine)
        searchEngine.selectTileset(tilesetParameters)
        verify(exactly = 1) {
            coreEngine.selectTileset(
                tilesetParameters.generatedDatasetName,
                "test-version",
            )
        }
    }

    @TestFactory
    fun `Check offline category search`() = TestCase {
        Given("OfflineSearchEngine with mocked dependencies") {
            val slotSearchCallback = slot<CoreSearchCallback>()
            every { coreEngine.searchOffline(any(), any(), any(), capture(slotSearchCallback)) } answers {
                slotSearchCallback.captured.run(TEST_RETRIEVED_SUCCESSFUL_CORE_RESPONSE)
            }

            When("Category search called") {
                val callback = mockk<OfflineSearchCallback>(relaxed = true)

                val task = searchEngine.categorySearch(
                    categoryName = "cafe",
                    options = OfflineCategorySearchOptions(),
                    executor = executor,
                    callback = callback
                )

                Then("Task is executed", true, task.isDone)

                VerifyOnce("Callbacks called inside executor") {
                    executor.execute(any())
                }

                VerifyOnce("CoreSearchEngine.searchOffline() called") {
                    coreEngine.searchOffline(
                        eq(""),
                        eq(listOf("cafe")),
                        eq(OfflineCategorySearchOptions().mapToCore()),
                        slotSearchCallback.captured
                    )
                }

                VerifyOnce("Results passed to callback") {
                    callback.onResults(
                        listOf(TEST_SEARCH_RESULT),
                        TEST_OFFLINE_RESPONSE_INFO,
                    )
                }

                VerifyNo("onError() wasn't called") {
                    callback.onError(any())
                }

                VerifyOnce("User activity reported") {
                    activityReporter.reportActivity(eq("offline-search-engine-category-search"))
                }
            }
        }
    }

    private companion object {

        const val TEST_QUERY = "Minsk"

        const val TEST_RESPONSE_UUID = "test response uuid"
        val TEST_SEARCH_REQUEST_CONTEXT = SearchRequestContext(CoreApiType.SBS, responseUuid = TEST_RESPONSE_UUID)

        val TEST_POINT: Point = Point.fromLngLat(10.0, 11.0)
        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.0, 11.0)
        val TEST_SEARCH_ADDRESS = createCoreSearchAddress()

        val TEST_RETRIEVED_CORE_SEARCH_RESULT = createTestCoreSearchResult(
            types = listOf(ResultType.ADDRESS),
            addresses = listOf(TEST_SEARCH_ADDRESS),
            center = Point.fromLngLat(20.0, 30.0),
        )

        val TEST_CORE_REQUEST_OPTIONS = createTestCoreRequestOptions(
            query = "",
            options = OfflineSearchOptions(proximity = TEST_USER_LOCATION).mapToCore()
        )

        val TEST_OFFLINE_RESPONSE_INFO = OfflineResponseInfo(TEST_CORE_REQUEST_OPTIONS.mapToOfflineSdkType())

        val TEST_RETRIEVED_SUCCESSFUL_CORE_RESPONSE = createTestCoreSearchResponseSuccess(
            request = TEST_CORE_REQUEST_OPTIONS,
            results = listOf(TEST_RETRIEVED_CORE_SEARCH_RESULT),
            responseUUID = TEST_RESPONSE_UUID
        )

        val TEST_SEARCH_RESULT = OfflineSearchResult(
            rawSearchResult = TEST_RETRIEVED_CORE_SEARCH_RESULT.mapToBase(),
        )

        val TEST_RETRIEVE_FEATURE = Feature.fromJson("""
                {
                    "type": "Feature",
                    "id": "132494314",
                    "geometry": {
                        "type": "Point",
                        "coordinates": [
                            -77.2214842,
                            39.0468692
                        ]
                    },
                    "properties": {
                        "class": "park_like",
                        "iso_3166_2": "US-MD",
                        "name_script": "Latin",
                        "filterrank": 1,
                        "type": "Nature Reserve",
                        "sizerank": 14,
                        "name": "Adventure Conservation Park",
                        "iso_3166_1": "US",
                        "maki": "park"
                    }
                }
            """.trimIndent())!!

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            resetLogImpl()
            mockkStatic(TestConstants.ASSERTIONS_KT_CLASS_NAME)
        }

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            reinitializeLogImpl()
            unmockkStatic(TestConstants.ASSERTIONS_KT_CLASS_NAME)
        }
    }
}
