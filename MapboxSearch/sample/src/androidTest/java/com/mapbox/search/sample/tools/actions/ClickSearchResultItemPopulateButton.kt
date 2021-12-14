package com.mapbox.search.sample.tools.actions

import android.view.View
import android.widget.ImageView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.mapbox.search.sample.R
import org.hamcrest.Matcher

class ClickSearchResultItemPopulateButton : ViewAction {

    override fun getDescription() = "Search result item populate button click"

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.withId(R.id.search_result_item)
    }

    override fun perform(uiController: UiController?, view: View) {
        view.findViewById<ImageView>(R.id.result_populate)?.callOnClick()
    }
}
