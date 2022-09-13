package com.mapbox.search.base

import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.base.task.ExtendedAsyncOperationTask
import com.mapbox.test.dsl.TestCase
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
                Then("Callback delegate is null", null, task.callbackDelegate)
            }

            When("Call CompletedAsyncOperationTask cancel()") {
                val onCancelCallback = mockk<AsyncOperationTaskImpl.OnCancelledCallback>(relaxed = true)
                task.addOnCancelledCallback(onCancelCallback)

                task.cancel()

                Then("Task is always done", true, task.isDone)
                Then("Task is not cancelled", false, task.isCancelled)
                Then("Callback delegate is null", null, task.callbackDelegate)

                VerifyNo("OnCancelledCallback is not called") {
                    onCancelCallback.onCancelled()
                }
            }
        }
    }

    @TestFactory
    fun `Check AsyncOperationTaskImpl`() = TestCase {
        Given("AsyncOperationTaskImpl instance") {
            val delegate = Runnable { }

            When("Object is in initial state") {
                val task = AsyncOperationTaskImpl<Any>()

                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task is not done", false, task.isDone)
                Then("callbackActionExecuted is false", false, task.callbackActionExecuted)
                Then("Callback delegate is null", null, task.callbackDelegate)
                Then("onCancelCallbacks are empty", false, task.hasOnCancelledCallbacks)
            }

            When("Object with delegate and onCancelCallback is in initial state") {
                val task = AsyncOperationTaskImpl<Any>(delegate)

                val onCancelCallback = mockk<AsyncOperationTaskImpl.OnCancelledCallback>(relaxed = true)
                task.addOnCancelledCallback(onCancelCallback)

                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task is not done", false, task.isDone)
                Then("callbackActionExecuted is false", false, task.callbackActionExecuted)
                Then("Callback delegate is set", delegate, task.callbackDelegate)
                Then("Task has onCancelCallbacks", true, task.hasOnCancelledCallbacks)
            }

            When("onComplete() called") {
                val task = AsyncOperationTaskImpl<Any>(delegate)

                val onCancelCallback = mockk<AsyncOperationTaskImpl.OnCancelledCallback>(relaxed = true)
                task.addOnCancelledCallback(onCancelCallback)

                task.onComplete()

                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task is done", true, task.isDone)
                Then("Callback delegate is null", null, task.callbackDelegate)
                Then("onCancelCallbacks are empty", false, task.hasOnCancelledCallbacks)

                VerifyNo("OnCancelledCallback is not called") {
                    onCancelCallback.onCancelled()
                }
            }

            When("markExecutedAndRunOnCallback() called") {
                val task = AsyncOperationTaskImpl<Any>(delegate)

                val onCancelCallback = mockk<AsyncOperationTaskImpl.OnCancelledCallback>(relaxed = true)
                task.addOnCancelledCallback(onCancelCallback)

                val action: Any.() -> Unit = mockk(relaxed = true)
                task.markExecutedAndRunOnCallback(action)

                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task is done", true, task.isDone)
                Then("callbackActionExecuted is true", true, task.callbackActionExecuted)
                Then("Callback delegate is null", null, task.callbackDelegate)
                Then("onCancelCallbacks are empty", false, task.hasOnCancelledCallbacks)

                VerifyOnce("Action called") {
                    action(delegate)
                }

                VerifyNo("OnCancelledCallback is not called") {
                    onCancelCallback.onCancelled()
                }
            }

            When("markExecutedAndRunOnCallback() called on a completed object") {
                val task = AsyncOperationTaskImpl<Any>(delegate)
                task.onComplete()

                val action: Any.() -> Unit = mockk(relaxed = true)
                task.markExecutedAndRunOnCallback(action)

                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task is done", true, task.isDone)
                Then("callbackActionExecuted is false", false, task.callbackActionExecuted)
                Then("Callback delegate is null", null, task.callbackDelegate)
                Then("onCancelCallbacks are empty", false, task.hasOnCancelledCallbacks)

                VerifyNo("Action is not called") {
                    action(any())
                }
            }

            When("markExecutedAndRunOnCallback() called on a cancelled object") {
                val task = AsyncOperationTaskImpl<Any>(delegate)
                task.cancel()

                val action: Any.() -> Unit = mockk(relaxed = true)
                task.markExecutedAndRunOnCallback(action)

                Then("Task is not cancelled", true, task.isCancelled)
                Then("Task is done", false, task.isDone)
                Then("callbackActionExecuted is false", false, task.callbackActionExecuted)
                Then("Callback delegate is null", null, task.callbackDelegate)
                Then("onCancelCallbacks are empty", false, task.hasOnCancelledCallbacks)

                VerifyNo("Action is not called") {
                    action(any())
                }
            }

            When("onComplete() called on a cancelled object") {
                val task = AsyncOperationTaskImpl<Any>(delegate)

                val onCancelCallback = mockk<AsyncOperationTaskImpl.OnCancelledCallback>(relaxed = true)
                task.addOnCancelledCallback(onCancelCallback)

                task.cancel()

                task.onComplete()

                Then("Task is cancelled", true, task.isCancelled)
                Then("Task is not done", false, task.isDone)
                Then("callbackActionExecuted is false", false, task.callbackActionExecuted)
                Then("Callback delegate is null", null, task.callbackDelegate)
                Then("onCancelCallbacks are empty", false, task.hasOnCancelledCallbacks)

                VerifyOnce("OnCancelledCallback called once") {
                    onCancelCallback.onCancelled()
                }
            }

            When("onComplete() called more than once") {
                val task = AsyncOperationTaskImpl<Any>(delegate)

                val onCancelCallback = mockk<AsyncOperationTaskImpl.OnCancelledCallback>(relaxed = true)
                task.addOnCancelledCallback(onCancelCallback)

                task.onComplete()
                task.onComplete()

                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task is done", true, task.isDone)
                Then("callbackActionExecuted is false", false, task.callbackActionExecuted)
                Then("Callback delegate is null", null, task.callbackDelegate)
                Then("onCancelCallbacks are empty", false, task.hasOnCancelledCallbacks)

                VerifyNo("OnCancelledCallback is not called") {
                    onCancelCallback.onCancelled()
                }
            }

            When("Object cancelled") {
                val task = AsyncOperationTaskImpl<Any>(delegate)

                val wrappedTask = mockk<Future<*>>(relaxed = true)
                task += wrappedTask

                val onCancelCallback = mockk<AsyncOperationTaskImpl.OnCancelledCallback>(relaxed = true)
                task.addOnCancelledCallback(onCancelCallback)

                task.cancel()

                Verify("cancel() called on a wrapped object") {
                    wrappedTask.cancel(true)
                }

                VerifyOnce("OnCancelledCallback called once") {
                    onCancelCallback.onCancelled()
                }

                Then("Task is cancelled", true, task.isCancelled)
                Then("Task is not done", false, task.isDone)
                Then("callbackActionExecuted is false", false, task.callbackActionExecuted)
                Then("Callback delegate is null", null, task.callbackDelegate)
                Then("onCancelCallbacks are empty", false, task.hasOnCancelledCallbacks)

                task.addOnCancelledCallback(onCancelCallback)
                Then("OnCancelledCallback can't be added to a cancelled object", false, task.hasOnCancelledCallbacks)

                task.callbackDelegate = delegate
                Then("Callback delegate can't be set on a cancelled object", null, task.callbackDelegate)
            }

            When("markCancelledAndRunOnCallback() called") {
                val task = AsyncOperationTaskImpl<Any>(delegate)

                val onCancelCallback = mockk<AsyncOperationTaskImpl.OnCancelledCallback>(relaxed = true)
                task.addOnCancelledCallback(onCancelCallback)

                val action: Any.() -> Unit = mockk(relaxed = true)
                task.markCancelledAndRunOnCallback(action)

                Then("Task is not cancelled", true, task.isCancelled)
                Then("Task is done", false, task.isDone)
                Then("callbackActionExecuted is true", true, task.callbackActionExecuted)
                Then("Callback delegate is null", null, task.callbackDelegate)
                Then("onCancelCallbacks are empty", false, task.hasOnCancelledCallbacks)

                VerifyOnce("Action called") {
                    action(delegate)
                }

                VerifyOnce("OnCancelledCallback callback once") {
                    onCancelCallback.onCancelled()
                }
            }

            When("markCancelledAndRunOnCallback() called on a completed object") {
                val task = AsyncOperationTaskImpl<Any>(delegate)
                task.onComplete()

                val action: Any.() -> Unit = mockk(relaxed = true)
                task.markCancelledAndRunOnCallback(action)

                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task is done", true, task.isDone)
                Then("callbackActionExecuted is false", false, task.callbackActionExecuted)
                Then("Callback delegate is null", null, task.callbackDelegate)
                Then("onCancelCallbacks are empty", false, task.hasOnCancelledCallbacks)

                VerifyNo("Action is not called") {
                    action(any())
                }
            }

            When("markCancelledAndRunOnCallback() called on a cancelled object") {
                val task = AsyncOperationTaskImpl<Any>(delegate)
                task.cancel()

                val action: Any.() -> Unit = mockk(relaxed = true)
                task.markCancelledAndRunOnCallback(action)

                Then("Task is not cancelled", true, task.isCancelled)
                Then("Task is done", false, task.isDone)
                Then("callbackActionExecuted is false", false, task.callbackActionExecuted)
                Then("Callback delegate is null", null, task.callbackDelegate)
                Then("onCancelCallbacks are empty", false, task.hasOnCancelledCallbacks)

                VerifyNo("Action is not called") {
                    action(any())
                }
            }

            When("cancel() called on a completed task") {
                val task = AsyncOperationTaskImpl<Any>()

                val onCancelCallback = mockk<AsyncOperationTaskImpl.OnCancelledCallback>(relaxed = true)
                task.addOnCancelledCallback(onCancelCallback)

                val wrappedTask = mockk<Future<*>>(relaxed = true)
                task += wrappedTask

                task.onComplete()
                task.cancel()

                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task is done", true, task.isDone)
                Then("callbackActionExecuted is false", false, task.callbackActionExecuted)
                Then("Callback delegate is null", null, task.callbackDelegate)
                Then("onCancelCallbacks are empty", false, task.hasOnCancelledCallbacks)

                VerifyNo("OnCancelledCallback is not called") {
                    onCancelCallback.onCancelled()
                }

                VerifyNo("cancel() is not called on a wrapped task") {
                    wrappedTask.cancel(any())
                }
            }

            When("Inner task added to a canceled task") {
                val task = AsyncOperationTaskImpl<Any>()

                val onCancelCallback = mockk<AsyncOperationTaskImpl.OnCancelledCallback>(relaxed = true)
                task.addOnCancelledCallback(onCancelCallback)

                task.cancel()

                val wrappedTask = mockk<Future<*>>(relaxed = true)
                task += wrappedTask

                Then("Task is cancelled", true, task.isCancelled)
                Then("Task is not done", false, task.isDone)
                Then("Callback delegate is null", null, task.callbackDelegate)
                Then("onCancelCallbacks are empty", false, task.hasOnCancelledCallbacks)

                VerifyOnce("OnCancelledCallback called once") {
                    onCancelCallback.onCancelled()
                }

                Verify("Inner task cancelled") {
                    wrappedTask.cancel(any())
                }
            }

            When("Inner task added to a completed task") {
                val task = AsyncOperationTaskImpl<Any>()

                val onCancelCallback = mockk<AsyncOperationTaskImpl.OnCancelledCallback>(relaxed = true)
                task.addOnCancelledCallback(onCancelCallback)

                task.onComplete()

                val wrappedTask = mockk<Future<*>>(relaxed = true)
                task += wrappedTask

                Then("Task is not cancelled", false, task.isCancelled)
                Then("Task is done", true, task.isDone)

                VerifyNo("OnCancelledCallback is not called") {
                    onCancelCallback.onCancelled()
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
