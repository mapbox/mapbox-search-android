package com.mapbox.search.common.ev

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * OCPI DisplayText.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/types.asciidoc#13-displaytext-class)
 * for more details.
 *
 * @property language Language Code ISO 639-1.
 * @property text Text to be displayed to an end user. No markup, HTML etc. allowed.
 */
@MapboxExperimental
@Parcelize
public class EvDisplayText @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    public val language: String,
    public val text: String,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EvDisplayText

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
