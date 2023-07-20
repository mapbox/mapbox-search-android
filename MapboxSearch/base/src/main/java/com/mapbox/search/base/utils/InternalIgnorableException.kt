package com.mapbox.search.base.utils

import com.mapbox.search.common.SearchRequestException
import java.lang.RuntimeException

/**
 * Exception thrown when internal error happens,
 * but should be ignored because the cause issue is known and awaiting for a fix.
 */
internal class InternalIgnorableException(
    override val message: String,
    override val cause: Exception? = null
) : RuntimeException(message, cause) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchRequestException

        if (message != other.message) return false
        if (cause != other.cause) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + (cause?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "InternalIgnorableException(message='$message', cause=$cause)"
    }
}
