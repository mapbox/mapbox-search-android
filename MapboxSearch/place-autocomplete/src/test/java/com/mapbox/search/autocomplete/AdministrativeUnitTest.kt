package com.mapbox.search.autocomplete

import com.mapbox.search.base.core.CoreQueryType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AdministrativeUnitTest {

    @Test
    fun `AdministrativeUnit core mapping is correct`() {
        AdministrativeUnit.values().forEach {
            val coreType = when(it) {
                AdministrativeUnit.COUNTRY -> CoreQueryType.COUNTRY
                AdministrativeUnit.REGION -> CoreQueryType.REGION
                AdministrativeUnit.POSTCODE -> CoreQueryType.POSTCODE
                AdministrativeUnit.DISTRICT -> CoreQueryType.DISTRICT
                AdministrativeUnit.PLACE -> CoreQueryType.PLACE
                AdministrativeUnit.LOCALITY -> CoreQueryType.LOCALITY
                AdministrativeUnit.NEIGHBORHOOD -> CoreQueryType.NEIGHBORHOOD
                AdministrativeUnit.STREET -> CoreQueryType.STREET
                AdministrativeUnit.ADDRESS -> CoreQueryType.ADDRESS
            }

            assertEquals(coreType, it.coreType)
        }
    }
}
