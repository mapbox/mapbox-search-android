package com.mapbox.search

import java.lang.RuntimeException

/**
 * Exception thrown when an HTTP response was not successful, i.e. if the code is not in [200..300).
 *
 * @property [message] the HTTP status message.
 * @property [code] the HTTP status code.
 * @property [cause] the cause of this exception.
 */
public class SearchRequestException(
    override val message: String,
    public val code: Int,
    override val cause: Exception? = null
) : RuntimeException(message, cause) {

    /**
     * Indicates, whether HTTP code is in [500..600) range.
     */
    public fun isServerError(): Boolean = code / 100 == 5

    /**
     * Indicates, whether HTTP code is in [400..500) range.
     */
    public fun isClientError(): Boolean = code / 100 == 4

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchRequestException(message='$message', code=$code, cause=$cause)"
    }
}
