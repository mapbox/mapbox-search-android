package com.mapbox.search.autocomplete

import com.mapbox.geojson.Point
import com.mapbox.search.autocomplete.test.utils.TypeObjectCreator
import com.mapbox.search.common.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.withPrefabTestBoundingBox
import com.mapbox.search.common.withPrefabTestPoint
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

internal class PlaceAutocompleteSuggestionTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(PlaceAutocompleteSuggestion::class.java)
            .withPrefabTestPoint()
            .withPrefabTestBoundingBox()
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = PlaceAutocompleteSuggestion::class,
            objectsFactory = ReflectionObjectsFactory(
                extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS + TypeObjectCreator.OPEN_HOURS_CREATOR
            ),
            ignoredProperties = listOf("result"),
            includeAllProperties = false
        ).verify()
    }

    @Test
    fun `result() function is correct`() {
        val result = PlaceAutocompleteResult(
            name = "Test result",
            coordinate = Point.fromLngLat(10.0, 15.0),
            routablePoints = null,
            makiIcon = "test",
            distanceMeters = 123.0,
            address = null,
            administrativeUnitType = AdministrativeUnit.PLACE,
            phone = "+123456789",
            website = "https://test.com",
            reviewCount = 123,
            averageRating = 5.0,
            openHours = null,
            primaryPhotos = listOf(),
            otherPhotos = listOf(),
        )

        val suggestion = PlaceAutocompleteSuggestion(result)

        assertSame(result, suggestion.result())
    }
}
