package com.mapbox.search.autocomplete

import com.mapbox.search.autocomplete.test.utils.testBaseRawSearchResult
import com.mapbox.search.autocomplete.test.utils.testBaseResult
import com.mapbox.search.base.core.countryIso1
import com.mapbox.search.base.core.countryIso2
import com.mapbox.search.base.mapToCore
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.result.BaseSearchResultType
import com.mapbox.search.base.result.mapToCore
import com.mapbox.search.base.utils.extension.mapToCore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class PlaceAutocompleteResultFactoryTest {

    private val factory = PlaceAutocompleteResultFactory()

    @Test
    fun `test createPlaceAutocompleteResult`() {
        val result = factory.createPlaceAutocompleteResult(testBaseResult)
        assertNotNull(result)
        requireNotNull(result)
        compare(testBaseRawSearchResult, result)
    }

    @Test
    fun `test createPlaceAutocompleteSuggestion`() {
        val result = factory.createPlaceAutocompleteResult(testBaseResult)
        val suggestion = factory.createPlaceAutocompleteSuggestion(testBaseResult)

        assertNotNull(result)
        assertNotNull(suggestion)

        assertEquals(result, suggestion?.result())
    }

    private fun compare(baseResult: BaseRawSearchResult, result: PlaceAutocompleteResult) {
        assertEquals(baseResult.names.first(), result.name)
        assertEquals(baseResult.center, result.coordinate)
        assertEquals(baseResult.routablePoints, result.routablePoints?.map { it.mapToCore() })
        assertEquals(baseResult.icon, result.makiIcon)
        assertEquals(baseResult.distanceMeters, result.distanceMeters)

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
