package com.mapbox.search.history

import com.mapbox.geojson.Point
import com.mapbox.search.BaseTest
import com.mapbox.search.ImageInfo
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.TestData
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.metadata.OpenHours
import com.mapbox.search.metadata.OpenPeriod
import com.mapbox.search.metadata.ParkingData
import com.mapbox.search.metadata.WeekDay
import com.mapbox.search.metadata.WeekTimestamp
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.record.HistoryRecordsSerializer
import com.mapbox.search.result.RoutablePoint
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.tests_support.createTestBaseRawSearchResult
import org.junit.Assert
import org.junit.Before
import org.junit.Test

internal class HistoryRecordsSerializerTest : BaseTest() {

    private lateinit var serializer: HistoryRecordsSerializer

    @Before
    override fun setUp() {
        super.setUp()
        serializer = HistoryRecordsSerializer()
    }

    @Test
    fun testSerializeEmptyData() {
        val serialized = serializer.serialize(emptyList())
        val deserialized = serializer.deserialize(serialized)
        Assert.assertEquals(emptyList<HistoryRecord>(), deserialized)
    }

    @Test
    fun testDataIsTheSameAfterSerialization() {
        val serialized = serializer.serialize(listOf(TEST_RECORD, TEST_EMPTY_RECORD))
        val deserialized = serializer.deserialize(serialized)
        Assert.assertEquals(listOf(TEST_RECORD, TEST_EMPTY_RECORD), deserialized)
    }

    @Test
    fun testDeserializeEmptyByteArray() {
        val deserialized = serializer.deserialize(ByteArray(0))
        Assert.assertEquals(emptyList<HistoryRecord>(), deserialized)
    }

    @Test
    fun testDeserializeCorrectJsonData() {
        val deserialized = serializer.deserialize(readBytesFromAssets("test_data_search_history.json"))
        Assert.assertEquals(
            listOf(EIFFEL_TOWER_HISTORY_RECORD, PRADO_MUSEUM_HISTORY_RECORD, OSLO_OPERA_HOUSE_HISTORY_RECORD),
            deserialized
        )
    }

    @Test
    fun tesIncorrectDataFiltered() {
        val deserialized = serializer.deserialize(readBytesFromAssets("test_data_search_history_incorrect.json"))
        Assert.assertEquals(
            listOf(TEST_EMPTY_RECORD),
            deserialized
        )
    }

    private companion object {

        val TEST_BASE_RAW_SEARCH_RESULT = createTestBaseRawSearchResult(
            id = "test id",
            types = listOf(BaseRawResultType.PLACE),
            names = listOf("test name"),
            languages = listOf("default"),
            addresses = listOf(SearchAddress(country = "Belarus")),
            distanceMeters = 123.0,
            center = Point.fromLngLat(.0, .1),
            categories = emptyList(),
        )

        val TEST_RECORD = HistoryRecord(
            id = TEST_BASE_RAW_SEARCH_RESULT.id,
            name = TEST_BASE_RAW_SEARCH_RESULT.names.first(),
            coordinate = Point.fromLngLat(.0, .1),
            descriptionText = "Belarus, Minsk",
            address = SearchAddress(country = "Belarus"),
            timestamp = 123L,
            type = SearchResultType.ADDRESS,
            makiIcon = "test maki",
            categories = listOf("test"),
            routablePoints = null,
            metadata = null,
        )

        val TEST_EMPTY_BASE_RAW_SEARCH_RESULT = createTestBaseRawSearchResult(
            id = "empty_correct_record_id",
            types = listOf(BaseRawResultType.POI),
            names = listOf("Empty correct record"),
            languages = listOf("default"),
        )

        val TEST_EMPTY_RECORD = HistoryRecord(
            id = TEST_EMPTY_BASE_RAW_SEARCH_RESULT.id,
            name = TEST_EMPTY_BASE_RAW_SEARCH_RESULT.names.first(),
            coordinate = null,
            descriptionText = null,
            address = null,
            timestamp = 123L,
            type = SearchResultType.POI,
            makiIcon = null,
            categories = null,
            routablePoints = null,
            metadata = null,
        )

        val EIFFEL_TOWER_HISTORY_RECORD = HistoryRecord(
            id = "poi.609885454495",
            name = "Eiffel Tower",
            coordinate = Point.fromLngLat(2.295135021209717, 48.859291076660156),
            descriptionText = TestData.EIFFEL_TOWER_ADDRESS_DESCRIPTION,
            address = TestData.EIFFEL_TOWER_ADDRESS,
            timestamp = 1598138435443L,
            type = SearchResultType.POI,
            makiIcon = null,
            categories = listOf("monument", "landmark", "historic"),
            routablePoints = null,
            metadata = null,
        )

        val PRADO_MUSEUM_HISTORY_RECORD = HistoryRecord(
            id = "poi.979252559345",
            name = "Museo del Prado",
            coordinate = Point.fromLngLat(-3.6922054290771484, 40.41393280029297),
            descriptionText = TestData.PRADO_MUSEUM_ADDRESS_DESCRIPTION,
            address = TestData.PRADO_MUSEUM_ADDRESS,
            timestamp = 1598138865525L,
            type = SearchResultType.ADDRESS,
            makiIcon = "museum",
            categories = listOf("monument", "landmark", "historic"),
            routablePoints = listOf(
                RoutablePoint(point = Point.fromLngLat(-3.6925262879649585, 40.413767129064276), name = "Entrance")
            ),
            metadata = SearchResultMetadata(
                metadata = hashMapOf("testKey" to "testValue"),
                reviewCount = 3456,
                phone = "+902 10 70 77",
                website = "https://www.museodelprado.es/en/visit-the-museum",
                averageRating = 9.7,
                description = "Museo Del Prado",
                primaryPhotos = listOf(ImageInfo(url = "https://museodelprado.es/pic1.jpg", width = 700, height = 150)),
                otherPhotos = listOf(
                    ImageInfo(url = "https://museodelprado.es/pic2.jpg", width = 500, height = 500),
                    ImageInfo(url = "https://museodelprado.es/pic3.jpg", width = 700, height = 300)
                ),
                openHours = OpenHours.Scheduled(
                    periods = listOf(
                        OpenPeriod(
                            open = WeekTimestamp(day = WeekDay.MONDAY, hour = 9, minute = 30),
                            closed = WeekTimestamp(day = WeekDay.MONDAY, hour = 17, minute = 0)
                        )
                    )
                ),
                parking = ParkingData(
                    totalCapacity = 4,
                    reservedForDisabilities = 2
                ),
                cpsJson = "{\"raw\":{}}"
            ),
        )

        val OSLO_OPERA_HOUSE_HISTORY_RECORD = HistoryRecord(
            id = "poi.798864010802",
            name = "Oslo Opera House",
            coordinate = Point.fromLngLat(10.752805709838867, 59.90727233886719),
            descriptionText = TestData.OSLO_OPERA_HOUSE_ADDRESS_DESCRIPTION,
            address = TestData.OSLO_OPERA_HOUSE_ADDRESS,
            timestamp = 1598138907968L,
            type = SearchResultType.POI,
            makiIcon = "opera",
            categories = listOf("theatre", "theater", "music", "show venue", "concert", "concert hall"),
            routablePoints = null,
            metadata = SearchResultMetadata(
                metadata = hashMapOf(),
                reviewCount = null,
                phone = null,
                website = null,
                averageRating = null,
                description = null,
                primaryPhotos = null,
                otherPhotos = emptyList(),
                openHours = null,
                parking = null,
                cpsJson = null
            ),
        )
    }
}
