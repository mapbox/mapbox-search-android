package com.mapbox.search.ui.robots.builders

import androidx.annotation.IdRes
import androidx.test.espresso.assertion.ViewAssertions
import com.adevinta.android.barista.assertion.BaristaListAssertions
import com.mapbox.search.ui.robots.RobotDsl
import com.mapbox.search.ui.tools.matchers.HistoryItemMatcher
import com.mapbox.search.ui.tools.matchers.RecentSearchesMatcher
import com.mapbox.search.ui.tools.matchers.SearchSdkMatchers.isEmptyRecentSearchItem

@RobotDsl
internal class HistoryRecyclerBuilder(@IdRes val recyclerId: Int) {

    var itemPosition: Int = 0
        private set

    fun recentSearchesTitle() {
        BaristaListAssertions.assertCustomAssertionAtPosition(
            recyclerId,
            itemPosition++,
            viewAssertion = ViewAssertions.matches(RecentSearchesMatcher())
        )
    }

    fun noRecentSearches() {
        BaristaListAssertions.assertCustomAssertionAtPosition(
            recyclerId,
            itemPosition++,
            viewAssertion = ViewAssertions.matches(isEmptyRecentSearchItem())
        )
    }

    fun historyResult(historyName: String, historyAddress: String) {
        BaristaListAssertions.assertCustomAssertionAtPosition(
            recyclerId,
            itemPosition++,
            viewAssertion = ViewAssertions.matches(HistoryItemMatcher(historyName, historyAddress))
        )
    }
}
