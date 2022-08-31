package com.mapbox.search.base.result

import android.os.Parcelable
import com.mapbox.search.base.core.CoreSearchAddress
import com.mapbox.search.base.utils.extension.nullIfEmpty
import kotlinx.parcelize.Parcelize

/**
 * TODO This is just a copy of CoreSearchAddress with fixed fields, that in some cases might be empty strings instead of null.
 */
@Parcelize
data class BaseSearchAddress(
    val houseNumber: String? = null,
    val street: String? = null,
    val neighborhood: String? = null,
    val locality: String? = null,
    val postcode: String? = null,
    val place: String? = null,
    val district: String? = null,
    val region: String? = null,
    val country: String? = null
) : Parcelable

@JvmSynthetic
internal fun CoreSearchAddress.mapToBaseSearchAddress(): BaseSearchAddress {
    return BaseSearchAddress(
        houseNumber = houseNumber?.nullIfEmpty(),
        street = street?.nullIfEmpty(),
        neighborhood = neighborhood?.nullIfEmpty(),
        locality = locality?.nullIfEmpty(),
        postcode = postcode?.nullIfEmpty(),
        place = place?.nullIfEmpty(),
        district = district?.nullIfEmpty(),
        region = region?.nullIfEmpty(),
        country = country?.nullIfEmpty()
    )
}

@JvmSynthetic
internal fun BaseSearchAddress.mapToCore(): CoreSearchAddress {
    return CoreSearchAddress(
        houseNumber,
        street,
        neighborhood,
        locality,
        postcode,
        place,
        district,
        region,
        country,
    )
}
