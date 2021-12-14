package com.mapbox.search.result

import android.os.Parcelable
import com.mapbox.search.core.CoreSuggestAction
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class SearchResultSuggestAction(
    val endpoint: String,
    val path: String,
    var query: String?,
    val body: ByteArray?,
    val multiRetrievable: Boolean,
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchResultSuggestAction

        if (endpoint != other.endpoint) return false
        if (path != other.path) return false
        if (query != other.query) return false
        if (body != null) {
            if (other.body == null) return false
            if (!body.contentEquals(other.body)) return false
        } else if (other.body != null) return false
        if (multiRetrievable != other.multiRetrievable) return false

        return true
    }

    override fun hashCode(): Int {
        var result = endpoint.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + query.hashCode()
        result = 31 * result + (body?.contentHashCode() ?: 0)
        result = 31 * result + multiRetrievable.hashCode()
        return result
    }
}

internal fun CoreSuggestAction.mapToPlatform() = SearchResultSuggestAction(
    endpoint = endpoint, path = path, query = query, body = body, multiRetrievable = multiRetrievable
)

internal fun SearchResultSuggestAction.mapToCore() = CoreSuggestAction(
    endpoint,
    path,
    query,
    body,
    multiRetrievable
)
