package com.mapbox.search.common.concurrent

import android.os.Handler
import android.os.Looper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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

    @Test
    fun `Check AndroidMainThreadWorkerImpl isMainThread on main thread`() {
        mockMainThread()

        val isMainThread = mainThreadWorker.isMainThread

        verify(exactly = 1) {
            Looper.getMainLooper()
        }

        verify(exactly = 1) {
            Looper.myLooper()
        }

        verify(exactly = 0) {
            mainHandler.post(any())
            mainHandler.postDelayed(any(), any(), any())
            mainHandler.removeCallbacksAndMessages(any())
        }

        assertEquals(true, isMainThread)
    }

    @Test
    fun `Check AndroidMainThreadWorkerImpl isMainThread outside of main thread`() {
        mockNonMainThread()

        val isMainThread = mainThreadWorker.isMainThread

        verify(exactly = 1) {
            Looper.getMainLooper()
        }

        verify(exactly = 1) {
            Looper.myLooper()
        }

        verify(exactly = 0) {
            mainHandler.post(any())
            mainHandler.postDelayed(any(), any(), any())
            mainHandler.removeCallbacksAndMessages(any())
        }

        assertEquals(false, isMainThread)
    }

    @Test
    fun `Check AndroidMainThreadWorkerImpl post() on main thread`() {
        mockMainThread()

        val runnable = mockk<Runnable>(relaxed = true)

        mainThreadWorker.post(runnable)

        verify(exactly = 1) {
            runnable.run()
        }

        verify(exactly = 0) {
            mainHandler.post(any())
            mainHandler.postDelayed(any(), any(), any())
            mainHandler.removeCallbacksAndMessages(any())
        }
    }

    @Test
    fun `Check AndroidMainThreadWorkerImpl post() outside of main thread`() {
        mockNonMainThread()

        val runnable = mockk<Runnable>(relaxed = true)

        mainThreadWorker.post(runnable)

        verify(exactly = 0) {
            runnable.run()
        }

        verify(exactly = 1) {
            mainHandler.post(runnable)
        }

        verify(exactly = 0) {
            mainHandler.postDelayed(any(), any(), any())
            mainHandler.removeCallbacksAndMessages(any())
        }
    }

    @Test
    fun `Check AndroidMainThreadWorkerImpl postDelayed()`() {
        val delay = 123L
        val unit = TimeUnit.SECONDS
        val runnable = mockk<Runnable>(relaxed = true)

        mainThreadWorker.postDelayed(delay, unit, runnable)

        verify(exactly = 0) {
            runnable.run()
        }

        verify(exactly = 1) {
            mainHandler.postDelayed(runnable, unit.toMillis(delay))
        }

        verify(exactly = 0) {
            mainHandler.post(any())
            mainHandler.removeCallbacksAndMessages(any())
        }
    }

    @Test
    fun `Check AndroidMainThreadWorkerImpl cancel()`() {
        val runnable = mockk<Runnable>(relaxed = true)

        mainThreadWorker.cancel(runnable)

        verify(exactly = 0) {
            runnable.run()
        }

        verify(exactly = 1) {
            mainHandler.removeCallbacks(runnable)
        }

        verify(exactly = 0) {
            mainHandler.post(any())
            mainHandler.postDelayed(any(), any(), any())
        }
    }
}
