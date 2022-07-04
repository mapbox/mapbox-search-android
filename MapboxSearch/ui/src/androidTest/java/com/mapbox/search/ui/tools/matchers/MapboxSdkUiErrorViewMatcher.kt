package com.mapbox.search.ui.tools.matchers

import android.view.View
import android.widget.TextView
import com.mapbox.search.ui.R
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

internal class MapboxSdkUiErrorViewMatcher(
    private val errorTitle: String,
    private val errorSubtitle: String
) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("Matcher for MapboxSdkUiErrorView with error title = ")
        description.appendValue(errorTitle)
        description.appendText(" and error subtitle = ")
        description.appendValue(errorSubtitle)
    }

    override fun matchesSafely(item: View?): Boolean {
        return item?.javaClass?.simpleName == "MapboxSdkUiErrorView" &&
            item.findViewById<TextView>(R.id.error_title)?.text == errorTitle &&
            item.findViewById<TextView>(R.id.error_subtitle)?.text == errorSubtitle
    }
}
