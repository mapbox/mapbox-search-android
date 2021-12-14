package com.mapbox.search.sample.tools.matchers

import android.view.View
import android.widget.TextView
import com.mapbox.search.sample.R
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class FavoriteItemByNameMatcher(private val favoriteName: String) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("Matcher for favorite item with name ")
        description.appendValue(favoriteName)
    }

    override fun matchesSafely(item: View?): Boolean {
        val uiFavoriteName = item?.findViewById<TextView>(R.id.favorite_name)?.text?.toString()
        return uiFavoriteName == favoriteName
    }
}
