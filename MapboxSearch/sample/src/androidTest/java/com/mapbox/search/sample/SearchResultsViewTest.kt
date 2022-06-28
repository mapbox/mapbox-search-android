package com.mapbox.search.sample

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.mapbox.search.sample.SearchResultsInflater.inflateSearchResults
import com.mapbox.search.sample.extensions.enqueue
import com.mapbox.search.sample.extensions.noInternetConnectionError
import com.mapbox.search.sample.robots.searchPlaceBottomSheet
import com.mapbox.search.sample.robots.searchResultsView
import org.junit.Assert
import org.junit.Test

class SearchResultsViewTest : MockServerSearchActivityTest() {

    private fun openSearchView() {
        onView(withId(R.id.action_search))
            .check(matches(isDisplayed()))
            .perform(click())
    }

    @Test
    fun testSearchSuggestionSelectionCreatesHistoryRecord() {
        mockServer.enqueueSuccessfulResponses(
            Constants.Assets.RANELAGH_SUGGESTIONS_ASSET,
            Constants.Assets.RANELAGH_ROYAL_SPA_RESULT_ASSET
        )

        openSearchView()

        searchResultsView {
            verifyHistory {
                noRecentSearches()
            }

            typeSearchViewText(RANELAGH_TERRACE_62_NAME)

            selectSearchResult(
                resultName = RANELAGH_TERRACE_62_NAME,
                addressSubstring = RANELAGH_TERRACE_62_ADDRESS_PART
            )
        }

        searchPlaceBottomSheet {
            verifyDistance(RANELAGH_TERRACE_62_SERVER_DISTANCE)
            close()
        }

        openSearchView()

        searchResultsView {
            verifyHistory {
                recentSearchesTitle()
                historyResult(RANELAGH_TERRACE_62_NAME, RANELAGH_TERRACE_62_ADDRESS)
            }
            selectHistoryItem(RANELAGH_TERRACE_62_NAME, RANELAGH_TERRACE_62_ADDRESS)
        }

        searchPlaceBottomSheet {
            verifyPlaceName(RANELAGH_TERRACE_62_NAME)
            verifyDistance(RANELAGH_TERRACE_62_LOCALLY_CALCULATED_DISTANCE)
        }
    }

    @Test
    fun testNoInternetConnectionSearchAndThenRetryWithConnection() {
        mockServer.enqueue(
            createNoNetworkConnectionResponse(),
            createSuccessfulResponse(Constants.Assets.RANELAGH_SUGGESTIONS_ASSET)
        )

        openSearchView()

        searchResultsView {
            verifyHistory {
                noRecentSearches()
            }
            typeSearchViewText(RANELAGH_TERRACE_62_NAME)

            awaitResults()
            verifySearchResults {
                noInternetConnectionError()
            }
            retryFromError()

            awaitResults()
            verifySearchResults {
                inflateSearchResults(Constants.Assets.RANELAGH_SUGGESTIONS_ASSET)
                submitMissingResultFeedback()
            }
        }
    }

    @Test
    fun testSearchResultArrowButton() {
        mockServer.enqueueSuccessfulResponses(
            Constants.Assets.MINSK_SUGGESTIONS_ASSET,
            Constants.Assets.MINSK_REGION_SUGGESTIONS_ASSET
        )

        openSearchView()

        searchResultsView {
            typeSearchViewText(MINSK_QUERY)
            val queryItems = withTestActivity {
                getSearchItems(this)
            }

            clickPopulateButtonForSearchResult(
                resultName = MINSK_REGION_SEARCH_RESULT_NAME
            )
            val populateItems = withTestActivity {
                getSearchItems(this)
            }

            Assert.assertNotEquals(queryItems, populateItems)
        }
    }

    @Test
    fun testSearchQueryWithEmptyResults() {
        mockServer.enqueueSuccessfulResponses(Constants.Assets.EMPTY_SUGGESTIONS_ASSET)

        openSearchView()

        searchResultsView {
            typeSearchViewText(NOT_SEARCHABLE_QUERY)
            verifySearchResults {
                noResults()
                submitMissingResultFeedback()
            }
        }
    }

    @Test
    fun testSearchAndClearQuery() {
        mockServer.enqueueSuccessfulResponses(Constants.Assets.MINSK_SUGGESTIONS_ASSET)

        openSearchView()

        searchResultsView {
            typeSearchViewText(MINSK_QUERY)
            typeSearchViewText("")
            verifyHistory {
                noRecentSearches()
            }
        }
    }

    private companion object {

        const val RANELAGH_TERRACE_62_NAME = "62 Ranelagh Terrace"
        const val RANELAGH_TERRACE_62_ADDRESS = "Royal Leamington Spa, Royal Leamington Spa, CV31 3BS, United Kingdom"
        const val RANELAGH_TERRACE_62_ADDRESS_PART = "Royal Leamington Spa"

        const val MINSK_QUERY = "Minsk"

        const val MINSK_REGION_SEARCH_RESULT_NAME = "Minsk Region"
        const val NOT_SEARCHABLE_QUERY = "aaaaaaaaaaaa"

        const val RANELAGH_TERRACE_62_SERVER_DISTANCE = "5281.7 mi"
        const val RANELAGH_TERRACE_62_LOCALLY_CALCULATED_DISTANCE = "5286 mi"
    }
}
