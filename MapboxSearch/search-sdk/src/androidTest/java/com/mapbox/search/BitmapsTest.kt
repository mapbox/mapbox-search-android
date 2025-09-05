package com.mapbox.search

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.mapbox.search.utils.bitmap.BitmapEncodeOptions
import com.mapbox.search.utils.bitmap.encodeBase64
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

internal class BitmapsTest : BaseTest() {

    // TODO(#418): Fix failing JPEG comparison test
    @Ignore("""
       Bitmap comparison fails for emulators / devices, that has different
       OS version / screen density. Commenting this test for now.
    """)
    @Test
    fun checkBase64EncodingForPortraitScreenshot() {
        val originalScreenshot = readBitmapFromAssets("screenshots/original_screenshot.png")
        val expectedValue = readBitmapFromAssets("screenshots/compressed_screenshot_400-90.jpeg")

        val encodeOptions = BitmapEncodeOptions(minSideSize = 400, compressQuality = 90)
        val base64Screenshot = originalScreenshot.encodeBase64(encodeOptions)
        val rawBytes = Base64.decode(base64Screenshot, Base64.DEFAULT)
        val actualValue = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)

        Assert.assertTrue(actualValue.sameAs(expectedValue))
    }

    private fun readBitmapFromAssets(fileName: String): Bitmap {
        val descriptor = targetApplication.resources.assets.open(fileName)
        return BitmapFactory.decodeStream(descriptor)
    }
}
