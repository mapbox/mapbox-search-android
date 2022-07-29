package com.mapbox.search.common.concurrent

import android.os.Handler
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

internal class AndroidMainThreadWorkerImplTest {

    private lateinit var mainHandler: Handler
    private lateinit var mainThreadWorker: AndroidMainThreadWorkerImpl

    @BeforeEach
    fun setUp() {
        mainHandler = mockk(relaxed = true)
        mainThreadWorker = AndroidMainThreadWorkerImpl(mainHandler)

        mockkStatic(Looper::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Looper::class)
    }

    private fun mockMainThread(): Looper {
        val mainLooperMock = mockk<Looper>(relaxed = true)
        every { Looper.getMainLooper() } returns mainLooperMock
        every { Looper.myLooper() } returns mainLooperMock
        return mainLooperMock
    }

    private fun mockNonMainThread() {
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        every { Looper.myLooper() } returns mockk(relaxed = true)
    }

    @TestFactory
    fun `Check AndroidMainThreadWorkerImpl isMainThread on main thread`() = TestCase {
        Given("AndroidMainThreadWorkerImpl default implementation") {
            When("isMainThread called on main thread") {
                mockMainThread()

                val isMainThread = mainThreadWorker.isMainThread

                VerifyOnce("Looper.getMainLooper() called") {
                    Looper.getMainLooper()
                }

                VerifyOnce("Looper.myLooper() called") {
                    Looper.myLooper()
                }

                VerifyNo("No interactions with Handler") {
                    mainHandler.post(any())
                    mainHandler.postDelayed(any(), any(), any())
                    mainHandler.removeCallbacksAndMessages(any())
                }

                Then("isMainThread is true", true, isMainThread)
            }
        }
    }

    @TestFactory
    fun `Check AndroidMainThreadWorkerImpl isMainThread outside of main thread`() = TestCase {
        Given("AndroidMainThreadWorkerImpl default implementation") {
            When("isMainThread called outside main thread") {
                mockNonMainThread()

                val isMainThread = mainThreadWorker.isMainThread

                VerifyOnce("Looper.getMainLooper() called") {
                    Looper.getMainLooper()
                }

                VerifyOnce("Looper.myLooper() called") {
                    Looper.myLooper()
                }

                VerifyNo("No interactions with Handler") {
                    mainHandler.post(any())
                    mainHandler.postDelayed(any(), any(), any())
                    mainHandler.removeCallbacksAndMessages(any())
                }

                Then("isMainThread is false", false, isMainThread)
            }
        }
    }

    @TestFactory
    fun `Check AndroidMainThreadWorkerImpl post() on main thread`() = TestCase {
        Given("AndroidMainThreadWorkerImpl default implementation") {
            When("post() called on main thread") {
                mockMainThread()

                val runnable = mockk<Runnable>(relaxed = true)

                mainThreadWorker.post(runnable)

                VerifyOnce("Runnable.run() called") {
                    runnable.run()
                }

                VerifyNo("No interactions with Handler") {
                    mainHandler.post(any())
                    mainHandler.postDelayed(any(), any(), any())
                    mainHandler.removeCallbacksAndMessages(any())
                }
            }
        }
    }

    @TestFactory
    fun `Check AndroidMainThreadWorkerImpl post() outside of main thread`() = TestCase {
        Given("AndroidMainThreadWorkerImpl default implementation") {
            When("post() called outside of main thread") {
                mockNonMainThread()

                val runnable = mockk<Runnable>(relaxed = true)

                mainThreadWorker.post(runnable)

                VerifyNo("Runnable.run() is not called immediately") {
                    runnable.run()
                }

                VerifyOnce("Runnable passed to Handler") {
                    mainHandler.post(runnable)
                }

                VerifyNo("No other interactions with Handler") {
                    mainHandler.postDelayed(any(), any(), any())
                    mainHandler.removeCallbacksAndMessages(any())
                }
            }
        }
    }

    @TestFactory
    fun `Check AndroidMainThreadWorkerImpl postDelayed()`() = TestCase {
        Given("AndroidMainThreadWorkerImpl default implementation") {
            When("postDelayed() called") {
                val delay = 123L
                val unit = TimeUnit.SECONDS
                val runnable = mockk<Runnable>(relaxed = true)

                mainThreadWorker.postDelayed(delay, unit, runnable)

                VerifyNo("Runnable.run() is not called immediately") {
                    runnable.run()
                }

                VerifyOnce("Handler.postDelayed() called with correct arguments") {
                    mainHandler.postDelayed(runnable, unit.toMillis(delay))
                }

                VerifyNo("No other interactions with Handler") {
                    mainHandler.post(any())
                    mainHandler.removeCallbacksAndMessages(any())
                }
            }
        }
    }

    @TestFactory
    fun `Check AndroidMainThreadWorkerImpl cancel()`() = TestCase {
        Given("AndroidMainThreadWorkerImpl default implementation") {
            When("cancel() called") {
                val runnable = mockk<Runnable>(relaxed = true)

                mainThreadWorker.cancel(runnable)

                VerifyNo("Runnable.run() is not called") {
                    runnable.run()
                }

                VerifyOnce("Handler.removeCallbacks() called with correct argument") {
                    mainHandler.removeCallbacks(runnable)
                }

                VerifyNo("No other interactions with Handler") {
                    mainHandler.post(any())
                    mainHandler.postDelayed(any(), any(), any())
                }
            }
        }
    }
}
