package com.mapbox.search.sample.tools

import android.util.Log
import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.mapbox.search.sample.tools.actions.DisableViewPagerItemAnimatorAction
import com.mapbox.search.ui.R
import junit.framework.AssertionFailedError

private const val CYCLE_STEP_TIMEOUT_MILLIS = 50L
private const val DEFAULT_TIMEOUT_MILLIS = 10_000L

private const val TAG = "CommonFunctions"

fun waitUntilViewAssertionSuccessful(timeout: Long = DEFAULT_TIMEOUT_MILLIS, action: () -> Unit) {
    var currentValue = timeout
    while (currentValue > 0) {
        try {
            action()
            return
        } catch (e: AssertionFailedError) {
            // Retry if assertion failed
        } catch (e: NoMatchingViewException) {
            // Retry if view wasn't found
        }
        Thread.sleep(CYCLE_STEP_TIMEOUT_MILLIS)
        currentValue -= CYCLE_STEP_TIMEOUT_MILLIS
    }
}

fun waitAtLeastOneInstanceOfViewInHierarchy(@IdRes viewId: Int, timeout: Long = DEFAULT_TIMEOUT_MILLIS) {
    var currentValue = timeout
    while (currentValue > 0) {
        try {
            onView(withId(viewId))
                .check(doesNotExist())
        } catch (e: Throwable) {
            Log.e(TAG, "Error during awaiting view(id = $viewId) appearing in view hierarchy", e)
            return
        }
        Thread.sleep(CYCLE_STEP_TIMEOUT_MILLIS)
        currentValue -= CYCLE_STEP_TIMEOUT_MILLIS
    }
}

fun disableItemAnimatorOnViewPagerNestedRecyclers(@IdRes viewPagerIdRes: Int = R.id.tab_view_viewpager) {
    onView(withId(viewPagerIdRes))
        .perform(DisableViewPagerItemAnimatorAction())
}
