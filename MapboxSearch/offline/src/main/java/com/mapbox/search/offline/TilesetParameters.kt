package com.mapbox.search.offline

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreOfflineSearchLanguage
import com.mapbox.search.base.core.CoreOfflineSearchTileset
import com.mapbox.search.base.core.CoreOfflineSearchTilesetSelection
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode

/**
 * Represents the parameters for a tileset.
 *
 * Use the [Builder] class to create an instance of [TilesetParameters].
 */
@MapboxExperimental
public class TilesetParameters internal constructor(
    internal val dataset: String,
    internal val version: String,
    internal val language: IsoLanguageCode?,
    internal val worldview: IsoCountryCode?,
    internal val overviewOnly: Boolean,
) {

    internal val generatedDatasetName: String by lazy {
        DatasetNameBuilder.buildDatasetName(
            dataset = dataset,
            language = language?.code,
            worldview = worldview?.code,
        )
    }

    // CoreOfflineSearchTileset accepts several languages, but TilesetParameters holds one on purpose.
    // OfflineSearchEngine.selectTileset() and createTilesetDescriptor(tilesetParameters: TilesetParameter)
    // work with exactly one language, so a list would make some TilesetParameters invalid for them.
    // Several languages are passed as one TilesetParameters per language, native merges them by dataset.
    @JvmSynthetic
    internal fun mapToCore(): CoreOfflineSearchTileset {
        return CoreOfflineSearchTileset(
            dataset,
            listOfNotNull(getCoreOfflineSearchLanguage()),
            overviewOnly,
        )
    }

    @JvmSynthetic
    internal fun getCoreOfflineSearchTilesetSelection(): CoreOfflineSearchTilesetSelection {
        return CoreOfflineSearchTilesetSelection(dataset, getCoreOfflineSearchLanguage())
    }

    private fun getCoreOfflineSearchLanguage(): CoreOfflineSearchLanguage? {
        DatasetNameBuilder.validate(language?.code, worldview?.code)
        return language?.let {
            CoreOfflineSearchLanguage(it.code.lowercase(), worldview?.code?.lowercase())
        }
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TilesetParameters

        if (dataset != other.dataset) return false
        if (version != other.version) return false
        if (language != other.language) return false
        if (worldview != other.worldview) return false
        if (overviewOnly != other.overviewOnly) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = dataset.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + (language?.hashCode() ?: 0)
        result = 31 * result + (worldview?.hashCode() ?: 0)
        result = 31 * result + overviewOnly.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "TilesetParameters(" +
                "dataset='$dataset', " +
                "version='$version', " +
                "language=$language, " +
                "worldview=$worldview, " +
                "overviewOnly=$overviewOnly" +
                ")"
    }

    /**
     * Builder for creating an instance of [TilesetParameters].
     *
     * @property dataset The dataset name.
     * @property version The dataset version. If empty, the latest version is used.
     */
    public class Builder @Deprecated(
        message = "The version parameter is deprecated, the latest version is used. " +
            "Use Builder(dataset) instead.",
        replaceWith = ReplaceWith("Builder(dataset)"),
    ) constructor(
        public val dataset: String = DEFAULT_DATASET,
        @Deprecated("The latest version is used.")
        public val version: String = DEFAULT_VERSION,
    ) {

        private var language: IsoLanguageCode? = null
        private var worldview: IsoCountryCode? = null
        private var overviewOnly: Boolean = false

        /**
         * Creates a builder for the latest version of the default dataset.
         */
        @Suppress("DEPRECATION")
        public constructor() : this(DEFAULT_DATASET, DEFAULT_VERSION)

        /**
         * Creates a builder for the latest version of the [dataset].
         *
         * @param dataset The dataset name.
         */
        @Suppress("DEPRECATION")
        public constructor(dataset: String) : this(dataset, DEFAULT_VERSION)

        /**
         * Sets the language for the tileset.
         * @param language The language code.
         * @return This builder instance.
         */
        public fun language(language: IsoLanguageCode): Builder = apply {
            this.language = language
        }

        /**
         * Sets the worldview for the tileset.
         * This method requires an explicit language specification.
         *
         * @param language The language code.
         * @param worldview The worldview code.
         * @return This builder instance.
         */
        public fun worldview(
            language: IsoLanguageCode,
            worldview: IsoCountryCode,
        ): Builder = apply {
            this.language = language
            this.worldview = worldview
        }

        /**
         * Sets whether the tileset should include only overview data, e.g. countries, regions, cities and
         * localities. Overview data downloads less data, at the cost of not finding detailed results such as
         * addresses, streets and POIs. The exact contents are subject to change.
         * Defaults to `false`, both detailed and overview data.
         *
         * Only affects [OfflineSearchEngine.createTilesetDescriptor],
         * [OfflineSearchEngine.selectTileset] ignores it.
         *
         * @param overviewOnly `true` to include only overview data.
         * @return This builder instance.
         */
        public fun overviewOnly(overviewOnly: Boolean): Builder = apply {
            this.overviewOnly = overviewOnly
        }

        /**
         * Returns a [TilesetParameters] instance with the specified parameters.
         * @return A new instance of [TilesetParameters].
         */
        public fun build(): TilesetParameters {
            @Suppress("DEPRECATION")
            return TilesetParameters(dataset, version, language, worldview, overviewOnly)
        }
    }

    internal companion object {
        @JvmSynthetic
        const val DEFAULT_DATASET = "mbx-gen2"
        @JvmSynthetic
        const val DEFAULT_VERSION = ""
    }
}
