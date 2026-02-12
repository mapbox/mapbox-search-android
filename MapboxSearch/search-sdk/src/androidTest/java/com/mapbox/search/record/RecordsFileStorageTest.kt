package com.mapbox.search.record

import com.mapbox.bindgen.Value
import com.mapbox.common.SettingsServiceFactory
import com.mapbox.common.SettingsServiceStorageType
import com.mapbox.search.BaseTest
import com.mapbox.search.utils.file.InternalFileSystem
import com.mapbox.search.utils.loader.DataLoader
import com.mapbox.search.utils.loader.InternalDataLoader
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

internal class RecordsFileStorageTest : BaseTest() {

    private val recordingDataLoader = RecordingDataLoader(
        delegate = InternalDataLoader(targetApplication, InternalFileSystem())
    )

    @Test
    fun testHistoryWithDefaultFileExtension() {
        SettingsServiceFactory.getInstance(SettingsServiceStorageType.PERSISTENT)
            .set(RECORDS_FILES_EXT_KEY, Value.nullValue())

        val historyStorage = RecordsFileStorage.History(recordingDataLoader)

        historyStorage.load()
        historyStorage.save(emptyList())

        assertEquals(
            listOf("search_history.bin"),
            recordingDataLoader.recordedLoads,
        )
        assertEquals(
            listOf("search_history.bin"),
            recordingDataLoader.recordedSaves,
        )
    }

    @Test
    fun testHistoryWithCustomFileExtension() {
        SettingsServiceFactory.getInstance(SettingsServiceStorageType.PERSISTENT)
            .set(RECORDS_FILES_EXT_KEY, Value(CUSTOM_EXTENSION))

        val historyStorage = RecordsFileStorage.History(recordingDataLoader)

        historyStorage.load()
        historyStorage.save(emptyList())

        assertEquals(
            listOf("search_history$CUSTOM_EXTENSION"),
            recordingDataLoader.recordedLoads,
        )
        assertEquals(
            listOf("search_history$CUSTOM_EXTENSION"),
            recordingDataLoader.recordedSaves,
        )
    }

    @Test
    fun testFavoritesWithDefaultFileExtension() {
        SettingsServiceFactory.getInstance(SettingsServiceStorageType.PERSISTENT)
            .set(RECORDS_FILES_EXT_KEY, Value.nullValue())

        val favoritesStorage = RecordsFileStorage.Favorite(recordingDataLoader)

        favoritesStorage.load()
        favoritesStorage.save(emptyList())

        assertEquals(
            listOf("favorites.bin"),
            recordingDataLoader.recordedLoads,
        )
        assertEquals(
            listOf("favorites.bin"),
            recordingDataLoader.recordedSaves,
        )
    }

    @Test
    fun testFavoritesWithCustomFileExtension() {
        SettingsServiceFactory.getInstance(SettingsServiceStorageType.PERSISTENT)
            .set(RECORDS_FILES_EXT_KEY, Value(CUSTOM_EXTENSION))

        val favoritesStorage = RecordsFileStorage.Favorite(recordingDataLoader)

        favoritesStorage.load()
        favoritesStorage.save(emptyList())

        assertEquals(
            listOf("favorites$CUSTOM_EXTENSION"),
            recordingDataLoader.recordedLoads,
        )
        assertEquals(
            listOf("favorites$CUSTOM_EXTENSION"),
            recordingDataLoader.recordedSaves,
        )
    }

    private class RecordingDataLoader(
        private val delegate: DataLoader<ByteArray>,
    ) : DataLoader<ByteArray> {

        val recordedLoads = CopyOnWriteArrayList<String>()
        val recordedSaves = CopyOnWriteArrayList<String>()

        override fun load(relativeDir: String, fileName: String): ByteArray {
            recordedLoads.add(fileName)
            return delegate.load(relativeDir, fileName)
        }

        override fun save(relativeDir: String, fileName: String, data: ByteArray) {
            recordedSaves.add(fileName)
            delegate.save(relativeDir, fileName, data)
        }
    }

    private companion object {
        const val RECORDS_FILES_EXT_KEY = "com.mapbox.search.internal.experimental.records_files_ext"
        const val CUSTOM_EXTENSION = ".json"
    }
}
