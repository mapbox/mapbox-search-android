package com.mapbox.search.record

import java.util.concurrent.ExecutorService

/**
 * [LocalDataProvider] typed to store [FavoriteRecord] items.
 *
 * To obtain [FavoritesDataProvider] instance, please, use [com.mapbox.search.ServiceProvider.favoritesDataProvider].
 *
 * @see IndexableDataProvider
 * @see LocalDataProvider
 * @see HistoryDataProvider
 */
public interface FavoritesDataProvider : LocalDataProvider<FavoriteRecord> {

    /**
     * Companion object.
     */
    public companion object {

        /**
         * [FavoritesDataProvider] unique name.
         */
        public const val PROVIDER_NAME: String = "com.mapbox.search.localProvider.favorite"

        /**
         * [FavoritesDataProvider] priority.
         * @see [IndexableDataProvider.priority]
         */
        public const val PROVIDER_PRIORITY: Int = 101
    }
}

internal class FavoritesDataProviderImpl(
    recordsStorage: RecordsFileStorage<FavoriteRecord>,
    backgroundTaskExecutorService: ExecutorService = defaultExecutor(FavoritesDataProvider.PROVIDER_NAME),
) : LocalDataProviderImpl<FavoriteRecord>(
    dataProviderName = FavoritesDataProvider.PROVIDER_NAME,
    priority = FavoritesDataProvider.PROVIDER_PRIORITY,
    recordsStorage = recordsStorage,
    backgroundTaskExecutorService = backgroundTaskExecutorService,
), FavoritesDataProvider
