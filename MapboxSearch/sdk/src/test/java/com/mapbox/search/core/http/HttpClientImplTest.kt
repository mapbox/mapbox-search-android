package com.mapbox.search.core.http

import com.mapbox.search.core.CoreHttpCallback
import com.mapbox.search.utils.UUIDProvider
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mock.MediaTypes.MEDIATYPE_JSON
import okhttp3.mock.MockInterceptor
import okhttp3.mock.body
import okhttp3.mock.respond
import okhttp3.mock.rule
import okhttp3.mock.startWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import java.io.IOException

internal class HttpClientImplTest {

    private lateinit var mockedHttpErrorsCache: HttpErrorsCache
    private lateinit var mockedOkHttp: OkHttpClient
    private lateinit var mockedUUIDProvider: UUIDProvider
    private lateinit var mockedUserAgentProvider: UserAgentProvider

    @BeforeEach
    fun setUp() {
        mockedHttpErrorsCache = mockk(relaxed = true)
        mockedOkHttp = mockk()

        mockedUUIDProvider = mockk(relaxed = true)
        every { mockedUUIDProvider.generateUUID() } returns TEST_UUID

        mockedUserAgentProvider = mockk()
        every { mockedUserAgentProvider.userAgent() } returns TEST_USER_AGENT
    }

    @TestFactory
    fun `Check url and headers`() = TestCase {
        val testUrl = "https://api.mapbox.com/"
        val testSessionId = "Test session id"

        Given("HttpClientImpl with mocked OkHttpClient") {
            val coreCallback = spyk<CoreHttpCallback>()
            val slot = slot<Request>()
            every { mockedOkHttp.newCall(capture(slot)) } returns mockk()

            val httpClient = HttpClientImpl(mockedOkHttp, mockedHttpErrorsCache, mockedUUIDProvider, mockedUserAgentProvider)

            When("Send request $testUrl with sessionId=\"$testSessionId\"") {
                httpClient.httpGet(testUrl, 0, testSessionId, coreCallback)

                Then(
                    "Url should be $testUrl",
                    testUrl,
                    slot.captured.url.toString()
                )

                Then(
                    "Session id should be \"$testSessionId\"",
                    testSessionId,
                    slot.captured.header("X-MBX-SEARCH-SID")
                )

                Then(
                    "'X-Request-ID' header should be '$TEST_UUID'",
                    TEST_UUID,
                    slot.captured.header("X-Request-ID")
                )

                Verify("UUID received from provider") {
                    mockedUUIDProvider.generateUUID()
                }

                Then(
                    "'User-Agent' header should be '$TEST_USER_AGENT'",
                    TEST_USER_AGENT,
                    slot.captured.header("User-Agent")
                )

                Verify("User agent received from provider") {
                    mockedUserAgentProvider.userAgent()
                }
            }
        }
    }

    @TestFactory
    fun `Check http client implementation`() = TestCase {
        Given("HttpClientImpl with mocked client") {
            val coreCallback = spyk<CoreHttpCallback>()
            testData.forEach { (input, expected) ->
                val httpClient = HttpClientImpl(
                    OkHttpHelper(debugLogsEnabled = false).getMockedClient(input.getInterceptor()),
                    mockedHttpErrorsCache,
                    mockedUUIDProvider,
                    mockedUserAgentProvider
                )
                When("Send request ${input.url}") {
                    httpClient.httpGet(input.url, TEST_REQUEST_ID, "", coreCallback)
                    Verify(
                        "Callback with body = ${expected.body}, code = ${expected.code}",
                        timeoutMs = 500L
                    ) {
                        coreCallback.run(expected.body, expected.code)
                    }

                    if (expected.isError) {
                        Verify("Error saved to errors cache", timeoutMs = 500L) {
                            mockedHttpErrorsCache.put(TEST_REQUEST_ID, any())
                        }
                    }
                }
            }
        }
    }

    private companion object {

        const val TEST_UUID = "test-generated-uuid"
        const val TEST_USER_AGENT = "test-user-agent"
        const val TEST_REQUEST_ID = 0

        val testData = mapOf(
            NormalInput("http://request1", 200, "") to ExpectedCall("", 200, isError = false),
            NormalInput("http://request2", 500, "error") to ExpectedCall("error", 500, isError = true),
            NormalInput(
                "htt://request3",
                500,
                "error"
            ) to ExpectedCall(
                "Expected URL scheme 'http' or 'https' but was 'htt'",
                Int.MIN_VALUE,
                isError = true
            ),
            ExceptionInput("http://request4", 500, IOException()) to ExpectedCall(
                "http error",
                Int.MIN_VALUE,
                isError = true
            ),
            ExceptionInput("http://request5", 500, IOException("error")) to ExpectedCall(
                "error",
                Int.MIN_VALUE,
                isError = true
            )
        )
    }
}

internal sealed class InputParams(open val url: String, open val httpCode: Int) {
    abstract fun getInterceptor(): MockInterceptor
}

internal data class ExceptionInput(
    override val url: String,
    override val httpCode: Int,
    val exception: Exception
) :
    InputParams(url, httpCode) {

    override fun getInterceptor() = MockInterceptor().apply {
        rule(okhttp3.mock.url startWith url) {
            respond(code = httpCode) {
                throw exception
            }
        }
    }
}

internal data class NormalInput(
    override val url: String,
    override val httpCode: Int,
    val body: String
) : InputParams(url, httpCode) {

    override fun getInterceptor() = MockInterceptor().apply {
        rule(okhttp3.mock.url startWith url) {
            respond(code = httpCode) {
                body(body, MEDIATYPE_JSON)
            }
        }
    }
}

internal data class ExpectedCall(val body: String, val code: Int, val isError: Boolean)
