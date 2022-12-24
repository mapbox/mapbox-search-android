package com.mapbox.search

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.IsoCountry
import com.mapbox.search.common.IsoLanguage
import com.mapbox.test.dsl.TestCase
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.TestFactory
import java.util.Locale
import java.util.concurrent.TimeUnit

@Suppress("LargeClass")
internal class SearchOptionsTest {

    private lateinit var defaultLocale: Locale

    @Before
    fun setUp() {
        defaultLocale = Locale.getDefault()
        Locale.setDefault(TEST_LOCALE)
    }

    @After
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @TestFactory
    fun `Check SearchOptions default builder`() = TestCase {
        Given("SearchOptions builder") {
            When("Build new SearchOptions with default values") {
                val actualOptions = SearchOptions.Builder().build()
                val expectedOptions = SearchOptions(
                    proximity = null,
                    boundingBox = null,
                    countries = null,
                    fuzzyMatch = null,
                    languages = listOf(IsoLanguage(TEST_LOCALE.language)),
                    limit = null,
                    types = null,
                    requestDebounce = null,
                    origin = null,
                    navigationOptions = null,
                    routeOptions = null,
                    unsafeParameters = null,
                    ignoreIndexableRecords = false,
                    indexableRecordsDistanceThresholdMeters = null,
                )

                Then("Options should be equal", expectedOptions, actualOptions)
            }
        }
    }

    @TestFactory
    fun `Check SearchOptions builder with all values set`() = TestCase {
        Given("SearchOptions builder") {
            When("Build new SearchOptions with all values set") {
                @Suppress("DEPRECATION")
                val actualOptions = SearchOptions.Builder()
                    .proximity(TEST_POINT)
                    .boundingBox(TEST_BOUNDING_BOX)
                    .countries(IsoCountry.UNITED_KINGDOM, IsoCountry.BELARUS)
                    .requestDebounce(300)
                    .fuzzyMatch(true)
                    .languages(IsoLanguage.ENGLISH, IsoLanguage("by"))
                    .limit(100)
                    .types(QueryType.ADDRESS, QueryType.COUNTRY, QueryType.DISTRICT)
                    .origin(TEST_ORIGIN_POINT)
                    .navigationOptions(TEST_NAV_OPTIONS)
                    .routeOptions(TEST_ROUTE_OPTIONS)
                    .unsafeParameters(TEST_UNSAFE_PARAMETERS)
                    .ignoreIndexableRecords(true)
                    .indexableRecordsDistanceThresholdMeters(50.0)
                    .build()

                val expectedOptions = SearchOptions(
                    proximity = TEST_POINT,
                    boundingBox = TEST_BOUNDING_BOX,
                    countries = listOf(IsoCountry.UNITED_KINGDOM, IsoCountry.BELARUS),
                    fuzzyMatch = true,
                    languages = listOf(IsoLanguage.ENGLISH, IsoLanguage("by")),
                    limit = 100,
                    types = listOf(QueryType.ADDRESS, QueryType.COUNTRY, QueryType.DISTRICT),
                    requestDebounce = 300,
                    origin = TEST_ORIGIN_POINT,
                    navigationOptions = TEST_NAV_OPTIONS,
                    routeOptions = TEST_ROUTE_OPTIONS,
                    unsafeParameters = TEST_UNSAFE_PARAMETERS,
                    ignoreIndexableRecords = true,
                    indexableRecordsDistanceThresholdMeters = 50.0,
                )

                Then("Options should be equal", expectedOptions, actualOptions)
            }
        }
    }

    @TestFactory
    fun `Check SearchOptions builder with all values set using list functions`() = TestCase {
        Given("SearchOptions builder") {
            When("Build new SearchOptions with all values set") {
                @Suppress("DEPRECATION")
                val actualOptions = SearchOptions.Builder()
                    .proximity(TEST_POINT)
                    .boundingBox(TEST_BOUNDING_BOX)
                    .countries(listOf(IsoCountry.UNITED_KINGDOM, IsoCountry.BELARUS))
                    .requestDebounce(300)
                    .fuzzyMatch(true)
                    .languages(listOf(IsoLanguage.ENGLISH, IsoLanguage("by")))
                    .limit(100)
                    .types(listOf(QueryType.ADDRESS, QueryType.COUNTRY, QueryType.DISTRICT))
                    .origin(TEST_ORIGIN_POINT)
                    .navigationOptions(TEST_NAV_OPTIONS)
                    .routeOptions(TEST_ROUTE_OPTIONS)
                    .unsafeParameters(TEST_UNSAFE_PARAMETERS)
                    .ignoreIndexableRecords(true)
                    .indexableRecordsDistanceThresholdMeters(100.123)
                    .build()

                val expectedOptions = SearchOptions(
                    proximity = TEST_POINT,
                    boundingBox = TEST_BOUNDING_BOX,
                    countries = listOf(IsoCountry.UNITED_KINGDOM, IsoCountry.BELARUS),
                    fuzzyMatch = true,
                    languages = listOf(IsoLanguage.ENGLISH, IsoLanguage("by")),
                    limit = 100,
                    types = listOf(QueryType.ADDRESS, QueryType.COUNTRY, QueryType.DISTRICT),
                    requestDebounce = 300,
                    origin = TEST_ORIGIN_POINT,
                    navigationOptions = TEST_NAV_OPTIONS,
                    routeOptions = TEST_ROUTE_OPTIONS,
                    unsafeParameters = TEST_UNSAFE_PARAMETERS,
                    ignoreIndexableRecords = true,
                    indexableRecordsDistanceThresholdMeters = 100.123,
                )

                Then("Options should be equal", expectedOptions, actualOptions)
            }
        }
    }

