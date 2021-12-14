package com.mapbox.search.sample.tools.matchers

import android.content.res.Resources.NotFoundException
import android.util.Log
import android.view.View
import androidx.annotation.DimenRes
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

fun withElevation(@DimenRes elevationIdRes: Int) = ElevationMatcher(elevationIdRes)

class ElevationMatcher(@DimenRes private val elevationIdRes: Int) : TypeSafeMatcher<View>() {

    private lateinit var resourceName: String
    private var resourceValue: Int = -1

    override fun describeTo(description: Description) {
        if (::resourceName.isInitialized) {
            description.appendText("with elevation from resource id ")
            description.appendValue(resourceName)
            description.appendText(" with resource value ")
            description.appendValue(resourceValue)
        }
    }

    override fun matchesSafely(item: View): Boolean {
        try {
            resourceName = item.resources.getResourceName(elevationIdRes)
        } catch (exception: NotFoundException) {
            // View could be from a context unaware of the resource id
            Log.e(TAG, "Wasn't able to resolve resource name for provided \"elevationIdRes\"", exception)
        }

        resourceValue = item.resources.getDimensionPixelSize(elevationIdRes)

        Log.d(TAG, "Comparing ${item.elevation} with $resourceValue")
        return item.elevation.toInt() == resourceValue
    }

    private companion object {
        const val TAG = "ElevationMatcher"
    }
}
