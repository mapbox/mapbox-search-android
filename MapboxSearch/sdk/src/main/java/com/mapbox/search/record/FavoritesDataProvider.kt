package com.mapbox.search.record

import com.mapbox.search.MapboxSearchSdk
import java.util.concurrent.ExecutorService

/**
 * [LocalDataProvider] typed to store [FavoriteRecord] items.
 *
 * To obtain [FavoritesDataProvider] instance, please, use [MapboxSearchSdk.serviceProvider].
 *
 * @see IndexableDataProvider
 * @see LocalDataProvider
 * @see HistoryDataProvider
 */
public interface FavoritesDataProvider : LocalDataProvider<FavoriteRecord> {

    /**
     * @suppress
     */
    public companion object {

        /**
         * [FavoritesDataProvider] unique name.
         */
        public const val PROVIDER_NAME: String = "com.mapbox.search.localProvider.favorite"
    }
}

internal class FavoritesDataProviderImpl(
    recordsStorage: RecordsFileStorage<FavoriteRecord>,
    backgroundTaskExecutorService: ExecutorService = defaultExecutor(FavoritesDataProvider.PROVIDER_NAME),
) : LocalDataProviderImpl<FavoriteRecord>(
    dataProviderName = FavoritesDataProvider.PROVIDER_NAME,
    recordsStorage = recordsStorage,
    backgroundTaskExecutorService = backgroundTaskExecutorService,
), FavoritesDataProvider
