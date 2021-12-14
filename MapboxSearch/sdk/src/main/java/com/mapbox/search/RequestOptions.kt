package com.mapbox.search

import android.os.Parcelable
import com.mapbox.search.core.CoreRequestOptions
import com.mapbox.search.result.SearchRequestContext
import kotlinx.parcelize.Parcelize

/**
 * Options describing search request.
 *
 * @property query Search query.
 *
 * @property options Search options, that were used for the original request.
 * See [proximityRewritten] and [originRewritten] to check whether some properties have been rewritten
 *
 * @property proximityRewritten denotes whether [SearchOptions.proximity] property has been rewritten by the Search SDK.
 * This may happen when passed to the [com.mapbox.search.SearchEngine] [SearchOptions] don't have [SearchOptions.proximity] set.
 *
 * @property originRewritten denotes whether [SearchOptions.origin] property has been rewritten by the Search SDK.
 * This may happen when passed to the [com.mapbox.search.SearchEngine] [SearchOptions] don't have [SearchOptions.origin] set.
 */
@Parcelize
public class RequestOptions internal constructor(
    public val query: String,
    public val options: SearchOptions,
    public val proximityRewritten: Boolean,
    public val originRewritten: Boolean,
    @get:JvmSynthetic internal val endpoint: String,
    @get:JvmSynthetic internal val sessionID: String,
    @get:JvmSynthetic internal val requestContext: SearchRequestContext,
) : Parcelable {

    /**
     * Creates new [RequestOptions] from current instance.
     */
    @JvmSynthetic
    internal fun copy(
        query: String = this.query,
        options: SearchOptions = this.options,
        proximityRewritten: Boolean = this.proximityRewritten,
        originRewritten: Boolean = this.originRewritten,
        endpoint: String = this.endpoint,
        sessionID: String = this.sessionID,
        requestContext: SearchRequestContext = this.requestContext,
    ): RequestOptions {
        return RequestOptions(
            query = query,
            options = options,
            proximityRewritten = proximityRewritten,
            originRewritten = originRewritten,
            endpoint = endpoint,
            sessionID = sessionID,
            requestContext = requestContext,
        )
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestOptions

        if (query != other.query) return false
        if (options != other.options) return false
        if (proximityRewritten != other.proximityRewritten) return false
        if (originRewritten != other.originRewritten) return false
        if (endpoint != other.endpoint) return false
        if (sessionID != other.sessionID) return false
        if (requestContext != other.requestContext) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = query.hashCode()
        result = 31 * result + options.hashCode()
        result = 31 * result + proximityRewritten.hashCode()
        result = 31 * result + originRewritten.hashCode()
        result = 31 * result + endpoint.hashCode()
        result = 31 * result + sessionID.hashCode()
        result = 31 * result + requestContext.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "RequestOptions(" +
                "query='$query', " +
                "options=$options, " +
                "proximityRewritten=$proximityRewritten, " +
                "originRewritten=$originRewritten, " +
                "endpoint='$endpoint', " +
                "sessionID='$sessionID', " +
                "requestContext=$requestContext" +
                ")"
    }
}

internal fun CoreRequestOptions.mapToPlatform(searchRequestContext: SearchRequestContext) = RequestOptions(
    query = query,
    options = options.mapToPlatform(),
    proximityRewritten = proximityRewritten,
    originRewritten = originRewritten,
    endpoint = endpoint,
    sessionID = sessionID,
    requestContext = searchRequestContext,
)

internal fun RequestOptions.mapToCore() = CoreRequestOptions(
    query,
    endpoint,
    options.mapToCore(),
    proximityRewritten,
    originRewritten,
    sessionID,
)
