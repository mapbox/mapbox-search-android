package com.mapbox.search.autofill

import com.mapbox.geojson.Point
import com.mapbox.search.base.core.createCoreResultMetadata
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import com.mapbox.search.common.tests.createCoreSearchAddress
import com.mapbox.search.common.tests.createCoreSearchAddressCountry
import com.mapbox.search.common.tests.createCoreSearchAddressRegion

internal object TypeObjectCreator {

    val SUGGESTION_CREATOR = CustomTypeObjectCreatorImpl(AddressAutofillSuggestion::class) { mode ->
        val searchAddress = createCoreSearchAddress(
            houseNumber = "5",
            street = "Rue De Marseille",
            neighborhood = "Porte-Saint-Martin",
            locality = "10th arrondissement of Paris",
            postcode = "75010",
            place = "Paris",
            district = "Paris district",
            region = createCoreSearchAddressRegion("Paris region"),
            country = createCoreSearchAddressCountry("France"),
        )

        val coreMetadata = createCoreResultMetadata(
            data = hashMapOf("iso_3166_1" to "fra", "iso_3166_2" to "fr")
        )

        val addressComponents = requireNotNull(
            AddressComponents.fromCoreSdkAddress(searchAddress, coreMetadata)
        )

        listOf(
            AddressAutofillSuggestion(
                name = "name",
                formattedAddress = "formattedAddress",
                coordinate = Point.fromLngLat(10.0, 15.0),
                address = addressComponents,
                underlying = null
            )
        )[mode.ordinal]
    }
}
