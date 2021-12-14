package com.mapbox.search.sample.tools.actions

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import com.mapbox.search.ui.view.SearchBottomSheetView
import org.hamcrest.Matcher

class SearchViewExpandAction : ViewAction {

    override fun getDescription() = "BottomSheet expand"

    override fun getConstraints(): Matcher<View> {
        return isAssignableFrom(SearchBottomSheetView::class.java)
    }

    override fun perform(uiController: UiController?, view: View) {
        (view as? SearchBottomSheetView)?.expand()
    }
}
