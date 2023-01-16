package com.mapbox.search.autofill

import com.mapbox.search.common.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.withPrefabTestBoundingBox
import com.mapbox.search.common.withPrefabTestPoint
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Test

internal class AddressAutofillResultTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(AddressAutofillResult::class.java)
            .withPrefabTestPoint()
            .withPrefabTestBoundingBox()
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = AddressAutofillResult::class,
            objectsFactory = ReflectionObjectsFactory(
                extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS
            )
        ).verify()
    }
}
