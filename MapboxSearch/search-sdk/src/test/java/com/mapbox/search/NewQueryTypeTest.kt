package com.mapbox.search

import com.mapbox.search.base.core.CoreQueryType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NewQueryTypeTest {

    @Test
    fun mappingNewQueryTypeToCoreQueryType() {
        assertEquals(CoreQueryType.COUNTRY, newQueryTypeToCore(NewQueryType.COUNTRY))
        assertEquals(CoreQueryType.REGION, newQueryTypeToCore(NewQueryType.REGION))
        assertEquals(CoreQueryType.POSTCODE, newQueryTypeToCore(NewQueryType.POSTCODE))
        assertEquals(CoreQueryType.DISTRICT, newQueryTypeToCore(NewQueryType.DISTRICT))
        assertEquals(CoreQueryType.PLACE, newQueryTypeToCore(NewQueryType.PLACE))
        assertEquals(CoreQueryType.LOCALITY, newQueryTypeToCore(NewQueryType.LOCALITY))
        assertEquals(CoreQueryType.NEIGHBORHOOD, newQueryTypeToCore(NewQueryType.NEIGHBORHOOD))
        assertEquals(CoreQueryType.STREET, newQueryTypeToCore(NewQueryType.STREET))
        assertEquals(CoreQueryType.ADDRESS, newQueryTypeToCore(NewQueryType.ADDRESS))
        assertEquals(CoreQueryType.POI, newQueryTypeToCore(NewQueryType.POI))
        assertEquals(CoreQueryType.CATEGORY, newQueryTypeToCore(NewQueryType.CATEGORY))
        assertEquals(CoreQueryType.BRAND, newQueryTypeToCore(NewQueryType.BRAND))
    }

    @Test
    fun newQueryTypeListMapsToCoreQueryTypeList() {
        val newTypes = listOf(NewQueryType.POI, NewQueryType.BRAND, NewQueryType.ADDRESS)
        assertEquals(
            listOf(CoreQueryType.POI, CoreQueryType.BRAND, CoreQueryType.ADDRESS),
            newTypes.mapNewQueryTypesToCore(),
        )
    }
}
