package com.mapbox.search.ui.utils.extenstion

import com.mapbox.search.autocomplete.PlaceAutocompleteAddress
import com.mapbox.search.autocomplete.PlaceAutocompleteType
import com.mapbox.search.result.NewSearchResultType
import com.mapbox.search.result.SearchAddress

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

@NewSearchResultType.Type
internal fun PlaceAutocompleteType.toNewSearchResultType(): String {
    return when (this) {
        PlaceAutocompleteType.Poi -> NewSearchResultType.POI
        PlaceAutocompleteType.AdministrativeUnit.Country -> NewSearchResultType.COUNTRY
        PlaceAutocompleteType.AdministrativeUnit.Region -> NewSearchResultType.REGION
        PlaceAutocompleteType.AdministrativeUnit.Postcode -> NewSearchResultType.POSTCODE
        PlaceAutocompleteType.AdministrativeUnit.Place -> NewSearchResultType.PLACE
        PlaceAutocompleteType.AdministrativeUnit.District -> NewSearchResultType.DISTRICT
        PlaceAutocompleteType.AdministrativeUnit.Locality -> NewSearchResultType.LOCALITY
        PlaceAutocompleteType.AdministrativeUnit.Neighborhood -> NewSearchResultType.NEIGHBORHOOD
        PlaceAutocompleteType.AdministrativeUnit.Street -> NewSearchResultType.STREET
        PlaceAutocompleteType.AdministrativeUnit.Address -> NewSearchResultType.ADDRESS
        else -> error { "Unsupported type: $this" }
    }
}
