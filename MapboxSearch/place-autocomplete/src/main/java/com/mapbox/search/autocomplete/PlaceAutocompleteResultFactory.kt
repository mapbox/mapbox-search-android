package com.mapbox.search.autocomplete

import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.geojson.Point
import com.mapbox.search.base.core.countryIso1
import com.mapbox.search.base.core.countryIso2
import com.mapbox.search.base.mapToPlatform
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.utils.extension.mapToPlatform
import com.mapbox.search.base.utils.extension.nullIfEmpty

internal class PlaceAutocompleteResultFactory {

    fun createPlaceAutocompleteSuggestion(
        coordinate: Point,
        type: PlaceAutocompleteType,
        suggestion: BaseSearchSuggestion,
    ): PlaceAutocompleteSuggestion {
        return with(suggestion) {
            PlaceAutocompleteSuggestion(
                name = name,
                formattedAddress = formattedAddress(),
                coordinate = coordinate,
                routablePoints = suggestion.routablePoints?.map { it.mapToPlatform() },
                makiIcon = makiIcon,
                distanceMeters = distanceMeters,
                etaMinutes = etaMinutes,
                type = type,
                categories = categories,
                underlying = PlaceAutocompleteSuggestion.Underlying.Suggestion(suggestion)
            )
        }
    }

    fun createPlaceAutocompleteSuggestion(
        type: PlaceAutocompleteType,
        result: BaseSearchResult
    ): PlaceAutocompleteSuggestion {
        return with(result) {
            PlaceAutocompleteSuggestion(
                name = name,
                formattedAddress = formattedAddress(),
                coordinate = coordinate,
                routablePoints = routablePoints?.map { it.mapToPlatform() },
                makiIcon = makiIcon,
                distanceMeters = distanceMeters,
                etaMinutes = etaMinutes,
                type = type,
                categories = categories,
                underlying = PlaceAutocompleteSuggestion.Underlying.Result(result)
            )
        }
    }

    fun createPlaceAutocompleteSuggestions(results: List<BaseSearchResult>): List<PlaceAutocompleteSuggestion> {
        return results.mapNotNull { result ->
            val type = result.types.firstNotNullOfOrNull {
                PlaceAutocompleteType.createFromBaseType(it)
            } ?: return@mapNotNull null
            createPlaceAutocompleteSuggestion(type, result)
        }
    }

    fun createPlaceAutocompleteResultOrError(result: BaseSearchResult): Expected<Exception, PlaceAutocompleteResult> {
        return createPlaceAutocompleteResult(result)?.let {
            ExpectedFactory.createValue(it)
        } ?: ExpectedFactory.createError(Exception("Unable to create PlaceAutocompleteResult from $result"))
    }

    private fun createPlaceAutocompleteResult(result: BaseSearchResult): PlaceAutocompleteResult? {
        with(result) {
            val type = types.firstNotNullOfOrNull { PlaceAutocompleteType.createFromBaseType(it) } ?: return null

            return PlaceAutocompleteResult(
                name = name,
                coordinate = coordinate,
                routablePoints = routablePoints?.map { it.mapToPlatform() },
                makiIcon = makiIcon,
                distanceMeters = distanceMeters,
                etaMinutes = etaMinutes,
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
                formattedAddress = result.formattedAddress(),
                countryIso1 = metadata?.countryIso1,
                countryIso2 = metadata?.countryIso2
            )
        }
    }

    private fun BaseSearchResult.formattedAddress(): String? = fullAddress ?: descriptionText

    private fun BaseSearchSuggestion.formattedAddress(): String? = fullAddress ?: descriptionText
}
