package com.mapbox.search

import com.mapbox.search.result.SearchRequestContext
import com.mapbox.search.utils.KeyboardLocaleProvider
import com.mapbox.search.utils.orientation.ScreenOrientationProvider

internal class SearchRequestContextProvider(
    private val keyboardLocaleProvider: KeyboardLocaleProvider,
    private val orientationProvider: ScreenOrientationProvider
) {
    fun provide(apiType: ApiType): SearchRequestContext {
        return SearchRequestContext(
            apiType = apiType,
            keyboardLocale = keyboardLocaleProvider.provideKeyboardLocale(),
            screenOrientation = orientationProvider.provideOrientation(),
        )
    }
}
