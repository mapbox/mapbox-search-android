package com.mapbox.search.sample

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.Country
import com.mapbox.search.EtaType
import com.mapbox.search.Language
import com.mapbox.search.QueryType
import com.mapbox.search.SearchNavigationOptions
import com.mapbox.search.SearchNavigationProfile
import com.mapbox.search.SearchOptions
import com.mapbox.search.sample.Constants.Assets.CAFE_SUGGESTIONS_ASSET
import com.mapbox.search.sample.Constants.Assets.CATEGORY_CAFE_RESULTS_ASSET
import com.mapbox.search.sample.Constants.Assets.EMPTY_SUGGESTIONS_ASSET
import com.mapbox.search.sample.Constants.Assets.MINSK_REGION_SUGGESTIONS_ASSET
import com.mapbox.search.sample.Constants.Assets.MINSK_SUGGESTIONS_ASSET
import com.mapbox.search.sample.Constants.Assets.RANELAGH_ROYAL_SPA_RESULT_ASSET
import com.mapbox.search.sample.Constants.Assets.RANELAGH_SUGGESTIONS_ASSET
import com.mapbox.search.sample.SearchResultsInflater.inflateSearchResults
import com.mapbox.search.sample.extensions.enqueue
import com.mapbox.search.sample.extensions.noInternetConnectionError
import com.mapbox.search.sample.robots.searchBottomSheet
import com.mapbox.search.sample.robots.searchPlaceBottomSheet
import com.mapbox.search.sample.robots.systemNavigation
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SearchFlowTest : MockServerSearchActivityTest() {

    /**
     * Test 1.
     * Search for address. Select search result, it must appear in history.
     * Select history item and check selected result.
     */
    @Test
    fun testSearchSuggestionSelectionCreatesHistoryRecord() {
        mockServer.enqueueSuccessfulResponses(
            RANELAGH_SUGGESTIONS_ASSET,
            RANELAGH_ROYAL_SPA_RESULT_ASSET
        )

        searchBottomSheet {
            clickSearchInput()
            verifyHistory {
                noRecentSearches()
            }
            typeQuery(RANELAGH_TERRACE_62_NAME)
            selectSearchResult(
                resultName = RANELAGH_TERRACE_62_NAME,
                addressSubstring = RANELAGH_TERRACE_62_ADDRESS_PART
            )
        }
        searchPlaceBottomSheet {
            verifyDistance(RANELAGH_TERRACE_62_SERVER_DISTANCE)
            close()
        }
        searchBottomSheet {
            clickSearchInput()
            verifyHistory {
                recentSearchesTitle()
                historyResult(RANELAGH_TERRACE_62_NAME)
            }
            selectHistoryItem(RANELAGH_TERRACE_62_NAME)
        }
        searchPlaceBottomSheet {
            verifyPlaceName(RANELAGH_TERRACE_62_NAME)
            verifyDistance(RANELAGH_TERRACE_62_LOCALLY_CALCULATED_DISTANCE)
        }
    }

    /**
     * Test 2.
     * Try to search without internet connection, then with internet connection.
     */
    @Test
    fun testNoInternetConnectionSearchAndThenRetryWithConnection() {
        mockServer.enqueue(
            createNoNetworkConnectionResponse(),
            createSuccessfulResponse(RANELAGH_SUGGESTIONS_ASSET)
        )

        searchBottomSheet {
            clickSearchInput()
            verifyHistory {
                noRecentSearches()
            }
            typeQuery(RANELAGH_TERRACE_62_NAME)

            awaitResults()
            verifySearchResults {
                noInternetConnectionError()
            }
            retryFromError()

            awaitResults()
            verifySearchResults {
                inflateSearchResults(RANELAGH_SUGGESTIONS_ASSET)
                submitMissingResultFeedback()
            }
        }
    }

    /**
     * Test 3.
     * Check arrow button from search result list item.
     */
    @Test
    fun testSearchResultArrowButton() {
        mockServer.enqueueSuccessfulResponses(
            MINSK_SUGGESTIONS_ASSET,
            MINSK_REGION_SUGGESTIONS_ASSET
        )

        searchBottomSheet {
            clickSearchInput()
            typeQuery(MINSK_QUERY)
            val queryItems = withTestActivity {
                getSearchItems(this)
            }

            clickPopulateButtonForSearchResult(
                resultName = MINSK_REGION_SEARCH_RESULT_NAME
            )
            val populateItems = withTestActivity {
                getSearchItems(this)
            }

            verifyQuery(MINSK_REGION_SEARCH_RESULT_NAME)
            assertNotEquals(queryItems, populateItems)
        }
    }

    /**
     * Test 4.
     * Check search query with empty results.
     */
    @Test
    fun testSearchQueryWithEmptyResults() {
        mockServer.enqueueSuccessfulResponses(EMPTY_SUGGESTIONS_ASSET)

        searchBottomSheet {
            clickSearchInput()
            typeQuery(NOT_SEARCHABLE_QUERY)
            verifySearchResults {
                noResults()
                submitMissingResultFeedback()
            }
        }
    }

    /**
     * Test 5.
     * Search for address and clear query input.
     */
    @Test
    fun testSearchAndClearQuery() {
        mockServer.enqueueSuccessfulResponses(MINSK_SUGGESTIONS_ASSET)

        searchBottomSheet {
            clickSearchInput()
            typeQuery(MINSK_QUERY)
            clearQueryInput()
            verifyHistory {
                noRecentSearches()
            }
        }
    }

    /**
     * Test 6.
     * Search for address and press system Back button.
     */
    @Test
    fun testSearchAndNavigateBack() {
        mockServer.enqueueSuccessfulResponses(MINSK_SUGGESTIONS_ASSET)

        searchBottomSheet {
            clickSearchInput()
            typeQuery(MINSK_QUERY, closeKeyboardAfterTyping = true)
        }
        systemNavigation {
            back()
        }
        searchBottomSheet {
            verifyCollapsed()
            verifyNoQuery()
            verifyQueryHintIsVisible()
            verifySearchInputNotInFocus()
        }
    }

    /**
     * Test 7.
     * Search for Cafe and select category suggestion
     */
    @Test
    fun testCategorySuggestionSelection() {
        mockServer.enqueueSuccessfulResponses(CAFE_SUGGESTIONS_ASSET, CATEGORY_CAFE_RESULTS_ASSET)

        searchBottomSheet {
            clickSearchInput()
            typeQuery(CAFE_CATEGORY, closeKeyboardAfterTyping = true)
            selectCategorySuggestion(CAFE_CATEGORY)

            awaitResults()
            verifySearchResults {
                inflateSearchResults(CATEGORY_CAFE_RESULTS_ASSET)
                submitMissingResultFeedback()
            }

            selectSearchResult(
                resultName = CAFE_SEARCH_RESULT_NAME,
                addressSubstring = CAFE_SEARCH_RESULT_ADDRESS
            )

            searchPlaceBottomSheet {
                verifyPlaceName(CAFE_SEARCH_RESULT_NAME)
                verifyCategoryName(CAFE_CATEGORY)
                verifyAddress(CAFE_SEARCH_RESULT_ADDRESS)
            }
        }
    }

    /**
     * Test 8.
     * Provided SearchOptions are used for search requests and can be overridden.
     */
    @Test
    fun testSearchOptions() {
        mockServer.enqueueSuccessfulResponses(MINSK_SUGGESTIONS_ASSET, MINSK_SUGGESTIONS_ASSET)

        val testPoint1 = Point.fromLngLat(10.5, 20.123)
        val testPoint2 = Point.fromLngLat(30.0, 50.0)

        val options = SearchOptions(
            proximity = testPoint1,
            boundingBox = BoundingBox.fromPoints(testPoint1, testPoint2),
            countries = listOf(Country.UNITED_STATES, Country.BELARUS),
            fuzzyMatch = true,
            languages = listOf(Language.ENGLISH),
            limit = 5,
            types = listOf(QueryType.COUNTRY, QueryType.LOCALITY, QueryType.ADDRESS),
            origin = testPoint2,
            navigationOptions = SearchNavigationOptions(
                navigationProfile = SearchNavigationProfile.DRIVING,
                etaType = EtaType.NAVIGATION
            )
        )

        val query = "query"

        searchBottomSheet {
            setSearchOptions(options)
            clickSearchInput()
            typeQuery(query, closeKeyboardAfterTyping = true)
            awaitResults()
            clearQueryInput()
        }

        assertSearchOptionsAreCorrect(query, options, mockServer.takeRequest())

        val overriddenOptions = SearchOptions(
            proximity = testPoint2,
            languages = listOf(Language.ENGLISH),
            origin = testPoint1,
        )

        val overriddenQuery = "new query"

        searchBottomSheet {
            setSearchOptions(overriddenOptions)
            clickSearchInput()
            typeQuery(overriddenQuery, closeKeyboardAfterTyping = true)
            awaitResults()
            clearQueryInput()
        }

        assertSearchOptionsAreCorrect(overriddenQuery, overriddenOptions, mockServer.takeRequest())
    }

    private companion object {

        const val RANELAGH_TERRACE_62_NAME = "62 Ranelagh Terrace"
        const val RANELAGH_TERRACE_62_ADDRESS_PART = "Royal Leamington Spa"

        const val MINSK_QUERY = "Minsk"
        const val CAFE_CATEGORY = "Cafe"

        const val MINSK_REGION_SEARCH_RESULT_NAME = "Minsk Region"
        const val NOT_SEARCHABLE_QUERY = "aaaaaaaaaaaa"

        const val RANELAGH_TERRACE_62_SERVER_DISTANCE = "5281.7 mi"
        const val RANELAGH_TERRACE_62_LOCALLY_CALCULATED_DISTANCE = "5286 mi"

        const val CAFE_SEARCH_RESULT_NAME = "Googleplex - Big Table Cafe"
        const val CAFE_SEARCH_RESULT_ADDRESS = "1900 Charleston Rd, Mountain View, California 94043, United States of America"

        fun assertSearchOptionsAreCorrect(query: String, options: SearchOptions, request: RecordedRequest) {
            val url = request.requestUrl!!
            assertEquals(query, url.pathSegments.last())

            assertEquals(formatPointsToBackendConvention(options.proximity), url.queryParameter("proximity"))
            assertEquals(
                options.boundingBox?.run { formatPointsToBackendConvention(southwest(), northeast()) },
                url.queryParameter("bbox")
            )
            assertEquals(options.countries?.joinToString(separator = ",") { it.code }, url.queryParameter("country"))
            assertEquals(Language.ENGLISH.code, url.queryParameter("language"))
            assertEquals(options.limit?.toString(), url.queryParameter("limit"))
            assertEquals(
                options.types?.joinToString(separator = ",") { it.name.toLowerCase() },
                url.queryParameter("types")
            )

            assertEquals(formatPointsToBackendConvention(options.origin), url.queryParameter("origin"))
            assertEquals(
                options.navigationOptions?.navigationProfile?.rawName,
                url.queryParameter("navigation_profile")
            )
            assertEquals(options.navigationOptions?.etaType?.rawName, url.queryParameter("eta_type"))
        }
    }
}
