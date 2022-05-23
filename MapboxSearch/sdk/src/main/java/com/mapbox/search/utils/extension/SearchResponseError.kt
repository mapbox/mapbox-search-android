package com.mapbox.search.utils.extension

import com.mapbox.search.SearchRequestException
import com.mapbox.search.internal.bindgen.SearchResponseError
import java.io.IOException

// Temporary solution for https://github.com/mapbox/mapbox-search-sdk/issues/857
internal fun SearchResponseError.toPlatformHttpException(): Exception {
    check(typeInfo == SearchResponseError.Type.HTTP_ERROR)
    return if (httpError.httpCode >= 200) {
        SearchRequestException(
            message = httpError.message,
            code = httpError.httpCode
        )
    } else {
        IOException(httpError.message)
    }
}
