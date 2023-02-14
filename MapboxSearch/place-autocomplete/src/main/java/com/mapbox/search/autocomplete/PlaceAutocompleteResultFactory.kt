package com.mapbox.search.autocomplete

import com.mapbox.search.base.core.countryIso1
import com.mapbox.search.base.core.countryIso2
import com.mapbox.search.base.mapToPlatform
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchResultType
import com.mapbox.search.base.utils.extension.mapToPlatform
import com.mapbox.search.base.utils.extension.nullIfEmpty

internal class PlaceAutocompleteResultFactory {

    fun createPlaceAutocompleteSuggestions(results: List<BaseSearchResult>): List<PlaceAutocompleteSuggestion> {
        return results.mapNotNull { createPlaceAutocompleteSuggestion(it) }
    }

    fun createPlaceAutocompleteSuggestion(result: BaseSearchResult): PlaceAutocompleteSuggestion? {
        return createPlaceAutocompleteResult(result)?.let {
            PlaceAutocompleteSuggestion(it)
        }
    }

    fun createPlaceAutocompleteResult(result: BaseSearchResult): PlaceAutocompleteResult? {
        with(result) {
            val type = types.firstNotNullOfOrNull { it.mapToAutocompleteType() } ?: return null

            return PlaceAutocompleteResult(
                name = name,
                coordinate = coordinate,
                routablePoints = routablePoints?.map { it.mapToPlatform() },
                makiIcon = makiIcon,
                distanceMeters = distanceMeters,
                address = createPlaceAutocompleteAddress(this),
                type = type,
                categories = categories,
                phone = metadata?.phone,
                website = metadata?.website,
                reviewCount = metadata?.reviewCount,
                averageRating = metadata?.avRating,
                openHours = metadata?.openHours?.mapToPlatform(),
                primaryPhotos = metadata?.primaryPhoto?.map { it.mapToPlatform() },
                otherPhotos = metadata?.otherPhoto?.map { it.mapToPlatform() },
            )
        }
    }

    private fun createPlaceAutocompleteAddress(result: BaseSearchResult): PlaceAutocompleteAddress {
        with(result) {
            return PlaceAutocompleteAddress(
                houseNumber = address?.houseNumber?.nullIfEmpty(),
                street = address?.street?.nullIfEmpty(),
                neighborhood = address?.neighborhood?.nullIfEmpty(),
                locality = address?.locality?.nullIfEmpty(),
                postcode = address?.postcode?.nullIfEmpty(),
                place = address?.place?.nullIfEmpty(),
                district = address?.district?.nullIfEmpty(),
                region = address?.region?.nullIfEmpty(),
                country = address?.country?.nullIfEmpty(),
                formattedAddress = result.fullAddress ?: result.descriptionText,
                countryIso1 = metadata?.countryIso1,
                countryIso2 = metadata?.countryIso2
            )
        }
    }

    private fun BaseSearchResultType.mapToAutocompleteType(): PlaceAutocompleteType {
        return when (this) {
            BaseSearchResultType.POI -> PlaceAutocompleteType.Poi
            BaseSearchResultType.COUNTRY -> PlaceAutocompleteType.AdministrativeUnit.Country
            BaseSearchResultType.REGION -> PlaceAutocompleteType.AdministrativeUnit.Region
            BaseSearchResultType.POSTCODE -> PlaceAutocompleteType.AdministrativeUnit.Postcode
            BaseSearchResultType.PLACE -> PlaceAutocompleteType.AdministrativeUnit.Place
            BaseSearchResultType.DISTRICT -> PlaceAutocompleteType.AdministrativeUnit.District
            BaseSearchResultType.LOCALITY -> PlaceAutocompleteType.AdministrativeUnit.Locality
            BaseSearchResultType.NEIGHBORHOOD -> PlaceAutocompleteType.AdministrativeUnit.Neighborhood
            BaseSearchResultType.STREET -> PlaceAutocompleteType.AdministrativeUnit.Street
            BaseSearchResultType.ADDRESS -> PlaceAutocompleteType.AdministrativeUnit.Address
            BaseSearchResultType.BLOCK -> PlaceAutocompleteType.AdministrativeUnit.Address
        }
    }
}
