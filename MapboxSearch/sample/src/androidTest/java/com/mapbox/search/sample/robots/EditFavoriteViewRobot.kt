package com.mapbox.search.sample.robots

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.mapbox.search.sample.Constants.SINGLE_ANDROID_ANIMATION_DELAY_MILLIS
import com.mapbox.search.sample.Constants.TEXT_INPUT_DELAY_MILLIS
import com.mapbox.search.sample.R
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf

fun editFavoriteView(block: EditFavoriteViewRobot.() -> Unit) {
    onView(withClassName(`is`("com.mapbox.search.ui.view.favorite.rename.EditFavoriteView")))
        .check(matches(isDisplayed()))
    EditFavoriteViewRobot().apply { block() }
}

@RobotDsl
class EditFavoriteViewRobot {

    fun close() {
        onView(withId(R.id.close_button))
            .perform(click())
        Thread.sleep(SINGLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun done() {
        onView(withId(R.id.done_button))
            .perform(click())
        Thread.sleep(SINGLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun clearNameInput() {
        onView(allOf(withId(R.id.clear_text_button), isDescendantOfA(withId(R.id.favorite_edit_layout))))
            .perform(click())
    }

    fun typeFavoriteName(favoriteName: String, closeKeyboardAfterTyping: Boolean = true) {
        onView(withId(R.id.search_input_edit_text))
            .perform(click())
            .perform(typeText(favoriteName))
        if (closeKeyboardAfterTyping) {
            closeSoftKeyboard()
        }
        Thread.sleep(TEXT_INPUT_DELAY_MILLIS)
    }
}
