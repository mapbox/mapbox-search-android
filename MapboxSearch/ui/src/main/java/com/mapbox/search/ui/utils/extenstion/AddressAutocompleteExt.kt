package com.mapbox.search.ui.utils.extenstion

import com.mapbox.search.autocomplete.PlaceAutocompleteAddress
import com.mapbox.search.autocomplete.PlaceAutocompleteType
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

internal fun PlaceAutocompleteType.toSearchResultType(): SearchResultType {
    return when (this) {
        PlaceAutocompleteType.Poi -> SearchResultType.POI
        PlaceAutocompleteType.AdministrativeUnit.Country -> SearchResultType.COUNTRY
        PlaceAutocompleteType.AdministrativeUnit.Region -> SearchResultType.REGION
        PlaceAutocompleteType.AdministrativeUnit.Postcode -> SearchResultType.POSTCODE
        PlaceAutocompleteType.AdministrativeUnit.Place -> SearchResultType.PLACE
        PlaceAutocompleteType.AdministrativeUnit.District -> SearchResultType.DISTRICT
        PlaceAutocompleteType.AdministrativeUnit.Locality -> SearchResultType.LOCALITY
        PlaceAutocompleteType.AdministrativeUnit.Neighborhood -> SearchResultType.NEIGHBORHOOD
        PlaceAutocompleteType.AdministrativeUnit.Street -> SearchResultType.STREET
        PlaceAutocompleteType.AdministrativeUnit.Address -> SearchResultType.ADDRESS
        else -> error { "Unsupported type: $this" }
    }
}
