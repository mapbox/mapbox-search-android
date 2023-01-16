package com.mapbox.search.autofill

import com.mapbox.search.base.core.countryIso1
import com.mapbox.search.base.core.countryIso2
import com.mapbox.search.base.result.BaseSearchAddress
import com.mapbox.search.common.CommonSdkTypeObjectCreators
import com.mapbox.search.common.createTestResultMetadata
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.withPrefabTestBoundingBox
import com.mapbox.search.common.withPrefabTestPoint
import io.mockk.every
import io.mockk.mockk
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class AddressComponentsTest {

    @Test
    fun `Check AddressComponents properties`() {
        val searchAddress = BaseSearchAddress(
            houseNumber = "5",
            street = "Rue De Marseille",
            neighborhood = "Porte-Saint-Martin",
            locality = "10th arrondissement of Paris",
            postcode = "75010",
            place = "Paris",
            district = "Paris district",
            region = "Paris region",
            country = "France"
        )

        val coreMetadata = createTestResultMetadata(
            data = hashMapOf("iso_3166_1" to "fra", "iso_3166_2" to "fr")
        )

        val addressComponents = requireNotNull(
            AddressComponents.fromCoreSdkAddress(searchAddress, coreMetadata)
        )

        assertEquals(searchAddress.houseNumber, addressComponents.houseNumber)
        assertEquals(searchAddress.street, addressComponents.street)
        assertEquals(searchAddress.neighborhood, addressComponents.neighborhood)
        assertEquals(searchAddress.locality, addressComponents.locality)
        assertEquals(searchAddress.postcode, addressComponents.postcode)
        assertEquals(searchAddress.place, addressComponents.place)
        assertEquals(searchAddress.district, addressComponents.district)
        assertEquals(searchAddress.region, addressComponents.region)
        assertEquals(searchAddress.country, addressComponents.country)
        assertEquals(coreMetadata.countryIso1, addressComponents.countryIso1)
        assertEquals(coreMetadata.countryIso2, addressComponents.countryIso2)
    }

    @Test
    fun `Check AddressComponents fromCoreSdkAddress() function`() {
        assertNotNull(AddressComponents.fromCoreSdkAddress(BaseSearchAddress(country = "France"), mockk()))
        assertNull(AddressComponents.fromCoreSdkAddress(BaseSearchAddress(), mockk()))
    }

    @Test
    fun `Check AddressComponents formattedAddress() function`() {
        val formattedAddress = "Rue de Marseille, Paris, France"

        val searchAddress = mockk<BaseSearchAddress>(relaxed = true)
        every { searchAddress.street } returns "Rue de Marseille"
        every { searchAddress.place } returns "Paris"
        every { searchAddress.country } returns "France"

        val addressComponents = requireNotNull(AddressComponents.fromCoreSdkAddress(searchAddress, mockk()))

        assertEquals(
            formattedAddress,
            addressComponents.formattedAddress()
        )
    }

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(AddressComponents::class.java)
            .withPrefabTestPoint()
            .withPrefabTestBoundingBox()
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = AddressComponents::class,
            objectsFactory = ReflectionObjectsFactory(
                extraCreators = CommonSdkTypeObjectCreators.ALL_CREATORS
            ),
            ignoredProperties = listOf("coreSdkAddress", "coreMetadata"),
        ).verify()
    }
}
