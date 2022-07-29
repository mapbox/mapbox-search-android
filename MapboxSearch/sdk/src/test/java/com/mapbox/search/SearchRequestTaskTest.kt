package com.mapbox.search

import com.mapbox.test.dsl.TestCase
import io.mockk.mockk
import org.junit.jupiter.api.TestFactory

internal class SearchRequestTaskTest {

    @TestFactory
    fun `Check SearchRequestTaskImpl`() = TestCase {
        Given("SearchRequestTaskImpl") {
            var task: SearchRequestTaskImpl<Runnable>

            When("SearchRequestTaskImpl created") {
                task = SearchRequestTaskImpl()
                Then("Task is not executed", false, task.isDone)
                Then("Task is not cancelled", false, task.isCancelled)
            }

            When("SearchRequestTaskImpl marked as executed") {
                val delegate = Runnable {}
                task = SearchRequestTaskImpl(delegate)

                val callbackAction = mockk<Runnable.() -> Unit>(relaxed = true)
                task.markExecutedAndRunOnCallback(callbackAction)

                Verify("Callback action executed") {
                    callbackAction(delegate)
                }

                Then("Task is executed", true, task.isDone)
                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task releases reference to delegate", true, task.callbackDelegate == null)
            }

            When("SearchRequestTaskImpl cancelled") {
                val delegate = Runnable {}
                task = SearchRequestTaskImpl(delegate)

                task.cancel()
                Then("Task is not executed", false, task.isDone)
                Then("Task is cancelled", true, task.isCancelled)
                Then("Task releases reference to delegate", true, task.callbackDelegate == null)
            }

            When("Already executed SearchRequestTaskImpl cancelled") {
                val delegate = Runnable {}
                task = SearchRequestTaskImpl(delegate)

                val callbackAction = mockk<Runnable.() -> Unit>(relaxed = true)
                task.markExecutedAndRunOnCallback(callbackAction)
                task.cancel()

                Verify("Callback action executed") {
                    callbackAction(delegate)
                }

                Then("Task is still executed", true, task.isDone)
                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task releases reference to delegate", true, task.callbackDelegate == null)
            }

            When("Cancelled SearchRequestTaskImpl executed") {
                val delegate = Runnable {}
                task = SearchRequestTaskImpl(delegate)

                task.cancel()

                val callbackAction = mockk<Runnable.() -> Unit>(relaxed = true)
                task.markExecutedAndRunOnCallback(callbackAction)

                VerifyNo("Callback action is not executed") {
                    callbackAction(any())
                }

                Then("Task is not executed", false, task.isDone)
                Then("Task is still cancelled", true, task.isCancelled)
                Then("Task releases reference to delegate", true, task.callbackDelegate == null)
            }

            When("markExecutedAndRunOnCallback() called on non completed task") {
                val delegate = Runnable {}
                task = SearchRequestTaskImpl(delegate)

                val callbackAction = mockk<Runnable.() -> Unit>(relaxed = true)
                task.markCancelledAndRunOnCallback(callbackAction)

                Verify("Callback action executed") {
                    callbackAction(delegate)
                }

                Then("Task is not executed", false, task.isDone)
                Then("Task is cancelled", true, task.isCancelled)
                Then("Task doesn't keep reference to delegate", true, task.callbackDelegate == null)
            }

            When("markExecutedAndRunOnCallback() called on executed task") {
                val delegate = Runnable {}
                task = SearchRequestTaskImpl(delegate)
                task.markExecutedAndRunOnCallback { }

                val callbackAction = mockk<Runnable.() -> Unit>(relaxed = true)
                task.markCancelledAndRunOnCallback(callbackAction)

                VerifyNo("Callback action is not executed") {
                    callbackAction(delegate)
                }

                Then("Task is executed", true, task.isDone)
                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task doesn't keep reference to delegate", true, task.callbackDelegate == null)
            }

            When("markExecutedAndRunOnCallback() called on cancelled task") {
                val delegate = Runnable {}
                task = SearchRequestTaskImpl(delegate)
                task.cancel()

                val callbackAction = mockk<Runnable.() -> Unit>(relaxed = true)
                task.markCancelledAndRunOnCallback(callbackAction)

                VerifyNo("Callback action is not executed") {
                    callbackAction(delegate)
                }

                Then("Task is not executed", false, task.isDone)
                Then("Task is cancelled", true, task.isCancelled)
                Then("Task doesn't keep reference to delegate", true, task.callbackDelegate == null)
            }
        }
    }
}
