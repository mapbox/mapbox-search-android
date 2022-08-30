package com.mapbox.search.offline

import android.os.Parcelable
import com.mapbox.search.base.core.CoreRequestOptions
import kotlinx.parcelize.Parcelize

/**
 * Options describing search request.
 *
 * @property query Search query.
 *
 * @property proximityRewritten denotes whether [OfflineSearchOptions.proximity] property has been rewritten by the Search SDK.
 * This may happen when passed to the [OfflineSearchEngine] [OfflineSearchOptions] don't have [OfflineSearchOptions.proximity] set.
 *
 * @property originRewritten denotes whether [OfflineSearchOptions.origin] property has been rewritten by the Search SDK.
 * This may happen when passed to the [OfflineSearchEngine] [OfflineSearchOptions] don't have [OfflineSearchOptions.origin] set.
 */
@Parcelize
public class OfflineRequestOptions internal constructor(
    public val query: String,
    public val proximityRewritten: Boolean,
    public val originRewritten: Boolean,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OfflineRequestOptions

        if (query != other.query) return false
        if (proximityRewritten != other.proximityRewritten) return false
        if (originRewritten != other.originRewritten) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = query.hashCode()
        result = 31 * result + proximityRewritten.hashCode()
        result = 31 * result + originRewritten.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "OfflineRequestOptions(query='$query', proximityRewritten=$proximityRewritten, originRewritten=$originRewritten)"
    }
}

@JvmSynthetic
internal fun CoreRequestOptions.mapToOfflineSdkType(): OfflineRequestOptions {
    return OfflineRequestOptions(
        query = query,
        proximityRewritten = proximityRewritten,
        originRewritten = originRewritten,
    )
}
