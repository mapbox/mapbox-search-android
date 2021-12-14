package com.mapbox.search.core.http

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import com.mapbox.search.BuildConfig
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory

internal class UserAgentProviderTest {

    private lateinit var packageInfo: PackageInfo
    private lateinit var packageManager: PackageManager
    private lateinit var context: Context
    private lateinit var userAgentProvider: UserAgentProvider

    @BeforeEach
    fun setUp() {
        packageInfo = mockk()
        packageInfo.versionName = TEST_VERSION_NAME
        @Suppress("DEPRECATION")
        packageInfo.versionCode = TEST_VERSION_CODE
        if (Build.VERSION.SDK_INT >= 28) {
            packageInfo.longVersionCode = TEST_VERSION_CODE.toLong()
        }

        packageManager = mockk(relaxed = true)
        every { packageManager.getPackageInfo(TEST_APP_PACKAGE, any()) } returns packageInfo

        context = mockk(relaxed = true)
        every { context.packageName } returns TEST_APP_PACKAGE
        every { context.packageManager } returns packageManager

        userAgentProvider = UserAgentProviderImpl(context)
    }

    @TestFactory
    fun `Test user agent`() = TestCase {
        Given("UserAgentProvider with mocked dependencies and known PackageInfo") {
            every { packageManager.getApplicationLabel(any()) } returns TEST_APP_LABEL

            When("Access user agent") {
                val userAgent = "$TEST_APP_LABEL/$TEST_VERSION_NAME " +
                        "($TEST_APP_PACKAGE; build:$TEST_VERSION_CODE; Android ${Build.VERSION.RELEASE}) " +
                        "MapboxSearchSDK-Android/${BuildConfig.VERSION_NAME}"

                Then("Returned value should be", userAgent, userAgentProvider.userAgent())

                Verify("context.packageName was accessed") {
                    context.packageName
                }

                Verify("packageManager.getApplicationLabel() was accessed") {
                    packageManager.getApplicationLabel(any())
                }
            }
        }
    }

    @TestFactory
    fun `Test user agent with unknown PackageInfo`() = TestCase {
        Given("UserAgentProvider with mocked dependencies and unknown PackageInfo") {
            every { packageManager.getApplicationLabel(any()) } returns TEST_APP_LABEL
            every { packageManager.getPackageInfo(TEST_APP_PACKAGE, any()) } returns null

            When("Access user agent") {
                val userAgent = "$TEST_APP_LABEL/Unknown " +
                        "($TEST_APP_PACKAGE; build:Unknown; Android ${Build.VERSION.RELEASE}) " +
                        "MapboxSearchSDK-Android/${BuildConfig.VERSION_NAME}"

                Then("Returned value should be", userAgent, userAgentProvider.userAgent())

                Verify("context.packageName was accessed") {
                    context.packageName
                }

                Verify("packageManager.getApplicationLabel() was accessed") {
                    packageManager.getApplicationLabel(any())
                }
            }
        }
    }

    @TestFactory
    fun `Test user agent with app name initialized from string resource with ASCII symbols`() = TestCase {
        Given("UserAgentProvider with mocked dependencies") {
            every { packageManager.getApplicationLabel(any()) } returns TEST_LOCALIZED_APP_LABEL

            val appInfo = ApplicationInfo().apply {
                labelRes = TEST_LOCALIZED_APP_NAME_RESOURCE_ID
            }
            every { context.applicationInfo } returns appInfo
            every { context.resources.configuration } returns Configuration()
            val overriddenContext = mockk<Context> {
                every { getText(TEST_LOCALIZED_APP_NAME_RESOURCE_ID) } returns TEST_APP_LABEL
            }
            every { context.createConfigurationContext(any()) } returns overriddenContext

            When("Access user agent") {
                val userAgent = "$TEST_APP_LABEL/$TEST_VERSION_NAME " +
                    "($TEST_APP_PACKAGE; build:$TEST_VERSION_CODE; Android ${Build.VERSION.RELEASE}) " +
                    "MapboxSearchSDK-Android/${BuildConfig.VERSION_NAME}"

                Then("Returned value should be", userAgent, userAgentProvider.userAgent())

                Verify("context.packageName was called") {
                    context.packageName
                }

                Verify("overriddenContext was called") {
                    overriddenContext.getText(TEST_LOCALIZED_APP_NAME_RESOURCE_ID)
                }

                VerifyNo("packageManager.getApplicationLabel() wasn't called") {
                    packageManager.getApplicationLabel(any())
                }
            }
        }
    }

    @TestFactory
    fun `Test user agent with app name initialized from string resource with non-ASCII symbols`() = TestCase {
        Given("UserAgentProvider with mocked dependencies") {
            every { packageManager.getApplicationLabel(any()) } returns TEST_LOCALIZED_APP_LABEL

            val appInfo = ApplicationInfo().apply {
                labelRes = TEST_LOCALIZED_APP_NAME_RESOURCE_ID
            }
            every { context.applicationInfo } returns appInfo
            every { context.resources.configuration } returns Configuration()
            val overriddenContext = mockk<Context> {
                every { getText(TEST_LOCALIZED_APP_NAME_RESOURCE_ID) } returns TEST_LOCALIZED_APP_LABEL
            }
            every { context.createConfigurationContext(any()) } returns overriddenContext

            When("Access user agent") {
                val userAgent = "$TEST_ENCODED_LOCALIZED_APP_LABEL/$TEST_VERSION_NAME " +
                    "($TEST_APP_PACKAGE; build:$TEST_VERSION_CODE; Android ${Build.VERSION.RELEASE}) " +
                    "MapboxSearchSDK-Android/${BuildConfig.VERSION_NAME}"

                Then("Returned value should be", userAgent, userAgentProvider.userAgent())

                Verify("context.packageName was called") {
                    context.packageName
                }

                Verify("overriddenContext was called") {
                    overriddenContext.getText(TEST_LOCALIZED_APP_NAME_RESOURCE_ID)
                }

                VerifyNo("packageManager.getApplicationLabel() wasn't called") {
                    packageManager.getApplicationLabel(any())
                }
            }
        }
    }

    companion object {

        const val TEST_VERSION_NAME = "test-version-name"
        const val TEST_APP_LABEL = "test-app-label"
        const val TEST_LOCALIZED_APP_LABEL = "test-app-label-\uD83D\uDE0F"
        const val TEST_ENCODED_LOCALIZED_APP_LABEL = "test-app-label-__"
        const val TEST_APP_PACKAGE = "test-app-package"
        const val TEST_VERSION_CODE = 123
        const val TEST_LOCALIZED_APP_NAME_RESOURCE_ID = 243
    }
}
