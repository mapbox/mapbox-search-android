package com.mapbox.search.sample.tools.actions

import android.view.View
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.material.tabs.TabLayout
import org.hamcrest.Matcher

class TabLayoutTabClickAction(private val tabIndex: Int) : ViewAction {

    override fun getDescription() = "Tab click by index $tabIndex"

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(TabLayout::class.java)
    }

    override fun perform(uiController: UiController?, view: View) {
        val tabLayout = view as TabLayout
        val tabAtIndex: TabLayout.Tab = tabLayout.getTabAt(tabIndex)
            ?: throw PerformException.Builder()
                .withCause(Throwable("No tab at index $tabIndex"))
                .build()

        tabAtIndex.select()
    }
}
