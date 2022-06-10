package com.mapbox.search.result

import android.os.Parcelable
import com.mapbox.search.core.CoreSearchResponse
import com.mapbox.search.core.CoreSearchResponseErrorType
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class OriginalSearchResponse(
    val result: Result,
    val responseUUID: String
) : Parcelable {

    sealed class Result : Parcelable {

        @Parcelize
        data class Success(val result: List<OriginalSearchResult>) : Result()

        sealed class Error : Result() {
            @Parcelize
            data class ConnectionError(val message: String) : Error()

            @Parcelize
            data class ServerError(val httpCode: Int, val message: String) : Error()

            @Parcelize
            data class InternalError(val message: String) : Error()

            @Parcelize
            data class RequestCancelled(val reason: String) : Error()
        }
    }
}

@JvmSynthetic
internal fun CoreSearchResponse.mapToPlatform(): OriginalSearchResponse {
    val result = if (results.isValue) {
        OriginalSearchResponse.Result.Success(
            requireNotNull(results.value).map { it.mapToPlatform() }
        )
    } else {
        val error = requireNotNull(results.error)
        when (error.typeInfo) {
            CoreSearchResponseErrorType.CONNECTION_ERROR -> OriginalSearchResponse.Result.Error.ConnectionError(
                error.httpError.message
            )
            CoreSearchResponseErrorType.HTTP_ERROR -> OriginalSearchResponse.Result.Error.ServerError(
                error.httpError.httpCode,
                error.httpError.message
            )
            CoreSearchResponseErrorType.INTERNAL_ERROR -> OriginalSearchResponse.Result.Error.InternalError(
                error.internalError.message
            )
            CoreSearchResponseErrorType.REQUEST_CANCELLED -> OriginalSearchResponse.Result.Error.RequestCancelled(
                error.requestCancelled.reason
            )
            null -> throw IllegalStateException()
        }
    }

    return OriginalSearchResponse(
        result = result,
        responseUUID = responseUUID
    )
}
