package com.mapbox.search.sample.tools.matchers

import android.view.View
import android.widget.TextView
import com.mapbox.search.sample.R
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class CategoryItemByNameMatcher(
    private val expectedCategoryName: String
) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText("Matcher for category item with name ")
        description.appendValue(expectedCategoryName)
    }

    override fun matchesSafely(item: View?): Boolean {
        val categoryName = item?.findViewById<TextView>(R.id.category_item)?.text?.toString()
        return categoryName == expectedCategoryName
    }
}
