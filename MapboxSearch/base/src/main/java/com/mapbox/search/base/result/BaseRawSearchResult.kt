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
    val categoryIds: List<String>?,
    val brand: List<String>?,
    val brandId: String?,
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
        categoryIds?.firstOrNull { it.isNotEmpty() } ?: extractFederatedValue(
            externalIDs,
            CATEGORY_CANONICAL_NAME_PREFIX,
        )
    }

    @IgnoredOnParcel
    val extractedBrandId: String? by lazy(LazyThreadSafetyMode.NONE) {
        brandId?.takeIf { it.isNotEmpty() } ?: extractFederatedValue(
            externalIDs,
            BRAND_CANONICAL_NAME_PREFIX,
        )
    }

    // TODO `brand` field is not parsed for SBS backend.
    @IgnoredOnParcel
    val extractedBrandName: String?
        get() = brand?.firstOrNull { it.isNotEmpty() } ?: names.firstOrNull { it.isNotEmpty() }

    @IgnoredOnParcel
    val isValidBrandType: Boolean
        get() = type == BaseRawResultType.BRAND &&
                extractedBrandName != null &&
                !extractedBrandId.isNullOrEmpty()

    @IgnoredOnParcel
    val isValidCategoryType: Boolean
        get() = type == BaseRawResultType.CATEGORY && categoryCanonicalName != null

    private companion object {

        const val CATEGORY_CANONICAL_NAME_PREFIX = "category."
        const val BRAND_CANONICAL_NAME_PREFIX = "brand."

        fun extractFederatedValue(externalIDs: Map<String, String>?, prefix: String): String? {
            return externalIDs?.get("federated")?.let { value ->
                if (value.startsWith(prefix) && value.length > prefix.length) {
                    value.removePrefix(prefix)
                } else {
                    null
                }
            }
        }
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
    categoryIds = categoryIDs,
    brand = brand,
    brandId = brandID,
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
    id = id,
    mapboxId = null,
    types = types.map { it.mapToCore() },
    names = names,
    languages = languages,
    addresses = addresses?.map { it.mapToCore() },
    descrAddress = descriptionAddress,
    matchingName = matchingName,
    fullAddress = fullAddress,
    distance = distanceMeters,
    eta = etaMinutes,
    center = center,
    accuracy = accuracy,
    routablePoints = routablePoints,
    categories = categories,
    categoryIDs = categoryIds,
    brand = brand,
    brandID = brandId,
    icon = icon,
    metadata = metadata,
    externalIDs = externalIDs?.let { (it as? HashMap<String, String>) ?: HashMap(it) },
    layer = layerId,
    userRecordID = userRecordId,
    userRecordPriority = userRecordPriority,
    action = action?.mapToCore(),
    serverIndex = serverIndex,
)
