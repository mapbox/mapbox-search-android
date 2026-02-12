package com.mapbox.search.record

import androidx.annotation.WorkerThread
import com.mapbox.common.SettingsServiceFactory
import com.mapbox.common.SettingsServiceStorageType
import com.mapbox.search.utils.loader.DataLoader
import com.mapbox.search.utils.serialization.RecordsSerializer

@WorkerThread
internal open class RecordsFileStorage<R : IndexableRecord>(
    private val dirName: String,
    fileName: String,
    private val serializer: RecordsSerializer<R, *, *>,
    private val dataLoader: DataLoader<ByteArray>
) : RecordsStorage<R> {

    private val fullFileName: String by lazy {
        getFullFileName(fileName)
    }

    override fun load(): List<R> {
        return serializer.deserialize(dataLoader.load(dirName, fullFileName))
    }

    override fun save(records: List<R>) {
        dataLoader.save(dirName, fullFileName, serializer.serialize(records))
    }

    class Favorite(dataLoader: DataLoader<ByteArray>) : RecordsFileStorage<FavoriteRecord>(
        DIR_NAME,
        FAVORITES_FILE_NAME,
        FavoriteRecordsSerializer(),
        dataLoader,
    )

    class History(dataLoader: DataLoader<ByteArray>) : RecordsFileStorage<HistoryRecord>(
        DIR_NAME,
        HISTORY_FILE_NAME,
        HistoryRecordsSerializer(),
        dataLoader,
    )

    private companion object {

        const val DIR_NAME = "mapbox_search_sdk"
        const val FAVORITES_FILE_NAME = "favorites"
        const val HISTORY_FILE_NAME = "search_history"

        const val DEFAULT_FILE_EXTENSION = ".bin"

        private fun getFullFileName(baseName: String): String {
            val ext = SettingsServiceFactory.getInstance(SettingsServiceStorageType.PERSISTENT)
                .get("com.mapbox.search.internal.experimental.records_files_ext")
                ?.value
                ?.contents as? String ?: DEFAULT_FILE_EXTENSION

            return "$baseName$ext"
        }
    }
}

internal interface RecordsStorage<R : IndexableRecord> {
    fun load(): List<R>
    fun save(records: List<R>)
}
