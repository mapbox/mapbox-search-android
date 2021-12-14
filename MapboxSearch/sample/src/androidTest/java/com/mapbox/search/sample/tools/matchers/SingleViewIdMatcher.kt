package com.mapbox.search.sample.tools.matchers

import android.view.View
import androidx.annotation.IdRes
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

fun withId(@IdRes viewId: Int, matcherDescription: String) = SingleViewIdMatcher(viewId, matcherDescription)

class SingleViewIdMatcher(
    @IdRes private val viewId: Int,
    private val matcherDescription: String
) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description) {
        description.appendText(matcherDescription)
    }

    override fun matchesSafely(item: View?): Boolean {
        return item?.id == viewId
    }
}
