package com.mapbox.search.base.result

import android.os.Parcelable
import com.mapbox.search.base.core.CoreSearchAddress
import com.mapbox.search.base.mapToCore
import com.mapbox.search.base.mapToPlatform
import com.mapbox.search.base.utils.extension.nullIfEmpty
import com.mapbox.search.common.SearchAddressCountry
import com.mapbox.search.common.SearchAddressRegion
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
    val country: String? = null,
    val regionInfo: SearchAddressRegion? = null,
    val countryInfo: SearchAddressCountry? = null,
) : Parcelable

@JvmSynthetic
fun CoreSearchAddress.mapToBaseSearchAddress(): BaseSearchAddress {
    return BaseSearchAddress(
        houseNumber = houseNumber?.nullIfEmpty(),
        street = street?.nullIfEmpty(),
        neighborhood = neighborhood?.nullIfEmpty(),
        locality = locality?.nullIfEmpty(),
        postcode = postcode?.nullIfEmpty(),
        place = place?.nullIfEmpty(),
        district = district?.nullIfEmpty(),
        region = region?.name?.nullIfEmpty(),
        country = country?.name?.nullIfEmpty(),
        regionInfo = region?.mapToPlatform(),
        countryInfo = country?.mapToPlatform(),
    )
}

@JvmSynthetic
fun BaseSearchAddress.mapToCore(): CoreSearchAddress {
    return CoreSearchAddress(
        houseNumber,
        street,
        neighborhood,
        locality,
        postcode,
        place,
        district,
        regionInfo?.mapToCore(),
        countryInfo?.mapToCore(),
    )
}
