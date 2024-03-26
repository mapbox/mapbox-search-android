package com.mapbox.search.autocomplete

import com.mapbox.search.autocomplete.test.utils.createTestBaseSearchSuggestion
import com.mapbox.search.autocomplete.test.utils.testBaseRawSearchResult
import com.mapbox.search.autocomplete.test.utils.testBaseRawSearchSuggestionWithCoordinates
import com.mapbox.search.autocomplete.test.utils.testBaseResult
import com.mapbox.search.base.core.countryIso1
import com.mapbox.search.base.core.countryIso2
import com.mapbox.search.base.mapToCore
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.utils.extension.mapToCore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PlaceAutocompleteResultFactoryTest {

    private val factory = PlaceAutocompleteResultFactory()

    @Test
    fun `test create PlaceAutocompleteSuggestion from base suggestion`() {
        val baseSuggestion = createTestBaseSearchSuggestion(testBaseRawSearchSuggestionWithCoordinates)
        val type = PlaceAutocompleteType.AdministrativeUnit.Address
        val suggestion = factory.createPlaceAutocompleteSuggestion(type, baseSuggestion)
        compare(testBaseRawSearchSuggestionWithCoordinates, suggestion)
    }

    @Test
    fun `test create PlaceAutocompleteSuggestion from base result`() {
        val suggestion = factory.createPlaceAutocompleteSuggestion(
            PlaceAutocompleteType.AdministrativeUnit.Address,
            testBaseResult
        )
        compare(testBaseResult.rawSearchResult, suggestion)
    }

    @Test
    fun `test createPlaceAutocompleteSuggestions`() {
        val suggestions = factory.createPlaceAutocompleteSuggestions(listOf(testBaseResult))
        assertEquals(1, suggestions.size)
        compare(testBaseResult.rawSearchResult, suggestions.first())
    }

    @Test
    fun `test createPlaceAutocompleteResult`() {
        val result = factory.createPlaceAutocompleteResultOrError(testBaseResult)
        assertTrue(result.isValue)
        compare(testBaseRawSearchResult, result.value!!)
    }

    private fun compare(baseResult: BaseRawSearchResult, suggestion: PlaceAutocompleteSuggestion) {
        assertEquals(baseResult.names.first(), suggestion.name)
        assertEquals(baseResult.fullAddress, suggestion.formattedAddress)
        assertEquals(baseResult.routablePoints, suggestion.routablePoints?.map { it.mapToCore() })
        assertEquals(baseResult.icon, suggestion.makiIcon)
        assertEquals(baseResult.distanceMeters, suggestion.distanceMeters)
        assertEquals(PlaceAutocompleteType.AdministrativeUnit.Address, suggestion.type)
        assertEquals(baseResult.categories, suggestion.categories)
    }

    private fun compare(baseResult: BaseRawSearchResult, result: PlaceAutocompleteResult) {
        assertEquals(baseResult.names.first(), result.name)
        assertEquals(baseResult.center, result.coordinate)
        assertEquals(baseResult.routablePoints, result.routablePoints?.map { it.mapToCore() })
        assertEquals(baseResult.icon, result.makiIcon)
        assertEquals(baseResult.distanceMeters, result.distanceMeters)
        assertEquals(baseResult.categories, result.categories)

        assertEquals(baseResult.fullAddress, result.address?.formattedAddress)
        assertEquals(baseResult.addresses?.first()?.houseNumber, result.address?.houseNumber)
        assertEquals(baseResult.addresses?.first()?.street, result.address?.street)
        assertEquals(baseResult.addresses?.first()?.neighborhood, result.address?.neighborhood)
        assertEquals(baseResult.addresses?.first()?.locality, result.address?.locality)
        assertEquals(baseResult.addresses?.first()?.postcode, result.address?.postcode)
        assertEquals(baseResult.addresses?.first()?.place, result.address?.place)
        assertEquals(baseResult.addresses?.first()?.district, result.address?.district)
        assertEquals(baseResult.addresses?.first()?.region, result.address?.region)
        assertEquals(baseResult.addresses?.first()?.country, result.address?.country)
        assertEquals(baseResult.metadata?.countryIso1, result.address?.countryIso1)
        assertEquals(baseResult.metadata?.countryIso2, result.address?.countryIso2)

        assertEquals(PlaceAutocompleteType.AdministrativeUnit.Address, result.type)

        assertEquals(baseResult.metadata?.phone, result.phone)
        assertEquals(baseResult.metadata?.website, result.website)
        assertEquals(baseResult.metadata?.reviewCount, result.reviewCount)
        assertEquals(baseResult.metadata?.avRating, result.averageRating)
        assertEquals(baseResult.metadata?.openHours, result.openHours?.mapToCore())
        assertEquals(baseResult.metadata?.primaryPhoto, result.primaryPhotos?.map { it.mapToCore() })
        assertEquals(baseResult.metadata?.otherPhoto, result.otherPhotos?.map { it.mapToCore() })
    }
}
