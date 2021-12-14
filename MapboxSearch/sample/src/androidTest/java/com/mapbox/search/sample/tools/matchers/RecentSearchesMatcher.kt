package com.mapbox.search.sample.tools.matchers

import android.view.View
import android.widget.TextView
import com.mapbox.search.sample.R
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class RecentSearchesMatcher : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("Matcher for \"RECENT SEARCHES\" title")
    }

    override fun matchesSafely(item: View?): Boolean {
        val uiHistoryName = item?.findViewById<TextView>(R.id.history_item)?.text?.toString()
        return uiHistoryName == "Recent searches"
    }
}
