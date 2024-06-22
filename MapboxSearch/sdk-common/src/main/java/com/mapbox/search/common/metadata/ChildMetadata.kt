package com.mapbox.search.common.metadata

import android.os.Parcelable
import com.mapbox.geojson.Point
import kotlinx.parcelize.Parcelize

@Parcelize
public class ChildMetadata(
    public val mapboxId: String,
    public val name: String,
    public val category: String,
    public val coordinates: Point
) : Parcelable {

    override fun toString(): String {
        return "ChildMetadata(\n  mapboxId='$mapboxId',\n" +
                "  name='$name', \n" +
                "  category: '$category',\n" +
                "  coordinates: '$coordinates'\n" +
                ")"
    }
}
