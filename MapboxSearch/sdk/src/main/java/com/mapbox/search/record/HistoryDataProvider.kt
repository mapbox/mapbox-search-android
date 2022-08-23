package com.mapbox.search.record

import com.mapbox.search.CompletionCallback
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.base.record.BaseIndexableRecord
import com.mapbox.search.base.record.SearchHistoryService
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.base.utils.LocalTimeProvider
import com.mapbox.search.base.utils.TimeProvider
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.result.mapToPlatform
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

internal class HistoryDataProviderImpl(
    recordsStorage: RecordsFileStorage<HistoryRecord>,
    backgroundTaskExecutorService: ExecutorService = defaultExecutor(HistoryDataProvider.PROVIDER_NAME),
    private val timeProvider: TimeProvider = LocalTimeProvider(),
    /**
     * This property was made as part of LocalDataProviderImpl class, because
     * during the test `addRecords()` function gets called before HistoryDataProviderImpl
     * finished initialization of its properties.
     */
    maxRecordsAmount: Int = DEFAULT_MAX_HISTORY_RECORDS_AMOUNT
) : LocalDataProviderImpl<HistoryRecord>(
    dataProviderName = HistoryDataProvider.PROVIDER_NAME,
    priority = HistoryDataProvider.PROVIDER_PRIORITY,
    recordsStorage = recordsStorage,
    backgroundTaskExecutorService = backgroundTaskExecutorService,
    maxRecordsAmount = maxRecordsAmount,
), HistoryDataProvider, SearchHistoryService {

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
        searchResult: BaseSearchResult,
        executor: Executor,
        callback: (Result<Boolean>) -> Unit
    ): AsyncOperationTask {
        return if (!searchResult.isHistory) {
            upsert(
                HistoryRecord(
                    id = searchResult.id,
                    name = searchResult.name,
                    descriptionText = searchResult.descriptionText,
                    address = searchResult.address?.mapToPlatform(),
                    routablePoints = searchResult.routablePoints?.map { it.mapToPlatform() },
                    categories = searchResult.categories,
                    makiIcon = searchResult.makiIcon,
                    coordinate = searchResult.coordinate,
                    type = searchResult.types.first().mapToPlatform(),
                    metadata = searchResult.metadata?.let { SearchResultMetadata(it) },
                    timestamp = timeProvider.currentTimeMillis(),
                ),
                executor,
                object : CompletionCallback<Unit> {
                    override fun onComplete(result: Unit) {
                        callback(Result.success(true))
                    }

                    override fun onError(e: Exception) {
                        callback(Result.failure(e))
                    }
                }
            )
        } else {
            executor.execute {
                callback(Result.success(false))
            }
            return AsyncOperationTaskImpl.COMPLETED
        }
    }

    private companion object {

        const val DEFAULT_MAX_HISTORY_RECORDS_AMOUNT: Int = 100

        val BaseSearchResult.isHistory: Boolean
            get() = (baseType as? BaseSearchResult.Type.IndexableRecordSearchResult)?.record?.isHistory == true

        val BaseIndexableRecord.isHistory: Boolean
            get() = sdkResolvedRecord is HistoryRecord
    }
}
