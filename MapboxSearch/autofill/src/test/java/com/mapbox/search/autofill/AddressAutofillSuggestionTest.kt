package com.mapbox.search.autofill

import com.mapbox.geojson.Point
import com.mapbox.search.base.result.BaseSearchAddress
import com.mapbox.search.common.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.withPrefabTestBoundingBox
import com.mapbox.search.common.withPrefabTestPoint
import io.mockk.mockk
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AddressAutofillSuggestionTest {

    @Test
    fun `Check AddressAutofillSuggestion result() function`() {
        val address = requireNotNull(
            AddressComponents.fromCoreSdkAddress(
                BaseSearchAddress(country = "test"),
                mockk()
            )
        )

        val suggestion = AddressAutofillSuggestion(
            name = "Test name",
            formattedAddress = "Test formatted address",
            coordinate = Point.fromLngLat(10.0, 11.0),
            address = address
        )

        assertEquals(
            AddressAutofillResult(suggestion, address),
            suggestion.result()
        )
    }

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(AddressAutofillSuggestion::class.java)
            .withPrefabTestPoint()
            .withPrefabTestBoundingBox()
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = AddressAutofillSuggestion::class,
            objectsFactory = ReflectionObjectsFactory(
                extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS
            ),
            ignoredProperties = listOf("address"),
        ).verify()
    }
}
