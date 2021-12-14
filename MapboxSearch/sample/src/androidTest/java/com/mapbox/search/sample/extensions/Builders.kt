package com.mapbox.search.sample.extensions

import com.mapbox.search.sample.robots.builders.SearchResultsRecyclerBuilder

fun SearchResultsRecyclerBuilder.noInternetConnectionError() {
    error(
        errorTitle = "No internet connection",
        errorSubtitle = "You’re offline. Try to reconnect."
    )
}
