package com.mapbox.search.base

import androidx.annotation.RestrictTo
import com.mapbox.search.base.core.CoreSearchAddressCountry
import com.mapbox.search.base.core.CoreSearchAddressRegion
import com.mapbox.search.common.SearchAddressCountry
import com.mapbox.search.common.SearchAddressRegion

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreSearchAddressCountry.mapToPlatform(): SearchAddressCountry {
    return SearchAddressCountry(
        name = name,
        isoCodeAlpha2 = countryCode,
        isoCodeAlpha3 = countryCodeAlpha3,
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun SearchAddressCountry.mapToCore(): CoreSearchAddressCountry {
    return CoreSearchAddressCountry(
        name = name,
        countryCode = isoCodeAlpha2,
        countryCodeAlpha3 = isoCodeAlpha3,
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreSearchAddressRegion.mapToPlatform(): SearchAddressRegion {
    return SearchAddressRegion(
        name = name,
        code = regionCode,
        codeFull = regionCodeFull,
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun SearchAddressRegion.mapToCore(): CoreSearchAddressRegion {
    return CoreSearchAddressRegion(
        name = name,
        regionCode = code,
        regionCodeFull = codeFull,
    )
}
