package com.mapbox.search.core

import com.mapbox.search.TestConstants
import com.mapbox.search.analytics.InternalAnalyticsService
import com.mapbox.search.common.logger.DEFAULT_SEARCH_SDK_LOG_TAG
import com.mapbox.search.common.logger.logd
import com.mapbox.search.common.logger.loge
import com.mapbox.search.common.logger.logi
import com.mapbox.search.common.logger.logw
import com.mapbox.search.core.http.HttpClient
import com.mapbox.search.tests_support.TestMainThreadWorker
import com.mapbox.search.tests_support.TestThreadExecutorService
import com.mapbox.search.utils.UUIDProvider
import com.mapbox.search.utils.concurrent.MainThreadWorker
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

internal class PlatformClientImplTest {

    private lateinit var platformClient: PlatformClientImpl
    private lateinit var httpClient: HttpClient
    private lateinit var analyticsService: InternalAnalyticsService
    private lateinit var uuidProvider: UUIDProvider
    private lateinit var callbackExecutor: ExecutorService
    private lateinit var callbackDecorator: (CoreHttpCallback) -> CoreHttpCallback
    private lateinit var mainThreadWorker: MainThreadWorker

    @BeforeEach
    fun setUp() {
        httpClient = mockk()
        analyticsService = mockk()
        uuidProvider = mockk()
        callbackExecutor = spyk(TestThreadExecutorService())
        mainThreadWorker = spyk(TestMainThreadWorker())
        callbackDecorator = mockk()

        every { callbackDecorator(TEST_HTTP_CALLBACK) } returns TEST_HTTP_CALLBACK_2

        platformClient = PlatformClientImpl(
            httpClient, analyticsService, uuidProvider, mainThreadWorker, callbackDecorator
        )
    }

    @TestFactory
    fun `Check httpGet function`() = TestCase {
        every { httpClient.httpGet(any(), any(), any(), any()) }.returns(Unit)

        Given("PlatformClientImpl with mocked http client") {
            When("Call httpGet()") {
                platformClient.httpRequest(TEST_URL, null, TEST_REQUEST_ID, TEST_SESSION_ID, TEST_HTTP_CALLBACK)

                Verify("Call dispatched to dependency") {
                    httpClient.httpGet(
                        TEST_URL,
                        TEST_REQUEST_ID,
                        TEST_SESSION_ID,
                        TEST_HTTP_CALLBACK_2,
                    )
                }

                Verify("Callback decorator was called") {
                    callbackDecorator(TEST_HTTP_CALLBACK)
                }
            }
        }
    }

    @TestFactory
    fun `Check log function`() = TestCase {
        Given("PlatformClientImpl with mocked search logger") {
            When("Call log(DEBUG)") {
                platformClient.log(CoreLogLevel.DEBUG, TEST_LOG_MSG)

                Verify("Call dispatched to Log") {
                    logd(message = TEST_LOG_MSG, tag = DEFAULT_SEARCH_SDK_LOG_TAG)
                }
            }

            When("Call log(INFO)") {
                platformClient.log(CoreLogLevel.INFO, TEST_LOG_MSG)

                Verify("Call dispatched to Log") {
                    logi(message = TEST_LOG_MSG, tag = DEFAULT_SEARCH_SDK_LOG_TAG)
                }
            }

            When("Call log(WARNING)") {
                platformClient.log(CoreLogLevel.WARNING, TEST_LOG_MSG)

                Verify("Call dispatched to Log") {
                    logw(message = TEST_LOG_MSG, tag = DEFAULT_SEARCH_SDK_LOG_TAG)
                }
            }

            When("Call log(ERROR)") {
                platformClient.log(CoreLogLevel.ERROR, TEST_LOG_MSG)

                Verify("Call dispatched to Log") {
                    loge(message = TEST_LOG_MSG, tag = DEFAULT_SEARCH_SDK_LOG_TAG)
                }
            }
        }
    }

    @TestFactory
    fun `Check postEvent function`() = TestCase {
        every { analyticsService.postJsonEvent(any()) }.returns(Unit)

        Given("PlatformClientImpl with mocked analytics service") {
            When("Call postEvent") {
                platformClient.postEvent(TEST_JSON_EVENT)

                Verify("Call dispatched to dependency") {
                    analyticsService.postJsonEvent(TEST_JSON_EVENT)
                }
            }
        }
    }

    @TestFactory
    fun `Check scheduleTask function`() = TestCase {
        val task: CoreTaskFunction = mockk()
        every { task.run() }.returns(Unit)

        Given("PlatformClientImpl with mocked executor") {
            When("Call scheduleTask()") {
                platformClient.scheduleTask(task, TEST_SCHEDULE_DELAY_MILLIS)

                Verify("Call dispatched to dependency") {
                    mainThreadWorker.postDelayed(
                        TEST_SCHEDULE_DELAY_MILLIS.toLong(),
                        TimeUnit.MILLISECONDS,
                        any()
                    )
                }

                Verify("Task function called") {
                    task.run()
                }
            }
        }
    }

    @TestFactory
    fun `Check uuid function`() = TestCase {
        every { uuidProvider.generateUUID() }.returns(TEST_UUID)

        Given("PlatformClientImpl with mocked UUID provider") {
            When("generateUUID() called") {
                val generatedUUID = platformClient.generateUUID()

                Then("Returned UUID is $TEST_UUID", TEST_UUID, generatedUUID)
                Verify("Call dispatched to dependency") {
                    uuidProvider.generateUUID()
                }
            }
        }
    }

    private companion object {

        const val TEST_URL = "https://mapbox.com"
        const val TEST_SESSION_ID = "session.id.test"
        const val TEST_REQUEST_ID = 0
        const val TEST_UUID = "test-generated-uuid"
        val TEST_HTTP_CALLBACK = CoreHttpCallback { _, _ -> }
        val TEST_HTTP_CALLBACK_2 = CoreHttpCallback { _, _ -> }
        const val TEST_LOG_MSG = "TestMsg"
        const val TEST_SCHEDULE_DELAY_MILLIS = 0
        const val TEST_JSON_EVENT = "{}"

        @Suppress("DEPRECATION", "JVM_STATIC_IN_PRIVATE_COMPANION")
        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            mockkStatic(TestConstants.LOG_KT_CLASS_NAME)
        }

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            unmockkStatic(TestConstants.LOG_KT_CLASS_NAME)
        }
    }
}
