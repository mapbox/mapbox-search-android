package com.mapbox.search.utils.extension

import com.mapbox.search.SearchRequestException
import com.mapbox.search.core.CoreSearchResponseError
import com.mapbox.search.core.CoreSearchResponseErrorType
import java.io.IOException

// Temporary solution for https://github.com/mapbox/mapbox-search-sdk/issues/857
internal fun CoreSearchResponseError.toPlatformHttpException(): Exception {
    check(typeInfo == CoreSearchResponseErrorType.HTTP_ERROR)
    return if (httpError.httpCode >= 200) {
        SearchRequestException(
            message = httpError.message,
            code = httpError.httpCode
        )
    } else {
        IOException(httpError.message)
    }
}
