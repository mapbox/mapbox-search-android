package com.mapbox.search.sample

import com.mapbox.search.sample.Constants.Assets.CATEGORY_CAFE_RESULTS_ASSET
import com.mapbox.search.sample.SearchResultsInflater.inflateSearchResults
import com.mapbox.search.sample.robots.searchBottomSheet
import com.mapbox.search.sample.robots.searchCategoriesBottomSheet
import com.mapbox.search.sample.robots.searchPlaceBottomSheet
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class HotCategoryButtonsTest(
    private val hotCategoryName: String,
    private val expandBottomSheet: Boolean
) : MockServerSearchActivityTest() {

    /**
     * (Categories Flow) Test 1.
     * Hot categories button check.
     */
    @Test
    fun testHotCategoryButton() {
        mockServer.enqueueSuccessfulResponses(CATEGORY_CAFE_RESULTS_ASSET)

        searchBottomSheet {
            if (expandBottomSheet) {
                expand()
            }
            selectHotCategory(hotCategoryName)
        }
        searchCategoriesBottomSheet {
            awaitResults()
            verifyTitle(hotCategoryName)
            verifyCategorySearchResults {
                inflateSearchResults(CATEGORY_CAFE_RESULTS_ASSET)
            }
            selectCategorySearchResult(position = 0)
        }
        searchPlaceBottomSheet {
            verifyPlaceName("Googleplex - Big Table Cafe")
            // Category name is retrieved from SearchResult response.
            // Used mock response contains results with category "Cafe",
            // so we will see "Cafe" name for any category.
            verifyCategoryName("Cafe")
            verifyAddress("1900 Charleston Rd, Mountain View, California 94043, United States of America")
            verifyDistance("0.2 mi")
        }
    }

    companion object {

        @Parameters(name = "hotCategoryName = {0}, expandBottomSheet = {1}")
        @JvmStatic
        fun data(): Collection<Any> {
            return listOf("Gas", "ATM", "Cafe", "Gym")
                .flatMap { categoryName ->
                    listOf(
                        arrayOf(categoryName, false),
                        arrayOf(categoryName, true)
                    )
                }
        }
    }
}
