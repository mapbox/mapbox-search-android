package com.mapbox.search.result

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.common.assertDebug
import com.mapbox.search.core.CoreSearchResult
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.HashMap

@Parcelize
internal data class OriginalSearchResult(
    val id: String,
    val types: List<OriginalResultType>,
    val names: List<String>,
    val languages: List<String>,
    val addresses: List<SearchAddress>?,
    val descriptionAddress: String?,
    val matchingName: String?,
    val distanceMeters: Double?,
    val center: Point?,
    val routablePoints: List<RoutablePoint>?,
    val categories: List<String>?,
    val icon: String?,
    val metadata: SearchResultMetadata?,
    val externalIDs: Map<String, String>?,
    val layerId: String?,
    val userRecordId: String?,
    val userRecordPriority: Int,
    val action: SearchResultSuggestAction?,
    val serverIndex: Int?,
    val etaMinutes: Double?
) : Parcelable {

    init {
        assertDebug(types.isValidMultiType()) {
            "Provided types should be valid, but was: $types"
        }
    }

    @IgnoredOnParcel
    val type: OriginalResultType = types.firstOrNull() ?: OriginalResultType.UNKNOWN

    @IgnoredOnParcel
    val categoryCanonicalName: String? by lazy(LazyThreadSafetyMode.NONE) {
        externalIDs?.get("federated")?.let { value ->
            if (value.startsWith(CATEGORY_CANONICAL_NAME_PREFIX) && value.length > CATEGORY_CANONICAL_NAME_PREFIX.length) {
                value.removePrefix(CATEGORY_CANONICAL_NAME_PREFIX)
            } else {
                null
            }
        }
    }

    private companion object {
        const val CATEGORY_CANONICAL_NAME_PREFIX = "category."
    }
}

internal fun CoreSearchResult.mapToPlatform() = OriginalSearchResult(
    id = id,
    types = types.map { it.mapToPlatform() },
    names = names,
    languages = languages,
    addresses = addresses?.map { it.mapToPlatform() },
    descriptionAddress = descrAddress,
    matchingName = matchingName,
    distanceMeters = distance,
    center = center,
    routablePoints = routablePoints?.map { it.mapToPlatform() },
    categories = categories,
    icon = icon,
    metadata = metadata?.let { SearchResultMetadata(it) },
    externalIDs = externalIDs,
    layerId = layer,
    userRecordId = userRecordID,
    userRecordPriority = userRecordPriority,
    action = action?.mapToPlatform(),
    serverIndex = serverIndex,
    etaMinutes = eta,
)

internal fun OriginalSearchResult.mapToCore() = CoreSearchResult(
    id,
    types.map { it.mapToCore() },
    names,
    languages,
    addresses?.map { it.mapToCore() },
    descriptionAddress,
    matchingName,
    distanceMeters,
    etaMinutes,
    center,
    routablePoints?.map { it.mapToCore() },
    categories,
    icon,
    metadata?.coreMetadata,
    externalIDs?.let { (it as? HashMap<String, String>) ?: HashMap(it) },
    layerId,
    userRecordId,
    userRecordPriority,
    action?.mapToCore(),
    serverIndex,
)
