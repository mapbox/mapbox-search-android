package com.mapbox.search.autocomplete

import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.autocomplete.test.utils.createTestBaseSearchSuggestion
import com.mapbox.search.autocomplete.test.utils.testBaseRawSearchSuggestionWithoutCoordinates
import com.mapbox.search.autocomplete.test.utils.testBaseResult
import com.mapbox.search.base.core.CoreReverseGeoOptions
import com.mapbox.search.base.core.CoreSearchOptions
import com.mapbox.search.base.core.createCoreReverseGeoOptions
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.engine.TwoStepsToOneStepSearchEngineAdapter
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.NavigationProfile
import com.mapbox.search.internal.bindgen.QueryType
import com.mapbox.search.internal.bindgen.UserActivityReporterInterface
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PlaceAutocompleteImplTest {

    private lateinit var searchEngine: TwoStepsToOneStepSearchEngineAdapter
    private lateinit var activityReporter: UserActivityReporterInterface
    private lateinit var resultFactory: PlaceAutocompleteResultFactory
    private lateinit var placeAutocomplete: PlaceAutocomplete

    @BeforeEach
    fun setUp() {
        searchEngine = mockk(relaxed = true)
        activityReporter = mockk(relaxed = true)
        resultFactory = spyk(PlaceAutocompleteResultFactory())

        placeAutocomplete = PlaceAutocompleteImpl(
            searchEngine = searchEngine,
            activityReporter = activityReporter,
            resultFactory = resultFactory
        )

        coEvery { searchEngine.searchResolveImmediately(any(), any()) } answers {
            ExpectedFactory.createValue(emptyList())
        }

        coEvery { searchEngine.search(any(), any()) } answers {
            ExpectedFactory.createValue(emptyList<BaseSearchSuggestion>() to mockk())
        }

        coEvery { searchEngine.reverseGeocoding(any()) } answers {
            ExpectedFactory.createValue(emptyList<BaseSearchResult>() to mockk())
        }
    }

    @Test
    fun `check correct returned data for forward geocoding request`() {
        coEvery { searchEngine.search(any(), any()) } answers {
            ExpectedFactory.createValue(TEST_BASE_SUGGESTIONS to mockk())
        }

        coEvery { searchEngine.resolveAll(any(), any()) } answers {
            ExpectedFactory.createValue(TEST_BASE_RESULTS)
        }

        val response = runBlocking {
            placeAutocomplete.suggestions(TEST_QUERY)
        }

        assertEquals(TEST_AUTOCOMPLETE_SUGGESTIONS, response.value)

        coVerify(exactly = 1) { searchEngine.search(eq(TEST_QUERY), any()) }
        coVerify(exactly = 1) { searchEngine.resolveAll(eq(TEST_BASE_SUGGESTIONS), eq(false)) }
        verify(exactly = 1) { resultFactory.createPlaceAutocompleteSuggestions(listOf(testBaseResult)) }
        verify(exactly = 1) { activityReporter.reportActivity(eq("place-autocomplete-forward-geocoding")) }
    }

    @Test
    fun `check correct returned error for forward geocoding request`() {
        val error = Exception()
        coEvery { searchEngine.search(any(), any()) } answers {
            ExpectedFactory.createError(error)
        }

        val response = runBlocking {
            placeAutocomplete.suggestions(TEST_QUERY)
        }

        assertSame(error, response.error)

        coVerify(exactly = 1) { searchEngine.search(eq(TEST_QUERY), any()) }
        verify(exactly = 0) { resultFactory.createPlaceAutocompleteSuggestions(any()) }
        verify(exactly = 1) { activityReporter.reportActivity(eq("place-autocomplete-forward-geocoding")) }
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
            ),
            navigationProfile = NavigationProfile.CYCLING
        )

        val coreOptions = createCoreSearchOptions(
            proximity = TEST_PROXIMITY,
            origin = TEST_PROXIMITY,
            bbox = TEST_BBOX.mapToCore(),
            countries = options.countries?.map { it.code },
            language = listOf(options.language.code),
            limit = options.limit,
            types = listOf(QueryType.POI, QueryType.ADDRESS, QueryType.STREET),
            navProfile = NavigationProfile.CYCLING.rawName,
            etaType = DEFAULT_ETA_TYPE,
            ignoreUR = false,
        )

        runBlocking { placeAutocomplete.suggestions(TEST_QUERY, TEST_BBOX, TEST_PROXIMITY, options) }
        coVerify(exactly = 1) { searchEngine.search(eq(TEST_QUERY), coreOptions) }
        verify(exactly = 1) { activityReporter.reportActivity(eq("place-autocomplete-forward-geocoding")) }
    }

    @Test
    fun `check request types for forward geocoding request with administrativeUnits = null`() {
        val slotOptions = slot<CoreSearchOptions>()
        coEvery { searchEngine.search(any(), capture(slotOptions)) } answers {
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
        coEvery { searchEngine.search(any(), capture(slotOptions)) } answers {
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

        val response = runBlocking {
            placeAutocomplete.suggestions(TEST_POINT)
        }

        assertEquals(TEST_AUTOCOMPLETE_SUGGESTIONS, response.value)

        coVerify(exactly = 1) { searchEngine.reverseGeocoding(any()) }
        verify(exactly = 1) { resultFactory.createPlaceAutocompleteSuggestions(listOf(testBaseResult)) }
        verify(exactly = 1) { activityReporter.reportActivity(eq("place-autocomplete-reverse-geocoding")) }
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
        verify(exactly = 1) { activityReporter.reportActivity(eq("place-autocomplete-reverse-geocoding")) }
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
        verify(exactly = 1) { activityReporter.reportActivity(eq("place-autocomplete-reverse-geocoding")) }
    }

    @Test
    fun `check request types for reverse geocoding request with administrativeUnits = null`() {
        val slotOptions = slot<CoreReverseGeoOptions>()
        coEvery { searchEngine.reverseGeocoding(capture(slotOptions)) } answers {
            ExpectedFactory.createValue(emptyList<BaseSearchResult>() to mockk())
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
            ExpectedFactory.createValue(emptyList<BaseSearchResult>() to mockk())
        }

        val options = PlaceAutocompleteOptions(
            types = emptyList()
        )

        runBlocking { placeAutocomplete.suggestions(TEST_POINT, options) }

        assertEquals(ALL_TYPES, slotOptions.captured.types)
    }

    @Test
    fun `check suggestion selection`() {
        val response = runBlocking {
            placeAutocomplete.select(TEST_AUTOCOMPLETE_SUGGESTIONS.first())
        }

        assertEquals(TEST_AUTOCOMPLETE_RESULT, response.value)

        coVerify(exactly = 0) { searchEngine.select(any()) }
        coVerify(exactly = 0) { searchEngine.resolveAll(any(), any()) }
        verify(exactly = 1) { resultFactory.createPlaceAutocompleteResultOrError(eq(testBaseResult)) }
        verify(exactly = 1) { activityReporter.reportActivity(eq("place-autocomplete-suggestion-select")) }
    }

    private companion object {

        const val DEFAULT_ETA_TYPE = "navigation"

        val TEST_POINT: Point = Point.fromLngLat(1.0, 5.0)
        val TEST_BBOX: BoundingBox = BoundingBox.fromPoints(Point.fromLngLat(10.0, 11.0), Point.fromLngLat(15.0, 16.0))
        val TEST_PROXIMITY: Point = Point.fromLngLat(100.0, 150.0)
        const val TEST_QUERY = "Test query"

        val TEST_BASE_SUGGESTIONS: List<BaseSearchSuggestion> = listOf(
            createTestBaseSearchSuggestion(testBaseRawSearchSuggestionWithoutCoordinates)
        )

        val TEST_BASE_RESULTS: List<BaseSearchResult> = listOf(testBaseResult)

        val TEST_AUTOCOMPLETE_SUGGESTIONS: List<PlaceAutocompleteSuggestion> = listOf(
            PlaceAutocompleteResultFactory().createPlaceAutocompleteSuggestion(
                testBaseResult.types.firstNotNullOfOrNull { PlaceAutocompleteType.createFromBaseType(it) }!!,
                testBaseResult
            )
        )

        val TEST_AUTOCOMPLETE_RESULT = PlaceAutocompleteResultFactory()
            .createPlaceAutocompleteResultOrError(testBaseResult).value!!

        private val ALL_TYPES = PlaceAutocompleteType.ALL_DECLARED_TYPES.map { it.coreType }
    }
}
