package com.mapbox.search.record

import androidx.annotation.WorkerThread
import com.mapbox.search.utils.loader.DataLoader
import com.mapbox.search.utils.serialization.RecordsSerializer

@WorkerThread
internal sealed class RecordsFileStorage<R : IndexableRecord>(
    private val dirName: String,
    private val fileName: String,
    private val serializer: RecordsSerializer<R, *, *>,
    private val dataLoader: DataLoader<ByteArray>
) : RecordsStorage<R> {

    override fun load(): List<R> {
        return serializer.deserialize(dataLoader.load(dirName, fileName))
    }

    override fun save(records: List<R>) {
        dataLoader.save(dirName, fileName, serializer.serialize(records))
    }

    class Favorite(dataLoader: DataLoader<ByteArray>) :
        RecordsFileStorage<FavoriteRecord>(DIR_NAME, FAVORITES_FILE_NAME, FavoriteRecordsSerializer(), dataLoader)

    class History(dataLoader: DataLoader<ByteArray>) :
        RecordsFileStorage<HistoryRecord>(DIR_NAME, HISTORY_FILE_NAME, HistoryRecordsSerializer(), dataLoader)

    private companion object {
        const val DIR_NAME = "mapbox_search_sdk"
        const val FAVORITES_FILE_NAME = "favorites.bin"
        const val HISTORY_FILE_NAME = "search_history.bin"
    }
}

internal interface RecordsStorage<R : IndexableRecord> {
    fun load(): List<R>
    fun save(records: List<R>)
}
