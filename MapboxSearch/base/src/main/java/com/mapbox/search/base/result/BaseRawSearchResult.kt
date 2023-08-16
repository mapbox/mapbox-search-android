package com.mapbox.search.base.result

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.base.assertDebug
import com.mapbox.search.base.core.CoreResultAccuracy
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.CoreRoutablePoint
import com.mapbox.search.base.core.CoreSearchResult
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.HashMap

@Parcelize
data class BaseRawSearchResult(
    val id: String,
    val types: List<BaseRawResultType>,
    val names: List<String>,
    val languages: List<String>,
    val addresses: List<BaseSearchAddress>?,
    val descriptionAddress: String?,
    val matchingName: String?,
    val fullAddress: String?,
    val distanceMeters: Double?,
    val center: Point?,
    val accuracy: CoreResultAccuracy?,
    val routablePoints: List<CoreRoutablePoint>?,
    val categories: List<String>?,
    val icon: String?,
    val metadata: CoreResultMetadata?,
    val externalIDs: Map<String, String>?,
    val layerId: String?,
    val userRecordId: String?,
    val userRecordPriority: Int,
    val action: BaseSuggestAction?,
    val serverIndex: Int?,
    val etaMinutes: Double?
) : Parcelable {

    init {
        assertDebug(types.isValidMultiType()) {
            "Provided types should be valid, but was: $types"
        }
    }

    @IgnoredOnParcel
    val type: BaseRawResultType = types.firstOrNull() ?: BaseRawResultType.UNKNOWN

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

fun CoreSearchResult.mapToBase() = BaseRawSearchResult(
    id = id,
    types = types.map { it.mapToBase() },
    names = names,
    languages = languages,
    addresses = addresses?.map { it.mapToBaseSearchAddress() },
    descriptionAddress = descrAddress,
    matchingName = matchingName,
    fullAddress = fullAddress,
    distanceMeters = distance,
    center = center,
    accuracy = accuracy,
    routablePoints = routablePoints,
    categories = categories,
    icon = icon,
    metadata = metadata,
    externalIDs = externalIDs,
    layerId = layer,
    userRecordId = userRecordID,
    userRecordPriority = userRecordPriority,
    action = action?.mapToBase(),
    serverIndex = serverIndex,
    etaMinutes = eta,
)

fun BaseRawSearchResult.mapToCore() = CoreSearchResult(
    id,
    types.map { it.mapToCore() },
    names,
    languages,
    addresses?.map { it.mapToCore() },
    descriptionAddress,
    matchingName,
    fullAddress,
    distanceMeters,
    etaMinutes,
    center,
    accuracy,
    routablePoints,
    categories,
    null, // category ids
    null,   // brand
    null,   // brand id
    icon,
    metadata,
    externalIDs?.let { (it as? HashMap<String, String>) ?: HashMap(it) },
    layerId,
    userRecordId,
    userRecordPriority,
    action?.mapToCore(),
    serverIndex,
)
