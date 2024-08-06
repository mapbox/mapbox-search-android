package com.mapbox.search.ui.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

@OptIn(ExperimentalCoroutinesApi::class)
internal class DebouncerTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Test action executes after specified time`() = runTest {
        val action = mock(Runnable::class.java)
        val debouncer = Debouncer(500L)

        debouncer.debounce { action.run() }
        advanceTimeBy(499L)
        runCurrent()

        verify(action, never()).run()

        advanceTimeBy(1L)
        runCurrent()

        verify(action, times(1)).run()
    }

    @Test
    fun `Test previous action is cancelled and never fires`() = runTest {
        val action1 = mock(Runnable::class.java)
        val action2 = mock(Runnable::class.java)
        val debouncer = Debouncer(500L)

        // start the first action
        debouncer.debounce { action1.run() }
        advanceTimeBy(300L)

        // trigger the second which should cancel the first
        debouncer.debounce { action2.run() }
        advanceTimeBy(200L)

        // run all currently triggered
        runCurrent()

        // verify nothing ran
        verify(action1, never()).run()
        verify(action2, never()).run()

        // move forward so the second action runs
        advanceTimeBy(300L)
        runCurrent()

        verify(action2, times(1)).run()
    }
}
