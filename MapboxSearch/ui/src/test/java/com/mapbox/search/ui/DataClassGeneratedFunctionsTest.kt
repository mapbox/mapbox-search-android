package com.mapbox.search.ui

import com.mapbox.geojson.Point
import com.mapbox.search.common.tests.CopyVerifier
import com.mapbox.search.common.tests.ReflectionObjectsFactory
import com.mapbox.search.common.tests.ToStringVerifier
import com.mapbox.search.common.tests.isEnum
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.ui.tests_support.UiCustomTypeObjectCreators
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.SearchMode
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.favorite.FavoriteTemplate
import com.mapbox.search.ui.view.feedback.IncorrectSearchPlaceFeedback
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.test.dsl.TestCase
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.TestFactory
import kotlin.reflect.KClass

/**
 * Test to track migration from data/sealed/enum class to a regular class.
 */
internal class DataClassGeneratedFunctionsTest {

    private val reflectionObjectFactory = ReflectionObjectsFactory(
        extraCreators = UiCustomTypeObjectCreators.ALL_CREATORS,
        subclassProvider = this::provideSubclass
    )

    @TestFactory
    fun `Test generated equals(), hashCode() and toString() methods`() = TestCase {
        listOf(
            // regular classes
            SearchPlace::class,
            FavoriteTemplate::class,
            Category::class,
            // TODO(#737): enable test for SearchResultFeedback
            // IncorrectSearchPlaceFeedback.SearchResultFeedback::class,
            IncorrectSearchPlaceFeedback.FavoriteFeedback::class,
            IncorrectSearchPlaceFeedback.HistoryFeedback::class,
            // enums
            SearchBottomSheetView.CollapsedStateAnchor::class,
            SearchMode::class,
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
                                .verify()
                        }

                        if (!isEnum(clazz)) {
                            Then("toString() function should use every declared property") {
                                ToStringVerifier(
                                    clazz = clazz,
                                    objectsFactory = reflectionObjectFactory,
                                ).verify()
                            }
                        }
                    }
                }
            }
    }

    @TestFactory
    fun `Test generated copy() method`() = TestCase {
        listOf(
            // custom copy() implementation
            SearchPlace::class,
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

    private fun provideSubclass(clazz: KClass<*>): KClass<*>? {
        return when (clazz) {
            IndexableRecord::class -> FavoriteRecord::class
            else -> null
        }
    }
}
