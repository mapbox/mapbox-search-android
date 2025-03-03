package com.mapbox.search.offline

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode

/**
 * Represents the parameters for a tileset.
 *
 * Use the [Builder] class to create an instance of [TilesetParameters].
 */
@MapboxExperimental
public class TilesetParameters internal constructor(
    private val dataset: String,
    internal val version: String,
    private val language: IsoLanguageCode?,
    private val worldview: IsoCountryCode?,
) {

    internal val generatedDatasetName: String by lazy {
        DatasetNameBuilder.buildDatasetName(
            dataset = dataset,
            language = language?.code,
            worldview = worldview?.code,
        )
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
                "worldview=$worldview" +
                ")"
    }

    /**
     * Builder for creating an instance of [TilesetParameters].
     *
     * @param dataset The dataset name.
     * @param version The dataset version. If empty, the version is chosen automatically.
     */
    public class Builder @JvmOverloads constructor(
        public val dataset: String = DEFAULT_DATASET,
        public val version: String = DEFAULT_VERSION
    ) {

        private var language: IsoLanguageCode? = null
        private var worldview: IsoCountryCode? = null

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
         * Returns a [TilesetParameters] instance with the specified parameters.
         * @return A new instance of [TilesetParameters].
         */
        public fun build(): TilesetParameters {
            return TilesetParameters(dataset, version, language, worldview)
        }
    }

    internal companion object {
        @JvmSynthetic
        const val DEFAULT_DATASET = "mbx-gen2"
        @JvmSynthetic
        const val DEFAULT_VERSION = ""
    }
}
