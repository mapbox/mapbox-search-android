package com.mapbox.search.record

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResultType
import kotlinx.parcelize.Parcelize

/**
 * History indexable record.
 * @see IndexableRecord
 */
@Parcelize
public class HistoryRecord(
    override val id: String,
    override val name: String,
    override val descriptionText: String?,
    override val address: SearchAddress?,
    override val routablePoints: List<RoutablePoint>?,
    override val categories: List<String>?,
    override val makiIcon: String?,
    override val coordinate: Point?,
    override val type: SearchResultType,
    override val metadata: SearchResultMetadata?,

    /**
     * History item creation time.
     * @see System.currentTimeMillis
     */
    public val timestamp: Long
) : IndexableRecord, Parcelable {

    // Fow now only resolved search result can be added to the search history.
    @get:JvmSynthetic
    internal val historyType: HistoryType
        get() = HistoryType.RESULT

    override val indexTokens: List<String>
        get() = listOfNotNull(address?.place, address?.street, address?.houseNumber)

    /**
     * Creates new [HistoryRecord] from current instance.
     */
    @JvmSynthetic
    public fun copy(
        id: String = this.id,
        name: String = this.name,
        descriptionText: String? = this.descriptionText,
        address: SearchAddress? = this.address,
        routablePoints: List<RoutablePoint>? = this.routablePoints,
        categories: List<String>? = this.categories,
        makiIcon: String? = this.makiIcon,
        coordinate: Point? = this.coordinate,
        type: SearchResultType = this.type,
        metadata: SearchResultMetadata? = this.metadata,
        timestamp: Long = this.timestamp,
    ): HistoryRecord {
        return HistoryRecord(
            id = id,
            name = name,
            descriptionText = descriptionText,
            address = address,
            routablePoints = routablePoints,
            categories = categories,
            makiIcon = makiIcon,
            coordinate = coordinate,
            type = type,
            metadata = metadata,
            timestamp = timestamp,
        )
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HistoryRecord

        if (id != other.id) return false
        if (name != other.name) return false
        if (descriptionText != other.descriptionText) return false
        if (address != other.address) return false
        if (routablePoints != other.routablePoints) return false
        if (categories != other.categories) return false
        if (makiIcon != other.makiIcon) return false
        if (coordinate != other.coordinate) return false
        if (type != other.type) return false
        if (metadata != other.metadata) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (descriptionText?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + (routablePoints?.hashCode() ?: 0)
        result = 31 * result + (categories?.hashCode() ?: 0)
        result = 31 * result + (makiIcon?.hashCode() ?: 0)
        result = 31 * result + (coordinate?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "HistoryRecord(" +
                "id='$id', " +
                "name='$name', " +
                "descriptionText=$descriptionText, " +
                "address=$address, " +
                "routablePoints=$routablePoints, " +
                "categories=$categories, " +
                "makiIcon=$makiIcon, " +
                "coordinate=$coordinate, " +
                "type=$type, " +
                "metadata=$metadata, " +
                "timestamp=$timestamp" +
                ")"
    }

    internal enum class HistoryType {
        RESULT,
        QUERY,
        CATEGORY
    }
}
