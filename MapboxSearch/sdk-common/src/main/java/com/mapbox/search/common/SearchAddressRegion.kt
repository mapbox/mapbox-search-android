package com.mapbox.search.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Address region information
 */
@Parcelize
public class SearchAddressRegion(

    /**
     * Region name
     */
    public val name: String,

    /**
     * Region code
     */
    public val code: String?,

    /**
     * Region ISO 3166-2 code
     */
    public val codeFull: String?
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchAddressRegion

        if (name != other.name) return false
        if (code != other.code) return false
        if (codeFull != other.codeFull) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (code?.hashCode() ?: 0)
        result = 31 * result + (codeFull?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchAddressRegion(name='$name', code=$code, codeFull=$codeFull)"
    }
}
