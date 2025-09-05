package com.mapbox.search.analytics

import android.graphics.Bitmap
import androidx.annotation.StringDef
import com.mapbox.search.analytics.FeedbackEvent.FeedbackReason.Companion.INCORRECT_ADDRESS
import com.mapbox.search.analytics.FeedbackEvent.FeedbackReason.Companion.INCORRECT_LOCATION
import com.mapbox.search.analytics.FeedbackEvent.FeedbackReason.Companion.INCORRECT_NAME
import com.mapbox.search.analytics.FeedbackEvent.FeedbackReason.Companion.INCORRECT_PHONE_NUMBER
import com.mapbox.search.analytics.FeedbackEvent.FeedbackReason.Companion.INCORRECT_RESULT_RANK
import com.mapbox.search.analytics.FeedbackEvent.FeedbackReason.Companion.OTHER

/**
 * Feedback information, provided by user.
 */
public class FeedbackEvent @JvmOverloads public constructor(

    /**
     * Reason for user's feedback.
     */
    @FeedbackReason
    public val reason: String,

    /**
     * User's feedback text. Should be provided, if "Other" feedback reason was specified.
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

    /**
     * Unique feedback ID. If the passed value is null, the id will be generated inside the Search SDK.
     * Normally SDK users don't have to provide it explicitly.
     * One of the cases, when users should provide their own id,
     * is when they want to have associated events in different analytics systems.
     */
    public val feedbackId: String? = null,
) {

    /**
     * Creates new [FeedbackEvent] from current instance.
     */
    @JvmSynthetic
    public fun copy(
        @FeedbackReason reason: String = this.reason,
        text: String? = this.text,
        screenshot: Bitmap? = this.screenshot,
        sessionId: String? = this.sessionId,
        feedbackId: String? = this.feedbackId,
    ): FeedbackEvent {
        return FeedbackEvent(
            reason = reason,
            text = text,
            screenshot = screenshot,
            sessionId = sessionId,
            feedbackId = feedbackId,
        )
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FeedbackEvent

        if (reason != other.reason) return false
        if (text != other.text) return false
        if (screenshot != other.screenshot) return false
        if (sessionId != other.sessionId) return false
        if (feedbackId != other.feedbackId) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = reason.hashCode()
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + (screenshot?.hashCode() ?: 0)
        result = 31 * result + (sessionId?.hashCode() ?: 0)
        result = 31 * result + (feedbackId?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "FeedbackEvent(" +
                "reason='$reason', " +
                "text=$text, " +
                "screenshot=$screenshot, " +
                "sessionId=$sessionId, " +
                "feedbackId=$feedbackId" +
                ")"
    }

    /**
     * Search feedback reason.
     */
    @Retention(AnnotationRetention.SOURCE)
    @StringDef(
        INCORRECT_NAME,
        INCORRECT_ADDRESS,
        INCORRECT_LOCATION,
        INCORRECT_PHONE_NUMBER,
        INCORRECT_RESULT_RANK,
        OTHER,
    )
    public annotation class FeedbackReason {

        /**
         * @suppress
         */
        public companion object {

            /**
             * Feedback reason for incorrect search result name.
             */
            public const val INCORRECT_NAME: String = "incorrect_name"

            /**
             * Feedback reason for incorrect search result address.
             */
            public const val INCORRECT_ADDRESS: String = "incorrect_address"

            /**
             * Feedback reason for incorrect search result location.
             */
            public const val INCORRECT_LOCATION: String = "incorrect_location"

            /**
             * Feedback reason for incorrect POI phone number.
             */
            public const val INCORRECT_PHONE_NUMBER: String = "incorrect_phone_number"

            /**
             * Feedback reason for a case where a user thinks that search result is expected to be ranked higher on the list.
             */
            public const val INCORRECT_RESULT_RANK: String = "incorrect_result_rank"

            /**
             * Feedback reason for other problems related to search results.
             */
            public const val OTHER: String = "other_result_issue"
        }
    }
}
