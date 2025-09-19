@file:Suppress("DEPRECATION")
package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.analytics.FeedbackEvent
import com.mapbox.search.analytics.MissingResultFeedbackEvent
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.common.NavigationProfile
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.common.metadata.ImageInfo
import com.mapbox.search.common.metadata.OpenHours
import com.mapbox.search.common.metadata.OpenPeriod
import com.mapbox.search.common.metadata.ParkingData
import com.mapbox.search.common.metadata.WeekDay
import com.mapbox.search.common.metadata.WeekTimestamp
import com.mapbox.search.common.tests.CopyVerifier
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.isEnum
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.SearchSuggestionType
import com.mapbox.search.tests_support.SdkCustomTypeObjectCreators
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory

/**
 * Test to track migration from data/sealed/enum class to a regular class.
 */
internal class DataClassGeneratedFunctionsTest {

    private val reflectionObjectFactory = ReflectionObjectsFactory(
        extraCreators = SdkCustomTypeObjectCreators.ALL_CREATORS,
    )

    @TestFactory
    fun `Test generated equals(), hashCode() and toString() methods`() = TestCase {
        listOf(
            // regular classes and objects
            SearchOptions::class,
            NavigationProfile::class,
            SearchNavigationOptions::class,
            RouteOptions::class,
            RouteOptions.Deviation.SarType::class,
            RouteOptions.Deviation.Time::class,
            ReverseGeoOptions::class,
            ResponseInfo::class,
            RequestOptions::class,
            IsoLanguageCode::class,
            ImageInfo::class,
            EtaType::class,
            IsoCountryCode::class,
            CategorySearchOptions::class,
            RoutablePoint::class,
            SearchAddress::class,
            SearchAddress.FormatStyle.Short::class,
            SearchAddress.FormatStyle.Medium::class,
            SearchAddress.FormatStyle.Long::class,
            SearchAddress.FormatStyle.Full::class,
            SearchSuggestionType.SearchResultSuggestion::class,
            SearchSuggestionType.Category::class,
            SearchSuggestionType.Brand::class,
            SearchSuggestionType.Query::class,
            SearchSuggestionType.IndexableRecordItem::class,
            FavoriteRecord::class,
            HistoryRecord::class,
            OpenHours.AlwaysOpen::class,
            OpenHours.TemporaryClosed::class,
            OpenHours.PermanentlyClosed::class,
            OpenHours.Scheduled::class,
            ParkingData::class,
            FeedbackEvent::class,
            MissingResultFeedbackEvent::class,
            SelectOptions::class,
            // enums
            QueryType::class,
            ApiType::class,
            SearchResultType::class,
            WeekDay::class,
            // data classes
            OpenPeriod::class,
            WeekTimestamp::class,
        )
            // Check only `data classes` and `classes`, don't check `objects`
            .filter { it.objectInstance == null }
            .forEach { clazz ->
                Given("${clazz.java.simpleName} class") {
                    When("${clazz.java.simpleName} class") {
                        Then("equals() and hashCode() functions should use every declared property") {
                            EqualsVerifier
                                .forClass(clazz.java)
                                .withPrefabValues(
                                    Point::class.java,
                                    Point.fromLngLat(2.0, 5.0),
                                    Point.fromLngLat(27.55140833333333, 53.911334999999994)
                                )
                                .verify()
                        }

                        if (!isEnum(clazz)) {
                            Then("toString() function should use every declared property") {
                                ToStringVerifier(
                                    clazz = clazz,
                                    objectsFactory = reflectionObjectFactory,
                                    includeAllProperties = false
                                ).verify()
                            }
                        }
                    }
                }
            }

        Given("SearchAddress.FormatComponent class") {
            When("SearchAddress.FormatComponent class") {
                Then("equals() and hashCode() functions should use every declared property") {
                    EqualsVerifier.forClass(SearchAddress.FormatComponent::class.java)
                        .withNonnullFields("rawName")
                        .verify()
                }

                Then("toString() function should use every declared property") {
                    ToStringVerifier(
                        clazz = SearchAddress.FormatComponent::class,
                        objectsFactory = reflectionObjectFactory,
                    ).verify()
                }
            }
        }
    }

    @TestFactory
    fun `Test generated copy() method`() = TestCase {
        listOf(
            // custom copy() implementation
            SearchOptions::class,
            ReverseGeoOptions::class,
            RequestOptions::class,
            CategorySearchOptions::class,
            SearchAddress::class,
            FavoriteRecord::class,
            HistoryRecord::class,
            FeedbackEvent::class,
            MissingResultFeedbackEvent::class,
            // data classes
            OpenPeriod::class,
            WeekTimestamp::class,
        )
            .forEach { clazz ->
                Given("${clazz.java.simpleName} class") {
                    When("${clazz.java.simpleName} class") {
                        Then("copy() function should use every declared property") {
                            CopyVerifier(
                                clazz = clazz,
                                objectsFactory = reflectionObjectFactory,
                            ).verify()
                        }
                    }
                }
            }
    }
}
