package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreReverseGeoOptions
import com.mapbox.search.base.core.CoreReverseMode
import com.mapbox.search.common.IsoLanguage
import com.mapbox.search.tests_support.checkEnumValues
import com.mapbox.test.dsl.TestCase
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.TestFactory
import java.util.Locale

@Suppress("LargeClass")
internal class ReverseGeoOptionsTest {

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
    fun `Check ReverseGeoOptions default builder`() = TestCase {
        Given("ReverseGeoOptions with default params") {
            val reverseGeoOptions = ReverseGeoOptions.Builder(center = Point.fromLngLat(2.0, 5.0)).build()

            When("Get default center") {
                val actualValue = reverseGeoOptions.center
                Then("It should be <LatLng(5.0, 2.0)>", Point.fromLngLat(2.0, 5.0), actualValue)
            }

            @Suppress("DEPRECATION")
            When("Get default countries") {
                val actualValue = reverseGeoOptions.countries
                Then("It should be <null>", null, actualValue)
            }

            When("Get default languages") {
                val actualValue = reverseGeoOptions.languages?.map { it.code }
                Then("It should be ${listOf(TEST_LOCALE.language)}", listOf(TEST_LOCALE.language), actualValue)
            }

            When("Get default limit") {
                val actualValue = reverseGeoOptions.limit
                Then("It should be <null>", null, actualValue)
            }

            @Suppress("DEPRECATION")
            When("Get default reverseMode") {
                val actualValue = reverseGeoOptions.reverseMode
                Then("It should be <null>", null, actualValue)
            }

            When("Get default types") {
                val actualValue = reverseGeoOptions.types
                Then("It should be <null>", null, actualValue)
            }
        }
    }

