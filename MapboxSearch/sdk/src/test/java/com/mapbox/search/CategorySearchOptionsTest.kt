package com.mapbox.search

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.utils.extension.mapToCore
import com.mapbox.search.common.createTestCoreSearchOptions
import com.mapbox.test.dsl.TestCase
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.TestFactory
import java.util.Locale
import java.util.concurrent.TimeUnit

@Suppress("LargeClass")
internal class CategorySearchOptionsTest {

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
    fun `Check CategorySearchOptions default builder`() = TestCase {
        Given("CategorySearchOptions builder") {
            When("Build new CategorySearchOptions with default values") {
                val actualOptions = CategorySearchOptions.Builder().build()
                val expectedOptions = CategorySearchOptions(
                    proximity = null,
                    boundingBox = null,
                    countries = null,
                    fuzzyMatch = null,
                    languages = listOf(Language(TEST_LOCALE.language)),
                    limit = null,
                    requestDebounce = null,
                    origin = null,
                    navigationProfile = null,
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
    fun `Check CategorySearchOptions builder with all values set`() = TestCase {
        Given("CategorySearchOptions builder") {
            When("Build new CategorySearchOptions with all values set") {
                @Suppress("DEPRECATION")
                val actualOptions = CategorySearchOptions.Builder()
                    .proximity(TEST_POINT)
                    .boundingBox(TEST_BOUNDING_BOX)
                    .countries(Country.UNITED_KINGDOM, Country.BELARUS)
                    .requestDebounce(300)
                    .fuzzyMatch(true)
                    .languages(Language.ENGLISH, Language("by"))
                    .limit(100)
                    .origin(TEST_ORIGIN_POINT)
                    .navigationProfile(TEST_NAV_PROFILE)
                    .routeOptions(TEST_ROUTE_OPTIONS)
                    .unsafeParameters(TEST_UNSAFE_PARAMETERS)
                    .ignoreIndexableRecords(true)
                    .indexableRecordsDistanceThresholdMeters(50.0)
                    .build()

                val expectedOptions = CategorySearchOptions(
                    proximity = TEST_POINT,
                    boundingBox = TEST_BOUNDING_BOX,
                    countries = listOf(Country.UNITED_KINGDOM, Country.BELARUS),
                    fuzzyMatch = true,
                    languages = listOf(Language.ENGLISH, Language("by")),
                    limit = 100,
                    requestDebounce = 300,
                    origin = TEST_ORIGIN_POINT,
                    navigationProfile = TEST_NAV_PROFILE,
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
    fun `Check CategorySearchOptions builder with all values set using list functions`() = TestCase {
        Given("CategorySearchOptions builder") {
            When("Build new CategorySearchOptions with all values set") {
                @Suppress("DEPRECATION")
                val actualOptions = CategorySearchOptions.Builder()
                    .proximity(TEST_POINT)
                    .boundingBox(TEST_BOUNDING_BOX)
                    .countries(listOf(Country.UNITED_KINGDOM, Country.BELARUS))
                    .requestDebounce(300)
                    .fuzzyMatch(true)
                    .languages(listOf(Language.ENGLISH, Language("by")))
                    .limit(100)
                    .origin(TEST_ORIGIN_POINT)
                    .navigationProfile(TEST_NAV_PROFILE)
                    .routeOptions(TEST_ROUTE_OPTIONS)
                    .unsafeParameters(TEST_UNSAFE_PARAMETERS)
                    .ignoreIndexableRecords(true)
                    .indexableRecordsDistanceThresholdMeters(100.123)
                    .build()

                val expectedOptions = CategorySearchOptions(
                    proximity = TEST_POINT,
                    boundingBox = TEST_BOUNDING_BOX,
                    countries = listOf(Country.UNITED_KINGDOM, Country.BELARUS),
                    fuzzyMatch = true,
                    languages = listOf(Language.ENGLISH, Language("by")),
                    limit = 100,
                    requestDebounce = 300,
                    origin = TEST_ORIGIN_POINT,
                    navigationProfile = TEST_NAV_PROFILE,
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
    fun `Check empty CategorySearchOptions mapToCore() function`() = TestCase {
        Given("CategorySearchOptions builder") {
            When("Build new CategorySearchOptions with all values set") {
                val actualOptions = CategorySearchOptions().mapToCoreCategory()

                val expectedOptions = createTestCoreSearchOptions(
                    language = listOf(TEST_LOCALE.language)
                )

                Then("Options should be as expected", expectedOptions, actualOptions)
            }
        }
    }

    @TestFactory
    fun `Check filled CategorySearchOptions mapToCore() function`() = TestCase {
        Given("CategorySearchOptions builder") {
            When("Build new CategorySearchOptions with all values set") {
                @Suppress("DEPRECATION")
                val originalOptions = CategorySearchOptions.Builder()
                    .proximity(TEST_POINT)
                    .boundingBox(TEST_BOUNDING_BOX)
                    .countries(listOf(Country.UNITED_KINGDOM, Country.BELARUS))
                    .requestDebounce(300)
                    .fuzzyMatch(true)
                    .languages(listOf(Language.ENGLISH, Language("by")))
                    .limit(100)
                    .origin(TEST_ORIGIN_POINT)
                    .navigationProfile(TEST_NAV_PROFILE)
                    .routeOptions(TEST_ROUTE_OPTIONS)
                    .unsafeParameters(TEST_UNSAFE_PARAMETERS)
                    .ignoreIndexableRecords(true)
                    .indexableRecordsDistanceThresholdMeters(10.0)
                    .build()

                val actualOptions = originalOptions.mapToCoreCategory()

                val expectedOptions = createTestCoreSearchOptions(
                    proximity = TEST_POINT,
                    origin = TEST_ORIGIN_POINT,
                    navProfile = TEST_NAV_PROFILE.rawName,
                    etaType = null,
                    bbox = TEST_BOUNDING_BOX.mapToCore(),
                    countries = listOf(Country.UNITED_KINGDOM, Country.BELARUS).map { it.code },
                    fuzzyMatch = true,
                    language = listOf(Language.ENGLISH, Language("by")).map { it.code },
                    limit = 100,
                    types = null,
                    ignoreUR = true,
                    urDistanceThreshold = 10.0,
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
    fun `Check filled CategorySearchOptions toBuilder() function`() = TestCase {
        Given("CategorySearchOptions") {
            When("Use toBuilder() function and then build new CategorySearchOptions") {
                val options = CategorySearchOptions(
                    proximity = TEST_POINT,
                    boundingBox = TEST_BOUNDING_BOX,
                    countries = listOf(Country.UNITED_KINGDOM, Country.BELARUS),
                    fuzzyMatch = true,
                    languages = listOf(Language.ENGLISH, Language("by")),
                    limit = 100,
                    requestDebounce = 300,
                    origin = TEST_ORIGIN_POINT,
                    navigationProfile = TEST_NAV_PROFILE,
                    routeOptions = TEST_ROUTE_OPTIONS,
                    unsafeParameters = TEST_UNSAFE_PARAMETERS,
                    ignoreIndexableRecords = true,
                    indexableRecordsDistanceThresholdMeters = 15.0,
                )

                Then("Options should be equal", options, options.toBuilder().build())
            }
        }
    }

    @TestFactory
    fun `Check limit field for CategorySearchOptions builder and constructor`() = TestCase {
        listOf(1, 10, Int.MAX_VALUE).forEach { inputValue ->
            Given("CategorySearchOptions with valid limit = $inputValue") {
                When("Create CategorySearchOptions with builder") {
                    val searchOptions = CategorySearchOptions.Builder()
                        .limit(inputValue).build()

                    val actualValue = searchOptions.limit
                    Then("It should be <$inputValue>", inputValue, actualValue)
                }

                When("Create CategorySearchOptions with constructor") {
                    val searchOptions = CategorySearchOptions(limit = inputValue)

                    val actualValue = searchOptions.limit
                    Then("It should be <$inputValue>", inputValue, actualValue)
                }
            }
        }

        listOf(0, Int.MIN_VALUE, -1).forEach { inputValue ->
            Given("CategorySearchOptions with ineligible limit = $inputValue") {
                WhenThrows("Create CategorySearchOptions with builder", IllegalArgumentException::class) {
                    CategorySearchOptions.Builder()
                        .limit(inputValue)
                        .build()
                }

                WhenThrows("Create CategorySearchOptions with constructor", IllegalArgumentException::class) {
                    CategorySearchOptions(limit = inputValue)
                }
            }
        }
    }

    @TestFactory
    fun `check indexableRecordsDistanceThresholdMeters field initialization`() = TestCase {
        listOf(.0, 1.0, Double.POSITIVE_INFINITY).forEach { inputValue ->
            Given("CategorySearchOptions with valid indexableRecordsDistanceThresholdMeters = $inputValue") {
                When("Create CategorySearchOptions with constructor") {
                    val searchOptions = CategorySearchOptions(indexableRecordsDistanceThresholdMeters = inputValue)

                    val actualValue = searchOptions.indexableRecordsDistanceThresholdMeters
                    Then("It should be <$inputValue>", inputValue, actualValue)
                }

                When("Create CategorySearchOptions with builder") {
                    val searchOptions = CategorySearchOptions.Builder()
                        .indexableRecordsDistanceThresholdMeters(inputValue)
                        .build()

                    val actualValue = searchOptions.indexableRecordsDistanceThresholdMeters
                    Then("It should be <$inputValue>", inputValue, actualValue)
                }
            }
        }

        listOf(-0.9, -1.0, Double.NEGATIVE_INFINITY).forEach { inputValue ->
            Given("CategorySearchOptions with ineligible indexableRecordsDistanceThresholdMeters = $inputValue") {
                WhenThrows("Create CategorySearchOptions with constructor", IllegalArgumentException::class) {
                    CategorySearchOptions(indexableRecordsDistanceThresholdMeters = inputValue)
                }

                WhenThrows("Create CategorySearchOptions with builder", IllegalArgumentException::class) {
                    CategorySearchOptions.Builder()
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
        val TEST_NAV_PROFILE: SearchNavigationProfile = SearchNavigationProfile.CYCLING
        val TEST_ROUTE_OPTIONS = RouteOptions(
            route = listOf(Point.fromLngLat(1.0, 2.0), Point.fromLngLat(3.0, 4.0), Point.fromLngLat(5.0, 6.0)),
            deviation = RouteOptions.Deviation.Time(
                value = 5,
                unit = TimeUnit.MINUTES,
                sarType = RouteOptions.Deviation.SarType.ISOCHROME
            )
        )
        val TEST_UNSAFE_PARAMETERS: Map<String, String> = mapOf(
            "routing" to "true",
            "autocomplete" to "false",
        )
    }
}
