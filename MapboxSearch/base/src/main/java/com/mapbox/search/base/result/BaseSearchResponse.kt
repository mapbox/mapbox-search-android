package com.mapbox.search.base.result

import android.os.Parcelable
import com.mapbox.search.base.core.CoreSearchResponse
import com.mapbox.search.base.core.CoreSearchResponseErrorType
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseSearchResponse(
    val result: Result,
    val responseUUID: String
) : Parcelable {

    sealed class Result : Parcelable {

        @Parcelize
        data class Success(val result: List<BaseRawSearchResult>) : Result()

        sealed class Error : Result() {
            @Parcelize
            data class ConnectionError(val message: String) : Error()

            @Parcelize
            data class HttpError(val httpCode: Int, val message: String) : Error()

            @Parcelize
            data class InternalError(val message: String) : Error()

            @Parcelize
            data class RequestCancelled(val reason: String) : Error()
        }
    }
}

fun CoreSearchResponse.mapToBase(): BaseSearchResponse {
    val result = if (results.isValue) {
        BaseSearchResponse.Result.Success(
            requireNotNull(results.value).map { it.mapToBase() }
        )
    } else {
        val error = requireNotNull(results.error)
        when (error.typeInfo) {
            CoreSearchResponseErrorType.CONNECTION_ERROR -> BaseSearchResponse.Result.Error.ConnectionError(
                error.connectionError.message
            )
            CoreSearchResponseErrorType.HTTP_ERROR -> BaseSearchResponse.Result.Error.HttpError(
                error.httpError.httpCode,
                error.httpError.message
            )
            CoreSearchResponseErrorType.INTERNAL_ERROR -> BaseSearchResponse.Result.Error.InternalError(
                error.internalError.message
            )
            CoreSearchResponseErrorType.REQUEST_CANCELLED -> BaseSearchResponse.Result.Error.RequestCancelled(
                error.requestCancelled.reason
            )
            null -> throw IllegalStateException()
        }
    }

    return BaseSearchResponse(
        result = result,
        responseUUID = responseUUID
    )
}
