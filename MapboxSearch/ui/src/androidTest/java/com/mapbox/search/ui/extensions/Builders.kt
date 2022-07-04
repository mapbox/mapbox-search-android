package com.mapbox.search.ui.extensions

import com.mapbox.search.ui.robots.builders.SearchResultsRecyclerBuilder

internal fun SearchResultsRecyclerBuilder.noInternetConnectionError() {
    error(
        errorTitle = "No internet connection",
        errorSubtitle = "You’re offline. Try to reconnect."
    )
}

internal fun SearchResultsRecyclerBuilder.unknownError() {
    error(
        errorTitle = "Error",
        errorSubtitle = "Something went wrong."
    )
}
