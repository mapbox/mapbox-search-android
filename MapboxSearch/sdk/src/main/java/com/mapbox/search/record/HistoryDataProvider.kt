package com.mapbox.search.record

import com.mapbox.search.AsyncOperationTask
import com.mapbox.search.CompletedAsyncOperationTask
import com.mapbox.search.CompletionCallback
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.result.IndexableRecordSearchResult
import com.mapbox.search.result.IndexableRecordSearchSuggestion
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.utils.LocalTimeProvider
import com.mapbox.search.utils.TimeProvider
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker
import java.util.PriorityQueue
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import kotlin.Comparator

/**
 * [LocalDataProvider] typed to store [HistoryRecord] items.
 *
 * Instance of [HistoryDataProvider] can be retrieved from [MapboxSearchSdk.serviceProvider].
 *
 * @see IndexableDataProvider
 * @see LocalDataProvider
 * @see FavoritesDataProvider
 */
public interface HistoryDataProvider : LocalDataProvider<HistoryRecord> {

    /**
     * @suppress
     */
    public companion object {

        /**
         * [HistoryDataProvider] unique name.
         */
        public const val PROVIDER_NAME: String = "com.mapbox.search.localProvider.history"

        /**
         * [HistoryDataProvider] priority.
         * @see [IndexableDataProvider.priority]
         */
        public const val PROVIDER_PRIORITY: Int = 100
    }
}

internal interface HistoryService : HistoryDataProvider {
    fun addToHistoryIfNeeded(
        searchResult: SearchResult,
        executor: Executor = SearchSdkMainThreadWorker.mainExecutor,
        callback: CompletionCallback<Boolean>
    ): AsyncOperationTask
}

internal class HistoryDataProviderImpl(
    recordsStorage: RecordsFileStorage<HistoryRecord>,
    backgroundTaskExecutorService: ExecutorService = defaultExecutor(HistoryDataProvider.PROVIDER_NAME),
    private val timeProvider: TimeProvider = LocalTimeProvider(),
    /**
     * This property was made as part of LocalDataProviderImpl class, because
     * during the test `addRecords()` function gets called before HistoryDataProviderImpl
     * finished initialization of its properties.
     */
    maxRecordsAmount: Int = Int.MAX_VALUE,
) : LocalDataProviderImpl<HistoryRecord>(
    dataProviderName = HistoryDataProvider.PROVIDER_NAME,
    priority = HistoryDataProvider.PROVIDER_PRIORITY,
    recordsStorage = recordsStorage,
    backgroundTaskExecutorService = backgroundTaskExecutorService,
    maxRecordsAmount = maxRecordsAmount,
), HistoryService {

    override fun MutableMap<String, HistoryRecord>.addAndTrimRecords(newRecords: List<HistoryRecord>): List<HistoryRecord> {
        putAll(newRecords.map { it.id to it })

        if (size <= maxRecordsAmount) {
            return emptyList()
        }

        // We sort items in timestamp descending order and also in descending "freshness" order
        // (newly added items should have higher priority over older items).
        val newRecordsMap = newRecords.asSequence()
            .mapIndexed { index, record -> record to index }
            .toMap()
        val comparator = Comparator<HistoryRecord> { record1, record2 ->
            val tsCompare = record2.timestamp.compareTo(record1.timestamp)
            return@Comparator if (tsCompare == 0) {
                val record1Priority = newRecordsMap[record1] ?: -1
                val record2Priority = newRecordsMap[record2] ?: -1
                record2Priority.compareTo(record1Priority)
            } else {
                tsCompare
            }
        }

        // We iterate in reverse order to ensure that records,
        // that were added to the `source` more recently,
        // will be placed in min heap with higher priority.
        val reversedRecords = values.reversed()

        // Prepare heap of size `deleteCount`
        val deleteCount = size - maxRecordsAmount
        val minHeap = PriorityQueue(deleteCount, comparator)
        minHeap.addAll(reversedRecords.take(deleteCount))

        // Determine `deleteCount` elements with lowest priority ...
        reversedRecords.asSequence().drop(deleteCount).forEach { nextRecord ->
            if (comparator.compare(nextRecord, minHeap.element()) >= 0) {
                minHeap.poll()
                minHeap.add(nextRecord)
            }
        }

        // ... and remove them
        minHeap.forEach { recordToRemove ->
            remove(recordToRemove.id)
        }

        return minHeap.toList()
    }

    override fun addToHistoryIfNeeded(
        searchResult: SearchResult,
        executor: Executor,
        callback: CompletionCallback<Boolean>
    ): AsyncOperationTask {
        return if (!searchResult.isHistory) {
            upsert(
                HistoryRecord(
                    id = searchResult.id,
                    name = searchResult.name,
                    descriptionText = searchResult.descriptionText,
                    address = searchResult.address,
                    routablePoints = searchResult.routablePoints,
                    categories = searchResult.categories,
                    makiIcon = searchResult.makiIcon,
                    coordinate = searchResult.coordinate,
                    type = searchResult.types.first(),
                    metadata = searchResult.metadata,
                    timestamp = timeProvider.currentTimeMillis(),
                ),
                executor,
                object : CompletionCallback<Unit> {
                    override fun onComplete(result: Unit) {
                        callback.onComplete(true)
                    }

                    override fun onError(e: Exception) {
                        callback.onError(e)
                    }
                }
            )
        } else {
            executor.execute {
                callback.onComplete(false)
            }
            return CompletedAsyncOperationTask
        }
    }

    private companion object {

        val SearchSuggestion.isHistory: Boolean
            get() = this is IndexableRecordSearchSuggestion && type.isHistoryRecord

        val SearchResult.isHistory: Boolean
            get() = this is IndexableRecordSearchResult && record is HistoryRecord
    }
}
