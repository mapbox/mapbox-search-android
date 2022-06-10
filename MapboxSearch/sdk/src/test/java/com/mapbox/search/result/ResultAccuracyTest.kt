package com.mapbox.search.result

import com.mapbox.search.BuildConfig
import com.mapbox.search.core.CoreResultAccuracy
import com.mapbox.search.tests_support.catchThrowable
import com.mapbox.test.dsl.TestCase
import kotlinx.parcelize.Parcelize
import org.junit.jupiter.api.TestFactory

internal class ResultAccuracyTest {

    @TestFactory
    fun `Check CoreResultAccuracy mapToPlatform()`() = TestCase {
        Given("All CoreResultAccuracy values") {
            CoreResultAccuracy.values().forEach { accuracy ->
                When("$accuracy mapped to platform type") {
                    val platform = when (accuracy) {
                        CoreResultAccuracy.POINT -> ResultAccuracy.Point
                        CoreResultAccuracy.ROOFTOP -> ResultAccuracy.Rooftop
                        CoreResultAccuracy.PARCEL -> ResultAccuracy.Parcel
                        CoreResultAccuracy.INTERPOLATED -> ResultAccuracy.Interpolated
                        CoreResultAccuracy.INTERSECTION -> ResultAccuracy.Intersection
                        CoreResultAccuracy.APPROXIMATE -> ResultAccuracy.Approximate
                        CoreResultAccuracy.STREET -> ResultAccuracy.Street
                    }

                    Then("Mapped value should be $platform", platform, accuracy.mapToPlatform())
                }
            }
        }
    }

    @TestFactory
    fun `Check ResultAccuracy mapToCore()`() = TestCase {
        Given("All ResultAccuracy subtypes") {
            val mapped = listOf(
                ResultAccuracy.Point to CoreResultAccuracy.POINT,
                ResultAccuracy.Rooftop to CoreResultAccuracy.ROOFTOP,
                ResultAccuracy.Parcel to CoreResultAccuracy.PARCEL,
                ResultAccuracy.Interpolated to CoreResultAccuracy.INTERPOLATED,
                ResultAccuracy.Intersection to CoreResultAccuracy.INTERSECTION,
                ResultAccuracy.Approximate to CoreResultAccuracy.APPROXIMATE,
                ResultAccuracy.Street to CoreResultAccuracy.STREET,
            )

            mapped.forEach { (platform, core) ->
                When("$platform mapped to core type") {
                    Then("Core value should be $core", core, platform.mapToCore())
                }
            }

            When("User custom ResultAccuracy mapped to core()") {
                val error = catchThrowable<Exception> {
                    CustomResultAccuracy.mapToCore()
                }
                Then(
                    "Should complete without errors: ${!BuildConfig.DEBUG}",
                    !BuildConfig.DEBUG,
                    error == null
                )
            }
        }
    }

    @Parcelize
    private object CustomResultAccuracy : ResultAccuracy()
}
