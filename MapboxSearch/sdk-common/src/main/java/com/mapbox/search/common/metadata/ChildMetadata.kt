package com.mapbox.search.common.metadata

import android.os.Parcelable
import com.mapbox.geojson.Point
import kotlinx.parcelize.Parcelize

/**
 * TODO
 *
 * @property mapboxId TODO description goes here.
 * @property name TODO description goes here.
 * @property category TODO description goes here.
 * @property coordinates TODO description goes here.
 */
@Parcelize
public class ChildMetadata(
    public val mapboxId: String?,
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
}
