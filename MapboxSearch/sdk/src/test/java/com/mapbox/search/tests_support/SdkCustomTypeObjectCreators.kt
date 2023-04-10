package com.mapbox.search.tests_support

import com.mapbox.geojson.Point
import com.mapbox.search.RouteOptions
import com.mapbox.search.base.core.CoreSearchResponse
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.common.metadata.OpenHours
import com.mapbox.search.common.metadata.WeekDay
import com.mapbox.search.common.metadata.WeekTimestamp
import com.mapbox.search.common.tests.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.CustomTypeObjectCreator
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import com.mapbox.search.common.tests.createTestCoreSearchResponseSuccess
import com.mapbox.search.record.IndexableRecord
import java.util.concurrent.TimeUnit

internal object SdkCustomTypeObjectCreators {

    internal val ROUTE_OPTIONS_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(RouteOptions::class) { mode ->
        listOf(
            RouteOptions(
                route = listOf(
                    Point.fromLngLat(27.0, 52.0),
                    Point.fromLngLat(28.0, 53.0),
                ),
                deviation = RouteOptions.Deviation.Time(5, TimeUnit.SECONDS)
            ),
            RouteOptions(
                route = listOf(
                    Point.fromLngLat(-17.0, 23.0),
                    Point.fromLngLat(-18.0, 22.0),
                ),
                deviation = RouteOptions.Deviation.Time(15, TimeUnit.MINUTES)
            ),
        )[mode.ordinal]
    }

    internal val WEEK_TIMESTAMP_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(WeekTimestamp::class) { mode ->
        listOf(
            WeekTimestamp(day = WeekDay.MONDAY, hour = 13, minute = 15),
            WeekTimestamp(day = WeekDay.TUESDAY, hour = 17, minute = 30),
        )[mode.ordinal]
    }

    internal val OPEN_HOURS_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(OpenHours::class) { mode ->
        listOf(
            OpenHours.AlwaysOpen,
            OpenHours.PermanentlyClosed,
        )[mode.ordinal]
    }

    internal val CORE_SEARCH_RESPONSE_CREATOR = CustomTypeObjectCreatorImpl(CoreSearchResponse::class) { mode ->
        listOf(
            createTestCoreSearchResponseSuccess(responseUUID = "test-response-uuid-1"),
            createTestCoreSearchResponseSuccess(responseUUID = "test-response-uuid-2"),
        )[mode.ordinal]
    }

    internal val BASE_RAW_SEARCH_RESULT_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(BaseRawSearchResult::class) { mode ->
        listOf(
            createTestBaseRawSearchResult(id = "test-result-1"),
            createTestBaseRawSearchResult(id = "test-result-2"),
        )[mode.ordinal]
    }

    internal val INDEXABLE_RECORD_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(IndexableRecord::class) { mode ->
        listOf(
            StubIndexableRecord(id = "test-indexable-record-1"),
            StubIndexableRecord(id = "test-indexable-record-2"),
        )[mode.ordinal]
    }

    internal val ALL_CREATORS = listOf<CustomTypeObjectCreator>(
        ROUTE_OPTIONS_OBJECT_CREATOR,
        WEEK_TIMESTAMP_OBJECT_CREATOR,
        OPEN_HOURS_OBJECT_CREATOR,
        CORE_SEARCH_RESPONSE_CREATOR,
        BASE_RAW_SEARCH_RESULT_OBJECT_CREATOR,
        INDEXABLE_RECORD_OBJECT_CREATOR,
    ) + CommonSdkTypeObjectCreators.ALL_CREATORS
}
