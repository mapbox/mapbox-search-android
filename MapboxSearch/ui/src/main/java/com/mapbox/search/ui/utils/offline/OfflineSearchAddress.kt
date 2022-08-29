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
        postcode = null,
        place = place,
        district = null,
        region = region,
        country = country
    )
}
