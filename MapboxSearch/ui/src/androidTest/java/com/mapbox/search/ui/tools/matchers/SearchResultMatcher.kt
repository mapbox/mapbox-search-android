package com.mapbox.search.ui.tools.matchers

import android.view.View
import android.widget.TextView
import com.mapbox.search.ui.R
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

internal class SearchResultMatcher(
    private val resultName: String,
    private val resultAddress: String,
    private val distance: String? = null
) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        with(description) {
            appendText("Matcher for search result with name ")
            appendValue(resultName)
            appendValue(" and address ")
            appendValue(resultAddress)
            if (distance != null) {
                appendText(" and distance ")
                appendValue(distance)
            }
        }
    }

    override fun matchesSafely(item: View?): Boolean {
        val searchResultName = item?.findViewById<TextView>(R.id.search_result_name)?.text?.toString()
        val searchResultAddress = item?.findViewById<TextView>(R.id.search_result_address)?.text?.toString()
        val searchResultDistance = item?.findViewById<TextView>(R.id.search_result_distance)?.text?.toString()
        return searchResultName == resultName && searchResultAddress == resultAddress &&
            (distance == null || searchResultDistance == distance)
    }
}
