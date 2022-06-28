package com.mapbox.search.ui.tests_support

import com.mapbox.geojson.Point
import com.mapbox.search.common.tests.CustomTypeObjectCreator
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import com.mapbox.search.metadata.OpenHours
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.ui.view.place.IncorrectSearchPlaceFeedback

internal object UiCustomTypeObjectCreators {

    private val POINT_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(Point::class) { mode ->
        listOf(
            Point.fromLngLat(27.0, 52.0),
            Point.fromLngLat(-17.0, 23.0),
        )[mode.ordinal]
    }

    private val OPEN_HOURS_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(OpenHours::class) { mode ->
        listOf(
            OpenHours.AlwaysOpen,
            OpenHours.PermanentlyClosed,
        )[mode.ordinal]
    }

    private val INCORRECT_SEARCH_PLACE_FEEDBACK_CREATOR = CustomTypeObjectCreatorImpl(IncorrectSearchPlaceFeedback::class) { mode ->
        listOf(
            IncorrectSearchPlaceFeedback.HistoryFeedback(createTestHistoryRecord()),
            IncorrectSearchPlaceFeedback.FavoriteFeedback(createTestFavoriteRecord()),
        )[mode.ordinal]
    }

    val ALL_CREATORS = listOf<CustomTypeObjectCreator>(
        POINT_OBJECT_CREATOR,
        OPEN_HOURS_OBJECT_CREATOR,
        INCORRECT_SEARCH_PLACE_FEEDBACK_CREATOR
    )

    // TODO(#737): use these helper functions from the SDK module
    private fun createTestFavoriteRecord(): FavoriteRecord = FavoriteRecord(
        id = "test id",
        name = "test name",
        coordinate = Point.fromLngLat(.0, .1),
        descriptionText = null,
        address = null,
        type = SearchResultType.POI,
        makiIcon = null,
        categories = null,
        routablePoints = null,
        metadata = null,
    )

    private fun createTestHistoryRecord(): HistoryRecord = HistoryRecord(
        id = "test_history_record_id",
        name = "Test history record",
        coordinate = Point.fromLngLat(10.0, 20.0),
        descriptionText = null,
        address = null,
        timestamp = 123L,
        type = SearchResultType.POI,
        routablePoints = null,
        metadata = null,
        makiIcon = null,
        categories = null,
    )
}
