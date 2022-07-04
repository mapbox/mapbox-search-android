package com.mapbox.search.ui.tools.matchers

import android.view.View
import android.widget.TextView
import com.mapbox.search.ui.R
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

internal class HistoryItemMatcher(
    private val historyName: String,
    private val historyAddress: String
) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("Matcher for history item with name ")
        description.appendValue(historyName)
        description.appendText(" and with address ")
        description.appendValue(historyAddress)
    }

    override fun matchesSafely(item: View?): Boolean {
        return item != null &&
                item.findViewById<TextView>(R.id.history_name)?.text?.toString() == historyName &&
                item.findViewById<TextView>(R.id.history_address)?.text?.toString() == historyAddress
    }
}
