package com.mapbox.search

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.mapbox.geojson.Point
import java.lang.reflect.Type

public class ChildMetadata(
    @SerializedName("mapbox_id") public val mapboxId: String,
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

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        TODO("Not yet implemented")
    }

    public class PointDeserializer : JsonDeserializer<Point> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Point {
            val jsonObject = json.asJsonObject
            val lng = jsonObject.get("longitude").asDouble
            val lat = jsonObject.get("latitude").asDouble
            return Point.fromLngLat(lng, lat)
        }
    }
}