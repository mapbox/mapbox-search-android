package com.mapbox.search.ui.utils

import androidx.annotation.UiThread
import com.mapbox.search.ui.view.GlobalViewPreferences
import com.mapbox.search.utils.concurrent.MainThreadWorker
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker
import java.util.concurrent.TimeUnit

@UiThread
internal interface ClickListener<T> : ((T) -> Unit) {
    fun onClick(arg: T)
}

@UiThread
internal abstract class DebounceClickListener<T>(
    private val timeout: Long = GlobalViewPreferences.DEFAULT_CLICK_DEBOUNCE_MILLIS,
    private val unit: TimeUnit = TimeUnit.MILLISECONDS,
    private val mainThreadWorker: MainThreadWorker = SearchSdkMainThreadWorker,
) : ClickListener<T> {

    init {
        require(timeout >= 0)
    }

    private var skipClick: Boolean = false
    private val cleanFlagRunnable = Runnable {
        skipClick = false
    }

    override fun invoke(arg: T) {
        if (skipClick) {
            return
        }
        onClick(arg)
        skipClick = true
        mainThreadWorker.postDelayed(timeout, unit, cleanFlagRunnable)
    }
}
