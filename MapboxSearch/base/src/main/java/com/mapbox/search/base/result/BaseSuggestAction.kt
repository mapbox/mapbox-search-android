package com.mapbox.search.base.result

import android.os.Parcelable
import com.mapbox.search.base.core.CoreSuggestAction
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseSuggestAction(
    val endpoint: String,
    val path: String,
    var query: String?,
    val body: ByteArray?,
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseSuggestAction

        if (endpoint != other.endpoint) return false
        if (path != other.path) return false
        if (query != other.query) return false
        if (body != null) {
            if (other.body == null) return false
            if (!body.contentEquals(other.body)) return false
        } else if (other.body != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = endpoint.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + query.hashCode()
        result = 31 * result + (body?.contentHashCode() ?: 0)
        return result
    }
}

fun CoreSuggestAction.mapToBase() = BaseSuggestAction(
    endpoint = endpoint, path = path, query = query, body = body
)

fun BaseSuggestAction.mapToCore() = CoreSuggestAction(
    endpoint,
    path,
    query,
    body,
    false
)
