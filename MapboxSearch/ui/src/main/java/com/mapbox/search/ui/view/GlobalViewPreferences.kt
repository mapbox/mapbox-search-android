package com.mapbox.search.ui.view

import com.mapbox.search.SearchOptions

internal object GlobalViewPreferences {

    const val DEFAULT_CLICK_DEBOUNCE_MILLIS = 300L
    const val DEFAULT_REQUESTS_DEBOUNCE_MILLIS = 300
    const val SEARCH_LIMIT = 10

    val DEFAULT_SEARCH_OPTIONS = SearchOptions(
        limit = SEARCH_LIMIT,
        requestDebounce = DEFAULT_REQUESTS_DEBOUNCE_MILLIS
    )
}
