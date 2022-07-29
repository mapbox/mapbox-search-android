package com.mapbox.search.base.utils.extension

import com.mapbox.search.base.core.CoreSearchResponseError
import com.mapbox.search.base.core.CoreSearchResponseErrorType
import com.mapbox.search.common.SearchRequestException
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
