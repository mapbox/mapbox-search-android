package com.mapbox.search.ui.utils.extenstion

import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler

internal fun Router.popToRootImmediately() {
    popToRoot(SimpleSwapChangeHandler())
}

internal fun Router.resetControllers() {
    popToRoot()
    while (backstackSize > 0) {
        popCurrentController()
    }
}

internal inline fun <reified T> Router.findControllerByTag(tag: String): T? {
    return getControllerWithTag(tag) as? T
}
