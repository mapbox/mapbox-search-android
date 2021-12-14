package com.mapbox.search

import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.TestFactory

internal class HighlightsCalculatorTest {

    @TestFactory
    fun `HighlightsCalculator test`() = TestCase {
        Given("HighlightsCalculatorImpl with mocked engine") {
            testData().forEach { testData ->
                val highlightsEngine = mockk<HighlightsEngine>()
                every { highlightsEngine.getHighlights(any(), any()) } returns testData.highlights
                val highlightsCalculator = HighlightsCalculatorImpl(highlightsEngine)

                When("Calculate highlights with query: ${testData.queryName} and core highlights: ${testData.highlights}") {
                    val actualHighlights = highlightsCalculator.highlights(testData.queryName, TEST_QUERY)

                    Then("Highlights should be ${testData.expectedHighlights}", testData.expectedHighlights, actualHighlights)
                    Verify("getHighlights() function called 1 time", exactly = 1) {
                        highlightsEngine.getHighlights(testData.queryName, TEST_QUERY)
                    }
                }
            }
        }
    }

    class TestData(val queryName: String, val highlights: List<Int>, val expectedHighlights: List<Pair<Int, Int>>)

    private companion object {

        const val TEST_QUERY = "test query"

        fun testData() = listOf(

            // correct highlights
            TestData(
                "0123456789", listOf(0, 1, 3, 5, 7, 10), listOf(
                    0 to 1,
                    3 to 5,
                    7 to 10
                )
            ),

            // fully highlighted query
            TestData(
                "Test name",
                listOf(0, "Test name".length),
                listOf(0 to "Test name".length)
            ),

            // no highlights
            TestData("", emptyList(), emptyList()),

            // empty highlights
            TestData(
                "0123456789",
                listOf(0, 0, 1, 1, 2, 2),
                emptyList()
            ),

            // incomplete highlights
            TestData("", listOf(0, 1, 3), emptyList()),

            // index out of query bounds
            TestData(
                "0123456789",
                listOf(0, "0123456789".length + 1),
                emptyList()
            ),

            // incorrect highlights
            TestData("", listOf(2, 1), emptyList()),

            // negative indices
            TestData(
                "Test name",
                listOf(-1, 1),
                emptyList()
            )
        )
    }
}
