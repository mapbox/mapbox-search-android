package com.mapbox.search.ui.robots.builders

import androidx.annotation.IdRes
import androidx.test.espresso.assertion.ViewAssertions
import com.adevinta.android.barista.assertion.BaristaListAssertions
import com.mapbox.search.ui.robots.RobotDsl
import com.mapbox.search.ui.tools.matchers.SearchResultMatcher
import com.mapbox.search.ui.tools.matchers.SearchSdkMatchers.isEmptySearchResultsItem
import com.mapbox.search.ui.tools.matchers.SearchSdkMatchers.isSearchProgressItem
import com.mapbox.search.ui.tools.matchers.SearchSdkMatchers.isSubmitMissingResultFeedbackView
import com.mapbox.search.ui.tools.matchers.SearchSdkMatchers.isUiErrorView

@RobotDsl
internal class SearchResultsRecyclerBuilder(@IdRes val recyclerId: Int) {

    var itemPosition: Int = 0
        private set

    fun noResults() {
        BaristaListAssertions.assertCustomAssertionAtPosition(
            recyclerId,
            itemPosition++,
            viewAssertion = ViewAssertions.matches(isEmptySearchResultsItem())
        )
    }

    fun loading() {
        BaristaListAssertions.assertCustomAssertionAtPosition(
            recyclerId,
            itemPosition++,
            viewAssertion = ViewAssertions.matches(isSearchProgressItem())
        )
    }

    fun error(errorTitle: String, errorSubtitle: String) {
        BaristaListAssertions.assertCustomAssertionAtPosition(
            recyclerId,
            itemPosition++,
            viewAssertion = ViewAssertions.matches(isUiErrorView(errorTitle, errorSubtitle))
        )
    }

    fun result(name: String, address: String, distance: String? = null) {
        BaristaListAssertions.assertCustomAssertionAtPosition(
            recyclerId,
            itemPosition++,
            viewAssertion = ViewAssertions.matches(
                SearchResultMatcher(
                    resultName = name,
                    resultAddress = address,
                    distance = distance
                )
            )
        )
    }

    fun submitMissingResultFeedback() {
        BaristaListAssertions.assertCustomAssertionAtPosition(
            recyclerId,
            itemPosition++,
            viewAssertion = ViewAssertions.matches(isSubmitMissingResultFeedbackView())
        )
    }
}
