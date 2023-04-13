package com.mapbox.search.autocomplete.test.utils

import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.mapbox.search.autocomplete.PlaceAutocompleteType
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

    val PLACE_TYPE_CREATOR = CustomTypeObjectCreatorImpl(PlaceAutocompleteType::class) { mode ->
        listOf(
            PlaceAutocompleteType.Poi,
            PlaceAutocompleteType.AdministrativeUnit.Address
        )[mode.ordinal]
    }

    val SUGGESTION_UNDERLYING_CREATOR = CustomTypeObjectCreatorImpl(PlaceAutocompleteSuggestion.Underlying::class) { mode ->
        listOf(
            PlaceAutocompleteSuggestion.Underlying.Suggestion(createTestBaseSearchSuggestion()),
            PlaceAutocompleteSuggestion.Underlying.Result(testBaseResult)
        )[mode.ordinal]
    }
}
