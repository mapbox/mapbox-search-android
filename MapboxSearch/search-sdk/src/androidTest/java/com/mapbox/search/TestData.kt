package com.mapbox.search

import com.mapbox.search.common.SearchAddressCountry
import com.mapbox.search.common.SearchAddressRegion
import com.mapbox.search.result.SearchAddress

internal object TestData {

    val EIFFEL_TOWER_ADDRESS = SearchAddress(
        country = "France",
        houseNumber = "5",
        locality = "7th arrondissement of Paris",
        neighborhood = "Gros-Caillou",
        place = "Paris",
        postcode = "75007",
        street = "avenue Anatole France",
        countryInfo = SearchAddressCountry("France", null, null),
    )

    const val EIFFEL_TOWER_ADDRESS_DESCRIPTION = "Champ de Mars, 5 Avenue Anatole France, 75007 Paris, France"

    val PRADO_MUSEUM_ADDRESS = SearchAddress(
        country = "Spain",
        locality = "Jerónimos",
        place = "Madrid",
        postcode = "28014",
        region = "Madrid",
        street = "C. de Ruiz de Alarcón, 23",
        countryInfo = SearchAddressCountry("Spain", null, null),
        regionInfo = SearchAddressRegion("Madrid", null, null),
    )

    const val PRADO_MUSEUM_ADDRESS_DESCRIPTION = "Calle de Ruiz de Alarcón, 23, 28014 Madrid, Spain"

    val OSLO_OPERA_HOUSE_ADDRESS = SearchAddress(
        country = "Norway",
        place = "Oslo",
        postcode = "0150",
        street = "Kirsten Flagstads Plass 1",
        countryInfo = SearchAddressCountry("Norway", null, null),
    )

    const val OSLO_OPERA_HOUSE_ADDRESS_DESCRIPTION = "Kirsten Flagstads Plass 1, 0150 Oslo, Norway"
}
