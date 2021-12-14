package com.mapbox.search.ui.view.common

import com.mapbox.search.SearchRequestException
import java.io.IOException
import java.lang.Exception

internal sealed class UiError {

    object NoInternetConnectionError : UiError()

    object ServerError : UiError()

    object ClientError : UiError()

    object UnknownError : UiError()

    companion object {
        fun fromException(e: Exception): UiError = when {
            // TODO(#337):Should check whether there is problem with connection
            //  indeed with more detailed checks
            e is IOException -> NoInternetConnectionError
            e is SearchRequestException && e.isServerError() -> ServerError
            e is SearchRequestException && e.isClientError() -> ClientError
            else -> UnknownError
        }
    }
}
