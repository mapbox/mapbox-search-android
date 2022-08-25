package com.mapbox.search.ui.utils.offline

import com.mapbox.search.offline.OfflineSearchAddress
import com.mapbox.search.result.SearchAddress

@JvmSynthetic
internal fun OfflineSearchAddress.mapToSdkSearchResultType(): SearchAddress {
    return SearchAddress(
        houseNumber = houseNumber,
        street = street,
        neighborhood = neighborhood,
        locality = locality,
        postcode = postcode,
        place = place,
        district = district,
        region = region,
        country = country
    )
}
