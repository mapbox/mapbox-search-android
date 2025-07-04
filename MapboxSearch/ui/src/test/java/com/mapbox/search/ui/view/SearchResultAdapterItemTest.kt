package com.mapbox.search.ui.view

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.geojson.Point
import com.mapbox.search.ResponseInfo
import com.mapbox.search.common.parking.ParkingRateCustomDurationValue
import com.mapbox.search.common.parking.ParkingRateValue
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.isEnum
import com.mapbox.search.ui.tests_support.UiCustomTypeObjectCreators
import com.mapbox.test.dsl.TestCase
import io.mockk.mockk
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.junit.jupiter.api.TestFactory

internal class SearchResultAdapterItemTest {

    @TestFactory
    fun `Test generated equals(), hashCode() and toString() methods`() = TestCase {
        listOf(
            SearchResultAdapterItem.Loading::class,
            SearchResultAdapterItem.RecentSearchesHeader::class,
            SearchResultAdapterItem.EmptyHistory::class,
            SearchResultAdapterItem.History::class,
            SearchResultAdapterItem.EmptySearchResults::class,
            SearchResultAdapterItem.Result::class,
            SearchResultAdapterItem.MissingResultFeedback::class,
            SearchResultAdapterItem.Error::class,
        )
            .forEach { clazz ->
                Given("${clazz.java.canonicalName} class") {
                    When("${clazz.java.canonicalName} class") {
                        Then("equals() and hashCode() functions should use every declared property") {
                            EqualsVerifier.forClass(clazz.java)
                                .withPrefabValues(
                                    Point::class.java,
                                    Point.fromLngLat(2.0, 5.0),
                                    Point.fromLngLat(27.55140833333333, 53.911334999999994)
                                )
                                .suppress(Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                                .verify()
                        }

                        if (!isEnum(clazz)) {
                            Then("toString() function should use every declared property") {
                                ToStringVerifier(
                                    clazz = clazz,
                                    objectsFactory = OBJ_FACTORY,
                                ).verify()
                            }
                        }
                    }
                }
            }
    }

    private companion object {

        val CHAR_SEQUENCE_OBJ_CREATOR = CustomTypeObjectCreatorImpl(CharSequence::class) { mode ->
            listOf("CharSequence-1", "CharSequence-2")[mode.ordinal]
        }

        val RESPONSE_INFO_CREATOR = CustomTypeObjectCreatorImpl(ResponseInfo::class) { mode ->
            listOf(mockk<ResponseInfo>(), mockk())[mode.ordinal]
        }

        val UI_ERROR_CREATOR = CustomTypeObjectCreatorImpl(UiError::class) { mode ->
            listOf(UiError.ClientError, UiError.ServerError)[mode.ordinal]
        }

        @OptIn(MapboxExperimental::class)
        private val PARKING_RATE_PRICE_CREATOR = CustomTypeObjectCreatorImpl(ParkingRateValue::class) { mode ->
            listOf(
                ParkingRateValue.IsoValue("test"),
                ParkingRateValue.CustomDurationValue(ParkingRateCustomDurationValue.DAYTIME),
            )[mode.ordinal]
        }

        private val OBJ_FACTORY = ReflectionObjectsFactory(
            extraCreators = UiCustomTypeObjectCreators.ALL_CREATORS +
                    CHAR_SEQUENCE_OBJ_CREATOR +
                    RESPONSE_INFO_CREATOR +
                    UI_ERROR_CREATOR +
                    PARKING_RATE_PRICE_CREATOR,
        )
    }
}
