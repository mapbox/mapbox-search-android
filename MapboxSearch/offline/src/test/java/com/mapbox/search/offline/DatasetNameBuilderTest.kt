package com.mapbox.search.offline

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class DatasetNameBuilderTest {

    @ParameterizedTest
    @MethodSource("testData")
    fun testDatasetName(testData: TestData) {
        assertEquals(
            testData.expected,
            DatasetNameBuilder.buildDatasetName(testData.datasetName, testData.language, testData.worldview)
        )
    }

    @Test
    fun testNullLanguageAndNonNullWorldview() {
        assertThrows<IllegalArgumentException> {
            DatasetNameBuilder.buildDatasetName("mbx", null, "fr")
        }
    }

    @Test
    fun testLanguageIncorrectFormat() {
        assertThrows<IllegalArgumentException> {
            DatasetNameBuilder.buildDatasetName("mbx", "test", null)
        }
    }

    @Test
    fun testWorldviewIncorrectFormat() {
        assertThrows<IllegalArgumentException> {
            DatasetNameBuilder.buildDatasetName("mbx", "fr", "test")
        }
    }

    class TestData(
        val datasetName: String,
        val language: String?,
        val worldview: String?,
        val expected: String
    )

    companion object {

        @JvmStatic
        fun testData(): List<TestData> = listOf(
            TestData("mbx", null, null, "mbx"),
            TestData("mbx", "en", null, "mbx_en"),
            TestData("mbx", "En", null, "mbx_en"),
            TestData("mbx", "en", "tr", "mbx_en-tr"),
            TestData("mbx", "En", "Tr", "mbx_en-tr"),
        )
    }
}