    @TestFactory
    fun `Check center field for ReverseGeoOptions builder`() = TestCase {
        mapOf(
            Point.fromLngLat(2.0, 5.0) to Point.fromLngLat(2.0, 5.0)
        ).forEach { (inputValue, expectedValue) ->

            Given("ReverseGeoOptions with center = $inputValue") {
                val reverseGeoOptions = ReverseGeoOptions.Builder(center = Point.fromLngLat(2.0, 5.0)).build()

                When("Get center") {
                    val actualValue = reverseGeoOptions.center
                    Then("It should be <$expectedValue>", expectedValue, actualValue)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    @TestFactory
    fun `Check countries field for ReverseGeoOptions builder`() = TestCase {
        mapOf(
            Country.BELARUS to arrayListOf("by"),
            Country.UNITED_STATES to arrayListOf("us")
        ).forEach { (inputValue, expectedValue) ->

            Given("ReverseGeoOptions with countries = $inputValue") {
                val reverseGeoOptions = ReverseGeoOptions.Builder(center = Point.fromLngLat(2.0, 5.0))
                    .countries(inputValue).build()

                When("Get countries") {
                    val actualValue = reverseGeoOptions.countries?.map { it.code }
                    Then("It should be <$expectedValue>", expectedValue, actualValue)
                }
            }
        }
    }

    @TestFactory
    fun `Check languages field for ReverseGeoOptions builder`() = TestCase {
        mapOf(
            IsoLanguage.ENGLISH to arrayListOf(IsoLanguage.ENGLISH.code),
            IsoLanguage.FRENCH to arrayListOf(IsoLanguage.FRENCH.code)
        ).forEach { (inputValue, expectedValue) ->

            Given("ReverseGeoOptions with languages = $inputValue") {
                val reverseGeoOptions = ReverseGeoOptions.Builder(center = Point.fromLngLat(2.0, 5.0))
                    .languages(inputValue)
                    .build()

                When("Get languages") {
                    val actualValue = reverseGeoOptions.languages?.map { it.code }
                    Then("It should be <$expectedValue>", expectedValue, actualValue)
                }
            }
        }
    }

    @TestFactory
    fun `Check limit field for ReverseGeoOptions builder and constructor`() = TestCase {
        mapOf(
            1 to 1,
            10 to 10,
            Int.MAX_VALUE to Int.MAX_VALUE,
        ).forEach { (inputValue, expectedValue) ->

            Given("ReverseGeoOptions with limit = $inputValue and creation via builder") {
                val reverseGeoOptions = ReverseGeoOptions.Builder(center = Point.fromLngLat(2.0, 5.0))
                    .limit(inputValue).build()

                When("Get limit") {
                    val actualValue = reverseGeoOptions.limit
                    Then("It should be <$expectedValue>", expectedValue, actualValue)
                }
            }
        }

        listOf(0, Int.MIN_VALUE, -1).forEach { inputValue ->
            Given("ReverseGeoOptions with limit = $inputValue and creation via builder") {
                WhenThrows("Set limit to builder", IllegalStateException::class) {
                    ReverseGeoOptions.Builder(center = Point.fromLngLat(2.0, 5.0))
                        .limit(inputValue).build()
                }
            }

            Given("ReverseGeoOptions with limit = $inputValue and creation via constructor") {
                WhenThrows("Pass limit to constructor", IllegalStateException::class) {
                    ReverseGeoOptions(center = Point.fromLngLat(2.0, 5.0), limit = inputValue)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    @TestFactory
    fun `Check reverseMode field for ReverseGeoOptions builder`() = TestCase {
        ReverseMode.values().forEach {
            Given("ReverseGeoOptions with reverseMode = $it") {
                val reverseGeoOptions = ReverseGeoOptions.Builder(center = Point.fromLngLat(2.0, 5.0))
                    .reverseMode(it).build()

                When("Get reverseMode") {
                    val actualValue = reverseGeoOptions.reverseMode
                    Then("It should be <$it>", it, actualValue)
                }
            }
        }
    }

    @TestFactory
    fun `Check types field for ReverseGeoOptions builder`() = TestCase {
        Given("ReverseGeoOptions with QueryType") {
            val types = listOf(QueryType.ADDRESS, QueryType.COUNTRY, QueryType.DISTRICT)
            val reverseGeoOptions = ReverseGeoOptions.Builder(center = Point.fromLngLat(2.0, 5.0))
                .types(types).build()

            When("Get types") {
                val actualValue = reverseGeoOptions.types
                Then("It should be <$types>", types, actualValue)
            }
        }
    }

    @TestFactory
    fun `Check ReverseGeoOptions custom builder`() = TestCase {
        Given("ReverseGeoOptions with custom params") {
            @Suppress("DEPRECATION")
            val reverseGeoOptions = ReverseGeoOptions.Builder(center = Point.fromLngLat(2.0, 5.0))
                .countries(Country.BELARUS)
                .languages(IsoLanguage.ENGLISH)
                .limit(5)
                .reverseMode(ReverseMode.DISTANCE)
                .types(QueryType.COUNTRY)
                .build()

            @Suppress("DEPRECATION")
            When("Get custom countries") {
                val actualValue = reverseGeoOptions.countries?.map { it.code }
                Then("It should be <arrayListOf<Country>(BELARUS)>", arrayListOf("by"), actualValue)
            }

            When("Get custom languages") {
                val actualValue = reverseGeoOptions.languages?.map { it.code }
                Then("It should be <arrayListOf<Language>(ENGLISH)>", arrayListOf("en"), actualValue)
            }

            When("Get custom limit") {
                val actualValue = reverseGeoOptions.limit
                Then("It should be <5>", 5, actualValue)
            }

            @Suppress("DEPRECATION")
            When("Get custom reverseMode") {
                val actualValue = reverseGeoOptions.reverseMode
                Then("It should be <DISTANCE>", ReverseMode.DISTANCE, actualValue)
            }

            When("Get custom types") {
                val actualValue = reverseGeoOptions.types
                Then("It should be <arrayListOf<QueryType>(COUNTRY)>", arrayListOf(QueryType.COUNTRY), actualValue)
            }
        }
    }

    @TestFactory
    fun `Check ReverseGeoOptions mapToCore() extension`() = TestCase {
        Given("ReverseGeoOptionsExtension") {
            When("Empty ReverseGeoOptions") {
                val reverseGeoOptions = ReverseGeoOptions.Builder(center = TEST_COORDINATE).build()
                val actualValue = reverseGeoOptions.mapToCore()
                val expectedValue = CoreReverseGeoOptions(
                    TEST_COORDINATE, null, null, listOf(TEST_LOCALE.language), null, null
                )
                Then("Core options should be as expected", expectedValue, actualValue)
            }

            When("Filled ReverseGeoOptions") {
                @Suppress("DEPRECATION")
                val reverseGeoOptions = ReverseGeoOptions.Builder(center = TEST_COORDINATE)
                    .countries(Country.UNITED_KINGDOM, Country.BELARUS)
                    .languages(IsoLanguage.ENGLISH, IsoLanguage("by"))
                    .limit(10)
                    .reverseMode(ReverseMode.DISTANCE)
                    .types(QueryType.DISTRICT, QueryType.COUNTRY, QueryType.ADDRESS)
                    .build()

                val actualValue = reverseGeoOptions.mapToCore()

                val expectedValue = CoreReverseGeoOptions(
                    TEST_COORDINATE,
                    @Suppress("DEPRECATION") ReverseMode.DISTANCE.mapToCore(),
                    listOf(Country.UNITED_KINGDOM, Country.BELARUS).map { it.code },
                    listOf(IsoLanguage.ENGLISH, IsoLanguage("by")).map { it.code },
                    10,
                    listOf(QueryType.DISTRICT, QueryType.COUNTRY, QueryType.ADDRESS).map { it.mapToCore() }
                )

                Then("Core options should be as expected", expectedValue, actualValue)
            }

            When("Filled ReverseGeoOptions using list builder functions") {
                @Suppress("DEPRECATION")
                val reverseGeoOptions = ReverseGeoOptions.Builder(center = TEST_COORDINATE)
                    .countries(listOf(Country.UNITED_KINGDOM, Country.BELARUS))
                    .languages(listOf(IsoLanguage.ENGLISH, IsoLanguage("by")))
                    .limit(10)
                    .reverseMode(ReverseMode.DISTANCE)
                    .types(listOf(QueryType.DISTRICT, QueryType.COUNTRY, QueryType.ADDRESS))
                    .build()

                val actualValue = reverseGeoOptions.mapToCore()

                val expectedValue = CoreReverseGeoOptions(
                    TEST_COORDINATE,
                    @Suppress("DEPRECATION") ReverseMode.DISTANCE.mapToCore(),
                    listOf(Country.UNITED_KINGDOM, Country.BELARUS).map { it.code },
                    listOf(IsoLanguage.ENGLISH, IsoLanguage("by")).map { it.code },
                    10,
                    listOf(QueryType.DISTRICT, QueryType.COUNTRY, QueryType.ADDRESS).map { it.mapToCore() }
                )

                Then("Core options should be as expected", expectedValue, actualValue)
            }
        }
    }

    @TestFactory
    fun `Check filled ReverseGeoOptions toBuilder() function`() = TestCase {
        Given("ReverseGeoOptions") {
            When("Use toBuilder() function and then build new ReverseGeoOptions") {
                val options = ReverseGeoOptions(
                    center = Point.fromLngLat(2.0, 5.0),
                    countries = listOf(Country.BELARUS),
                    languages = listOf(IsoLanguage.ENGLISH),
                    limit = 5,
                    reverseMode = @Suppress("DEPRECATION") ReverseMode.DISTANCE,
                    types = listOf(QueryType.COUNTRY)
                )

                Then("Options should be equal", options, options.toBuilder().build())
            }
        }
    }

    @Suppress("DEPRECATION")
    @TestFactory
    fun `Check ReverseMode public api fields`() = TestCase {
        Given("ReverseMode values") {
            checkEnumValues(REVERSE_MODE_VALUES, ReverseMode::class.java)
        }
    }

    @Suppress("DEPRECATION")
    @TestFactory
    fun `Check mapping ReverseMode to core`() = TestCase {
        Given("ReverseMode mapToCore() extension") {
            mapOf(
                ReverseMode.DISTANCE to CoreReverseMode.DISTANCE,
                ReverseMode.SCORE to CoreReverseMode.SCORE
            ).forEach { (inputValue, expectedValue) ->
                When("Convert Platform QueryType <$inputValue> to core") {
                    val actualValue = inputValue.mapToCore()
                    Then("Core QueryType should be <$expectedValue>", actualValue, expectedValue)
                }
            }
        }
    }

    private companion object {
        val TEST_LOCALE: Locale = Locale.ENGLISH

        const val TEST_LATITUDE = 53.0
        const val TEST_LONGITUDE = 27.0

        val TEST_COORDINATE: Point = Point.fromLngLat(
            TEST_LONGITUDE,
            TEST_LATITUDE
        )

        val REVERSE_MODE_VALUES = listOf("DISTANCE", "SCORE")
    }
}
