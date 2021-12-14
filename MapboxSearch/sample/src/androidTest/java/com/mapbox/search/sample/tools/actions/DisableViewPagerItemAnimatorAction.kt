package com.mapbox.search.sample.tools.actions

import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.viewpager2.widget.ViewPager2
import org.hamcrest.Matcher

class DisableViewPagerItemAnimatorAction : ViewAction {

    override fun getConstraints(): Matcher<View> = isAssignableFrom(ViewPager2::class.java)

    override fun getDescription(): String = "Disable ItemAnimator on nested Recycler views Action"

    override fun perform(uiController: UiController?, view: View) {
        view as ViewPager2

        view.children.find { it is RecyclerView }?.let {
            it as RecyclerView
            it.itemAnimator = null
        }
    }
}
