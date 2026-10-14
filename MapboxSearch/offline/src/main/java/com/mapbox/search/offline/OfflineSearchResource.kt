package com.mapbox.search.offline

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreOfflineSearchResource
import com.mapbox.search.common.RestrictedMapboxSearchAPI

/**
 * Defines additional resources an offline search region may download alongside its tiles.
 * Resources are not part of any tile, but offline search needs them to read some tile data.
 *
 * This is a restricted API intended for specific datasets only.
 * Request a resource only if you know that your dataset needs it.
 */
@MapboxExperimental
@RestrictedMapboxSearchAPI
public object OfflineSearchResource {

    /**
     * Dictionary for splitting Japanese text into words, since Japanese doesn't use spaces between words.
     * Needed to search datasets whose Japanese text was split into words with this dictionary
     * when the tiles were built.
     */
    public const val JAPANESE_DICTIONARY: String = "JAPANESE_DICTIONARY"

    /**
     * Retention policy for the [OfflineSearchResource].
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        JAPANESE_DICTIONARY,
    )
    public annotation class Type

    @JvmSynthetic
    internal val ALL: List<String> = listOf(JAPANESE_DICTIONARY)

    @JvmSynthetic
    internal fun mapToCoreOfflineSearchResource(
        resources: List<String>,
    ): List<CoreOfflineSearchResource> {
        return resources.mapNotNull {
            when (it) {
                JAPANESE_DICTIONARY -> CoreOfflineSearchResource.JAPANESE_DICTIONARY
                else -> null
            }
        }
    }
}
