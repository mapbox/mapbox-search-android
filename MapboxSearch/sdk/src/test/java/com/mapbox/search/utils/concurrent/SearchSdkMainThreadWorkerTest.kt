package com.mapbox.search.utils.concurrent

import android.os.Looper
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import java.util.concurrent.TimeUnit

internal class SearchSdkMainThreadWorkerTest {

    @BeforeEach
    fun setUp() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        every { Looper.myLooper() } returns mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Looper::class)
    }

    @TestFactory
    fun `Check SearchSdkMainThreadWorker delegate`() = TestCase {
        Given("SearchSdkMainThreadWorker object with mocked delegate") {
            val delegate = mockk<MainThreadWorker>(relaxed = true)
            every { delegate.isMainThread } returns true

            SearchSdkMainThreadWorker.delegate = delegate

            When("isMainThread property called") {
                val value = SearchSdkMainThreadWorker.isMainThread

                VerifyOnce("Delegate should be called") {
                    delegate.isMainThread
                }

                Then("Returned value should be as in delegate", true, value)
            }

            When("post() called") {
                val runnable = mockk<Runnable>(relaxed = true)
                SearchSdkMainThreadWorker.post(runnable)

                VerifyOnce("Delegate's post() called with correct argument") {
                    delegate.post(runnable)
                }
            }

            When("postDelayed() called") {
                val delay = 123L
                val unit = TimeUnit.SECONDS
                val runnable = mockk<Runnable>(relaxed = true)

                SearchSdkMainThreadWorker.postDelayed(delay, unit, runnable)

                VerifyOnce("Delegate's postDelayed() called with correct argument") {
                    delegate.postDelayed(delay, unit, runnable)
                }
            }

            When("cancel() called") {
                val runnable = mockk<Runnable>(relaxed = true)
                SearchSdkMainThreadWorker.cancel(runnable)

                VerifyOnce("Delegate's cancel() called with correct argument") {
                    delegate.cancel(runnable)
                }
            }
        }
    }

    @TestFactory
    fun `Check SearchSdkMainThreadWorker reset delegate`() = TestCase {
        Given("SearchSdkMainThreadWorker object") {
            When("External delegate set and then resetDelegate() called") {
                val delegate = mockk<MainThreadWorker>(relaxed = true)
                SearchSdkMainThreadWorker.delegate = delegate
                SearchSdkMainThreadWorker.resetDelegate()

                SearchSdkMainThreadWorker.isMainThread

                VerifyNo("External delegate shouldn't be called") {
                    delegate.isMainThread
                }

                Verify("Looper.myLooper() is called") {
                    Looper.myLooper()
                }

                Verify("Looper.getMainLooper() is called") {
                    Looper.getMainLooper()
                }
            }
        }
    }
}
