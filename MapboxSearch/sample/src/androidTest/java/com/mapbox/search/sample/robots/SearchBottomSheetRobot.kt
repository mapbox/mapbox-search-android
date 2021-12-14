package com.mapbox.search.sample.robots

import android.app.Activity
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.mapbox.search.SearchOptions
import com.mapbox.search.sample.Constants.CHANGE_CARDS_NAVIGATION_ANIMATION_TIME_MILLIS
import com.mapbox.search.sample.Constants.DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS
import com.mapbox.search.sample.Constants.SINGLE_ANDROID_ANIMATION_DELAY_MILLIS
import com.mapbox.search.sample.Constants.TEXT_INPUT_DELAY_MILLIS
import com.mapbox.search.sample.robots.builders.HistoryRecyclerBuilder
import com.mapbox.search.sample.robots.builders.SearchResultsRecyclerBuilder
import com.mapbox.search.sample.tools.SearchSdkUiInteractions
import com.mapbox.search.sample.tools.actions.AssignOptionsForSearchBottomSheetViewAction
import com.mapbox.search.sample.tools.actions.ClickSearchResultItemPopulateButton
import com.mapbox.search.sample.tools.actions.FavoriteItemMoreButtonClickAction
import com.mapbox.search.sample.tools.actions.SearchSdkActions.customTypeText
import com.mapbox.search.sample.tools.actions.SearchViewExpandAction
import com.mapbox.search.sample.tools.actions.TabLayoutTabClickAction
import com.mapbox.search.sample.tools.assertions.RecyclerViewItemCountAssertion
import com.mapbox.search.sample.tools.matchers.CategoryItemByNameMatcher
import com.mapbox.search.sample.tools.matchers.CategorySearchSuggestionMatcher
import com.mapbox.search.sample.tools.matchers.FavoriteItemByNameMatcher
import com.mapbox.search.sample.tools.matchers.FavoriteItemMatcher
import com.mapbox.search.sample.tools.matchers.FavoriteTemplateItemMatcher
import com.mapbox.search.sample.tools.matchers.HistoryItemMatcher
import com.mapbox.search.sample.tools.matchers.SearchResultByAddressMatcher
import com.mapbox.search.sample.tools.matchers.withElevation
import com.mapbox.search.sample.tools.matchers.withSearchViewBottomSheetState
import com.mapbox.search.sample.tools.waitAtLeastOneInstanceOfViewInHierarchy
import com.mapbox.search.sample.tools.waitUntilViewAssertionSuccessful
import com.mapbox.search.ui.R
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.SearchBottomSheetView.Companion.COLLAPSED
import com.mapbox.search.ui.view.SearchBottomSheetView.Companion.EXPANDED
import com.mapbox.search.ui.view.SearchBottomSheetView.Companion.HIDDEN
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertCustomAssertionAtPosition
import com.schibsted.spain.barista.assertion.BaristaListAssertions.assertListItemCount
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf

fun searchBottomSheet(block: SearchBottomSheetRobot.() -> Unit) {
    onView(withId(R.id.search_card_root))
        .check(matches(isDisplayed()))
    SearchBottomSheetRobot().apply { block() }
}

@RobotDsl
class SearchBottomSheetRobot {

    fun setSearchOptions(searchOptions: SearchOptions) {
        onView(withChild(withId(R.id.search_card_root)))
            .perform(AssignOptionsForSearchBottomSheetViewAction(searchOptions))
    }

