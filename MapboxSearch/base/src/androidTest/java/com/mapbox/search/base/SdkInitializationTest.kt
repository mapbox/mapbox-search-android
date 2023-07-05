package com.mapbox.search.base

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mapbox.common.SdkInfoRegistryFactory
import com.mapbox.search.base.utils.UserAgentProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SdkInitializationTest {

    @Test
    fun testSdkInformationProvided() {
        val sdkInformation = SdkInfoRegistryFactory.getInstance().sdkInformation.find {
            it.packageName == UserAgentProvider.sdkPackageName
        }

        assertNotNull(sdkInformation)
        assertEquals(UserAgentProvider.sdkName, sdkInformation!!.name)
        assertEquals(UserAgentProvider.sdkVersionName, sdkInformation.version)
        assertEquals(UserAgentProvider.sdkPackageName, sdkInformation.packageName)
    }
}
