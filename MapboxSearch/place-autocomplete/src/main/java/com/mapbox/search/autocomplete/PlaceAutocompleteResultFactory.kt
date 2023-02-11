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
            val type = types.firstNotNullOfOrNull { it.mapToAdministrativeUnit() } ?: return null

            return PlaceAutocompleteResult(
                name = name,
                coordinate = coordinate,
                routablePoints = routablePoints?.map { it.mapToPlatform() },
                makiIcon = makiIcon,
                distanceMeters = distanceMeters,
                address = createPlaceAutocompleteAddress(this),
                administrativeUnitType = type,
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

    private fun BaseSearchResultType.mapToAdministrativeUnit(): AdministrativeUnit? {
        return when (this) {
            BaseSearchResultType.COUNTRY -> AdministrativeUnit.COUNTRY
            BaseSearchResultType.REGION -> AdministrativeUnit.REGION
            BaseSearchResultType.POSTCODE -> AdministrativeUnit.POSTCODE
            BaseSearchResultType.PLACE -> AdministrativeUnit.PLACE
            BaseSearchResultType.DISTRICT -> AdministrativeUnit.DISTRICT
            BaseSearchResultType.LOCALITY -> AdministrativeUnit.LOCALITY
            BaseSearchResultType.NEIGHBORHOOD -> AdministrativeUnit.NEIGHBORHOOD
            BaseSearchResultType.STREET -> AdministrativeUnit.STREET
            BaseSearchResultType.ADDRESS -> AdministrativeUnit.ADDRESS
            else -> null
        }
    }
}
