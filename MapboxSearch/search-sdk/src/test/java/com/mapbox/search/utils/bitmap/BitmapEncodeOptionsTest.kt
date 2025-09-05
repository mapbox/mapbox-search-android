package com.mapbox.search.utils.bitmap

import com.mapbox.search.common.tests.catchThrowable
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory
import java.lang.IllegalStateException

internal class BitmapEncodeOptionsTest {

    @TestFactory
    fun `Check BitmapEncodeOptions creation validity`() = TestCase {
        TEST_BITMAP_OPTIONS_PARAMS.forEach { (params, isValid) ->
            val (width, compressQuality) = params
            Given("width = $width, compressQuality = $compressQuality") {
                When("Create BitmapEncodeOptions with this params") {
                    val assertionPassed = catchThrowable<IllegalStateException> {
                        BitmapEncodeOptions(minSideSize = width, compressQuality = compressQuality)
                    } == null
                    Then("Event is valid should be <$isValid>", isValid, assertionPassed)
                }
            }
        }
    }

    private companion object {

        val TEST_BITMAP_OPTIONS_PARAMS: Map<Pair<Int, Int>, Boolean>
            get() = mapOf(
                (100 to 50) to true,
                (1 to 50) to true,
                (0 to 50) to false,
                (-1 to 50) to false,
                (100 to 100) to true,
                (100 to 101) to false,
                (100 to 0) to true,
                (100 to -1) to false,
            )
    }
}
