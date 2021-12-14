package com.mapbox.search.analytics

import com.mapbox.search.analytics.events.AppMetadata
import com.mapbox.search.analytics.events.BaseSearchEvent
import com.mapbox.search.analytics.events.BaseSearchSessionEvent
import com.mapbox.search.analytics.events.QueryChangeEvent
import com.mapbox.search.analytics.events.SearchFeedbackEvent
import com.mapbox.search.analytics.events.SearchSelectEvent
import com.mapbox.search.analytics.events.SearchStartEvent
import com.mapbox.search.utils.assertEqualsButNotSame
import com.mapbox.search.utils.cloneFromParcel
import org.junit.Test

internal class AnalyticsEventParcelizationTest {

    @Test
    fun searchSelectEventParcelTest() {
        val event = createTestSelectEvent()
        val clonedEvent = event.cloneFromParcel()
        assertEqualsButNotSame(event, clonedEvent)
    }

    @Test
    fun emptySearchSelectEventParcelTest() {
        val event = SearchSelectEvent()
        val clonedEvent = event.cloneFromParcel()
        assertEqualsButNotSame(event, clonedEvent)
    }

    @Test
    fun searchStartEventParcelTest() {
        val event = SearchStartEvent().apply {
            fillBaseSearchSessionData()
        }
        val clonedEvent = event.cloneFromParcel()
        assertEqualsButNotSame(event, clonedEvent)
    }

    @Test
    fun emptySearchStartEventParcelTest() {
        val event = SearchStartEvent()
        val clonedEvent = event.cloneFromParcel()
        assertEqualsButNotSame(event, clonedEvent)
    }

    @Test
    fun queryChangeEventParcelTest() {
        val event = createTestQueryChangeEvent()
        val clonedEvent = event.cloneFromParcel()
        assertEqualsButNotSame(event, clonedEvent)
    }

    @Test
    fun emptyQueryChangeEventParcelTest() {
        val event = QueryChangeEvent()
        val clonedEvent = event.cloneFromParcel()
        assertEqualsButNotSame(event, clonedEvent)
    }

    @Test
    fun feedbackEventParcelTest() {
        val event = createTestFeedbackEvent()
        val clonedEvent = event.cloneFromParcel()
        assertEqualsButNotSame(event, clonedEvent)
    }

    @Test
    fun emptyFeedbackEventParcelTest() {
        val event = SearchFeedbackEvent()
        val clonedEvent = event.cloneFromParcel()
        assertEqualsButNotSame(event, clonedEvent)
    }

    private companion object {

        fun createTestQueryChangeEvent(): QueryChangeEvent {
            return QueryChangeEvent().apply {
                fillBaseSearchData()
                oldQuery = "map"
                newQuery = "maps"
                changeType = "keyboard_type"
            }
        }

        fun createTestSelectEvent(): SearchSelectEvent {
            return SearchSelectEvent().apply {
                fillBaseSearchSessionData()
                resultIndex = 0
                resultPlaceName = "test resultPlaceName"
                resultId = "region.16795317862473520"
            }
        }

        fun createTestFeedbackEvent(): SearchFeedbackEvent {
            return SearchFeedbackEvent().apply {
                fillBaseSearchSessionData()
                feedbackReason = "Reason Incorrect data"
                feedbackText = "Text Incorrect data"
                resultIndex = 99
                selectedItemName = "test selectedItemName"
                resultId = "region.16795317862473520"
                responseUuid = "e0a2b1d6-3621-11eb-adc1-0242ac120002"
                requestParamsJson = "{\"navigation_profile\":\"driving\",\"eta_type\":\"navigation\",\"route\":\"iikeFfygjVixNiwhAjvNkw\",\"route_geometry\":\"polyline6\",\"time_deviation\":1,\"sar_type\":\"isochrone\",\"origin\":[1.0,1.0]}"
                appMetadata = AppMetadata(
                    name = "App Name",
                    version = "v1.1",
                    userId = "test-user-id",
                    sessionId = "test-session-id"
                )
                searchResultsJson = "{\"results\":[{\"address\":\"Minsk Region, Belarus, Planet Earth\",\"coordinates\":[27.234342,53.940465],\"external_ids\":{\"carmen\":\"place.11543680732831130\"},\"id\":\"place.11543680732831130\",\"language\":[\"en\"],\"name\":\"Minsk\",\"result_type\":[\"PLACE\",\"REGION\"]}],\"multiStepSearch\":true}"
            }
        }

        fun BaseSearchSessionEvent.fillBaseSearchSessionData() {
            fillBaseSearchData()
            cached = true
            queryString = "test query"
        }

        fun BaseSearchEvent.fillBaseSearchData() {
            event = "test event"
            created = "created date"
            latitude = 1.0
            longitude = 1.0
            sessionIdentifier = "test sessionIdentifier"
            userAgent = "test userAgent"
            boundingBox = listOf(.0, .1)
            autocomplete = true
            routing = true
            country = listOf("test country")
            types = listOf("address", "poi")
            endpoint = "test"
            orientation = "portrait"
            proximity = listOf(.0, .1)
            fuzzyMatch = true
            limit = 100
            language = listOf("by")
            keyboardLocale = "by"
            mapZoom = 11.0f
            mapCenterLatitude = 10.1
            mapCenterLongitude = 11.1234567
            schema = "test-schema-v1.0"
        }
    }
}
