package com.mapbox.search.sample.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.mapbox.search.sample.Constants.SINGLE_ANDROID_ANIMATION_DELAY_MILLIS
import com.mapbox.search.ui.R

fun favoriteActionsDialog(block: FavoriteActionsDialogRobot.() -> Unit) {
    onView(withId(R.id.favorite_actions_container))
        .check(matches(isDisplayed()))
    FavoriteActionsDialogRobot().apply { block() }
}

@RobotDsl
class FavoriteActionsDialogRobot {

    fun rename() {
        onView(withId(R.id.favorite_rename_layout_action))
            .perform(click())
        Thread.sleep(SINGLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun editLocation() {
        onView(withId(R.id.favorite_edit_location_action))
            .perform(click())
        Thread.sleep(SINGLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun delete() {
        onView(withId(R.id.favorite_delete_action))
            .check(matches(withText(R.string.mapbox_search_sdk_favorite_action_delete)))
            .perform(click())
        Thread.sleep(SINGLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun removeLocation() {
        onView(withId(R.id.favorite_delete_action))
            .check(matches(withText(R.string.mapbox_search_sdk_favorite_action_remove_location)))
            .perform(click())
        Thread.sleep(SINGLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }
}
