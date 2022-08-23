package com.mapbox.search.base.logger

import androidx.annotation.VisibleForTesting
import com.mapbox.common.CommonSdkLog

@VisibleForTesting
fun resetLogImpl() {
    CommonSdkLog.resetLogImpl()
}

@VisibleForTesting
fun reinitializeLogImpl() {
    CommonSdkLog.reinitializeLogImpl()
}

fun logd(message: String, tag: String? = null) {
    CommonSdkLog.logd(tag, message)
}

fun logi(message: String, tag: String? = null) {
    CommonSdkLog.logi(tag, message)
}

fun logw(message: String, tag: String? = null) {
    CommonSdkLog.logw(tag, message)
}

fun loge(message: String, tag: String? = null) {
    CommonSdkLog.loge(tag, message)
}
