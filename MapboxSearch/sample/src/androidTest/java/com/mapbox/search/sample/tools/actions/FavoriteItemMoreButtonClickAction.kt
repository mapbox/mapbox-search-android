package com.mapbox.search.sample.tools.actions

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.mapbox.search.sample.R
import org.hamcrest.Matcher

class FavoriteItemMoreButtonClickAction : ViewAction {

    override fun getDescription() = "More button click"

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(ViewGroup::class.java)
    }

    override fun perform(uiController: UiController?, view: View) {
        view.findViewById<ImageView>(R.id.favourite_more_actions)?.callOnClick()
    }
}
