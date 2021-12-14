package com.mapbox.search.ui.utils.extenstion

import androidx.annotation.DrawableRes
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.mapbox.search.ui.utils.SearchSdkVerticalChangeHandler

internal fun RouterTransaction.withVerticalAnimation(
    @DrawableRes animationBackgroundRes: Int? = null
): RouterTransaction {
    val verticalChangeHandler = SearchSdkVerticalChangeHandler(animationBackgroundRes)
    return pushChangeHandler(verticalChangeHandler).popChangeHandler(verticalChangeHandler)
}

internal fun RouterTransaction.withHorizontalAnimation(): RouterTransaction {
    val changeHandler = HorizontalChangeHandler()
    return pushChangeHandler(changeHandler).popChangeHandler(changeHandler)
}
