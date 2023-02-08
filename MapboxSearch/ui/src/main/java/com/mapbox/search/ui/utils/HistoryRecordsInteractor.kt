package com.mapbox.search.ui.utils

import androidx.annotation.UiThread
import com.mapbox.search.ServiceProvider
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryDataProvider
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.record.LocalDataProvider
import java.util.concurrent.ConcurrentHashMap

@UiThread
internal class HistoryRecordsInteractor(
    private val historyDataProvider: HistoryDataProvider = ServiceProvider.INSTANCE.historyDataProvider(),
    private val historyFavoritesDataProvider: CompoundIndexableDataProvider<HistoryRecord, FavoriteRecord> = CompoundIndexableDataProvider(
        ServiceProvider.INSTANCE.historyDataProvider(),
        ServiceProvider.INSTANCE.favoritesDataProvider()
    )
) {

    private val retrieveTasks = ConcurrentHashMap<HistoryListener, AsyncOperationTask>()
    private val changeListeners = ConcurrentHashMap<HistoryListener, LocalDataProvider.OnDataChangedListener<HistoryRecord>>()

    private val locallyRemovedHistoryRecords = mutableSetOf<HistoryRecord>()

    /**
     * Removes [HistoryRecord] from the [HistoryDataProvider].
     *
     * Prevents the provided [HistoryRecord] from appearing in the [HistoryListener.onHistoryItems]
     * until [HistoryDataProvider.remove] completes. If [HistoryDataProvider.remove] completes with error,
     * the provided [HistoryRecord] will be available in the [HistoryListener] updates again.
     */
    fun remove(historyRecord: HistoryRecord): AsyncOperationTask {
        locallyRemovedHistoryRecords.add(historyRecord)
        return historyDataProvider.remove(historyRecord.id, object : CompletionCallback<Boolean> {
            override fun onComplete(result: Boolean) {
                locallyRemovedHistoryRecords.remove(historyRecord)
            }

            override fun onError(e: Exception) {
                locallyRemovedHistoryRecords.remove(historyRecord)
            }
        })
    }

    /**
     * Adds a listener to be notified of history data changes.
     * Unlike [LocalDataProvider.addOnDataChangedListener], emits initial data to listener.
     */
    fun subscribeToChanges(listener: HistoryListener) {
        if (isSubscribed(listener)) {
            return
        }

        val callback = object : CompletionCallback<Pair<List<HistoryRecord>, List<FavoriteRecord>>> {
            override fun onComplete(result: Pair<List<HistoryRecord>, List<FavoriteRecord>>) {
                listener.onHistoryItems(createHistoryItems(result.first, result.second))

                val dataChangeListener = object : LocalDataProvider.OnDataChangedListener<HistoryRecord> {
                    override fun onDataChanged(newData: List<HistoryRecord>) {
                        retrieveTasks[listener]?.cancel()

                        val innerCallback =
                            object : CompletionCallback<Pair<List<HistoryRecord>, List<FavoriteRecord>>> {
                                override fun onComplete(result: Pair<List<HistoryRecord>, List<FavoriteRecord>>) {
                                    listener.onHistoryItems(createHistoryItems(result.first, result.second))
                                }

                                override fun onError(e: Exception) {
                                    listener.onError(e)
                                    unsubscribe(listener)
                                }
                            }

                        retrieveTasks[listener] = historyFavoritesDataProvider.getAll(innerCallback)
                    }
                }

                changeListeners[listener] = dataChangeListener
                historyDataProvider.addOnDataChangedListener(dataChangeListener)
            }

            override fun onError(e: Exception) {
                listener.onError(e)
                unsubscribe(listener)
            }
        }
        retrieveTasks[listener] = historyFavoritesDataProvider.getAll(callback)
    }

    fun unsubscribe(listener: HistoryListener) {
        retrieveTasks[listener]?.let {
            it.cancel()
            retrieveTasks.remove(listener)
        }

        changeListeners[listener]?.let {
            historyDataProvider.removeOnDataChangedListener(it)
            changeListeners.remove(listener)
        }
    }

    private fun isSubscribed(listener: HistoryListener) = retrieveTasks.containsKey(listener)

    private fun createHistoryItems(
        history: List<HistoryRecord>,
        favorites: List<FavoriteRecord>
    ): List<Pair<HistoryRecord, Boolean>> {
        val favoritesMap = favorites.associateBy { it.address to it.coordinate }
        return history
            .filter { !locallyRemovedHistoryRecords.contains(it) }
            .map {
                Pair(it, favoritesMap.containsKey(it.address to it.coordinate))
            }
    }

    /**
     * Listener to be notified of history records changes.
     */
    interface HistoryListener {

        /**
         * Called when new history items are available.
         * Each history record is matched with a boolean flag which indicates whether the record is also a favorite.
         */
        fun onHistoryItems(items: List<Pair<HistoryRecord, Boolean>>)

        /**
         * Called on error.
         */
        fun onError(e: Exception)
    }
}