    fun expand() {
        onView(withChild(withId(R.id.search_card_root)))
            .perform(SearchViewExpandAction())
        Thread.sleep(SINGLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun selectHotCategory(hotCategoryName: String) {
        onView(allOf(
            withParent(withId(R.id.hot_categories)),
            withChild(allOf(
                withId(R.id.title),
                withText(hotCategoryName)
            ))
        ))
            .perform(click())
        Thread.sleep(SINGLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun selectCategory(categoryName: String) {
        onView(withId(R.id.search_tab_category_recycler))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    CategoryItemByNameMatcher(categoryName),
                    click()
                )
            )
        Thread.sleep(DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun clickSearchInput() {
        onView(withId(R.id.search_edit_text))
            .perform(click())
        closeSoftKeyboard()
        Thread.sleep(SINGLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun openFavoritesTab() {
        onView(withId(R.id.tab_view_tab_layout))
            .perform(TabLayoutTabClickAction(1))
        Thread.sleep(SINGLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun typeQuery(query: String, closeKeyboardAfterTyping: Boolean = true) {
        onView(withId(R.id.search_edit_text))
            .perform(*customTypeText(query))
        if (closeKeyboardAfterTyping) {
            closeSoftKeyboard()
        }
        Thread.sleep(TEXT_INPUT_DELAY_MILLIS)
    }

    fun retryFromError() {
        SearchSdkUiInteractions.retryFromError()
    }

    fun clearQueryInput() {
        onView(allOf(withId(R.id.clear_text_button), isDescendantOfA(withId(R.id.search_input_edit_text))))
            .perform(click())
    }

    fun awaitResults() {
        SearchSdkUiInteractions.awaitResultsInSearchResultsAdapter()
    }

    /**
     * @param addressSubstring part of search result address, that should be matched
     */
    fun selectSearchResult(resultName: String, addressSubstring: String) {
        waitAtLeastOneInstanceOfViewInHierarchy(R.id.search_result_item)
        onView(withId(R.id.search_results_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    SearchResultByAddressMatcher(resultName, addressSubstring),
                    click()
                )
            )
        Thread.sleep(DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun selectHistoryItem(itemName: String) {
        waitAtLeastOneInstanceOfViewInHierarchy(R.id.search_result_item)
        onView(withId(R.id.search_results_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    HistoryItemMatcher(itemName),
                    click()
                )
            )
        Thread.sleep(DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun selectCategorySuggestion(categoryName: String) {
        waitAtLeastOneInstanceOfViewInHierarchy(R.id.search_result_item)
        onView(withId(R.id.search_results_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    CategorySearchSuggestionMatcher(categoryName),
                    click()
                )
            )
        Thread.sleep(DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun clickPopulateButtonForSearchResult(resultName: String, addressSubstring: String = "") {
        waitAtLeastOneInstanceOfViewInHierarchy(R.id.search_result_item)
        onView(withId(R.id.search_results_view))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    SearchResultByAddressMatcher(resultName, addressSubstring),
                    ClickSearchResultItemPopulateButton()
                )
            )
        Thread.sleep(TEXT_INPUT_DELAY_MILLIS)
    }

    fun addFavorite() {
        onView(withId(R.id.search_tab_favorites_recycler))
            .perform(
                actionOnItem<RecyclerView.ViewHolder>(
                    withId(R.id.add_favorite),
                    click()
                )
            )
        Thread.sleep(SINGLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun selectFavoriteWithName(favoriteName: String) {
        onView(allOf(isCompletelyDisplayed(), withId(R.id.search_tab_favorites_recycler))).perform(
            actionOnItem<RecyclerView.ViewHolder>(
                FavoriteItemByNameMatcher(favoriteName),
                click()
            )
        )
        Thread.sleep(SINGLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun showFavoriteOptions(favoriteName: String) {
        onView(allOf(isCompletelyDisplayed(), withId(R.id.search_tab_favorites_recycler))).perform(
            actionOnItem<RecyclerView.ViewHolder>(
                FavoriteItemByNameMatcher(favoriteName),
                FavoriteItemMoreButtonClickAction()
            )
        )
        Thread.sleep(SINGLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun getSearchItems(activity: Activity): List<Any>? {
        val recycler = activity.findViewById<RecyclerView>(R.id.search_results_view)
        return recycler.adapter?.let { adapter ->
            val getItemsFunc = adapter::class.java.getMethod("getItems")
            @Suppress("UNCHECKED_CAST")
            getItemsFunc.invoke(adapter) as? List<Any>
        }
    }

    fun verifyRootElevation(@DimenRes elevationRes: Int) {
        onView(withId(R.id.search_card_root))
            .check(matches(withElevation(elevationRes)))
    }

    fun verifyFavoriteTemplate(templateName: String, itemPosition: Int) {
        assertCustomAssertionAtPosition(
            R.id.search_tab_favorites_recycler,
            itemPosition,
            viewAssertion = matches(FavoriteTemplateItemMatcher(templateName))
        )
    }

    fun verifyCategoriesItemCount(expectedItemCount: Int) {
        onView(withId(R.id.search_tab_category_recycler))
            .check(matches(isDisplayed()))
            .check(RecyclerViewItemCountAssertion(expectedItemCount))
    }

    fun verifyQuery(query: String) {
        onView(withId(R.id.search_edit_text))
            .check(matches(withText(query)))
    }

    fun verifyNoQuery() {
        onView(withId(R.id.search_edit_text))
            .check(matches(withText("")))
    }

    fun verifyQueryHintIsVisible() {
        onView(withId(R.id.search_edit_text))
            .check(matches(isDisplayed()))
            .check(matches(withHint("Where to?")))
    }

    fun verifySearchInputNotInFocus() {
        onView(withId(R.id.search_edit_text))
            .check(matches(not(hasFocus())))
    }

    fun verifyExpanded() {
        awaitRestedStateForBottomSheet()
        onView(withClassName(`is`(SearchBottomSheetView::class.qualifiedName)))
            .check(matches(withSearchViewBottomSheetState(EXPANDED)))
    }

    fun verifyCollapsed() {
        awaitRestedStateForBottomSheet()
        onView(withClassName(`is`(SearchBottomSheetView::class.qualifiedName)))
            .check(matches(withSearchViewBottomSheetState(COLLAPSED)))
    }

    private fun awaitRestedStateForBottomSheet() {
        waitUntilViewAssertionSuccessful(timeout = CHANGE_CARDS_NAVIGATION_ANIMATION_TIME_MILLIS) {
            onView(withClassName(`is`(SearchBottomSheetView::class.qualifiedName)))
                .check(matches(withSearchViewBottomSheetState(COLLAPSED, EXPANDED, HIDDEN)))
        }
    }

    fun verifyHistory(block: HistoryRecyclerBuilder.() -> Unit) {
        HistoryRecyclerBuilder(R.id.search_results_view).apply {
            block()
            assertListItemCount(R.id.search_results_view, itemPosition)
        }
    }

    fun verifySearchResults(block: SearchResultsRecyclerBuilder.() -> Unit) {
        SearchResultsRecyclerBuilder(R.id.search_results_view).apply {
            block()
            assertListItemCount(R.id.search_results_view, itemPosition)
        }
    }

    /**
     * @param addressSubstring part of favorite item address, that should be matched
     */
    fun verifyFavoriteItem(favoriteName: String, addressSubstring: String, itemPosition: Int) {
        assertCustomAssertionAtPosition(
            R.id.search_tab_favorites_recycler,
            itemPosition,
            viewAssertion = matches(FavoriteItemMatcher(favoriteName, addressSubstring))
        )
    }

    fun verifyFavoriteItemsCount(itemsCount: Int) {
        onView(allOf(isCompletelyDisplayed(), withId(R.id.search_tab_favorites_recycler)))
            .check(RecyclerViewItemCountAssertion(itemsCount))
    }
}
