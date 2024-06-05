package com.mapbox.search.base.utils

import java.lang.RuntimeException

/**
 * Exception thrown when internal error happens,
 * but should be ignored because the cause issue is known and awaiting for a fix.
 */
internal data class InternalIgnorableException(
    override val message: String,
    override val cause: Exception? = null
) : RuntimeException(message, cause)
