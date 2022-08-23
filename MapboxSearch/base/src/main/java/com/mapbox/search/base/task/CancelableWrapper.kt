package com.mapbox.search.base.task

import com.mapbox.common.Cancelable
import com.mapbox.search.common.AsyncOperationTask
import java.util.concurrent.Future

fun interface CancelableWrapper {

    fun cancel()

    companion object {

        fun fromTask(task: AsyncOperationTask): CancelableWrapper {
            return CancelableWrapperImpl {
                task.cancel()
            }
        }

        fun fromMapboxCommonCancellable(cancelable: Cancelable): CancelableWrapper {
            return CancelableWrapperImpl {
                cancelable.cancel()
            }
        }

        fun fromFuture(future: Future<*>): CancelableWrapper {
            return CancelableWrapperImpl {
                future.cancel(true)
            }
        }
    }
}

internal class CancelableWrapperImpl(private val cancellation: () -> Unit) : CancelableWrapper {
    override fun cancel() {
        cancellation()
    }
}
