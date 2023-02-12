package com.mapbox.search.ui.utils.extenstion

import com.mapbox.search.autocomplete.AdministrativeUnit
import com.mapbox.search.autocomplete.PlaceAutocompleteAddress
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResultType

internal fun PlaceAutocompleteAddress.toSearchAddress(): SearchAddress {
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

internal fun AdministrativeUnit.toSearchResultType(): SearchResultType {
    return when (this) {
        AdministrativeUnit.COUNTRY -> SearchResultType.COUNTRY
        AdministrativeUnit.REGION -> SearchResultType.REGION
        AdministrativeUnit.POSTCODE -> SearchResultType.POSTCODE
        AdministrativeUnit.PLACE -> SearchResultType.PLACE
        AdministrativeUnit.DISTRICT -> SearchResultType.DISTRICT
        AdministrativeUnit.LOCALITY -> SearchResultType.LOCALITY
        AdministrativeUnit.NEIGHBORHOOD -> SearchResultType.NEIGHBORHOOD
        AdministrativeUnit.STREET -> SearchResultType.STREET
        AdministrativeUnit.ADDRESS -> SearchResultType.ADDRESS
    }
}
