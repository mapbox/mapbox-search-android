package com.mapbox.search.sample.robots

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.mapbox.search.sample.Constants
import com.mapbox.search.sample.Constants.TEXT_INPUT_DELAY_MILLIS
import com.mapbox.search.sample.robots.builders.SearchResultsRecyclerBuilder
import com.mapbox.search.sample.tools.SearchSdkUiInteractions
import com.mapbox.search.sample.tools.actions.SearchSdkActions.customTypeText
import com.mapbox.search.sample.tools.matchers.SearchResultByAddressMatcher
import com.mapbox.search.sample.tools.waitAtLeastOneInstanceOfViewInHierarchy
import com.mapbox.search.ui.R
import com.schibsted.spain.barista.assertion.BaristaListAssertions
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf

fun addressSearchView(block: AddressSearchViewRobot.() -> Unit) {
    onView(withClassName(Matchers.`is`("com.mapbox.search.ui.view.search.address.AddressSearchView")))
        .check(matches(isDisplayed()))
    AddressSearchViewRobot().apply { block() }
}

@RobotDsl
class AddressSearchViewRobot {

    fun typeQuery(query: String, closeKeyboardAfterTyping: Boolean = true) {
        onView(allOf(withId(R.id.search_edit_text), isDescendantOfA(withId(R.id.address_search_view))))
            .perform(*customTypeText(query))
        if (closeKeyboardAfterTyping) {
            closeSoftKeyboard()
        }
        Thread.sleep(TEXT_INPUT_DELAY_MILLIS)
    }

    /**
     * @param addressSubstring part of search result address, that should be matched
     */
    fun selectSearchResult(resultName: String, addressSubstring: String) {
        waitAtLeastOneInstanceOfViewInHierarchy(R.id.search_result_item)
        onView(withId(R.id.search_results_view))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    SearchResultByAddressMatcher(resultName, addressSubstring),
                    click()
                )
            )
        Thread.sleep(Constants.SINGLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun awaitResults() {
        SearchSdkUiInteractions.awaitResultsInSearchResultsAdapter()
    }

    fun verifySearchResults(block: SearchResultsRecyclerBuilder.() -> Unit) {
        SearchResultsRecyclerBuilder(R.id.search_results_view).apply {
            block()
            BaristaListAssertions.assertListItemCount(R.id.search_results_view, itemPosition)
        }
    }

    fun retryFromError() {
        SearchSdkUiInteractions.retryFromError()
    }
}
