package com.mapbox.search.base

import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import com.mapbox.search.common.TestConstants
import com.mapbox.search.common.catchThrowable
import com.mapbox.search.common.metadata.WeekTimestamp
import com.mapbox.test.dsl.TestCase
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestFactory

private typealias WeekTimestampParams = Triple<Byte, Int, Int>

internal class WeekTimestampTest {

    @TestFactory
    fun `Check validness of WeekTimestamp data`() = TestCase {
        Given("WeekTimestamp constructor") {
            WEEK_TIMESTAMP_VALIDNESS_CHECKS.forEach { (params, isValid) ->
                When("Creating WeekTimestamp$params") {
                    val failedOnAssertion = catchThrowable<Exception> {
                        params.toTimestamp()
                    } != null
                    Then(
                        "Should complete without errors: $isValid",
                        isValid,
                        !failedOnAssertion
                    )
                }
            }
        }
    }

    private fun WeekTimestampParams.toTimestamp(): WeekTimestamp {
        return WeekTimestamp(weekDayFromCore(first), second, third)
    }

    companion object {
        val WEEK_TIMESTAMP_VALIDNESS_CHECKS = mapOf(
            WeekTimestampParams(0, 9, 30) to true,
            WeekTimestampParams(0, 0, 0) to true,
            WeekTimestampParams(6, 24, 0) to true,
            WeekTimestampParams(5, 0, 0) to true,
            WeekTimestampParams(0, 25, 30) to false,
            WeekTimestampParams(2, 5, 60) to false,
        )

        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            resetLogImpl()
            mockkStatic(TestConstants.ASSERTIONS_KT_CLASS_NAME)
        }

        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            reinitializeLogImpl()
            unmockkStatic(TestConstants.ASSERTIONS_KT_CLASS_NAME)
        }
    }
}
