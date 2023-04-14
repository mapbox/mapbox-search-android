package com.mapbox.search.autocomplete

import com.mapbox.search.autocomplete.test.utils.TypeObjectCreator
import com.mapbox.search.common.tests.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.withPrefabTestBoundingBox
import com.mapbox.search.common.tests.withPrefabTestPoint
import nl.jqno.equalsverifier.EqualsVerifier
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
                extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS +
                        TypeObjectCreator.OPEN_HOURS_CREATOR +
                        TypeObjectCreator.PLACE_TYPE_CREATOR +
                        TypeObjectCreator.SUGGESTION_UNDERLYING_CREATOR
            ),
            ignoredProperties = listOf("underlying"),
            includeAllProperties = false
        ).verify()
    }
}
