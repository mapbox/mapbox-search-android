package com.mapbox.search.sample.tools.actions

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.mapbox.search.ui.view.SearchBottomSheetView
import org.hamcrest.Matcher

class SearchViewCollapseAction : ViewAction {

    override fun getDescription() = "BottomSheet collapse"

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(SearchBottomSheetView::class.java)
    }

    override fun perform(uiController: UiController?, view: View) {
        (view as SearchBottomSheetView).open()
    }
}
