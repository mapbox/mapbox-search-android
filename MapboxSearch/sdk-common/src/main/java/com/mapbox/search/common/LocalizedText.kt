package com.mapbox.search.common

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * Represents a piece of text associated with a specific language.
 * One of examples is [OCPI DisplayText](https://github.com/ocpi/ocpi/blob/2.2.1/types.asciidoc#13-displaytext-class).
 *
 * @property language Language Code ISO 639-1.
 * @property text The localized text content in the specified language. No markup, HTML etc. allowed.
 */
@MapboxExperimental
@Parcelize
public class LocalizedText @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    public val language: String,
    public val text: String,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalizedText

        if (language != other.language) return false
        if (text != other.text) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = language.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "EvDisplayText(" +
                "language=$language, " +
                "text=$text" +
                ")"
    }
}
