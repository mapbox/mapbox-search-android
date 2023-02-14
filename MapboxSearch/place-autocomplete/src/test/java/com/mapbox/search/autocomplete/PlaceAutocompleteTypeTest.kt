package com.mapbox.search.autocomplete

import com.mapbox.search.base.core.CoreQueryType
import com.mapbox.search.internal.bindgen.QueryType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PlaceAutocompleteTypeTest {

    @Test
    fun `check core mapping`() {
        PlaceAutocompleteType.ALL_DECLARED_TYPES.forEach {
            val coreType = when (it) {
                is PlaceAutocompleteType.Poi -> CoreQueryType.POI
                is PlaceAutocompleteType.AdministrativeUnit.Country -> CoreQueryType.COUNTRY
                is PlaceAutocompleteType.AdministrativeUnit.Region -> CoreQueryType.REGION
                is PlaceAutocompleteType.AdministrativeUnit.Postcode -> CoreQueryType.POSTCODE
                is PlaceAutocompleteType.AdministrativeUnit.District -> CoreQueryType.DISTRICT
                is PlaceAutocompleteType.AdministrativeUnit.Place -> CoreQueryType.PLACE
                is PlaceAutocompleteType.AdministrativeUnit.Locality -> CoreQueryType.LOCALITY
                is PlaceAutocompleteType.AdministrativeUnit.Neighborhood -> CoreQueryType.NEIGHBORHOOD
                is PlaceAutocompleteType.AdministrativeUnit.Street -> CoreQueryType.STREET
                is PlaceAutocompleteType.AdministrativeUnit.Address -> CoreQueryType.ADDRESS
                else -> error("Unsupported type: $it")
            }

            assertEquals(coreType, it.coreType)
        }
    }

    @Test
    fun `check all types are different`() {
        val set = PlaceAutocompleteType.ALL_DECLARED_TYPES.toSet()
        assertEquals(PlaceAutocompleteType.ALL_DECLARED_TYPES.size, set.size)
    }

    @Test
    fun `check all types completeness`() {
        val coreTypes = PlaceAutocompleteType.ALL_DECLARED_TYPES.map { it.coreType }.sorted()
        val allValidCoreTypes = CoreQueryType.values().toMutableSet().apply {
            remove(QueryType.CATEGORY)
        }.toList().sorted()
        assertEquals(allValidCoreTypes, coreTypes)
    }
}
