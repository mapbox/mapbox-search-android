package com.mapbox.search.discover

import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseSearchCallback
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchResultType
import com.mapbox.search.base.result.BaseServerSearchResultImpl
import com.mapbox.search.base.result.mapToBase
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.TestExecutor
import com.mapbox.search.common.createTestCoreSearchResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

internal class DiscoverImplTest {

    private lateinit var discover: Discover
    private lateinit var engine: DiscoverSearchEngine

    private val testExecutor = TestExecutor()

    @BeforeEach
    fun setUp() {
        engine = mockk()
        discover = DiscoverImpl(engine)
    }

    @Test
    fun `Check request parameters for nearby search`() {
        coEvery { engine.search(any(), any()) } returns ExpectedFactory.createError(Exception())

        runBlocking {
            discover.search(TEST_QUERY, TEST_USER_LOCATION, TEST_OPTIONS)
        }

        val coreOptions = createCoreSearchOptions(
            proximity = TEST_USER_LOCATION,
            limit = TEST_OPTIONS.limit,
            language = listOf(TEST_OPTIONS.language.code),
            ignoreUR = true,
        )

        coVerify(exactly = 1) {
            engine.search(
                eq(TEST_QUERY.canonicalName),
                eq(coreOptions)
            )
        }
    }

    @Test
    fun `Check request parameters for nearby search with default options`() {
        coEvery { engine.search(any(), any()) } returns ExpectedFactory.createError(Exception())

        runBlocking {
            discover.search(TEST_QUERY, TEST_USER_LOCATION)
        }

        val defaultOptions = DiscoverOptions()

        val coreOptions = createCoreSearchOptions(
            proximity = TEST_USER_LOCATION,
            limit = defaultOptions.limit,
            language = listOf(defaultOptions.language.code),
            ignoreUR = true,
        )

        coVerify(exactly = 1) {
            engine.search(
                eq(TEST_QUERY.canonicalName),
                eq(coreOptions)
            )
        }
    }

    @Test
    fun `Check request parameters for callback-based nearby search`() {
        val slotCallback = slot<BaseSearchCallback>()
        every { engine.search(any(), any(), any(), capture(slotCallback)) } answers {
            slotCallback.captured.onError(Exception())
            mockk()
        }

        val defaultOptions = DiscoverOptions()

        val callback = mockk<CompletionCallback<List<DiscoverResult>>>(relaxed = true)
        discover.search(TEST_QUERY, TEST_USER_LOCATION, defaultOptions, testExecutor, callback)

        val coreOptions = createCoreSearchOptions(
            proximity = TEST_USER_LOCATION,
            limit = defaultOptions.limit,
            language = listOf(defaultOptions.language.code),
            ignoreUR = true,
        )

        verify(exactly = 1) {
            engine.search(
                eq(TEST_QUERY.canonicalName),
                eq(coreOptions),
                eq(testExecutor),
                eq(slotCallback.captured)
            )
        }
    }

    @Test
    fun `Check request parameters for search in region`() {
        coEvery { engine.search(any(), any()) } returns ExpectedFactory.createError(Exception())

        runBlocking {
            discover.search(TEST_QUERY, TEST_BOUNDING_BOX, TEST_USER_LOCATION, TEST_OPTIONS)
        }

        val coreOptions = createCoreSearchOptions(
            proximity = TEST_USER_LOCATION,
            bbox = TEST_BOUNDING_BOX.mapToCore(),
            limit = TEST_OPTIONS.limit,
            language = listOf(TEST_OPTIONS.language.code),
            ignoreUR = true,
        )

        coVerify(exactly = 1) {
            engine.search(
                eq(TEST_QUERY.canonicalName),
                eq(coreOptions)
            )
        }
    }

    @Test
    fun `Check request parameters for search in region with default proximity and options`() {
        coEvery { engine.search(any(), any()) } returns ExpectedFactory.createError(Exception())

        runBlocking {
            discover.search(query = TEST_QUERY, region = TEST_BOUNDING_BOX)
        }

        val defaultOptions = DiscoverOptions()

        val coreOptions = createCoreSearchOptions(
            proximity = null,
            bbox = TEST_BOUNDING_BOX.mapToCore(),
            limit = defaultOptions.limit,
            language = listOf(defaultOptions.language.code),
            ignoreUR = true,
        )

        coVerify(exactly = 1) {
            engine.search(
                eq(TEST_QUERY.canonicalName),
                eq(coreOptions)
            )
        }
    }

