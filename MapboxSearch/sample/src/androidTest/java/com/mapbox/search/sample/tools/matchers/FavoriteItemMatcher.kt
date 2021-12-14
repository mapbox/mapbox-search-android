package com.mapbox.search.sample.tools.matchers

import android.view.View
import android.widget.TextView
import com.mapbox.search.sample.R
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class FavoriteItemMatcher(
    private val favoriteName: String,
    private val addressSubstring: String
) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("Matcher for favorite item with name ")
        description.appendValue(favoriteName)
        description.appendText(" and address, containing ")
        description.appendValue(addressSubstring)
    }

    override fun matchesSafely(item: View?): Boolean {
        val uiFavoriteName = item?.findViewById<TextView>(R.id.favorite_name)?.text?.toString()
        if (uiFavoriteName != favoriteName) {
            return false
        }
        val uiFavoriteAddress = item.findViewById<TextView>(R.id.favorite_address)?.text?.toString()
        return uiFavoriteAddress?.contains(addressSubstring) == true
    }
}
