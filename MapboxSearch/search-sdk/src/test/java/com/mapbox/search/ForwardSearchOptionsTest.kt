package com.mapbox.search

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.createCoreSearchOptions
import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.NavigationProfile
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.TestConstants
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.withPrefabTestPoint
import com.mapbox.search.tests_support.SdkCustomTypeObjectCreators
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

internal class ForwardSearchOptionsTest {

    private lateinit var defaultLocale: Locale

    @BeforeEach
    fun setUp() {
        defaultLocale = Locale.getDefault()
        Locale.setDefault(TEST_LOCALE)
    }

    @AfterEach
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `Test generated equals(), hashCode() and toString() methods`() {
        EqualsVerifier.forClass(ForwardSearchOptions::class.java)
            .withPrefabTestPoint()
            .verify()

        ToStringVerifier(
            clazz = ForwardSearchOptions::class,
            objectsFactory = ReflectionObjectsFactory(SdkCustomTypeObjectCreators.ALL_CREATORS)
        ).verify()
    }

    @Test
    fun `Test mapToCore() function`() {
        val options = TEST_FILLED_OPTIONS

        val expectedCoreOptions = createCoreSearchOptions(
            language = listOf(options.language!!.code),
            limit = options.limit,
            proximity = options.proximity,
            bbox = options.boundingBox?.mapToCore(),
            countries = options.countries?.map { it.code },
            types = @Suppress("DEPRECATION") options.types?.map { it.mapToCore() },
            navProfile = options.navigationOptions?.navigationProfile?.rawName,
            etaType = options.navigationOptions?.etaType?.rawName,
            origin = options.origin,
            requestDebounce = options.requestDebounce,
            addonAPI = options.unsafeParameters,
            ignoreUR = options.ignoreIndexableRecords,
            urDistanceThreshold = options.indexableRecordsDistanceThresholdMeters,
            attributeSets = options.attributeSets?.map { it.mapToCore() },
        )

        assertEquals(expectedCoreOptions, options.mapToCore())
    }

    @Test
    fun `Test default parameters`() {
        val options = ForwardSearchOptions.Builder()
            .build()

        val expectedCoreOptions = createCoreSearchOptions(
            language = listOf(TEST_LOCALE.language),
            limit = null,
            proximity = null,
            bbox = null,
            countries = null,
            types = null,
            navProfile = null,
            etaType = null,
            origin = null,
            requestDebounce = null,
            addonAPI = null,
            ignoreUR = false,
            urDistanceThreshold = null,
            attributeSets = null,
        )

        assertEquals(expectedCoreOptions, options.mapToCore())
    }

    @Test
    fun `Test copy() function`() {
        val options = ForwardSearchOptions.Builder().build()
        val newOptions = options.copy(
            proximity = TEST_FILLED_OPTIONS.proximity,
            boundingBox = TEST_FILLED_OPTIONS.boundingBox,
            countries = TEST_FILLED_OPTIONS.countries,
            language = TEST_FILLED_OPTIONS.language,
            limit = TEST_FILLED_OPTIONS.limit,
            types = @Suppress("DEPRECATION") TEST_FILLED_OPTIONS.types,
            requestDebounce = TEST_FILLED_OPTIONS.requestDebounce,
            origin = TEST_FILLED_OPTIONS.origin,
            navigationOptions = TEST_FILLED_OPTIONS.navigationOptions,
            unsafeParameters = TEST_FILLED_OPTIONS.unsafeParameters,
            ignoreIndexableRecords = TEST_FILLED_OPTIONS.ignoreIndexableRecords,
            indexableRecordsDistanceThresholdMeters = TEST_FILLED_OPTIONS.indexableRecordsDistanceThresholdMeters,
            attributeSets = TEST_FILLED_OPTIONS.attributeSets,
        )
        assertEquals(TEST_FILLED_OPTIONS, newOptions)
    }

    @Test
    fun `Test copy() without parameters returns the same object`() {
        assertEquals(TEST_FILLED_OPTIONS, TEST_FILLED_OPTIONS.copy())
    }

    @Test
    fun `Test mapToCore uses newTypes when newTypes provided`() {
        val options = ForwardSearchOptions.Builder()
            .newTypes(NewQueryType.BRAND, NewQueryType.POI)
            .build()
        val coreOptions = options.mapToCore()
        assertEquals(
            listOf(com.mapbox.search.base.core.CoreQueryType.BRAND, com.mapbox.search.base.core.CoreQueryType.POI),
            coreOptions.types,
        )
    }

    @Test
    @Suppress("DEPRECATION")
    fun `Test mapToCore uses newTypes when both types and newTypes provided`() {
        val options = ForwardSearchOptions.Builder()
            .types(QueryType.ADDRESS, QueryType.COUNTRY)
            .newTypes(NewQueryType.BRAND, NewQueryType.POI)
            .build()
        val coreOptions = options.mapToCore()
        assertEquals(
            listOf(com.mapbox.search.base.core.CoreQueryType.BRAND, com.mapbox.search.base.core.CoreQueryType.POI),
            coreOptions.types,
        )
    }

    private companion object {

        val TEST_LOCALE: Locale = Locale.FRANCE
        val TEST_POINT: Point = Point.fromLngLat(10.0, 20.0)
        val TEST_ORIGIN_POINT: Point = Point.fromLngLat(20.0, 30.0)
        val TEST_BOUNDING_BOX: BoundingBox = BoundingBox.fromPoints(Point.fromLngLat(10.0, 20.0), Point.fromLngLat(20.0, 30.0))
        val TEST_NAV_OPTIONS: SearchNavigationOptions = SearchNavigationOptions(
            navigationProfile = NavigationProfile.WALKING,
            etaType = EtaType.NAVIGATION
        )
        val TEST_UNSAFE_PARAMETERS: Map<String, String> = mapOf(
            "routing" to "true",
            "autocomplete" to "false",
        )

        @Suppress("DEPRECATION")
        val TEST_FILLED_OPTIONS = ForwardSearchOptions.Builder()
            .language(IsoLanguageCode.FRENCH)
            .limit(7)
            .proximity(TEST_POINT)
            .boundingBox(TEST_BOUNDING_BOX)
            .countries(listOf(IsoCountryCode.FRANCE, IsoCountryCode.GERMANY))
            .types(listOf(QueryType.ADDRESS, QueryType.COUNTRY, QueryType.POI))
            .navigationOptions(TEST_NAV_OPTIONS)
            .origin(TEST_ORIGIN_POINT)
            .requestDebounce(123)
            .unsafeParameters(TEST_UNSAFE_PARAMETERS)
            .ignoreIndexableRecords(true)
            .indexableRecordsDistanceThresholdMeters(123.0)
            .attributeSets(listOf(AttributeSet.BASIC, AttributeSet.PHOTOS))
            .build()

        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            resetLogImpl()
            mockkStatic(TestConstants.ASSERTIONS_KT_CLASS_NAME)
        }

        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            reinitializeLogImpl()
            unmockkStatic(TestConstants.ASSERTIONS_KT_CLASS_NAME)
        }
    }
}
