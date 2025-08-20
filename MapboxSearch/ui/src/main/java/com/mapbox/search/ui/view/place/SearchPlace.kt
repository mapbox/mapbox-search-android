@file:Suppress("DEPRECATION")

package com.mapbox.search.ui.view.place

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.autocomplete.PlaceAutocompleteResult
import com.mapbox.search.base.utils.extension.safeCompareTo
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.internal.newSearchResultTypeToFromOld
import com.mapbox.search.internal.newSearchResultTypeToOld
import com.mapbox.search.offline.OfflineSearchResult
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.NewSearchResultType
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.ui.utils.extenstion.toNewSearchResultType
import com.mapbox.search.ui.utils.extenstion.toSearchAddress
import com.mapbox.search.ui.utils.offline.createNewSearchResultTypeFromOfflineType
import com.mapbox.search.ui.utils.offline.mapToSdkSearchResultType
import kotlinx.parcelize.Parcelize
import java.util.UUID

/**
 * Search place UI model to show in [SearchPlaceBottomSheetView].
 */
@Parcelize
public class SearchPlace
@Deprecated("Use constructor that don't accept resultTypes")
@JvmOverloads constructor(

    /**
     * Search place id.
     */
    public val id: String,

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
     *
     * Deprecated, use [newTypes] to identify the actual type of this [SearchPlace].
     */
    @Deprecated(
        message = "This property is deprecated and should be replaced by newTypes",
        replaceWith = ReplaceWith("newTypes"),
    )
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

    /**
     * Non-empty list of [NewSearchResultType.Type] values.
     */
    public val newTypes: List<String> = resultTypes.map {
        newSearchResultTypeToFromOld(it)
    },
) : Parcelable {

    /**
     * Secondary constructor that accepts [newTypes] instead of the deprecated [resultTypes].
     */
    public constructor(
        id: String,
        name: String,
        descriptionText: String?,
        address: SearchAddress?,
        record: IndexableRecord?,
        coordinate: Point,
        routablePoints: List<RoutablePoint>?,
        categories: List<String>?,
        makiIcon: String?,
        metadata: SearchResultMetadata?,
        distanceMeters: Double?,
        feedback: IncorrectSearchPlaceFeedback?,
        newTypes: List<String>,
    ) : this(
        id = id,
        name = name,
        descriptionText = descriptionText,
        address = address,
        resultTypes = newTypes.map { newSearchResultTypeToOld(it) },
        record = record,
        coordinate = coordinate,
        routablePoints = routablePoints,
        categories = categories,
        makiIcon = makiIcon,
        metadata = metadata,
        distanceMeters = distanceMeters,
        feedback = feedback,
        newTypes = newTypes,
    )

    /**
     * Creates new [SearchPlace] from current instance.
     */
    @JvmSynthetic
    public fun copy(
        id: String = this.id,
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
        newTypes: List<String> = this.newTypes,
    ): SearchPlace {
        return SearchPlace(
            id = id,
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
            newTypes = newTypes,
        )
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchPlace

        if (id != other.id) return false
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
        if (newTypes != other.newTypes) return false

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
        result = 31 * result + resultTypes.hashCode()
        result = 31 * result + (record?.hashCode() ?: 0)
        result = 31 * result + coordinate.hashCode()
        result = 31 * result + (routablePoints?.hashCode() ?: 0)
        result = 31 * result + (categories?.hashCode() ?: 0)
        result = 31 * result + (makiIcon?.hashCode() ?: 0)
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + (distanceMeters?.hashCode() ?: 0)
        result = 31 * result + (feedback?.hashCode() ?: 0)
        result = 31 * result + newTypes.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchPlace(" +
                "id='$id', " +
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
                "feedback=$feedback, " +
                "newTypes=$newTypes" +
                ")"
    }

    /**
     * Companion object.
     */
    public companion object {

        /**
         * Creates a new search place instance from search result and geojson point.
         *
         * @param searchResult Search result as base data for place creation.
         * @param responseInfo Search response and request information.
         * @param distanceMeters Distance in meters to the given search place.
         *
         * @return Search place instance
         */
        @JvmStatic
        @JvmOverloads
        public fun createFromSearchResult(
            searchResult: SearchResult,
            responseInfo: ResponseInfo,
            distanceMeters: Double? = searchResult.distanceMeters
        ): SearchPlace {
            return SearchPlace(
                id = searchResult.id,
                name = searchResult.name,
                descriptionText = searchResult.descriptionText,
                address = searchResult.address,
                record = searchResult.indexableRecord,
                coordinate = searchResult.coordinate,
                routablePoints = searchResult.routablePoints,
                categories = searchResult.categories,
                makiIcon = searchResult.makiIcon,
                metadata = searchResult.metadata,
                distanceMeters = distanceMeters,
                feedback = IncorrectSearchPlaceFeedback.SearchResultFeedback(searchResult, responseInfo),
                newTypes = searchResult.newTypes,
            )
        }

        /**
         * Creates a new search place instance from offline search result.
         *
         * @param searchResult Search result as base data for place creation.
         * @param distanceMeters Distance in meters to the given search place.
         *
         * @return Search place instance
         */
        @JvmStatic
        @JvmOverloads
        public fun createFromOfflineSearchResult(
            searchResult: OfflineSearchResult,
            distanceMeters: Double? = searchResult.distanceMeters
        ): SearchPlace {
            return SearchPlace(
                id = searchResult.id,
                name = searchResult.name,
                descriptionText = searchResult.descriptionText,
                address = searchResult.address?.mapToSdkSearchResultType(),
                record = null,
                coordinate = searchResult.coordinate,
                routablePoints = searchResult.routablePoints,
                categories = null,
                makiIcon = null,
                metadata = null,
                distanceMeters = distanceMeters,
                feedback = null,
                newTypes = listOf(createNewSearchResultTypeFromOfflineType(searchResult.newType))
            )
        }

        /**
         * Creates a new search place instance from [IndexableRecord] and geojson point.
         *
         * @param record A record describing geo place.
         * @param distanceMeters Distance in meters to the given search place.
         *
         * @return Search place instance
         */
        @JvmStatic
        public fun createFromIndexableRecord(
            record: IndexableRecord,
            distanceMeters: Double?
        ): SearchPlace {
            val feedback = when (record) {
                is HistoryRecord -> IncorrectSearchPlaceFeedback.HistoryFeedback(record)
                is FavoriteRecord -> IncorrectSearchPlaceFeedback.FavoriteFeedback(record)
                else -> null
            }
            return SearchPlace(
                id = record.id,
                name = record.name,
                descriptionText = record.descriptionText,
                address = record.address,
                record = record,
                coordinate = record.coordinate,
                routablePoints = record.routablePoints,
                categories = record.categories,
                makiIcon = record.makiIcon,
                metadata = record.metadata,
                distanceMeters = distanceMeters,
                feedback = feedback,
                newTypes = listOf(record.newType),
            )
        }

        /**
         * Creates a new search place instance from [PlaceAutocompleteResult].
         *
         * @param result [PlaceAutocompleteResult].
         * @param distanceMeters Distance in meters to the given search place.
         * By default [PlaceAutocompleteResult.distanceMeters] will be used.
         *
         * @return Search place instance
         */
        @JvmStatic
        @JvmOverloads
        public fun createFromPlaceAutocompleteResult(
            result: PlaceAutocompleteResult,
            distanceMeters: Double? = result.distanceMeters
        ): SearchPlace {
            return SearchPlace(
                id = result.name + UUID.randomUUID().toString(),
                name = result.name,
                address = result.address?.toSearchAddress(),
                coordinate = result.coordinate,
                routablePoints = result.routablePoints,
                makiIcon = result.makiIcon,
                distanceMeters = distanceMeters,
                record = null,
                categories = null,
                descriptionText = null,
                metadata = null,
                feedback = null,
                newTypes = listOf(result.type.toNewSearchResultType()),
            )
        }
    }
}
