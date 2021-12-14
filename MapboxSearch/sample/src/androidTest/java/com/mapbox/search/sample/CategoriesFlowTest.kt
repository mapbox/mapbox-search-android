package com.mapbox.search.sample

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.CategorySearchOptions
import com.mapbox.search.Country
import com.mapbox.search.Language
import com.mapbox.search.sample.Constants.Assets.CATEGORY_CAFE_RESULTS_ASSET
import com.mapbox.search.sample.SearchResultsInflater.inflateSearchResults
import com.mapbox.search.sample.robots.searchBottomSheet
import com.mapbox.search.sample.robots.searchCategoriesBottomSheet
import com.mapbox.search.sample.robots.systemNavigation
import com.mapbox.search.ui.view.category.Category
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoriesFlowTest : MockServerSearchActivityTest() {

    override fun beforeEachTest() {
        super.beforeEachTest()
        mockServer.enqueueSuccessfulResponses(CATEGORY_CAFE_RESULTS_ASSET)
    }

    /**
     * Test 2.
     * Check categories list.
     */
    @Test
    fun testCategoriesListContainsAllCategories() {
        searchBottomSheet {
            expand()
            verifyCategoriesItemCount(CATEGORIES_TOTAL_COUNT)
            // TODO Verify all items have icons and appropriate title
        }
    }

    /**
     * Test 3.
     * Select category and close category search results view.
     */
    @Test
    fun testCategorySearchAndClose() {
        searchBottomSheet {
            expand()
            selectCategory(CAFE_CATEGORY_NAME)
        }
        searchCategoriesBottomSheet {
            verifyTitle(CAFE_CATEGORY_NAME)
            close()
        }
        searchBottomSheet {
            verifyCollapsed()
            verifyNoQuery()
            verifyQueryHintIsVisible()
            verifySearchInputNotInFocus()
        }
    }

    /**
     * Test 4.
     * Select category and press system Back button.
     */
    @Test
    fun testCategorySearchAndNavigateBack() {
        searchBottomSheet {
            expand()
            selectCategory(CAFE_CATEGORY_NAME)
        }
        searchCategoriesBottomSheet {
            verifyTitle(CAFE_CATEGORY_NAME)
        }
        systemNavigation {
            back()
        }
        searchBottomSheet {
            verifyExpanded()
            verifyNoQuery()
            verifyQueryHintIsVisible()
            verifySearchInputNotInFocus()
        }
    }

    /**
     * Test 5.`
     * Select category and validate received category search results.
     */
    @Test
    fun testCategorySearchShowResults() {
        searchBottomSheet {
            expand()
            selectCategory(CAFE_CATEGORY_NAME)
        }
        searchCategoriesBottomSheet {
            awaitResults()
            verifyTitle(CAFE_CATEGORY_NAME)
            verifyCategorySearchResults {
                inflateSearchResults(CATEGORY_CAFE_RESULTS_ASSET)
            }
        }
    }

    /**
     * Test 6.
     * Provided CategorySearchOptions are used for search requests and can be overridden.
     */
    @Test
    fun testCategorySearchOptions() {
        mockServer.enqueueSuccessfulResponses(CATEGORY_CAFE_RESULTS_ASSET)

        val testPoint1 = Point.fromLngLat(10.5, 20.123)
        val testPoint2 = Point.fromLngLat(30.0, 50.0)

        val options = CategorySearchOptions(
            proximity = testPoint1,
            boundingBox = BoundingBox.fromPoints(testPoint1, testPoint2),
            countries = listOf(Country.UNITED_STATES, Country.BELARUS),
            fuzzyMatch = true,
            languages = listOf(Language.ENGLISH),
            limit = 5,
            origin = testPoint2
        )

        searchCategoriesBottomSheet(Category.COFFEE_SHOP_CAFE, options) {
            awaitResults()
        }

        assertSearchOptionsAreCorrect("cafe", options, mockServer.takeRequest())

        val overriddenOptions = CategorySearchOptions(
            proximity = testPoint2,
            origin = testPoint1
        )

        searchCategoriesBottomSheet(Category.RESTAURANTS, overriddenOptions) {
            awaitResults()
        }

        assertSearchOptionsAreCorrect("restaurant", overriddenOptions, mockServer.takeRequest())
    }

    private companion object {

        const val CATEGORIES_TOTAL_COUNT = 22
        const val CAFE_CATEGORY_NAME = "Cafe"

        fun assertSearchOptionsAreCorrect(category: String, options: CategorySearchOptions, request: RecordedRequest) {
            val url = request.requestUrl!!

            assertEquals(category, url.pathSegments.last())

            assertEquals(formatPointsToBackendConvention(options.proximity), url.queryParameter("proximity"))
            assertEquals(
                options.boundingBox?.run { formatPointsToBackendConvention(southwest(), northeast()) },
                url.queryParameter("bbox")
            )
            assertEquals(options.countries?.joinToString(separator = ",") { it.code }, url.queryParameter("country"))
            assertEquals(Language.ENGLISH.code, url.queryParameter("language"))
            assertEquals(options.limit?.toString(), url.queryParameter("limit"))

            assertEquals(url.queryParameter("origin"), formatPointsToBackendConvention(options.origin))
            assertEquals(options.navigationProfile?.rawName, url.queryParameter("navigation_profile"))
        }
    }
}
