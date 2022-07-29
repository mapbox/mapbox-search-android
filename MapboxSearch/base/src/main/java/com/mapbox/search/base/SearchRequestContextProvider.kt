package com.mapbox.search.base

import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.base.utils.KeyboardLocaleProvider
import com.mapbox.search.base.utils.orientation.ScreenOrientationProvider

class SearchRequestContextProvider(
    private val keyboardLocaleProvider: KeyboardLocaleProvider,
    private val orientationProvider: ScreenOrientationProvider
) {
    fun provide(apiType: CoreApiType): SearchRequestContext {
        return SearchRequestContext(
            apiType = apiType,
            keyboardLocale = keyboardLocaleProvider.provideKeyboardLocale(),
            screenOrientation = orientationProvider.provideOrientation(),
        )
    }
}
