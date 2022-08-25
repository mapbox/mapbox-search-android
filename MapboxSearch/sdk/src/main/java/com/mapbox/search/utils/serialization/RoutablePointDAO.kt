package com.mapbox.search.utils.serialization

import com.google.gson.annotations.SerializedName
import com.mapbox.geojson.Point
import com.mapbox.search.common.RoutablePoint

internal data class RoutablePointDAO(
    @SerializedName("point") val point: Point? = null,
    @SerializedName("name") val name: String? = null
) : DataAccessObject<RoutablePoint> {

    override val isValid: Boolean
        get() = point != null && name != null

    override fun createData(): RoutablePoint {
        return RoutablePoint(
            point = point!!,
            name = name!!
        )
    }

    companion object {

        fun create(routablePoint: RoutablePoint?): RoutablePointDAO? {
            routablePoint ?: return null
            return with(routablePoint) {
                RoutablePointDAO(
                    point = point,
                    name = name
                )
            }
        }
    }
}
