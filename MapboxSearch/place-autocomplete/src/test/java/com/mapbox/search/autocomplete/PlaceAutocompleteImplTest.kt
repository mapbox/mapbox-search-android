package com.mapbox.search.autocomplete

import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.autocomplete.test.utils.testBaseResult
import com.mapbox.search.base.core.CoreReverseGeoOptions
import com.mapbox.search.base.core.CoreSearchOptions
import com.mapbox.search.base.core.createCoreReverseGeoOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.engine.TwoStepsToOneStepSearchEngineAdapter
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.internal.bindgen.QueryType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PlaceAutocompleteImplTest {

    private lateinit var searchEngine: TwoStepsToOneStepSearchEngineAdapter
    private lateinit var resultFactory: PlaceAutocompleteResultFactory
    private lateinit var placeAutocomplete: PlaceAutocomplete

    @BeforeEach
    fun setUp() {
        searchEngine = mockk(relaxed = true)
        resultFactory = mockk(relaxed = true)

        placeAutocomplete = PlaceAutocompleteImpl(searchEngine, resultFactory)

        coEvery { searchEngine.searchResolveImmediately(any(), any()) } answers {
            ExpectedFactory.createValue(mockk())
        }

        coEvery { searchEngine.reverseGeocoding(any()) } answers {
            ExpectedFactory.createValue(mockk<List<BaseSearchResult>>() to mockk())
        }

        every { resultFactory.createPlaceAutocompleteSuggestions(any()) } answers {
            mockk()
        }
    }

    @Test
    fun `check correct returned data for forward geocoding request`() {
        coEvery { searchEngine.searchResolveImmediately(any(), any()) } answers {
            ExpectedFactory.createValue(TEST_BASE_RESULTS)
        }

        every { resultFactory.createPlaceAutocompleteSuggestions(eq(TEST_BASE_RESULTS)) } answers {
            TEST_AUTOCOMPLETE_SUGGESTIONS
        }

        val response = runBlocking {
            placeAutocomplete.suggestions(TEST_QUERY)
        }

        assertSame(TEST_AUTOCOMPLETE_SUGGESTIONS, response.value)

        coVerify(exactly = 1) { searchEngine.searchResolveImmediately(any(), any()) }
        verify(exactly = 1) { resultFactory.createPlaceAutocompleteSuggestions(listOf(testBaseResult)) }
    }

    @Test
    fun `check correct returned error for forward geocoding request`() {
        val error = Exception()
        coEvery { searchEngine.searchResolveImmediately(any(), any()) } answers {
            ExpectedFactory.createError(error)
        }

        val response = runBlocking {
            placeAutocomplete.suggestions(TEST_QUERY)
        }

        assertSame(error, response.error)

        coVerify(exactly = 1) { searchEngine.searchResolveImmediately(any(), any()) }
        verify(exactly = 0) { resultFactory.createPlaceAutocompleteSuggestions(any()) }
    }

    @Test
    fun `check request parameters for forward geocoding request`() {
        val options = PlaceAutocompleteOptions(
            limit = 5,
            countries = listOf(IsoCountryCode.FRANCE),
            language = IsoLanguageCode.FRENCH,
            types = listOf(
                PlaceAutocompleteType.Poi,
                PlaceAutocompleteType.AdministrativeUnit.Address,
                PlaceAutocompleteType.AdministrativeUnit.Street,
            )
        )

        val coreOptions = createCoreSearchOptions(
            proximity = TEST_PROXIMITY,
            bbox = TEST_BBOX.mapToCore(),
            countries = options.countries?.map { it.code },
            language = listOf(options.language.code),
            limit = options.limit,
            types = listOf(QueryType.POI, QueryType.ADDRESS, QueryType.STREET),
            ignoreUR = false,
        )

        runBlocking { placeAutocomplete.suggestions(TEST_QUERY, TEST_BBOX, TEST_PROXIMITY, options) }
        coVerify { searchEngine.searchResolveImmediately(eq(TEST_QUERY), coreOptions) }
    }

    @Test
    fun `check request types for forward geocoding request with administrativeUnits = null`() {
        val slotOptions = slot<CoreSearchOptions>()
        coEvery { searchEngine.searchResolveImmediately(any(), capture(slotOptions)) } answers {
            ExpectedFactory.createError(mockk())
        }

        val options = PlaceAutocompleteOptions(
            types = null
        )

        runBlocking { placeAutocomplete.suggestions(TEST_QUERY, options = options) }

        assertEquals(ALL_TYPES, slotOptions.captured.types)
    }

    @Test
    fun `check request types for forward geocoding request with empty administrativeUnits`() {
        val slotOptions = slot<CoreSearchOptions>()
        coEvery { searchEngine.searchResolveImmediately(any(), capture(slotOptions)) } answers {
            ExpectedFactory.createError(mockk())
        }

        val options = PlaceAutocompleteOptions(
            types = emptyList()
        )

        runBlocking { placeAutocomplete.suggestions(TEST_QUERY, options = options) }

        assertEquals(ALL_TYPES, slotOptions.captured.types)
    }

    @Test
    fun `check correct returned data for reverse geocoding request`() {
        coEvery { searchEngine.reverseGeocoding(any()) } answers {
            ExpectedFactory.createValue(TEST_BASE_RESULTS to mockk())
        }

        every { resultFactory.createPlaceAutocompleteSuggestions(eq(TEST_BASE_RESULTS)) } answers {
            TEST_AUTOCOMPLETE_SUGGESTIONS
        }

        val response = runBlocking {
            placeAutocomplete.suggestions(TEST_POINT)
        }

        assertSame(TEST_AUTOCOMPLETE_SUGGESTIONS, response.value)

        coVerify(exactly = 1) { searchEngine.reverseGeocoding(any()) }
        verify(exactly = 1) { resultFactory.createPlaceAutocompleteSuggestions(listOf(testBaseResult)) }
    }

    @Test
    fun `check correct returned error for forward reverse request`() {
        val error = Exception()
        coEvery { searchEngine.reverseGeocoding(any()) } answers {
            ExpectedFactory.createError(error)
        }

        val response = runBlocking {
            placeAutocomplete.suggestions(TEST_POINT)
        }

        assertSame(error, response.error)

        coVerify(exactly = 1) { searchEngine.reverseGeocoding(any()) }
        verify(exactly = 0) { resultFactory.createPlaceAutocompleteSuggestions(any()) }
    }

    @Test
    fun `check request parameters for reverse geocoding request`() {
        val options = PlaceAutocompleteOptions(
            limit = 5,
            countries = listOf(IsoCountryCode.FRANCE),
            language = IsoLanguageCode.FRENCH,
            types = listOf(
                PlaceAutocompleteType.Poi,
                PlaceAutocompleteType.AdministrativeUnit.Address,
                PlaceAutocompleteType.AdministrativeUnit.Street,
            )
        )

        val coreOptions = createCoreReverseGeoOptions(
            point = TEST_POINT,
            countries = options.countries?.map { it.code },
            language = listOf(options.language.code),
            limit = options.limit,
            types = listOf(QueryType.POI, QueryType.ADDRESS, QueryType.STREET),
        )

        runBlocking { placeAutocomplete.suggestions(TEST_POINT, options) }
        coVerify { searchEngine.reverseGeocoding(coreOptions) }
    }

    @Test
    fun `check request types for reverse geocoding request with administrativeUnits = null`() {
        val slotOptions = slot<CoreReverseGeoOptions>()
        coEvery { searchEngine.reverseGeocoding(capture(slotOptions)) } answers {
            ExpectedFactory.createValue(mockk<List<BaseSearchResult>>() to mockk())
        }

        val options = PlaceAutocompleteOptions(
            types = null
        )

        runBlocking { placeAutocomplete.suggestions(TEST_POINT, options) }

        assertEquals(ALL_TYPES, slotOptions.captured.types)
    }

    @Test
    fun `check request types for reverse geocoding request with empty administrativeUnits`() {
        val slotOptions = slot<CoreReverseGeoOptions>()
        coEvery { searchEngine.reverseGeocoding(capture(slotOptions)) } answers {
            ExpectedFactory.createValue(mockk<List<BaseSearchResult>>() to mockk())
        }

        val options = PlaceAutocompleteOptions(
            types = emptyList()
        )

        runBlocking { placeAutocomplete.suggestions(TEST_POINT, options) }

        assertEquals(ALL_TYPES, slotOptions.captured.types)
    }

    private companion object {

        val TEST_POINT: Point = Point.fromLngLat(1.0, 5.0)
        val TEST_BBOX: BoundingBox = BoundingBox.fromPoints(Point.fromLngLat(10.0, 11.0), Point.fromLngLat(15.0, 16.0))
        val TEST_PROXIMITY: Point = Point.fromLngLat(100.0, 150.0)
        const val TEST_QUERY = "Test query"

        val TEST_BASE_RESULTS: List<BaseSearchResult> = listOf(testBaseResult)

        val TEST_AUTOCOMPLETE_RESULT: PlaceAutocompleteResult = PlaceAutocompleteResultFactory().createPlaceAutocompleteResult(testBaseResult)!!
        val TEST_AUTOCOMPLETE_SUGGESTIONS: List<PlaceAutocompleteSuggestion> = listOf(PlaceAutocompleteSuggestion(TEST_AUTOCOMPLETE_RESULT))

        private val ALL_TYPES = PlaceAutocompleteType.ALL_DECLARED_TYPES.map { it.coreType }
    }
}
