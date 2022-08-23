package com.mapbox.search.base.record

import com.mapbox.search.common.AsyncOperationTask
import java.util.concurrent.Executor

interface IndexableRecordResolver {
    fun resolve(
        dataProviderName: String,
        userRecordId: String,
        executor: Executor,
        callback: (Result<BaseIndexableRecord>) -> Unit
    ): AsyncOperationTask
}
