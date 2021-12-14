package com.mapbox.search.sample.tools.matchers

import android.view.View
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.mapbox.search.sample.R
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class CategorySearchSuggestionMatcher(var categoryName: String) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("Matcher to search category search suggestion with name ")
        description.appendValue(categoryName)
    }

    override fun matchesSafely(item: View?): Boolean {
        val nameView = item?.findViewById<TextView>(R.id.search_result_name)
        val addressView = item?.findViewById<View>(R.id.search_result_address)
        return nameView != null && addressView != null &&
                nameView.isVisible &&
                nameView.text.toString().equals(categoryName, ignoreCase = true) &&
                addressView.isGone
    }
}
