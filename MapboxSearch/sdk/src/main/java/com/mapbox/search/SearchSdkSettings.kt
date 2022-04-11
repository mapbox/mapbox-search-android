package com.mapbox.search

/**
 * Settings used for Search SDK initialization.
 * @see MapboxSearchSdk.initialize
 */
public class SearchSdkSettings @JvmOverloads public constructor(
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
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchSdkSettings
        if (maxHistoryRecordsAmount != other.maxHistoryRecordsAmount) return false
        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return maxHistoryRecordsAmount.hashCode()
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchSdkSettings(" +
            "maxHistoryRecordsAmount=$maxHistoryRecordsAmount" +
            ")"
    }

    /**
     * @suppress
     */
    public companion object {

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
