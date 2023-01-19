package com.mapbox.search.common.concurrent

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

internal class SearchSdkMainThreadWorkerTest {

    private lateinit var mainThreadDelegate: MainThreadWorker

    @BeforeEach
    fun setUp() {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)
        every { Looper.myLooper() } returns mockk(relaxed = true)

        mainThreadDelegate = mockk(relaxed = true)
        every { mainThreadDelegate.isMainThread } returns true
        SearchSdkMainThreadWorker.delegate = mainThreadDelegate
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Looper::class)
    }

    @Test
    fun `Check SearchSdkMainThreadWorker's isMainThread`() {
        val value = SearchSdkMainThreadWorker.isMainThread

        verify(exactly = 1) {
            mainThreadDelegate.isMainThread
        }

        assertEquals(true, value)
    }

    @Test
    fun `Check SearchSdkMainThreadWorker's post()`() {
        val runnable = mockk<Runnable>(relaxed = true)
        SearchSdkMainThreadWorker.post(runnable)

        verify(exactly = 1) {
            mainThreadDelegate.post(runnable)
        }
    }

    @Test
    fun `Check SearchSdkMainThreadWorker's postDelayed()`() {
        val delay = 123L
        val unit = TimeUnit.SECONDS
        val runnable = mockk<Runnable>(relaxed = true)

        SearchSdkMainThreadWorker.postDelayed(delay, unit, runnable)

        verify(exactly = 1) {
            mainThreadDelegate.postDelayed(delay, unit, runnable)
        }
    }

    @Test
    fun `Check SearchSdkMainThreadWorker's cancel()`() {
        val runnable = mockk<Runnable>(relaxed = true)
        SearchSdkMainThreadWorker.cancel(runnable)

        verify(exactly = 1) {
            mainThreadDelegate.cancel(runnable)
        }
    }

    @Test
    fun `Check SearchSdkMainThreadWorker reset delegate`() {
        SearchSdkMainThreadWorker.resetDelegate()

        SearchSdkMainThreadWorker.isMainThread

        verify(exactly = 0) {
            mainThreadDelegate.isMainThread
        }

        verify {
            Looper.myLooper()
        }

        verify {
            Looper.getMainLooper()
        }
    }
}
