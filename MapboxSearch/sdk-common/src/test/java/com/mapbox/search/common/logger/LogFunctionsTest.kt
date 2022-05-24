package com.mapbox.search.common.logger

import com.mapbox.common.CommonSdkLog
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory

internal class LogFunctionsTest {

    @BeforeEach
    fun setUp() {
        mockkObject(CommonSdkLog)
        every { CommonSdkLog.logd(any(), any()) } returns Unit
        every { CommonSdkLog.logi(any(), any()) } returns Unit
        every { CommonSdkLog.logw(any(), any()) } returns Unit
        every { CommonSdkLog.loge(any(), any()) } returns Unit
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(CommonSdkLog)
    }

    @TestFactory
    fun `Check logger`() = TestCase {
        Given("Logger functions") {
            When("Call debug log") {
                logd(message = MESSAGE, tag = TAG)
                VerifyOnce("Call passed to logger instance") {
                    CommonSdkLog.logd(TAG, MESSAGE)
                }
            }

            When("Call info log") {
                logi(message = MESSAGE, tag = TAG)
                VerifyOnce("Call passed to logger instance") {
                    CommonSdkLog.logi(TAG, MESSAGE)
                }
            }

            When("Call warning log") {
                logw(message = MESSAGE, tag = TAG)
                VerifyOnce("Call passed to logger instance") {
                    CommonSdkLog.logw(TAG, MESSAGE)
                }
            }

            When("Call error log") {
                loge(message = MESSAGE, tag = TAG)
                VerifyOnce("Call passed to logger instance") {
                    CommonSdkLog.loge(TAG, MESSAGE)
                }
            }
        }
    }

    private companion object {
        const val MESSAGE = "message"
        const val TAG = "tag"
    }
}
