package com.mapbox.search.record

import android.annotation.SuppressLint
import androidx.annotation.CheckResult
import androidx.annotation.WorkerThread
import com.mapbox.search.AsyncOperationTask
import com.mapbox.search.AsyncOperationTaskImpl
import com.mapbox.search.CompletionCallback
import com.mapbox.search.plusAssign
import com.mapbox.search.runIfNotCancelled
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker
import okhttp3.internal.notifyAll
import okhttp3.internal.wait
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * [IndexableDataProvider] that represents stored locally by the SDK user data.
 * @see IndexableDataProvider
 * @see FavoritesDataProvider
 * @see HistoryDataProvider
 */
public interface LocalDataProvider<R : IndexableRecord> : IndexableDataProvider<R> {

    /**
     * Adds a listener to be notified of data provider changes.
     *
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param listener The listener to be notified of data change events.
     */
    public fun addOnDataChangedListener(executor: Executor, listener: OnDataChangedListener<R>)

    /**
     * Adds a listener to be notified of data provider changes.
     *
     * @param listener The listener to be notified of data change events. By default events are dispatched on the main thread.
     */
    public fun addOnDataChangedListener(listener: OnDataChangedListener<R>): Unit = addOnDataChangedListener(
        executor = SearchSdkMainThreadWorker.mainExecutor,
        listener = listener
    )

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeOnDataChangedListener(listener: OnDataChangedListener<R>)

    /**
     * Adds a listener to be notified when [IndexableDataProviderEngine] registered in this data provider.
     * When a new listener is added, it will be invoked for each previously registered [IndexableDataProviderEngine].
     *
     * @param executor Executor used for events dispatching. By default events are dispatched on the main thread.
     * @param listener The listener to be notified of registration events.
     * @see registerIndexableDataProviderEngine
     */
    public fun addOnDataProviderEngineRegisterListener(
        executor: Executor,
        listener: OnDataProviderEngineRegisterListener
    )

    /**
     * Adds a listener to be notified when [IndexableDataProviderEngine] registered in this data provider.
     * When a new listener is added, it will be invoked for each previously registered [IndexableDataProviderEngine].
     *
     * @param listener The listener to be notified of registration events. Events are dispatched on the main thread.
     * @see registerIndexableDataProviderEngine
     */
    public fun addOnDataProviderEngineRegisterListener(
        listener: OnDataProviderEngineRegisterListener
    ): Unit = addOnDataProviderEngineRegisterListener(
        executor = SearchSdkMainThreadWorker.mainExecutor,
        listener = listener,
    )

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeOnDataProviderEngineRegisterListener(
        listener: OnDataProviderEngineRegisterListener
    )

    /**
     * Listener to be notified when [IndexableDataProviderEngine] registered in this data provider.
     */
    public interface OnDataProviderEngineRegisterListener {

        /**
         * Invoked when [IndexableDataProviderEngine] has been registered in this data provider.
         * @param engine Registered [IndexableDataProviderEngine].
         */
        public fun onEngineRegistered(engine: IndexableDataProviderEngine)

        /**
         * Invoked when an error happened during [IndexableDataProviderEngine] registration.
         * @param e Exception, occurred during registration.
         */
        public fun onEngineRegistrationError(e: Exception)
    }

    /**
     * Listener to be notified of data provider changes.
     */
    public interface OnDataChangedListener<R : IndexableRecord> {

        /**
         * Invoked when data provider items changed.
         * @param newData current items of data provider.
         */
        public fun onDataChanged(newData: List<R>)
    }
}

