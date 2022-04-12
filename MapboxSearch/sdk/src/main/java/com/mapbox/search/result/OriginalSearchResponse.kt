package com.mapbox.search.result

import android.os.Parcelable
import com.mapbox.search.core.CoreSearchResponse
import com.mapbox.search.internal.bindgen.SearchResponseError
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class OriginalSearchResponse(
    val requestId: Int,
    val result: Result,
    val responseUUID: String
) : Parcelable {

    sealed class Result : Parcelable {

        @Parcelize
        data class Success(val result: List<OriginalSearchResult>) : Result()

        sealed class Error : Result() {

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
            SearchResponseError.Type.HTTP_ERROR -> OriginalSearchResponse.Result.Error.ServerError(
                error.httpError.httpCode,
                error.httpError.message
            )
            SearchResponseError.Type.INTERNAL_ERROR -> OriginalSearchResponse.Result.Error.InternalError(
                error.internalError.message
            )
            SearchResponseError.Type.REQUEST_CANCELLED -> OriginalSearchResponse.Result.Error.RequestCancelled(
                error.requestCancelled.reason
            )
            null -> throw IllegalStateException()
        }
    }

    return OriginalSearchResponse(
        requestId = requestID,
        result = result,
        responseUUID = responseUUID
    )
}
