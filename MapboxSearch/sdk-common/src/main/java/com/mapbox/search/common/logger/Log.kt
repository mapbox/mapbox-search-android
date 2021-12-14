package com.mapbox.search.common.logger

import com.mapbox.base.common.logger.Logger
import com.mapbox.base.common.logger.model.Message
import com.mapbox.base.common.logger.model.Tag

const val DEFAULT_SEARCH_SDK_LOG_TAG = "SearchSDK"

var searchSdkLogger: Logger? = null

fun logd(throwable: Throwable, message: String, tag: String = DEFAULT_SEARCH_SDK_LOG_TAG) {
    searchSdkLogger?.d(tag = Tag(tag), msg = Message(message), tr = throwable)
}

fun logd(message: String, tag: String = DEFAULT_SEARCH_SDK_LOG_TAG) {
    searchSdkLogger?.d(tag = Tag(tag), msg = Message(message))
}

fun logi(throwable: Throwable, message: String, tag: String = DEFAULT_SEARCH_SDK_LOG_TAG) {
    searchSdkLogger?.i(tag = Tag(tag), msg = Message(message), tr = throwable)
}

fun logi(message: String, tag: String = DEFAULT_SEARCH_SDK_LOG_TAG) {
    searchSdkLogger?.i(tag = Tag(tag), msg = Message(message))
}

fun logw(throwable: Throwable, message: String, tag: String = DEFAULT_SEARCH_SDK_LOG_TAG) {
    searchSdkLogger?.w(tag = Tag(tag), msg = Message(message), tr = throwable)
}

fun logw(message: String, tag: String = DEFAULT_SEARCH_SDK_LOG_TAG) {
    searchSdkLogger?.w(tag = Tag(tag), msg = Message(message))
}

fun loge(throwable: Throwable, message: String, tag: String = DEFAULT_SEARCH_SDK_LOG_TAG) {
    searchSdkLogger?.e(tag = Tag(tag), msg = Message(message), tr = throwable)
}

fun loge(message: String, tag: String = DEFAULT_SEARCH_SDK_LOG_TAG) {
    searchSdkLogger?.e(tag = Tag(tag), msg = Message(message))
}
