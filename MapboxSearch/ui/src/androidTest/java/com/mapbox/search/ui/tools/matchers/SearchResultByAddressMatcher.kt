package com.mapbox.search.ui.tools.matchers

import android.view.View
import android.widget.TextView
import com.mapbox.search.ui.R
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

internal class SearchResultByAddressMatcher(
    private val resultName: String,
    private val addressSubstring: String,
) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("Matcher for search result with name ")
        description.appendValue(resultName)
        description.appendValue(" and address, containing ")
        description.appendValue(addressSubstring)
    }

    override fun matchesSafely(item: View?): Boolean {
        val searchResultAddress = item?.findViewById<TextView>(R.id.search_result_address)?.text?.toString()
        val searchResultName = item?.findViewById<TextView>(R.id.search_result_name)?.text?.toString()
        return searchResultAddress?.contains(addressSubstring) == true && searchResultName == resultName
    }
}
