package com.mapbox.search.sample.tools.matchers

import android.util.Log
import android.view.View
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.SearchBottomSheetView.BottomSheetState
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

fun withSearchViewBottomSheetState(
    @BottomSheetState state: Int,
    @BottomSheetState vararg states: Int
) = SearchViewBottomSheetStateMatcher(state, *states)

class SearchViewBottomSheetStateMatcher(
    @BottomSheetState
    private val expectedStates: List<Int>
) : TypeSafeMatcher<View>() {

    constructor(@BottomSheetState vararg states: Int) : this(states.toList())

    override fun describeTo(description: Description) {
        description.appendText(
            "Matcher for SearchBottomSheetView with one of specified states (state codes = $expectedStates)"
        )
    }

    override fun matchesSafely(view: View): Boolean {
        return if (view is SearchBottomSheetView) {
            expectedStates.contains(view.state)
        } else {
            Log.e(TAG, "Matcher was applied to ${view::class.java.simpleName}, instead of SearchBottomSheetView")
            false
        }
    }

    private companion object {
        const val TAG = "SearchViewBottomSheetStateMatcher"
    }
}