    @TestFactory
    fun `Check empty SearchOptions mapToCore() function`() = TestCase {
        Given("SearchOptions builder") {
            When("Build new SearchOptions with all values set") {
                val actualOptions = SearchOptions().mapToCore()

                val expectedOptions = com.mapbox.search.common.createTestCoreSearchOptions(
                    language = listOf(TEST_LOCALE.language)
                )

                Then("Options should be as expected", expectedOptions, actualOptions)
            }
        }
    }

    @TestFactory
    fun `Check filled SearchOptions mapToCore() function`() = TestCase {
        Given("SearchOptions builder") {
            When("Build new SearchOptions with all values set") {
                @Suppress("DEPRECATION")
                val originalOptions = SearchOptions.Builder()
                    .proximity(TEST_POINT)
                    .boundingBox(TEST_BOUNDING_BOX)
                    .countries(listOf(IsoCountry.UNITED_KINGDOM, IsoCountry.BELARUS))
                    .requestDebounce(300)
                    .fuzzyMatch(true)
                    .languages(listOf(IsoLanguage.ENGLISH, IsoLanguage("by")))
                    .limit(100)
                    .types(listOf(QueryType.ADDRESS, QueryType.COUNTRY, QueryType.DISTRICT))
                    .origin(TEST_ORIGIN_POINT)
                    .navigationOptions(TEST_NAV_OPTIONS)
                    .routeOptions(TEST_ROUTE_OPTIONS)
                    .unsafeParameters(TEST_UNSAFE_PARAMETERS)
                    .ignoreIndexableRecords(true)
                    .indexableRecordsDistanceThresholdMeters(15.0)
                    .build()

                val actualOptions = originalOptions.mapToCore()

                val expectedOptions = com.mapbox.search.common.createTestCoreSearchOptions(
                    proximity = TEST_POINT,
                    origin = TEST_ORIGIN_POINT,
                    navProfile = TEST_NAV_OPTIONS.navigationProfile.rawName,
                    etaType = TEST_NAV_OPTIONS.etaType?.rawName,
                    bbox = TEST_BOUNDING_BOX.mapToCore(),
                    countries = listOf(IsoCountry.UNITED_KINGDOM, IsoCountry.BELARUS).map { it.code },
                    fuzzyMatch = true,
                    language = listOf(IsoLanguage.ENGLISH, IsoLanguage("by")).map { it.code },
                    limit = 100,
                    types = listOf(QueryType.ADDRESS, QueryType.COUNTRY, QueryType.DISTRICT).map { it.mapToCore() },
                    ignoreUR = true,
                    urDistanceThreshold = 15.0,
                    requestDebounce = 300,
                    route = TEST_ROUTE_OPTIONS.route,
                    sarType = "isochrone",
                    timeDeviation = TEST_ROUTE_OPTIONS.timeDeviationMinutes,
                    addonAPI = HashMap(TEST_UNSAFE_PARAMETERS)
                )

                Then("Options should be equal", expectedOptions, actualOptions)
            }
        }
    }

    @TestFactory
    fun `Check filled SearchOptions toBuilder() function`() = TestCase {
        Given("SearchOptions") {
            When("Use toBuilder() function and then build new SearchOptions") {
                val options = SearchOptions(
                    proximity = TEST_POINT,
                    boundingBox = TEST_BOUNDING_BOX,
                    countries = listOf(IsoCountry.UNITED_KINGDOM, IsoCountry.BELARUS),
                    fuzzyMatch = true,
                    languages = listOf(IsoLanguage.ENGLISH, IsoLanguage("by")),
                    limit = 100,
                    types = listOf(QueryType.ADDRESS, QueryType.COUNTRY, QueryType.DISTRICT),
                    requestDebounce = 300,
                    origin = TEST_ORIGIN_POINT,
                    navigationOptions = TEST_NAV_OPTIONS,
                    routeOptions = TEST_ROUTE_OPTIONS,
                    unsafeParameters = TEST_UNSAFE_PARAMETERS,
                    ignoreIndexableRecords = true,
                    indexableRecordsDistanceThresholdMeters = 11.0,
                )

                Then("Options should be equal", options, options.toBuilder().build())
            }
        }
    }

