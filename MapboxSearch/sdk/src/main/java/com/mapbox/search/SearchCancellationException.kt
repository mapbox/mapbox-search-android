package com.mapbox.search

import java.lang.RuntimeException

/**
 * Exception thrown when a search request was automatically cancelled by the Search SDK.
 * @property [message] cancellation reason.
 */
public class SearchCancellationException(
    override val message: String
) : RuntimeException(message) {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchCancellationException

        if (message != other.message) return false
        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return message.hashCode()
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchCancellationException(message='$message')"
    }
}
