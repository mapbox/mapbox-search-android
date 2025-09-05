package com.mapbox.search.utils.bitmap

import android.graphics.Bitmap
import com.mapbox.test.dsl.TestCase
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestFactory

internal class BitmapsTest {

    @TestFactory
    fun `Check encodeBase64()`() = TestCase {
        Given("BitmapEncodeOptions with mocked big portrait Bitmap") {
            val mocks = mockBitmapForCheckingEncode(width = 768, height = 1024)
            val options = BitmapEncodeOptions(minSideSize = 210, compressQuality = 50)

            When("Bitmap width(768) > options.minSideSize(210)") {
                mocks.bitmap.encodeBase64(options)

                Then("Scaled width is 210", 210, mocks.scaledWidth.captured)

                Then("Scaled height is 280", 280, mocks.scaledHeight.captured)

                Then("Scaled quality is 50", 50, mocks.compressQuality.captured)
            }
        }

        Given("BitmapEncodeOptions with mocked big landscape Bitmap") {
            val mocks = mockBitmapForCheckingEncode(width = 1024, height = 768)
            val options = BitmapEncodeOptions(minSideSize = 210, compressQuality = 50)

            When("Bitmap height(768) > options.minSideSize(210)") {
                mocks.bitmap.encodeBase64(options)

                Then("Scaled width is 280", 280, mocks.scaledWidth.captured)

                Then("Scaled height is 210", 210, mocks.scaledHeight.captured)

                Then("Scaled quality is 50", 50, mocks.compressQuality.captured)
            }
        }

        Given("BitmapEncodeOptions with mocked small portrait Bitmap") {
            val mocks = mockBitmapForCheckingEncode(width = 210, height = 280)
            val options = BitmapEncodeOptions(minSideSize = 768, compressQuality = 50)

            When("Bitmap width(210) < options.minSideSize(768)") {
                mocks.bitmap.encodeBase64(options)

                Then("Scaled width is 210", 210, mocks.scaledWidth.captured)

                Then("Scaled height is 280", 280, mocks.scaledHeight.captured)

                Then("Scaled quality is 50", 50, mocks.compressQuality.captured)
            }
        }
    }

    private fun mockBitmapForCheckingEncode(width: Int, height: Int): BitmapWithSlot {
        val bitmap: Bitmap = mockk()
        every { bitmap.width }.returns(width)
        every { bitmap.height }.returns(height)

        val scaledBitmap: Bitmap = mockk()
        val compressQualitySlot = slot<Int>()
        every { scaledBitmap.compress(any(), capture(compressQualitySlot), any()) }.returns(true)

        val widthSlot = slot<Int>()
        val heightSlot = slot<Int>()
        every { Bitmap.createScaledBitmap(any(), capture(widthSlot), capture(heightSlot), any()) } answers { scaledBitmap }

        return BitmapWithSlot(bitmap, compressQualitySlot, widthSlot, heightSlot)
    }

    private data class BitmapWithSlot(
        val bitmap: Bitmap,
        val compressQuality: CapturingSlot<Int>,
        val scaledWidth: CapturingSlot<Int>,
        val scaledHeight: CapturingSlot<Int>
    )

    companion object {

        @BeforeAll
        @JvmStatic
        fun setUp() {
            mockkStatic(Bitmap::class)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            unmockkStatic(Bitmap::class)
        }
    }
}
