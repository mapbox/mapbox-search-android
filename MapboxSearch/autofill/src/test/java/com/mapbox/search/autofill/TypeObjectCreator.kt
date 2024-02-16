package com.mapbox.search.autofill

import com.mapbox.search.base.result.BaseSearchAddress
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import com.mapbox.search.common.tests.createTestResultMetadata

internal object TypeObjectCreator {

    val SUGGESTION_CREATOR = CustomTypeObjectCreatorImpl(AddressAutofillSuggestion::class) { mode ->
        val searchAddress = BaseSearchAddress(
            houseNumber = "5",
            street = "Rue De Marseille",
            neighborhood = "Porte-Saint-Martin",
            locality = "10th arrondissement of Paris",
            postcode = "75010",
            place = "Paris",
            district = "Paris district",
            region = "Paris region",
            country = "France"
        )

        val coreMetadata = createTestResultMetadata(
            data = hashMapOf("iso_3166_1" to "fra", "iso_3166_2" to "fr")
        )

        val addressComponents = requireNotNull(
            AddressComponents.fromCoreSdkAddress(searchAddress, coreMetadata)
        )

        listOf(
            // TODO : create BaseSearchSuggestion somehow and pass instead of null
            AddressAutofillSuggestion("name", "formattedAddress", addressComponents, null)
        )[mode.ordinal]
    }
}
