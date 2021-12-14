package com.mapbox.search.core.http

import androidx.annotation.VisibleForTesting
import com.mapbox.search.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

internal class OkHttpHelper(debugLogsEnabled: Boolean) {

    private val httpLogsLevel = if (BuildConfig.DEBUG || debugLogsEnabled) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    }

    fun getClient(): OkHttpClient {
        return getOkHttpBuilder().build()
    }

    @VisibleForTesting
    fun getMockedClient(mockInterceptor: Interceptor, timeout: Long = 10_000L): OkHttpClient {
        return getOkHttpBuilder()
            .readTimeout(timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout, TimeUnit.MILLISECONDS)
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .addInterceptor(mockInterceptor)
            .build()
    }

    private fun getOkHttpBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(httpLogsLevel))
    }
}
