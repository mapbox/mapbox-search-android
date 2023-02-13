package com.mapbox.search.autocomplete

import com.mapbox.search.autocomplete.test.utils.TypeObjectCreator
import com.mapbox.search.common.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.withPrefabTestBoundingBox
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Test

internal class TextQueryTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(TextQuery::class.java)
            .withPrefabTestBoundingBox()
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = TextQuery::class,
            objectsFactory = ReflectionObjectsFactory(
                extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS + TypeObjectCreator.OPEN_HOURS_CREATOR
            ),
            includeAllProperties = false
        ).verify()
    }
}
