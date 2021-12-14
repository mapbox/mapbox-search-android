package com.mapbox.search.task

import com.mapbox.common.Cancelable
import com.mapbox.search.AsyncOperationTask
import java.util.concurrent.Future

internal interface CancelableWrapper {

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
