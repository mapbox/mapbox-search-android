package com.mapbox.search.analytics.events

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class SearchResultEntry(
    @SerializedName("name") var name: String? = null,
    @SerializedName("address") var address: String? = null,
    @SerializedName("coordinates") var coordinates: DoubleArray? = null,
    @SerializedName("id") var id: String? = null,
    @SerializedName("language") var language: List<String>? = null,
    @SerializedName("result_type") var types: List<String>? = null,
    @SerializedName("external_ids") var externalIDs: Map<String, String>? = null,
    @SerializedName("category") var category: List<String>? = null,
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchResultEntry

        if (name != other.name) return false
        if (address != other.address) return false
        if (!coordinates.contentEquals(other.coordinates)) return false
        if (id != other.id) return false
        if (language != other.language) return false
        if (types != other.types) return false
        if (externalIDs != other.externalIDs) return false
        if (category != other.category) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + (coordinates?.contentHashCode() ?: 0)
        result = 31 * result + (id?.hashCode() ?: 0)
        result = 31 * result + (language?.hashCode() ?: 0)
        result = 31 * result + (types?.hashCode() ?: 0)
        result = 31 * result + (externalIDs?.hashCode() ?: 0)
        result = 31 * result + (category?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "SearchResultEntry(" +
                "name=$name, " +
                "address=$address, " +
                "coordinates=${coordinates.contentToString()}, " +
                "id=$id, " +
                "language=$language, " +
                "types=$types, " +
                "externalIDs=$externalIDs, " +
                "category=$category" +
                ")"
    }
}
