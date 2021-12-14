package com.mapbox.search.ui.view.place

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.common.extension.safeCompareTo
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.IndexableRecordSearchResult
import com.mapbox.search.result.RoutablePoint
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.ui.view.feedback.IncorrectSearchPlaceFeedback
import kotlinx.parcelize.Parcelize

/**
 * Search place UI model to show in [SearchPlaceBottomSheetView].
 */
@Parcelize
public class SearchPlace(

    /**
     * Search place name.
     */
    public val name: String,

    /**
     * Additional description for the search result.
     */
    public val descriptionText: String?,

    /**
     * Search address.
     */
    public val address: SearchAddress?,

    /**
     * List of result's types.
     */
    public val resultTypes: List<SearchResultType>,

    /**
     * IndexableRecord instance for case if search result produced from some user data.
     */
    public val record: IndexableRecord?,

    /**
     * Search place coordinate.
     */
    public val coordinate: Point,

    /**
     * List of points near [coordinate], that can be used for more convenient navigation.
     */
    public val routablePoints: List<RoutablePoint>?,

    /**
     * Search place categories.
     */
    public val categories: List<String>?,

    /**
     * [Maki](https://github.com/mapbox/maki/) icon name for search place.
     */
    public val makiIcon: String?,

    /**
     * Search result metadata containing geo place's detailed information if available.
     */
    public val metadata: SearchResultMetadata?,

    /**
     * Distance in meters to the given search place.
     */
    public val distanceMeters: Double?,

    /**
     * Information about a place required to report a feedback.
     */
    public val feedback: IncorrectSearchPlaceFeedback?,
) : Parcelable {

    /**
     * Creates new [SearchPlace] from current instance.
     */
    @JvmSynthetic
    public fun copy(
        name: String = this.name,
        descriptionText: String? = this.descriptionText,
        address: SearchAddress? = this.address,
        resultTypes: List<SearchResultType> = this.resultTypes,
        record: IndexableRecord? = this.record,
        coordinate: Point = this.coordinate,
        routablePoints: List<RoutablePoint>? = this.routablePoints,
        categories: List<String>? = this.categories,
        makiIcon: String? = this.makiIcon,
        metadata: SearchResultMetadata? = this.metadata,
        distanceMeters: Double? = this.distanceMeters,
        feedback: IncorrectSearchPlaceFeedback? = this.feedback,
    ): SearchPlace {
        return SearchPlace(
            name = name,
            descriptionText = descriptionText,
            address = address,
            resultTypes = resultTypes,
            record = record,
            coordinate = coordinate,
            routablePoints = routablePoints,
            categories = categories,
            makiIcon = makiIcon,
            metadata = metadata,
            distanceMeters = distanceMeters,
            feedback = feedback,
        )
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchPlace

        if (name != other.name) return false
        if (descriptionText != other.descriptionText) return false
        if (address != other.address) return false
        if (resultTypes != other.resultTypes) return false
        if (record != other.record) return false
        if (coordinate != other.coordinate) return false
        if (routablePoints != other.routablePoints) return false
        if (categories != other.categories) return false
        if (makiIcon != other.makiIcon) return false
        if (metadata != other.metadata) return false
        if (!distanceMeters.safeCompareTo(other.distanceMeters)) return false
        if (feedback != other.feedback) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (descriptionText?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + resultTypes.hashCode()
        result = 31 * result + (record?.hashCode() ?: 0)
        result = 31 * result + coordinate.hashCode()
        result = 31 * result + (routablePoints?.hashCode() ?: 0)
        result = 31 * result + (categories?.hashCode() ?: 0)
        result = 31 * result + (makiIcon?.hashCode() ?: 0)
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + (distanceMeters?.hashCode() ?: 0)
        result = 31 * result + (feedback?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchPlace(" +
                "name='$name', " +
                "descriptionText=$descriptionText, " +
                "address=$address, " +
                "resultTypes=$resultTypes, " +
                "record=$record, " +
                "coordinate=$coordinate, " +
                "routablePoints=$routablePoints, " +
                "categories=$categories, " +
                "makiIcon=$makiIcon, " +
                "metadata=$metadata, " +
                "distanceMeters=$distanceMeters," +
                "feedback=$feedback" +
                ")"
    }

    /**
     * @suppress
     */
    public companion object {

        /**
         * Creates search place from search result and geojson point.
         *
         * @param searchResult Search result as base data for place creation.
         * @param responseInfo Search response and request information.
         * @param coordinate Geojson point with place coordinates.
         * @param distanceMeters Distance in meters to the given search place.
         *
         * @return Search place instance
         */
        @JvmStatic
        @JvmOverloads
        public fun createFromSearchResult(
            searchResult: SearchResult,
            responseInfo: ResponseInfo,
            coordinate: Point,
            distanceMeters: Double? = searchResult.distanceMeters
        ): SearchPlace {
            return SearchPlace(
                name = searchResult.name,
                descriptionText = searchResult.descriptionText,
                address = searchResult.address,
                resultTypes = searchResult.types,
                record = (searchResult as? IndexableRecordSearchResult)?.record,
                coordinate = coordinate,
                routablePoints = searchResult.routablePoints,
                categories = searchResult.categories,
                makiIcon = searchResult.makiIcon,
                metadata = searchResult.metadata,
                distanceMeters = distanceMeters,
                feedback = IncorrectSearchPlaceFeedback.SearchResultFeedback(searchResult, responseInfo),
            )
        }

        /**
         * Creates search place from [IndexableRecord] and geojson point.
         *
         * @param record A record describing geo place.
         * @param coordinate Geojson point with place coordinates.
         * @param distanceMeters Distance in meters to the given search place.
         *
         * @return Search place instance
         */
        @JvmStatic
        public fun createFromIndexableRecord(
            record: IndexableRecord,
            coordinate: Point,
            distanceMeters: Double?
        ): SearchPlace {
            val feedback = when (record) {
                is HistoryRecord -> IncorrectSearchPlaceFeedback.HistoryFeedback(record)
                is FavoriteRecord -> IncorrectSearchPlaceFeedback.FavoriteFeedback(record)
                else -> null
            }
            return SearchPlace(
                name = record.name,
                descriptionText = record.descriptionText,
                address = record.address,
                resultTypes = listOf(record.type),
                record = record,
                coordinate = coordinate,
                routablePoints = record.routablePoints,
                categories = record.categories,
                makiIcon = record.makiIcon,
                metadata = record.metadata,
                distanceMeters = distanceMeters,
                feedback = feedback,
            )
        }
    }
}
