package com.mapbox.search.sample.tools.matchers

import android.view.View
import android.widget.TextView
import com.mapbox.search.sample.R
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class FavoriteTemplateItemMatcher(
    private val templateName: String
) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("Matcher for favorite template item with name ")
        description.appendValue(templateName)
        description.appendText(" and in initial state (address isn't specified)")
    }

    override fun matchesSafely(item: View?): Boolean {
        val uiFavoriteName = item?.findViewById<TextView>(R.id.favorite_name)?.text?.toString()
        val uiFavoriteAddress = item?.findViewById<TextView>(R.id.favorite_address)?.text?.toString()
        return uiFavoriteName == templateName && uiFavoriteAddress == "Tap to add"
    }
}
