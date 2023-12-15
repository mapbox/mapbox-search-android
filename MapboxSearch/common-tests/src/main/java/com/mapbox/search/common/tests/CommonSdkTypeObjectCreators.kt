package com.mapbox.search.common.tests

import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import java.io.IOException
import java.net.URI

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

    internal val URI_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(URI::class) { mode ->
        listOf(
            URI.create("https://api.mapbox.com"),
            URI.create("https://cloudfront-staging.tilestream.net")
        )[mode.ordinal]
    }

    internal val EXCEPTION_CREATOR = CustomTypeObjectCreatorImpl(Exception::class) { mode ->
        listOf(Exception(), IOException())[mode.ordinal]
    }

    val ALL_CREATORS = listOf<CustomTypeObjectCreator>(
        POINT_OBJECT_CREATOR,
        BOUNDING_BOX_OBJECT_CREATOR,
        URI_OBJECT_CREATOR,
        EXCEPTION_CREATOR
    )
}
