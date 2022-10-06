package com.mapbox.search.ui.view

import com.mapbox.search.common.SearchRequestException
import java.io.IOException
import java.lang.Exception

/**
 * A type that represents an error that can happen during a search request.
 * Used to render a title that can help a user to understand what has happened during the request.
 */
public abstract class UiError {

    /**
     * Error that happened because of internet connection issue.
     */
    public object NoInternetConnectionError : UiError()

    /**
     * Error that happened because of the server's issue, for example, 500 http code or response format that could not be parsed.
     */
    public object ServerError : UiError()

    /**
     * Error that happened because of the client's issue, for example, incorrect search parameters.
     */
    public object ClientError : UiError()

    /**
     * Any other error.
     */
    public object UnknownError : UiError()

    /**
     * Companion object.
     */
    public companion object {

        /**
         * Factory function that helps to determine [UiError] type from the given [Exception].
         * @param e The [Exception] from which [UiError] should be constructed.
         * @return [UiError] type that represents the given [Exception].
         */
        @JvmStatic
        public fun createFromException(e: Exception): UiError = when {
            // TODO(#337):Should check whether there is problem with connection
            //  indeed with more detailed checks
            e is IOException -> NoInternetConnectionError
            e is SearchRequestException && e.isServerError() -> ServerError
            e is SearchRequestException && e.isClientError() -> ClientError
            else -> UnknownError
        }
    }
}
