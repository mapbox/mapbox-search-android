package com.mapbox.search.autocomplete

import android.os.Parcelable
import com.mapbox.geojson.BoundingBox
import kotlinx.parcelize.Parcelize

/**
 * Text query used for the Place Autocomplete forward geocoding.
 */
@Parcelize
public class TextQuery internal constructor(

    /**
     * Query text.
     */
    public val query: String,

    /**
     * Limit results to only those contained within the supplied bounding box.
     */
    public val boundingBox: BoundingBox?,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextQuery

        if (query != other.query) return false
        if (boundingBox != other.boundingBox) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = query.hashCode()
        result = 31 * result + (boundingBox?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "TextQuery(query='$query', boundingBox=$boundingBox)"
    }

    /**
     * Companion object.
     */
    public companion object {

        /**
         * Creates [TextQuery] instance.
         * @param query Query text.
         * @param boundingBox Limit results to only those contained within the supplied bounding box.
         * @return [TextQuery] instance.
         */
        @JvmStatic
        @JvmOverloads
        public fun create(query: String, boundingBox: BoundingBox? = null): TextQuery {
            return TextQuery(query, boundingBox)
        }
    }
}
