package com.mapbox.search.sample.robots

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.adevinta.android.barista.assertion.BaristaListAssertions.assertListItemCount
import com.mapbox.search.sample.Constants.DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS
import com.mapbox.search.sample.Constants.TEXT_INPUT_DELAY_MILLIS
import com.mapbox.search.sample.robots.builders.HistoryRecyclerBuilder
import com.mapbox.search.sample.robots.builders.SearchResultsRecyclerBuilder
import com.mapbox.search.sample.tools.SearchSdkUiInteractions
import com.mapbox.search.sample.tools.actions.ClickSearchResultItemPopulateButton
import com.mapbox.search.sample.tools.matchers.HistoryItemMatcher
import com.mapbox.search.sample.tools.matchers.SearchResultByAddressMatcher
import com.mapbox.search.sample.tools.waitAtLeastOneInstanceOfViewInHierarchy
import com.mapbox.search.ui.R

fun searchResultsView(block: SearchBottomSheetRobot.() -> Unit) {
    onView(withId(com.mapbox.search.sample.R.id.search_results_view))
        .check(matches(isDisplayed()))
    SearchBottomSheetRobot().apply { block() }
}

@RobotDsl
class SearchBottomSheetRobot {

    fun typeSearchViewText(text: String) {
        onView(withId(com.mapbox.search.sample.R.id.search_src_text))
            .perform(ViewActions.replaceText(text))

        Thread.sleep(TEXT_INPUT_DELAY_MILLIS)
    }

    fun retryFromError() {
        SearchSdkUiInteractions.retryFromError()
    }

    fun awaitResults() {
        SearchSdkUiInteractions.awaitResultsInSearchResultsAdapter()
    }

    /**
     * @param resultName search result name that should be matched
     * @param addressSubstring part of search result address, that should be matched
     */
    fun selectSearchResult(resultName: String, addressSubstring: String) {
        waitAtLeastOneInstanceOfViewInHierarchy(R.id.search_result_item)
        onView(withId(com.mapbox.search.sample.R.id.search_results_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    SearchResultByAddressMatcher(resultName, addressSubstring),
                    click()
                )
            )
        Thread.sleep(DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun selectHistoryItem(historyName: String, historyAddress: String) {
        waitAtLeastOneInstanceOfViewInHierarchy(R.id.search_result_item)
        onView(withId(com.mapbox.search.sample.R.id.search_results_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    HistoryItemMatcher(historyName, historyAddress),
                    click()
                )
            )
        Thread.sleep(DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun clickPopulateButtonForSearchResult(resultName: String, addressSubstring: String = "") {
        waitAtLeastOneInstanceOfViewInHierarchy(R.id.search_result_item)
        onView(withId(com.mapbox.search.sample.R.id.search_results_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    SearchResultByAddressMatcher(resultName, addressSubstring),
                    ClickSearchResultItemPopulateButton()
                )
            )
        Thread.sleep(TEXT_INPUT_DELAY_MILLIS)
    }

    fun getSearchItems(activity: Activity): List<Any>? {
        val recycler = activity.findViewById<RecyclerView>(com.mapbox.search.sample.R.id.search_results_view)
        return recycler.adapter?.let { adapter ->
            val getItemsFunc = adapter::class.java.getMethod("getItems")
            @Suppress("UNCHECKED_CAST")
            getItemsFunc.invoke(adapter) as? List<Any>
        }
    }

    fun verifyHistory(block: HistoryRecyclerBuilder.() -> Unit) {
        HistoryRecyclerBuilder(com.mapbox.search.sample.R.id.search_results_view).apply {
            block()
            assertListItemCount(com.mapbox.search.sample.R.id.search_results_view, itemPosition)
        }
    }

    fun verifySearchResults(block: SearchResultsRecyclerBuilder.() -> Unit) {
        SearchResultsRecyclerBuilder(com.mapbox.search.sample.R.id.search_results_view).apply {
            block()
            assertListItemCount(com.mapbox.search.sample.R.id.search_results_view, itemPosition)
        }
    }
}
