package com.mapbox.search.sample.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.mapbox.search.sample.Constants.DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS
import com.mapbox.search.ui.R
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not

fun searchPlaceBottomSheet(block: SearchPlaceBottomSheet.() -> Unit) {
    onView(withClassName(`is`(SearchPlaceBottomSheetView::class.qualifiedName)))
        .check(matches(isDisplayed()))
    SearchPlaceBottomSheet().apply { block() }
}

@RobotDsl
class SearchPlaceBottomSheet {

    fun close() {
        onView(allOf(withId(R.id.card_close_button), withParent(withId(R.id.search_place_card))))
            .perform(click())
        Thread.sleep(DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun verifyPlaceName(name: String) {
        onView(allOf(withId(R.id.search_result_name), withParent(withId(R.id.search_place_card))))
            .check(matches(withText(name)))
            .check(matches(isDisplayed()))
    }

    fun verifyCategoryName(categoryName: String) {
        onView(allOf(withId(R.id.search_result_category), withParent(withId(R.id.search_place_card))))
            .check(matches(withText(categoryName)))
            .check(matches(isDisplayed()))
    }

    fun verifyCategoryNameIsHidden() {
        onView(allOf(withId(R.id.search_result_category), withParent(withId(R.id.search_place_card))))
            .check(matches(not(isDisplayed())))
    }

    fun verifyAddress(address: String) {
        onView(allOf(withId(R.id.search_result_address), withParent(withId(R.id.search_place_card))))
            .check(matches(withText(address)))
            .check(matches(isDisplayed()))
    }

    fun verifyDistance(distance: String) {
        onView(allOf(withId(R.id.search_result_distance), withParent(withId(R.id.search_place_card))))
            .check(matches(withText(distance)))
            .check(matches(isDisplayed()))
    }

    fun verifyAddedToFavoritesButtonIsVisible() {
        onView(allOf(withId(R.id.mapbox_sdk_button_text_view), withParent(withId(R.id.search_result_button_favorite))))
            .check(matches(withText("Added to favorites")))
    }
}