    @Test
    fun `Check request parameters for callback-based search in region`() {
        val slotCallback = slot<BaseSearchCallback>()
        every { engine.search(any(), any(), any(), capture(slotCallback)) } answers {
            slotCallback.captured.onError(Exception())
            mockk()
        }

        val defaultOptions = DiscoverOptions()

        val callback = mockk<CompletionCallback<List<DiscoverResult>>>(relaxed = true)
        discover.search(TEST_QUERY, TEST_BOUNDING_BOX, TEST_USER_LOCATION, defaultOptions, testExecutor, callback)

        val coreOptions = createCoreSearchOptions(
            proximity = TEST_USER_LOCATION,
            bbox = TEST_BOUNDING_BOX.mapToCore(),
            limit = defaultOptions.limit,
            language = listOf(defaultOptions.language.code),
            ignoreUR = true,
        )

        verify(exactly = 1) {
            engine.search(
                eq(TEST_QUERY.canonicalName),
                eq(coreOptions),
                eq(testExecutor),
                eq(slotCallback.captured)
            )
        }
    }

    @Test
    fun `Check request parameters for search along the route`() {
        coEvery { engine.search(any(), any()) } returns ExpectedFactory.createError(Exception())

        runBlocking {
            discover.search(TEST_QUERY, TEST_ROUTE, TEST_DEVIATION, TEST_OPTIONS)
        }

        val coreOptions = createCoreSearchOptions(
            limit = TEST_OPTIONS.limit,
            language = listOf(TEST_OPTIONS.language.code),
            route = TEST_ROUTE,
            sarType = TEST_DEVIATION.sarType?.rawName,
            timeDeviation = TEST_DEVIATION.timeDeviationMinutes,
            ignoreUR = true,
        )

        coVerify(exactly = 1) {
            engine.search(
                eq(TEST_QUERY.canonicalName),
                eq(coreOptions)
            )
        }
    }

    @Test
    fun `Check request parameters for search along the route with default deviation and options`() {
        coEvery { engine.search(any(), any()) } returns ExpectedFactory.createError(Exception())

        runBlocking {
            discover.search(query = TEST_QUERY, route = TEST_ROUTE)
        }

        val defaultOptions = DiscoverOptions()

        val coreOptions = createCoreSearchOptions(
            limit = defaultOptions.limit,
            language = listOf(defaultOptions.language.code),
            route = TEST_ROUTE,
            sarType = RouteDeviationOptions.SarType.ISOCHROME.rawName,
            timeDeviation = RouteDeviationOptions.Time(value = 10L, unit = TimeUnit.MINUTES).timeDeviationMinutes,
            ignoreUR = true,
        )

        coVerify(exactly = 1) {
            engine.search(
                eq(TEST_QUERY.canonicalName),
                eq(coreOptions)
            )
        }
    }

    @Test
    fun `Check request parameters for callback-based search along the route`() {
        val slotCallback = slot<BaseSearchCallback>()
        every { engine.search(any(), any(), any(), capture(slotCallback)) } answers {
            slotCallback.captured.onError(Exception())
            mockk()
        }

        val defaultOptions = DiscoverOptions()

        val callback = mockk<CompletionCallback<List<DiscoverResult>>>(relaxed = true)
        discover.search(TEST_QUERY, TEST_ROUTE, RouteDeviationOptions.DEFAULT_DEVIATION, defaultOptions, testExecutor, callback)

        val coreOptions = createCoreSearchOptions(
            limit = defaultOptions.limit,
            language = listOf(defaultOptions.language.code),
            route = TEST_ROUTE,
            sarType = RouteDeviationOptions.SarType.ISOCHROME.rawName,
            timeDeviation = RouteDeviationOptions.Time(value = 10L, unit = TimeUnit.MINUTES).timeDeviationMinutes,
            ignoreUR = true,
        )

        verify(exactly = 1) {
            engine.search(
                eq(TEST_QUERY.canonicalName),
                eq(coreOptions),
                eq(testExecutor),
                eq(slotCallback.captured)
            )
        }
    }

