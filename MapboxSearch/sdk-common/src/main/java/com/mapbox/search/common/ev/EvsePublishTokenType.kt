package com.mapbox.search.common.ev

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import kotlinx.parcelize.Parcelize

/**
 * Defines the set of values that identify a token to which a Location might be published.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#1420-publishtokentype-class)
 * for more details.
 */
@MapboxExperimental
@Parcelize
public class EvsePublishTokenType @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(

    /**
     * Unique ID by which this Token can be identified.
     */
    public val uid: String?,

    /**
     * TokenType value indicating type of the token.
     */
    @EvseTokenType.Type public val tokenType: String?,

    /**
     * Visual readable number/identification as printed on the Token (RFID card).
     */
    public val visualNumber: String?,

    /**
     * Issuing company, most of the times the name of the company printed on the token (RFID card),
     * not necessarily the eMSP.
     */
    public val issuer: String?,

    /**
     * This ID groups a couple of tokens. This can be used to make two or more tokens work as one.
     */
    public val groupId: String?,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EvsePublishTokenType

        if (uid != other.uid) return false
        if (tokenType != other.tokenType) return false
        if (visualNumber != other.visualNumber) return false
        if (issuer != other.issuer) return false
        if (groupId != other.groupId) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = uid?.hashCode() ?: 0
        result = 31 * result + (tokenType?.hashCode() ?: 0)
        result = 31 * result + (visualNumber?.hashCode() ?: 0)
        result = 31 * result + (issuer?.hashCode() ?: 0)
        result = 31 * result + (groupId?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "EvsePublishTokenType(" +
                "uid=$uid, " +
                "tokenType=$tokenType, " +
                "visualNumber=$visualNumber, " +
                "issuer=$issuer, " +
                "groupId=$groupId" +
                ")"
    }
}
