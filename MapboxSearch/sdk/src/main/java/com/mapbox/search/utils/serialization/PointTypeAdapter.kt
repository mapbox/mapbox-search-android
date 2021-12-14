package com.mapbox.search.utils.serialization

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.mapbox.geojson.Point

internal class PointTypeAdapter : TypeAdapter<Point>() {

    override fun write(writer: JsonWriter, value: Point?) {
        checkNotNull(value)
        writer.writeArray {
            value(value.longitude())
            value(value.latitude())
        }
    }

    override fun read(reader: JsonReader): Point? {
        return reader.readFromArray {
            Point.fromLngLat(nextDouble(), nextDouble())
        }
    }

    private companion object {

        fun JsonWriter.writeArray(writer: JsonWriter.() -> Unit) {
            beginArray()
            writer()
            endArray()
        }

        fun <T> JsonReader.readFromArray(reader: JsonReader.() -> T): T {
            beginArray()
            return reader().also {
                endArray()
            }
        }
    }
}
