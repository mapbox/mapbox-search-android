package com.mapbox.search.base.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun interface FormattedTimeProvider {
    fun currentTimeIso8601Formatted(): String
}

class FormattedTimeProviderImpl(
    private val timeProvider: TimeProvider
) : FormattedTimeProvider {

    override fun currentTimeIso8601Formatted(): String {
        val currentTime = timeProvider.currentTimeMillis()
        return ISO_8601_DATE_FORMATTER.format(Date(currentTime))
    }

    private companion object {
        val ISO_8601_DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
    }
}