    @Test
    fun `Check successful search response mapping to Discover format`() {
        val coreResults = listOf(
            createTestSearchResult(point(1, 2)),
            createTestSearchResult(point(3, 4))
        )

        coEvery { engine.search(any(), any()) } returns ExpectedFactory.createValue(coreResults to mockk())

        val discoverResult = runBlocking {
            discover.search(TEST_QUERY, TEST_USER_LOCATION, TEST_OPTIONS)
        }

        assertTrue(discoverResult.isValue)

        assertEquals(
            coreResults.map { DiscoverResult.createFromSearchResult(it) },
            discoverResult.value
        )
    }

    @Test
    fun `Check callback-based successful search response mapping to Discover format`() {
        val coreResults = listOf(
            createTestSearchResult(point(1, 2)),
            createTestSearchResult(point(3, 4))
        )

        val mockedTask = AsyncOperationTaskImpl<Unit>()
        val slotCallback = slot<BaseSearchCallback>()
        every { engine.search(any(), any(), any(), capture(slotCallback)) } answers {
            slotCallback.captured.onResults(coreResults, mockk())
            mockedTask
        }

        val callback = mockk<CompletionCallback<List<DiscoverResult>>>(relaxed = true)
        val searchTask = discover.search(TEST_QUERY, TEST_USER_LOCATION, TEST_OPTIONS, testExecutor, callback)

        verify(exactly = 1) {
            callback.onComplete(eq(coreResults.map { DiscoverResult.createFromSearchResult(it) }))
        }

        assertSame(mockedTask, searchTask)
    }

    @Test
    fun `Check error search response mapping to Discover format`() {
        val error = IOException("Test Unknown Host Error")
        coEvery { engine.search(any(), any()) } returns ExpectedFactory.createError(error)

        val result = runBlocking {
            discover.search(TEST_QUERY, TEST_USER_LOCATION, TEST_OPTIONS)
        }

        assertTrue(result.isError)
        assertSame(error, result.error)
    }

    @Test
    fun `Check callback-based error search response mapping to Discover format`() {
        val error = IOException("Test Unknown Host Error")

        val mockedTask = AsyncOperationTaskImpl<Unit>()
        val slotCallback = slot<BaseSearchCallback>()
        every { engine.search(any(), any(), any(), capture(slotCallback)) } answers {
            slotCallback.captured.onError(error)
            mockedTask
        }

        val callback = mockk<CompletionCallback<List<DiscoverResult>>>(relaxed = true)
        val searchTask = discover.search(TEST_QUERY, TEST_USER_LOCATION, TEST_OPTIONS, testExecutor, callback)

        verify(exactly = 1) {
            callback.onError(eq(error))
        }

        assertSame(mockedTask, searchTask)
    }

    private companion object {

        val TEST_USER_LOCATION: Point = point(1, 2)
        val TEST_BOUNDING_BOX: BoundingBox = BoundingBox.fromPoints(point(3, 4), point(5, 6))
        val TEST_QUERY = DiscoverQuery.Category.COFFEE_SHOP_CAFE
        val TEST_OPTIONS = DiscoverOptions(limit = 5, language = IsoLanguageCode.FRENCH)
        val TEST_ROUTE = listOf(point(1, 2), point(3, 4), point(5, 6))
        val TEST_DEVIATION = RouteDeviationOptions.Time(15, TimeUnit.HOURS, RouteDeviationOptions.SarType("Test SAR type"))

        fun point(lng: Int, lat: Int): Point = Point.fromLngLat(lng.toDouble(), lat.toDouble())

        fun createTestSearchResult(coordinate: Point): BaseSearchResult {
            return BaseServerSearchResultImpl(
                listOf(BaseSearchResultType.POI),
                createTestCoreSearchResult(center = coordinate).mapToBase(),
                mockk(),
            )
        }
    }
}
