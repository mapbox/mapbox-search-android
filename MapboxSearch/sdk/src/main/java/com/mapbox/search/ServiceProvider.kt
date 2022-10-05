package com.mapbox.search

import com.mapbox.search.base.record.SearchHistoryService
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.record.HistoryDataProviderImpl

/**
 * This class provides access to search-related services and utility classes.
 */
public interface ServiceProvider {

    /**
     * Provides entity to calculate distances between geographical points.
     * @param latitude the area in which fast distance calculation is performed. If second point's latitude far from latitude from constructor, better to use static [DistanceCalculator.distanceOnSphere] to minimize error level.
     * @return [DistanceCalculator] instance.
     */
    public fun distanceCalculator(latitude: Double): DistanceCalculator

    /**
     * Provides entity to calculate highlights ranges in search results.
     * @return [HighlightsCalculator] instance.
     */
    public fun highlightsCalculator(): HighlightsCalculator

    /**
     * Provides entity to work with [com.mapbox.search.record.FavoriteRecord].
     * @return [FavoritesDataProvider] instance.
     */
    public fun favoritesDataProvider(): FavoritesDataProvider

    /**
     * Provides entity to work with [com.mapbox.search.record.HistoryRecord].
     * @return [HistoryDataProvider] instance.
     */
    public fun historyDataProvider(): HistoryDataProvider

    /**
     * Companion object.
     */
    public companion object {

        @JvmSynthetic
        internal lateinit var INTERNAL_INSTANCE: InternalServiceProvider

        /**
         * Shared [ServiceProvider] instance.
         */
        @JvmStatic
        @get:JvmName("getInstance")
        public val INSTANCE: ServiceProvider
            get() = INTERNAL_INSTANCE
    }
}

internal interface InternalServiceProvider : ServiceProvider {
    fun historyService(): SearchHistoryService
}

internal class ServiceProviderImpl(
    private val historyDataProvider: HistoryDataProviderImpl,
    private val favoritesDataProvider: FavoritesDataProvider,
) : ServiceProvider, InternalServiceProvider {

    override fun distanceCalculator(latitude: Double): DistanceCalculator = DistanceCalculatorImpl(latitude)

    override fun highlightsCalculator(): HighlightsCalculator = HighlightsCalculatorImpl()

    override fun favoritesDataProvider(): FavoritesDataProvider = favoritesDataProvider

    override fun historyDataProvider(): HistoryDataProvider = historyDataProvider

    override fun historyService(): SearchHistoryService = historyDataProvider
}
