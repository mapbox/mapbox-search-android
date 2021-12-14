package com.mapbox.search.common.logger

import com.mapbox.base.common.logger.model.Message
import com.mapbox.base.common.logger.model.Tag
import com.mapbox.test.dsl.TestCase
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory

internal class LogFunctionsTest {

    @BeforeEach
    fun setUp() {
        searchSdkLogger = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        searchSdkLogger = null
    }

    @TestFactory
    fun `Check logger`() = TestCase {
        Given("Logger functions") {
            When("Call debug log") {
                logd(message = MESSAGE, tag = TAG)
                VerifyOnce("Call passed to logger instance") {
                    searchSdkLogger?.d(tag = Tag(TAG), msg = Message(MESSAGE))
                }
            }

            When("Call debug log with exception") {
                logd(throwable = ERROR_CAUSE, message = MESSAGE, tag = TAG)
                VerifyOnce("Call passed to logger instance") {
                    searchSdkLogger?.d(tag = Tag(TAG), msg = Message(MESSAGE), tr = ERROR_CAUSE)
                }
            }

            When("Call info log") {
                logi(message = MESSAGE, tag = TAG)
                VerifyOnce("Call passed to logger instance") {
                    searchSdkLogger?.i(tag = Tag(TAG), msg = Message(MESSAGE))
                }
            }

            When("Call info log with exception") {
                logi(throwable = ERROR_CAUSE, message = MESSAGE, tag = TAG)
                VerifyOnce("Call passed to logger instance") {
                    searchSdkLogger?.i(tag = Tag(TAG), msg = Message(MESSAGE), tr = ERROR_CAUSE)
                }
            }

            When("Call warning log") {
                logw(message = MESSAGE, tag = TAG)
                VerifyOnce("Call passed to logger instance") {
                    searchSdkLogger?.w(tag = Tag(TAG), msg = Message(MESSAGE))
                }
            }

            When("Call warning log with exception") {
                logw(throwable = ERROR_CAUSE, message = MESSAGE, tag = TAG)
                VerifyOnce("Call passed to logger instance") {
                    searchSdkLogger?.w(tag = Tag(TAG), msg = Message(MESSAGE), tr = ERROR_CAUSE)
                }
            }

            When("Call error log") {
                loge(message = MESSAGE, tag = TAG)
                VerifyOnce("Call passed to logger instance") {
                    searchSdkLogger?.e(tag = Tag(TAG), msg = Message(MESSAGE))
                }
            }

            When("Call error log with exception") {
                loge(throwable = ERROR_CAUSE, message = MESSAGE, tag = TAG)
                VerifyOnce("Call passed to logger instance") {
                    searchSdkLogger?.e(tag = Tag(TAG), msg = Message(MESSAGE), tr = ERROR_CAUSE)
                }
            }
        }
    }

    private companion object {
        const val MESSAGE = "message"
        const val TAG = "tag"
        val ERROR_CAUSE = Exception()
    }
}
