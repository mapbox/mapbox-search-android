package com.mapbox.search.analytics

import android.graphics.Bitmap
import com.mapbox.search.ResponseInfo

/**
 * Feedback information for use case, when user can't find appropriate POI / place
 * in received list of [com.mapbox.search.result.SearchResult] or
 * [com.mapbox.search.result.SearchSuggestion] from one of search engines.
 */
public class MissingResultFeedbackEvent @JvmOverloads public constructor(

    /**
     * Information about response, in which user couldn't find appropriate POI / place.
     */
    public val responseInfo: ResponseInfo,

    /**
     * User's feedback text.
     */
    public val text: String? = null,

    /**
     * Screenshot with useful information for improving Search SDK.
     */
    public val screenshot: Bitmap? = null,

    /**
     * Unique ID for identifying several related Mapbox events per session.
     */
    public val sessionId: String? = null,
) {

    /**
     * Creates new [MissingResultFeedbackEvent] from current instance.
     */
    @JvmSynthetic
    public fun copy(
        responseInfo: ResponseInfo = this.responseInfo,
        text: String? = this.text,
        screenshot: Bitmap? = this.screenshot,
        sessionId: String? = this.sessionId,
    ): MissingResultFeedbackEvent {
        return MissingResultFeedbackEvent(
            responseInfo = responseInfo,
            text = text,
            screenshot = screenshot,
            sessionId = sessionId,
        )
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MissingResultFeedbackEvent

        if (responseInfo != other.responseInfo) return false
        if (text != other.text) return false
        if (screenshot != other.screenshot) return false
        if (sessionId != other.sessionId) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = responseInfo.hashCode()
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + (screenshot?.hashCode() ?: 0)
        result = 31 * result + (sessionId?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "MissingResultFeedbackEvent(" +
                "responseInfo=$responseInfo, " +
                "text=$text, " +
                "screenshot=$screenshot, " +
                "sessionId=$sessionId" +
                ")"
    }
}
