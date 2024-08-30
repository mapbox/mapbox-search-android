package com.mapbox.search.common.metadata

import android.os.Parcelable
import com.mapbox.geojson.Point
import kotlinx.parcelize.Parcelize
import java.util.Objects

/**
 * Metadata for children (ie. sub destinations) for POIs.
 *
 * @property mapboxId Mapbox stable identifier
 * @property name of the child POI
 * @property category id of the POI
 * @property coordinates of the POI
 */
@Parcelize
public class ChildMetadata(
    public val mapboxId: String,
    public val name: String?,
    public val category: String?,
    public val coordinates: Point?
) : Parcelable {

    /**
     * @suppress
     */
    override fun toString(): String {
        return "ChildMetadata(" +
                "mapboxId=$mapboxId, " +
                "name=$name, " +
                "category=$category" +
                "coordinates=$coordinates" +
                ")"
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChildMetadata) return false

        if (mapboxId != other.mapboxId) return false
        if (name != other.name) return false
        if (category != other.category) return false
        if (coordinates != other.coordinates) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int = Objects.hash(mapboxId, name, category, coordinates)
}
