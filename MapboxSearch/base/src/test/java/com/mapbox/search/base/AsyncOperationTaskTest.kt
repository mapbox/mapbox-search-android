package com.mapbox.search.base

import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.base.task.ExtendedAsyncOperationTask
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.TestFactory
import java.util.concurrent.Future

internal class AsyncOperationTaskTest {

    @TestFactory
    fun `Check CompletedAsyncOperationTask`() = TestCase {
        Given("CompletedAsyncOperationTask object") {
            val task = AsyncOperationTaskImpl.COMPLETED
            When("CompletedAsyncOperationTask is in initial state") {
                Then("Task is always done", true, task.isDone)
                Then("Task is not cancelled", false, task.isCancelled)
            }

            When("Call CompletedAsyncOperationTask cancel()") {
                task.cancel()
                Then("Task is always done", true, task.isDone)
                Then("Task is not cancelled", false, task.isCancelled)
            }
        }
    }

    @TestFactory
    fun `Check AsyncOperationTaskImpl`() = TestCase {
        Given("AsyncOperationTaskImpl instance") {

            fun mockOnCancelledCallback(): () -> Unit {
                val callback = mockk<() -> Unit>(relaxed = true)
                every { callback() } returns Unit
                return callback
            }

            When("Object is in initial state") {
                val task = AsyncOperationTaskImpl<Any>()

                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task is not done", false, task.isDone)
            }

            When("Object cancelled") {
                val task = AsyncOperationTaskImpl<Any>()

                val wrappedTask = mockk<Future<*>>(relaxed = true)
                task += wrappedTask

                val callback = mockOnCancelledCallback()
                task.onCancelCallback = callback

                task.cancel()

                Verify("cancel() called on a wrapped object") {
                    wrappedTask.cancel(true)
                }

                VerifyOnce("onCancelCallback called") {
                    callback()
                }

                Then("Task is cancelled", true, task.isCancelled)
                Then("Task is not done", false, task.isDone)
                Then("onCancelCallback cleared", null, task.onCancelCallback)

                task.onCancelCallback = callback

                Then("onCancelCallback can't be set on a cancelled object", null, task.onCancelCallback)
            }

            When("onComplete() called on a cancelled object") {
                val task = AsyncOperationTaskImpl<Any>()

                val callback = mockOnCancelledCallback()
                task.onCancelCallback = callback

                task.cancel()

                task.onComplete()

                Then("Task is cancelled", true, task.isCancelled)
                Then("Task is not done", false, task.isDone)

                VerifyOnce("Callback is not called again") {
                    callback()
                }
            }

            When("onComplete() called more than once") {
                val task = AsyncOperationTaskImpl<Any>()

                val callback = mockOnCancelledCallback()
                task.onCancelCallback = callback

                task.onComplete()
                task.onComplete()

                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task is done", true, task.isDone)

                VerifyNo("Callback is not called") {
                    callback()
                }
            }

            When("cancel() called on a completed task") {
                val task = AsyncOperationTaskImpl<Any>()

                val callback = mockOnCancelledCallback()
                task.onCancelCallback = callback

                val wrappedTask = mockk<Future<*>>(relaxed = true)
                task += wrappedTask

                task.onComplete()
                task.cancel()

                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task is done", true, task.isDone)

                VerifyNo("Callback is not called") {
                    callback()
                }

                VerifyNo("cancel() is not called on a wrapped task") {
                    wrappedTask.cancel(any())
                }
            }

            When("Inner task added to a canceled task") {
                val task = AsyncOperationTaskImpl<Any>()

                val callback = mockOnCancelledCallback()
                task.onCancelCallback = callback

                task.cancel()

                val wrappedTask = mockk<Future<*>>(relaxed = true)
                task += wrappedTask

                Then("Task is cancelled", true, task.isCancelled)
                Then("Task is not done", false, task.isDone)

                VerifyOnce("Callback called once") {
                    callback()
                }

                Verify("Inner task cancelled") {
                    wrappedTask.cancel(any())
                }
            }

            When("Inner task added to a completed task") {
                val task = AsyncOperationTaskImpl<Any>()

                val callback = mockOnCancelledCallback()
                task.onCancelCallback = callback

                task.onComplete()

                val wrappedTask = mockk<Future<*>>(relaxed = true)
                task += wrappedTask

                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task is done", true, task.isDone)

                VerifyNo("On cancel callback not called") {
                    callback()
                }

                VerifyNo("Inner task not cancelled") {
                    wrappedTask.cancel(any())
                }
            }

            When("runIfNotCancelled() called on not complete and not cancelled task") {
                val task = AsyncOperationTaskImpl<Any>()
                val action: ExtendedAsyncOperationTask<*>.() -> Unit = mockk(relaxed = true)
                task.runIfNotCancelled(action)

                Verify("Action called") {
                    action(task)
                }
            }

            When("runIfNotCancelled() called on cancelled task") {
                val task = AsyncOperationTaskImpl<Any>().apply {
                    cancel()
                }

                val action: ExtendedAsyncOperationTask<*>.() -> Unit = mockk(relaxed = true)
                task.runIfNotCancelled(action)

                VerifyNo("Action not called") {
                    action(task)
                }
            }

            When("runIfNotCancelled() called on completed task") {
                val task = AsyncOperationTaskImpl<Any>().apply {
                    onComplete()
                }

                val action: ExtendedAsyncOperationTask<*>.() -> Unit = mockk(relaxed = true)
                task.runIfNotCancelled(action)

                // Yes, this is correct behavior for now.
                Verify("Action called") {
                    action(task)
                }
            }
        }
    }
}
