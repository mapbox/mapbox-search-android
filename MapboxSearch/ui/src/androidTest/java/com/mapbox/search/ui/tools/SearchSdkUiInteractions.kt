package com.mapbox.search.ui.tools

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import com.adevinta.android.barista.assertion.BaristaListAssertions.assertCustomAssertionAtPosition
import com.mapbox.search.ui.Constants.DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS
import com.mapbox.search.ui.Constants.MAX_NETWORK_REQUEST_TIMEOUT_MILLIS
import com.mapbox.search.ui.test.R
import com.mapbox.search.ui.tools.matchers.SearchSdkMatchers.isSearchProgressItem
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf

internal object SearchSdkUiInteractions {

    fun awaitResultsInSearchResultsAdapter() {
        Thread.sleep(DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS)
        waitUntilViewAssertionSuccessful(timeout = MAX_NETWORK_REQUEST_TIMEOUT_MILLIS) {
            assertCustomAssertionAtPosition(
                R.id.search_results_view,
                0,
                viewAssertion = matches(not(isSearchProgressItem()))
            )
        }
        Thread.sleep(DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS)
    }

    fun retryFromError() {
        onView(
            allOf(
                withId(R.id.retry_button),
                withParent(withClassName(`is`("com.mapbox.search.ui.view.common.MapboxSdkUiErrorView"))),
                isDisplayed()
            )
        )
            .perform(click())
    }
}
