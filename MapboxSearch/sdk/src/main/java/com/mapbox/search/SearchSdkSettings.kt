package com.mapbox.search

/**
 * Settings used for Search SDK initialization.
 * @see MapboxSearchSdk.initialize
 */
public class SearchSdkSettings @JvmOverloads public constructor(

    /**
     * Geocoding API endpoint URL.
     */
    public val geocodingEndpointBaseUrl: String = DEFAULT_ENDPOINT_GEOCODING,

    /**
     * Single Box Search endpoint URL.
     */
    public val singleBoxSearchBaseUrl: String? = null,

    /**
     * Specify the maximum allowed [history records][com.mapbox.search.record.HistoryRecord] amount for [HistoryDataProvider][com.mapbox.search.record.HistoryDataProvider].
     * Default value is [DEFAULT_MAX_HISTORY_RECORDS_AMOUNT].
     *
     * Please note, you cannot specify max amount higher than [MAX_HISTORY_RECORDS_AMOUNT_HIGHER_BOUND].
     */
    public val maxHistoryRecordsAmount: Int = DEFAULT_MAX_HISTORY_RECORDS_AMOUNT,
) {

    init {
        val upperBound = MAX_HISTORY_RECORDS_AMOUNT_HIGHER_BOUND
        require(maxHistoryRecordsAmount in 1..upperBound) {
            "'maxHistoryRecordsAmount' should be in [1..$upperBound] interval (passed value: $maxHistoryRecordsAmount)."
        }
    }

    /**
     * Creates new [SearchSdkSettings.Builder] from current [SearchSdkSettings] instance.
     */
    public fun toBuilder(): Builder {
        return Builder(this)
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchSdkSettings

        if (geocodingEndpointBaseUrl != other.geocodingEndpointBaseUrl) return false
        if (singleBoxSearchBaseUrl != other.singleBoxSearchBaseUrl) return false
        if (maxHistoryRecordsAmount != other.maxHistoryRecordsAmount) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = geocodingEndpointBaseUrl.hashCode()
        result = 31 * result + (singleBoxSearchBaseUrl?.hashCode() ?: 0)
        result = 31 * result + maxHistoryRecordsAmount
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchSdkSettings(" +
            "geocodingEndpointBaseUrl='$geocodingEndpointBaseUrl', " +
            "singleBoxSearchBaseUrl=$singleBoxSearchBaseUrl, " +
            "maxHistoryRecordsAmount=$maxHistoryRecordsAmount" +
            ")"
    }

    /**
     * Builder for comfortable creation of [SearchSdkSettings] instance.
     */
    public class Builder() {

        private var geocodingEndpointBaseUrl: String = DEFAULT_ENDPOINT_GEOCODING
        private var singleBoxSearchBaseUrl: String? = null
        private var maxHistoryRecordsAmount: Int = DEFAULT_MAX_HISTORY_RECORDS_AMOUNT

        internal constructor(settings: SearchSdkSettings) : this() {
            geocodingEndpointBaseUrl = settings.geocodingEndpointBaseUrl
            singleBoxSearchBaseUrl = settings.singleBoxSearchBaseUrl
            maxHistoryRecordsAmount = settings.maxHistoryRecordsAmount
        }

        /**
         * Geocoding API endpoint URL.
         */
        public fun geocodingEndpointBaseUrl(baseUrl: String): Builder = apply { this.geocodingEndpointBaseUrl = baseUrl }

        /**
         * Geocoding API endpoint URL.
         */
        public fun singleBoxSearchBaseUrl(baseUrl: String?): Builder = apply { this.singleBoxSearchBaseUrl = baseUrl }

        /**
         * Specify the maximum allowed [history records][com.mapbox.search.record.HistoryRecord] amount for [HistoryDataProvider][com.mapbox.search.record.HistoryDataProvider].
         * Default value is [DEFAULT_MAX_HISTORY_RECORDS_AMOUNT].
         *
         * Please note, you cannot specify max amount higher than [MAX_HISTORY_RECORDS_AMOUNT_HIGHER_BOUND].
         */
        public fun maxHistoryRecordsAmount(amount: Int): Builder = apply { this.maxHistoryRecordsAmount = amount }

        /**
         * Create [SearchSdkSettings] instance from builder data.
         */
        public fun build(): SearchSdkSettings = SearchSdkSettings(
            geocodingEndpointBaseUrl = geocodingEndpointBaseUrl,
            singleBoxSearchBaseUrl = singleBoxSearchBaseUrl,
            maxHistoryRecordsAmount = maxHistoryRecordsAmount,
        )
    }

    /**
     * @suppress
     */
    public companion object {
        private const val DEFAULT_ENDPOINT_GEOCODING: String = "https://api.mapbox.com"

        /**
         * Default value for [SearchSdkSettings.maxHistoryRecordsAmount].
         */
        public const val DEFAULT_MAX_HISTORY_RECORDS_AMOUNT: Int = 100

        /**
         * Higher bound for [SearchSdkSettings.maxHistoryRecordsAmount].
         */
        public const val MAX_HISTORY_RECORDS_AMOUNT_HIGHER_BOUND: Int = 100
    }
}