    @TestFactory
    fun `Check limit field for SearchOptions builder and constructor`() = TestCase {
        listOf(1, 10, Int.MAX_VALUE).forEach { inputValue ->
            Given("SearchOptions with valid limit = $inputValue") {
                When("Create SearchOptions with builder") {
                    val searchOptions = SearchOptions.Builder()
                        .limit(inputValue).build()

                    val actualValue = searchOptions.limit
                    Then("It should be <$inputValue>", inputValue, actualValue)
                }

                When("Create SearchOptions with constructor") {
                    val searchOptions = SearchOptions(limit = inputValue)

                    val actualValue = searchOptions.limit
                    Then("It should be <$inputValue>", inputValue, actualValue)
                }
            }
        }

        listOf(0, Int.MIN_VALUE, -1).forEach { inputValue ->
            Given("SearchOptions with ineligible limit = $inputValue") {
                WhenThrows("Create SearchOptions with builder", IllegalArgumentException::class) {
                    SearchOptions.Builder()
                        .limit(inputValue)
                        .build()
                }

                WhenThrows("Create SearchOptions with constructor", IllegalArgumentException::class) {
                    SearchOptions(limit = inputValue)
                }
            }
        }
    }

    @TestFactory
    fun `check indexableRecordsDistanceThresholdMeters field initialization`() = TestCase {
        listOf(.0, 1.0, Double.POSITIVE_INFINITY).forEach { inputValue ->
            Given("SearchOptions with valid indexableRecordsDistanceThresholdMeters = $inputValue") {
                When("Create SearchOptions with constructor") {
                    val searchOptions = SearchOptions(indexableRecordsDistanceThresholdMeters = inputValue)

                    val actualValue = searchOptions.indexableRecordsDistanceThresholdMeters
                    Then("It should be <$inputValue>", inputValue, actualValue)
                }

                When("Create SearchOptions with builder") {
                    val searchOptions = SearchOptions.Builder()
                        .indexableRecordsDistanceThresholdMeters(inputValue)
                        .build()

                    val actualValue = searchOptions.indexableRecordsDistanceThresholdMeters
                    Then("It should be <$inputValue>", inputValue, actualValue)
                }
            }
        }

        listOf(-0.9, -1.0, Double.NEGATIVE_INFINITY).forEach { inputValue ->
            Given("SearchOptions with ineligible indexableRecordsDistanceThresholdMeters = $inputValue") {
                WhenThrows("Create SearchOptions with constructor", IllegalArgumentException::class) {
                    SearchOptions(indexableRecordsDistanceThresholdMeters = inputValue)
                }

                WhenThrows("Create SearchOptions with builder", IllegalArgumentException::class) {
                    SearchOptions.Builder()
                        .indexableRecordsDistanceThresholdMeters(inputValue)
                        .build()
                }
            }
        }
    }

    private companion object {

        val TEST_LOCALE: Locale = Locale.ENGLISH

        val TEST_BOUNDING_BOX: BoundingBox = BoundingBox.fromPoints(Point.fromLngLat(10.0, 20.0), Point.fromLngLat(20.0, 30.0))
        val TEST_POINT: Point = Point.fromLngLat(10.0, 10.0)
        val TEST_ORIGIN_POINT: Point = Point.fromLngLat(20.0, 20.0)
        val TEST_NAV_OPTIONS: SearchNavigationOptions = SearchNavigationOptions(
            navigationProfile = SearchNavigationProfile.DRIVING,
            etaType = EtaType.NAVIGATION
        )
        val TEST_ROUTE_OPTIONS = RouteOptions(
            route = listOf(Point.fromLngLat(1.0, 2.0), Point.fromLngLat(3.0, 4.0), Point.fromLngLat(5.0, 6.0)),
            deviation = RouteOptions.Deviation.Time(value = 5, unit = TimeUnit.MINUTES, sarType = RouteOptions.Deviation.SarType.ISOCHROME)
        )
        val TEST_UNSAFE_PARAMETERS: Map<String, String> = mapOf(
            "routing" to "true",
            "autocomplete" to "false",
        )
    }
}
