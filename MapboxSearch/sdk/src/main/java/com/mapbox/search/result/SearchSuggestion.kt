@file:OptIn(RestrictedMapboxSearchAPI::class)

package com.mapbox.search.result

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.RequestOptions
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.base.RestrictedMapboxSearchAPI
import com.mapbox.search.base.result.BaseIndexableRecordSearchSuggestion
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.result.BaseSearchSuggestionType
import com.mapbox.search.base.utils.extension.mapToPlatform
import com.mapbox.search.base.utils.extension.safeCompareTo
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.mapToPlatform
import com.mapbox.search.record.IndexableRecord
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Collections

/**
 * Autocomplete common suggestion type.
 */
@Parcelize
public class SearchSuggestion internal constructor(
    internal val base: BaseSearchSuggestion,
) : Parcelable {

    /**
     * Unique identifier for suggestion result.
     * Attention: Mapbox backend may change the identifier of the object in the future.
     */
    @IgnoredOnParcel
    public val id: String = base.id

    /**
     * Suggestion name.
     */
    @IgnoredOnParcel
    public val name: String = base.name

    /**
     * Search suggestion coordinate.
     * Coordinates at suggestions step are available for selected customers only.
     * Contact our team, if you're interested in this API.
     */
    @IgnoredOnParcel
    @RestrictedMapboxSearchAPI
    public val coordinate: Point? = base.coordinate

    /**
     * List of points near [coordinate], that represents entries to associated building.
     * Routable points at suggestions step are available for selected customers only.
     * Contact our team, if you're interested in this API.
     */
    @IgnoredOnParcel
    @RestrictedMapboxSearchAPI
    public val routablePoints: List<RoutablePoint>? = base.routablePoints?.map { it.mapToPlatform() }

    /**
     * The feature name, as matched by the search algorithm.
     */
    @IgnoredOnParcel
    public val matchingName: String? = base.matchingName

    /**
     * Suggestion description.
     */
    @IgnoredOnParcel
    public val descriptionText: String? = base.descriptionText

    /**
     * Address, might be null or incomplete.
     */
    @IgnoredOnParcel
    public val address: SearchAddress? = base.address?.mapToPlatform()

    /**
     * Full formatted address.
     */
    @IgnoredOnParcel
    public val fullAddress: String? = base.fullAddress

    /**
     * Request options, that produced this suggestion.
     */
    @IgnoredOnParcel
    public val requestOptions: RequestOptions = base.requestOptions.mapToPlatform()

    /**
     * Distance in meters from result to requested origin.
     */
    @IgnoredOnParcel
    public val distanceMeters: Double? = base.distanceMeters

    /**
     * Poi categories. Always empty for non-POI suggestions.
     * @see type
     */
    @IgnoredOnParcel
    public val categories: List<String>? = base.categories

    /**
     * [Maki](https://github.com/mapbox/maki/) icon name for search suggestion.
     */
    @IgnoredOnParcel
    public val makiIcon: String? = base.makiIcon

    /**
     * Estimated time of arrival (in minutes) based on the specified in the [com.mapbox.search.SearchOptions] origin point and navigation profile.
     */
    @IgnoredOnParcel
    public val etaMinutes: Double? = base.etaMinutes

    /**
     * Search result metadata containing geo place's detailed information if available.
     */
    @IgnoredOnParcel
    public val metadata: SearchResultMetadata? = base.metadata?.let { SearchResultMetadata(it) }

    /**
     * Map of external ids. Returned Map instance is unmodifiable.
     */
    @IgnoredOnParcel
    public val externalIDs: Map<String, String> = Collections.unmodifiableMap(base.externalIDs)

    /**
     * Denotes whether this suggestion can be passed as a parameter to a batch selection method of the [com.mapbox.search.SearchEngine].
     */
    @IgnoredOnParcel
    public val isBatchResolveSupported: Boolean = base.isBatchResolveSupported

    /**
     * Index in response from server.
     */
    @IgnoredOnParcel
    public val serverIndex: Int? = base.serverIndex

    /**
     * Type of the suggestion.
     */
    public val type: SearchSuggestionType
        get() = base.getSearchSuggestionType()

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchSuggestion

        if (id != other.id) return false
        if (name != other.name) return false
        if (coordinate != other.coordinate) return false
        if (routablePoints != other.routablePoints) return false
        if (matchingName != other.matchingName) return false
        if (descriptionText != other.descriptionText) return false
        if (address != other.address) return false
        if (fullAddress != other.fullAddress) return false
        if (requestOptions != other.requestOptions) return false
        if (!distanceMeters.safeCompareTo(other.distanceMeters)) return false
        if (categories != other.categories) return false
        if (makiIcon != other.makiIcon) return false
        if (!etaMinutes.safeCompareTo(other.etaMinutes)) return false
        if (metadata != other.metadata) return false
        if (externalIDs != other.externalIDs) return false
        if (isBatchResolveSupported != other.isBatchResolveSupported) return false
        if (serverIndex != other.serverIndex) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (coordinate?.hashCode() ?: 0)
        result = 31 * result + (routablePoints?.hashCode() ?: 0)
        result = 31 * result + (matchingName?.hashCode() ?: 0)
        result = 31 * result + (descriptionText?.hashCode() ?: 0)
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + (fullAddress?.hashCode() ?: 0)
        result = 31 * result + requestOptions.hashCode()
        result = 31 * result + (distanceMeters?.hashCode() ?: 0)
        result = 31 * result + (categories?.hashCode() ?: 0)
        result = 31 * result + (makiIcon?.hashCode() ?: 0)
        result = 31 * result + (etaMinutes?.hashCode() ?: 0)
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + externalIDs.hashCode()
        result = 31 * result + isBatchResolveSupported.hashCode()
        result = 31 * result + (serverIndex ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchSuggestion(" +
                "id='$id', " +
                "name='$name', " +
                "coordinate=$coordinate, " +
                "routablePoints=$routablePoints, " +
                "matchingName=$matchingName, " +
                "descriptionText=$descriptionText, " +
                "address=$address, " +
                "fullAddress=$fullAddress, " +
                "requestOptions=$requestOptions, " +
                "distanceMeters=$distanceMeters, " +
                "categories=$categories, " +
                "makiIcon=$makiIcon, " +
                "etaMinutes=$etaMinutes, " +
                "metadata=$metadata, " +
                "externalIDs=$externalIDs, " +
                "isBatchResolveSupported=$isBatchResolveSupported, " +
                "serverIndex=$serverIndex, " +
                "type=$type" +
                ")"
    }
}

@JvmSynthetic
internal fun BaseSearchSuggestion.mapToPlatform(): SearchSuggestion {
    return SearchSuggestion(this)
}

internal val SearchSuggestion.isIndexableRecordSuggestion: Boolean
    get() = base is BaseIndexableRecordSearchSuggestion

internal val SearchSuggestion.record: IndexableRecord?
    get() = (base as? BaseIndexableRecordSearchSuggestion)?.record?.sdkResolvedRecord as? IndexableRecord

@JvmSynthetic
internal fun BaseSearchSuggestion.getSearchSuggestionType(): SearchSuggestionType {
    return when (val t = type) {
        is BaseSearchSuggestionType.SearchResultSuggestion -> SearchSuggestionType.SearchResultSuggestion(
            t.types.map { it.mapToPlatform() }
        )
        is BaseSearchSuggestionType.Category -> SearchSuggestionType.Category(t.canonicalName)
        is BaseSearchSuggestionType.Brand -> SearchSuggestionType.Brand(
            brandName = t.brandName,
            brandId = t.brandId
        )
        is BaseSearchSuggestionType.Query -> SearchSuggestionType.Query
        is BaseSearchSuggestionType.IndexableRecordItem -> SearchSuggestionType.IndexableRecordItem(
            t.record.sdkResolvedRecord as IndexableRecord,
            t.dataProviderName,
        )
    }
}