@Suppress("LargeClass")
internal abstract class LocalDataProviderImpl<R : IndexableRecord>(
    override val dataProviderName: String,
    override val priority: Int,
    private val recordsStorage: RecordsStorage<R>,
    private val dataProviderEngines: CopyOnWriteArrayList<IndexableDataProviderEngine> = CopyOnWriteArrayList(),
    protected val backgroundTaskExecutorService: ExecutorService = defaultExecutor(dataProviderName),
    protected val maxRecordsAmount: Int = Int.MAX_VALUE,
) : IndexableDataProvider<R>, LocalDataProvider<R> {

    private val dataChangeListeners: MutableMap<LocalDataProvider.OnDataChangedListener<R>, Executor> = ConcurrentHashMap()

    private val engineRegisterListeners: MutableMap<LocalDataProvider.OnDataProviderEngineRegisterListener, Executor> = ConcurrentHashMap()
    private val dataProviderEngineLock = Any()

    // TODO this variable must be synchronized so that it can be used in a multi-threaded environment.
    // In fact, now it's used only from one thread (because of Executors.newSingleThreadExecutor())
    @Volatile
    protected var dataState: DataState<R>? = null

    private val initializingLock: Any = Any()

    init {
        require(maxRecordsAmount > 0) {
            "Provided 'maxRecordsAmount' should be greater than 0 (provided value: $maxRecordsAmount)"
        }
        initialRead()
    }

    @SuppressLint("CheckResult")
    private fun initialRead() {
        backgroundTaskExecutorService.submit {
            try {
                val loaded = recordsStorage.load()

                val records: MutableMap<String, R> = Collections.synchronizedMap(LinkedHashMap())
                records.addAndTrimRecords(loaded)

                dataState = DataState.Data(records)

                val recordsList = records.values.toList()
                dataProviderEngines.forEach { dataProviderEngine ->
                    dataProviderEngine.addAll(recordsList)
                }
            } catch (e: Exception) {
                dataState = DataState.Error(e)
            } finally {
                synchronized(initializingLock) {
                    initializingLock.notifyAll()
                }
            }
        }
    }

    @WorkerThread
    private fun getLocalData(): DataState<R> {
        var data = dataState
        if (data == null) {
            synchronized(initializingLock) {
                data = dataState
                while (data == null) {
                    initializingLock.wait()
                    data = dataState
                }
            }
        }
        return requireNotNull(data)
    }

    @WorkerThread
    private fun persistData(records: List<R>) {
        recordsStorage.save(records)
    }

    @WorkerThread
    private fun notifyListeners(records: List<R>) {
        dataChangeListeners.entries.forEach { (listener, executor) ->
            executor.execute {
                listener.onDataChanged(records)
            }
        }
    }

    /**
     * Special-purpose function to add [newRecords] and trim map's size to [maxRecordsAmount].
     * Created for an optimisation purpose.
     *
     * @return list of records, that have been removed from the given map.
     */
    @WorkerThread
    @CheckResult
    protected open fun MutableMap<String, R>.addAndTrimRecords(newRecords: List<R>): List<R> {
        putAll(newRecords.map { it.id to it })
        return emptyList()
    }

    private fun postOnExecutorIfNeeded(task: AsyncOperationTaskImpl, executor: Executor, action: Runnable) {
        task.runIfNotCancelled {
            executor.execute {
                task.runIfNotCancelled {
                    onComplete()
                    action.run()
                }
            }
        }
    }

    override fun registerIndexableDataProviderEngine(
        dataProviderEngine: IndexableDataProviderEngine,
        executor: Executor,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask {
        val task = AsyncOperationTaskImpl()
        task += backgroundTaskExecutorService.submit {
            when (val dataState = getLocalData()) {
                is DataState.Data -> {
                    dataProviderEngine.addAll(dataState.records.values)

                    synchronized(dataProviderEngineLock) {
                        dataProviderEngines.add(dataProviderEngine)
                        engineRegisterListeners.entries.forEach { (listener, executor) ->
                            executor.execute {
                                listener.onEngineRegistered(dataProviderEngine)
                            }
                        }
                    }

                    postOnExecutorIfNeeded(task, executor) {
                        callback.onComplete(Unit)
                    }
                }
                is DataState.Error -> {
                    synchronized(dataProviderEngineLock) {
                        engineRegisterListeners.entries.forEach { (listener, executor) ->
                            executor.execute {
                                listener.onEngineRegistrationError(dataState.error)
                            }
                        }
                    }

                    postOnExecutorIfNeeded(task, executor) {
                        callback.onError(dataState.error)
                    }
                }
            }
        }
        return task
    }

    override fun unregisterIndexableDataProviderEngine(
        dataProviderEngine: IndexableDataProviderEngine,
        executor: Executor,
        callback: CompletionCallback<Boolean>
    ): AsyncOperationTask {
        val task = AsyncOperationTaskImpl()
        task += backgroundTaskExecutorService.submit {
            val isRemoved = dataProviderEngines.remove(dataProviderEngine)
            if (isRemoved) {
                dataProviderEngine.clear()
            }
            postOnExecutorIfNeeded(task, executor) {
                callback.onComplete(isRemoved)
            }
        }
        return task
    }

    override fun get(id: String, executor: Executor, callback: CompletionCallback<in R?>): AsyncOperationTask {
        val task = AsyncOperationTaskImpl()
        task += backgroundTaskExecutorService.submit {
            when (val dataState = getLocalData()) {
                is DataState.Data -> {
                    val data = dataState.records
                    val result = data[id]
                    postOnExecutorIfNeeded(task, executor) {
                        callback.onComplete(result)
                    }
                }
                is DataState.Error -> {
                    postOnExecutorIfNeeded(task, executor) {
                        callback.onError(dataState.error)
                    }
                }
            }
        }
        return task
    }

    override fun getAll(executor: Executor, callback: CompletionCallback<List<R>>): AsyncOperationTask {
        val task = AsyncOperationTaskImpl()
        task += backgroundTaskExecutorService.submit {
            when (val dataState = getLocalData()) {
                is DataState.Data -> {
                    val data = dataState.records
                    val result = data.values.toList()
                    postOnExecutorIfNeeded(task, executor) {
                        callback.onComplete(result)
                    }
                }
                is DataState.Error -> {
                    postOnExecutorIfNeeded(task, executor) {
                        callback.onError(dataState.error)
                    }
                }
            }
        }
        return task
    }

    override fun contains(id: String, executor: Executor, callback: CompletionCallback<Boolean>): AsyncOperationTask {
        val task = AsyncOperationTaskImpl()
        task += backgroundTaskExecutorService.submit {
            when (val dataState = getLocalData()) {
                is DataState.Data -> {
                    val data = dataState.records
                    val result = data.contains(id)
                    postOnExecutorIfNeeded(task, executor) {
                        callback.onComplete(result)
                    }
                }
                is DataState.Error -> {
                    postOnExecutorIfNeeded(task, executor) {
                        callback.onError(dataState.error)
                    }
                }
            }
        }
        return task
    }

    // TODO should we replace old item when we add a new with the same id?
    override fun add(record: R, executor: Executor, callback: CompletionCallback<Unit>): AsyncOperationTask {
        return addAll(listOf(record), executor, callback)
    }

    override fun addAll(
        records: List<R>,
        executor: Executor,
        callback: CompletionCallback<Unit>
    ): AsyncOperationTask {
        val task = AsyncOperationTaskImpl()
        task += backgroundTaskExecutorService.submit {
            when (val dataState = getLocalData()) {
                is DataState.Data -> {
                    try {
                        val data = dataState.recordsCopy()
                        val removeList = data.addAndTrimRecords(records).map { it.id }

                        val recordsList = data.values.toList()
                        persistData(recordsList)

                        this.dataState = DataState.Data(data)

                        dataProviderEngines.forEach { dataProviderEngine ->
                            dataProviderEngine.executeBatchUpdate { engine ->
                                engine.addAll(records)
                                engine.removeAll(removeList)
                            }
                        }

                        postOnExecutorIfNeeded(task, executor) {
                            callback.onComplete(Unit)
                        }

                        notifyListeners(recordsList)
                    } catch (e: Exception) {
                        postOnExecutorIfNeeded(task, executor) {
                            callback.onError(e)
                        }
                    }
                }
                is DataState.Error -> {
                    postOnExecutorIfNeeded(task, executor) {
                        callback.onError(dataState.error)
                    }
                }
            }
        }
        return task
    }

    override fun update(record: R, executor: Executor, callback: CompletionCallback<Unit>): AsyncOperationTask {
        val task = AsyncOperationTaskImpl()
        task += backgroundTaskExecutorService.submit {
            when (val dataState = getLocalData()) {
                is DataState.Data -> {
                    try {
                        val data = dataState.recordsCopy()
                        val removeList = data.addAndTrimRecords(listOf(record)).map { it.id }

                        val recordsList = data.values.toList()
                        persistData(recordsList)

                        this.dataState = DataState.Data(data)

                        dataProviderEngines.forEach { dataProviderEngine ->
                            dataProviderEngine.executeBatchUpdate { engine ->
                                engine.update(record)
                                engine.removeAll(removeList)
                            }
                        }

                        postOnExecutorIfNeeded(task, executor) {
                            callback.onComplete(Unit)
                        }

                        notifyListeners(recordsList)
                    } catch (e: Exception) {
                        postOnExecutorIfNeeded(task, executor) {
                            callback.onError(e)
                        }
                    }
                }
                is DataState.Error -> {
                    postOnExecutorIfNeeded(task, executor) {
                        callback.onError(dataState.error)
                    }
                }
            }
        }
        return task
    }

    override fun remove(id: String, executor: Executor, callback: CompletionCallback<Boolean>): AsyncOperationTask {
        val task = AsyncOperationTaskImpl()
        task += backgroundTaskExecutorService.submit {
            when (val dataState = getLocalData()) {
                is DataState.Data -> {
                    try {
                        val data = dataState.recordsCopy()
                        val removed = data.remove(id)
                        val isRemoved = removed != null

                        val recordsList = data.values.toList()
                        if (isRemoved) {
                            persistData(recordsList)
                            this.dataState = DataState.Data(data)
                        }

                        dataProviderEngines.forEach { dataProviderEngine ->
                            dataProviderEngine.remove(id)
                        }

                        postOnExecutorIfNeeded(task, executor) {
                            callback.onComplete(isRemoved)
                        }

                        notifyListeners(recordsList)
                    } catch (e: Exception) {
                        postOnExecutorIfNeeded(task, executor) {
                            callback.onError(e)
                        }
                    }
                }
                is DataState.Error -> {
                    postOnExecutorIfNeeded(task, executor) {
                        callback.onError(dataState.error)
                    }
                }
            }
        }
        return task
    }

    override fun clear(executor: Executor, callback: CompletionCallback<Unit>): AsyncOperationTask {
        val task = AsyncOperationTaskImpl()
        task += backgroundTaskExecutorService.submit {
            when (val dataState = getLocalData()) {
                is DataState.Data -> {
                    try {
                        val data = dataState.recordsCopy()
                        val changed = data.isNotEmpty()
                        data.clear()

                        val recordsList = data.values.toList()
                        if (changed) {
                            persistData(recordsList)
                            this.dataState = DataState.Data(data)
                        }

                        dataProviderEngines.forEach { dataProviderEngine ->
                            dataProviderEngine.clear()
                        }

                        postOnExecutorIfNeeded(task, executor) {
                            callback.onComplete(Unit)
                        }

                        notifyListeners(recordsList)
                    } catch (e: Exception) {
                        postOnExecutorIfNeeded(task, executor) {
                            callback.onError(e)
                        }
                    }
                }
                is DataState.Error -> {
                    postOnExecutorIfNeeded(task, executor) {
                        callback.onError(dataState.error)
                    }
                }
            }
        }
        return task
    }

    override fun addOnDataChangedListener(executor: Executor, listener: LocalDataProvider.OnDataChangedListener<R>) {
        dataChangeListeners[listener] = executor
    }

    override fun removeOnDataChangedListener(listener: LocalDataProvider.OnDataChangedListener<R>) {
        dataChangeListeners.remove(listener)
    }

    override fun addOnDataProviderEngineRegisterListener(
        executor: Executor,
        listener: LocalDataProvider.OnDataProviderEngineRegisterListener
    ) {
        synchronized(dataProviderEngineLock) {
            engineRegisterListeners[listener] = executor
            dataProviderEngines.forEach { engine ->
                executor.execute {
                    listener.onEngineRegistered(engine)
                }
            }
        }
    }

    override fun removeOnDataProviderEngineRegisterListener(
        listener: LocalDataProvider.OnDataProviderEngineRegisterListener
    ) {
        synchronized(dataProviderEngineLock) {
            engineRegisterListeners.remove(listener)
        }
    }

    protected sealed class DataState<R> {
        data class Data<R>(val records: MutableMap<String, R>) : DataState<R>() {
            fun recordsCopy(): MutableMap<String, R> = Collections.synchronizedMap(LinkedHashMap(records))
        }

        data class Error<R>(val error: Exception) : DataState<R>()
    }

    companion object {
        fun defaultExecutor(providerName: String): ExecutorService {
            return Executors.newSingleThreadExecutor { runnable ->
                Thread(runnable, "LocalDataProvider executor for $providerName")
            }
        }
    }
}
