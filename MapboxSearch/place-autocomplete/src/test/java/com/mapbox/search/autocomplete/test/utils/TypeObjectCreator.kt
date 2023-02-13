package com.mapbox.search.autocomplete.test.utils

import com.mapbox.search.common.metadata.OpenHours
import com.mapbox.search.common.metadata.OpenPeriod
import com.mapbox.search.common.metadata.WeekDay
import com.mapbox.search.common.metadata.WeekTimestamp
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl

internal object TypeObjectCreator {

    val OPEN_HOURS_CREATOR = CustomTypeObjectCreatorImpl(OpenHours::class) { mode ->
        listOf(
            OpenHours.AlwaysOpen,
            OpenHours.Scheduled(
                listOf(
                    OpenPeriod(
                        open = WeekTimestamp(WeekDay.MONDAY, 9, 0),
                        closed = WeekTimestamp(WeekDay.MONDAY, 17, 0)
                    )
                )
            )
        )[mode.ordinal]
    }
}
