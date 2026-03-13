@file:Suppress("DEPRECATION")
package com.mapbox.search

import com.mapbox.search.base.core.CoreQueryType
import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import com.mapbox.search.common.tests.TestConstants
import com.mapbox.search.tests_support.checkEnumValues
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class QueryTypeTest {

    @Test
    fun `Check QueryType public api fields`() {
        checkEnumValues(QUERY_TYPE_VALUES, QueryType::class.java)
    }

    @Test
    fun `Check mapping QueryType to core`() {
        mapOf(
            QueryType.COUNTRY to CoreQueryType.COUNTRY,
            QueryType.REGION to CoreQueryType.REGION,
            QueryType.POSTCODE to CoreQueryType.POSTCODE,
            QueryType.DISTRICT to CoreQueryType.DISTRICT,
            QueryType.PLACE to CoreQueryType.PLACE,
            QueryType.LOCALITY to CoreQueryType.LOCALITY,
            QueryType.NEIGHBORHOOD to CoreQueryType.NEIGHBORHOOD,
            QueryType.STREET to CoreQueryType.STREET,
            QueryType.ADDRESS to CoreQueryType.ADDRESS,
            QueryType.POI to CoreQueryType.POI,
            QueryType.CATEGORY to CoreQueryType.CATEGORY,
        ).forEach { (inputValue, expectedValue) ->
            assertEquals(expectedValue, inputValue.mapToCore())
        }
    }

    @Test
    fun `resolveQueryTypesToCore uses types when only types provided`() {
        val types = listOf(QueryType.POI, QueryType.ADDRESS)
        val newTypes: List<String>? = null
        val result = resolveQueryTypesToCore(types, newTypes)
        assertEquals(listOf(CoreQueryType.POI, CoreQueryType.ADDRESS), result)
    }

    @Test
    fun `resolveQueryTypesToCore uses newTypes when only newTypes provided`() {
        val types: List<QueryType>? = null
        val newTypes = listOf(NewQueryType.BRAND, NewQueryType.POI)
        val result = resolveQueryTypesToCore(types, newTypes)
        assertEquals(listOf(CoreQueryType.BRAND, CoreQueryType.POI), result)
    }

    @Test
    fun `resolveQueryTypesToCore uses newTypes when both types and newTypes provided`() {
        val types = listOf(QueryType.ADDRESS, QueryType.COUNTRY)
        val newTypes = listOf(NewQueryType.BRAND)
        val result = resolveQueryTypesToCore(types, newTypes)
        assertEquals(listOf(CoreQueryType.BRAND), result)
    }

    private companion object {

        val QUERY_TYPE_VALUES = listOf(
            "COUNTRY", "REGION", "POSTCODE", "DISTRICT", "PLACE", "LOCALITY",
            "NEIGHBORHOOD", "STREET", "ADDRESS", "POI", "CATEGORY"
        )

        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            resetLogImpl()
            mockkStatic(TestConstants.ASSERTIONS_KT_CLASS_NAME)
        }

        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            reinitializeLogImpl()
            unmockkStatic(TestConstants.ASSERTIONS_KT_CLASS_NAME)
        }
    }
}
