package com.mapbox.search.ui.utils.extenstion

import com.mapbox.search.AsyncOperationTask

@get:JvmSynthetic
internal val AsyncOperationTask?.isCompleted: Boolean
    get() = this != null && !(isDone || isCancelled)
