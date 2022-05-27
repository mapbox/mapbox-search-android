package com.mapbox.search.common

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.common.tests.CustomTypeObjectCreator
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl

object CommonSdkTypeObjectCreators {

    val POINT_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(Point::class) { mode ->
        listOf(
            Point.fromLngLat(27.0, 52.0),
            Point.fromLngLat(-17.0, 23.0),
        )[mode.ordinal]
    }

    val BOUNDING_BOX_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(BoundingBox::class) { mode ->
        listOf(
            BoundingBox.fromPoints(Point.fromLngLat(1.0, 2.0), Point.fromLngLat(3.0, 4.0)),
            BoundingBox.fromPoints(Point.fromLngLat(5.0, 6.0), Point.fromLngLat(7.0, 8.0)),
        )[mode.ordinal]
    }

    val ALL_CREATORS = listOf<CustomTypeObjectCreator>(
        POINT_OBJECT_CREATOR,
        BOUNDING_BOX_OBJECT_CREATOR,
    )
}
