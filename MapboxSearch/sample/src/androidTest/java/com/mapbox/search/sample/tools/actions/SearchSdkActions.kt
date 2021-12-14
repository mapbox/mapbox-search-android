package com.mapbox.search.sample.tools.actions

import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText

object SearchSdkActions {

    fun customTypeText(text: String): Array<ViewAction> {
        return arrayOf(click(), replaceText(text))
        // TODO(#250):
        // On API 21 typing text "62 Ranelagh Terrace" is not smooth:
        // First system types "6", then waits for several second and types the rest.
        // This causes 2 network requests, which breaks our MockWebServer logic.
        //
        // return typeText(text)
    }
}
