package com.mapbox.search.sample.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.mapbox.search.CategorySearchOptions
import com.mapbox.search.sample.Constants.DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS
import com.mapbox.search.sample.Constants.MAX_NETWORK_REQUEST_TIMEOUT_MILLIS
import com.mapbox.search.sample.robots.builders.SearchResultsRecyclerBuilder
import com.mapbox.search.sample.tools.actions.OpenCategoriesBottomSheetViewAction
import com.mapbox.search.sample.tools.waitAtLeastOneInstanceOfViewInHierarchy
import com.mapbox.search.sample.tools.waitUntilViewAssertionSuccessful
import com.mapbox.search.ui.R
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.category.SearchCategoriesBottomSheetView
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertListItemCount
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertListNotEmpty
import com.schibsted.spain.barista.interaction.BaristaListInteractions.clickListItem
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf

fun searchCategoriesBottomSheet(block: SearchCategoriesBottomSheetRobot.() -> Unit) {
    onView(withClassName(`is`(SearchCategoriesBottomSheetView::class.qualifiedName)))
        .check(matches(isDisplayed()))
    SearchCategoriesBottomSheetRobot().apply { block() }
}

fun searchCategoriesBottomSheet(
    category: Category,
    searchOptions: CategorySearchOptions,
    block: SearchCategoriesBottomSheetRobot.() -> Unit
) {
    onView(withChild(withId(R.id.search_categories_container)))
        .perform(OpenCategoriesBottomSheetViewAction(category, searchOptions))

    onView(withClassName(`is`(SearchCategoriesBottomSheetView::class.qualifiedName)))
        .check(matches(isDisplayed()))

    SearchCategoriesBottomSheetRobot().apply { block() }
}

@RobotDsl
class SearchCategoriesBottomSheetRobot {

    fun awaitResults() {
        Thread.sleep(DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS)
        waitUntilViewAssertionSuccessful(timeout = MAX_NETWORK_REQUEST_TIMEOUT_MILLIS) {
            onView(withId(R.id.categories_result_recycler))
                .check(matches(isDisplayed()))
            assertListNotEmpty(R.id.categories_result_recycler)
        }
        Thread.sleep(DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun selectCategorySearchResult(position: Int) {
        waitAtLeastOneInstanceOfViewInHierarchy(R.id.categories_result_recycler)
        clickListItem(R.id.categories_result_recycler, position)
        Thread.sleep(DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun close() {
        onView(allOf(withId(R.id.card_close_button), isDescendantOfA(withId(R.id.search_categories_container))))
            .perform(click())
        Thread.sleep(DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun verifyTitle(title: String) {
        onView(withId(R.id.search_category_name))
            .check(matches(ViewMatchers.withText(title)))
    }

    fun verifyCategorySearchResults(block: SearchResultsRecyclerBuilder.() -> Unit) {
        SearchResultsRecyclerBuilder(R.id.categories_result_recycler).apply {
            block()
            assertListItemCount(R.id.categories_result_recycler, itemPosition)
        }
    }
}
