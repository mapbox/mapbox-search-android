package com.mapbox.search.offline

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Information about search response and associated search request.
 * @property requestOptions Offline search request options.
 */
@Parcelize
public class OfflineResponseInfo internal constructor(
    public val requestOptions: OfflineRequestOptions,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineResponseInfo

        if (requestOptions != other.requestOptions) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return requestOptions.hashCode()
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "OfflineResponseInfo(requestOptions=$requestOptions)"
    }
}
