package com.mapbox.search.analytics

import com.mapbox.search.BaseTest
import com.mapbox.search.analytics.events.AppMetadata
import com.mapbox.search.analytics.events.SearchFeedbackEvent
import org.json.JSONException
import org.junit.Assert
import org.junit.Test

internal class AnalyticsEventJsonParserTest : BaseTest() {

    @Test
    fun testFeedbackEventParse() {
        val jsonParser = AnalyticsEventJsonParser()
        val textJson = readFileFromAssets("search-feedback-event.json")

        val event = SearchFeedbackEvent().apply {
            event = "search.feedback"
            feedbackReason = "Reason Incorrect data"
            feedbackText = "Text Incorrect data"
            resultIndex = 99
            selectedItemName = "test selectedItemName"
            resultId = "region.16795317862473520"
            responseUuid = "e0a2b1d6-3621-11eb-adc1-0242ac120002"
            feedbackId = "e0a2b1d6-3621-11eb-adc1-0242ac120003"
            isTest = true
            screenshot = "VGhlIHBhdGggb2YgdGhlIHJpZ2h0ZW91cyBtYW4gaXMgYmVzZXQ"
            resultCoordinates = listOf(2.294423282146454, 48.85825817805569)
            requestParamsJson = "{\"navigation_profile\":\"driving\",\"eta_type\":\"navigation\",\"route\":\"iikeFfygjVixNiwhAjvNkw\",\"route_geometry\":\"polyline6\",\"time_deviation\":1,\"sar_type\":\"isochrone\",\"origin\":[1.0,1.0]}"
            appMetadata = AppMetadata(
                name = "App Name",
                version = "v1.1",
                userId = "test-user-id",
                sessionId = "test-session-id"
            )
            searchResultsJson = "{\"results\":[{\"address\":\"Minsk Region, Belarus, Planet Earth\",\"coordinates\":[27.234342,53.940465],\"external_ids\":{\"carmen\":\"place.11543680732831130\"},\"id\":\"place.11543680732831130\",\"language\":[\"en\"],\"name\":\"Minsk\",\"result_type\":[\"PLACE\",\"REGION\"]}],\"multiStepSearch\":true}"
            queryString = "Minsk"
            cached = false
            fillBaseSearchData()
        }

        Assert.assertEquals(event, jsonParser.parse(textJson))
    }

    @Test
    fun testFeedbackEventSerialize() {
        val jsonParser = AnalyticsEventJsonParser()

        val event = SearchFeedbackEvent().apply {
            event = "search.feedback"
            feedbackReason = "Reason Incorrect data"
            feedbackText = "Text Incorrect data"
            resultIndex = 99
            selectedItemName = "test selectedItemName"
            resultId = "region.16795317862473520"
            responseUuid = "e0a2b1d6-3621-11eb-adc1-0242ac120002"
            feedbackId = "e0a2b1d6-3621-11eb-adc1-0242ac120003"
            isTest = true
            screenshot = "VGhlIHBhdGggb2YgdGhlIHJpZ2h0ZW91cyBtYW4gaXMgYmVzZXQ"
            resultCoordinates = listOf(2.294423282146454, 48.85825817805569)
            requestParamsJson = "{\"navigation_profile\":\"driving\",\"eta_type\":\"navigation\",\"route\":\"iikeFfygjVixNiwhAjvNkw\",\"route_geometry\":\"polyline6\",\"time_deviation\":1,\"sar_type\":\"isochrone\",\"origin\":[1.0,1.0]}"
            appMetadata = AppMetadata(
                name = "App Name",
                version = "v1.1",
                userId = "test-user-id",
                sessionId = "test-session-id"
            )
            searchResultsJson = "{\"results\":[{\"address\":\"Minsk Region, Belarus, Planet Earth\",\"coordinates\":[27.234342,53.940465],\"external_ids\":{\"carmen\":\"place.11543680732831130\"},\"id\":\"place.11543680732831130\",\"language\":[\"en\"],\"name\":\"Minsk\",\"result_type\":[\"PLACE\",\"REGION\"]}],\"multiStepSearch\":true}"
            queryString = "Minsk"
            cached = false
            fillBaseSearchData()
        }

        val serializedEvent = jsonParser.serialize(event)
        Assert.assertEquals(event, jsonParser.parse(serializedEvent))
    }

    @Test
    fun testEmptyFeedbackEventParse() {
        val jsonParser = AnalyticsEventJsonParser()
        val textJson = readFileFromAssets("search-feedback-event-empty.json")

        val event = SearchFeedbackEvent().apply {
            event = "search.feedback"
        }

        Assert.assertEquals(event, jsonParser.parse(textJson))
    }

    @Test(expected = JSONException::class)
    fun testEmptyJsonFile() {
        AnalyticsEventJsonParser().parse("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testUnknownEventType() {
        AnalyticsEventJsonParser().parse(readFileFromAssets("search-event-unknown.json"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIncorrectEventSerialize() {
        AnalyticsEventJsonParser().serialize(
            SearchFeedbackEvent().apply {
                event = "incorrect event"
            }
        )
    }

    private companion object {

        fun SearchFeedbackEvent.fillBaseSearchData() {
            created = "2020-05-16T23:06:35+0300"
            latitude = 53.911334999999994
            limit = 10
            longitude = 27.55140833333333
            proximity = listOf(27.55140833333333, 53.911334999999994)
            sessionIdentifier = "2068adda-16c0-4dfe-a5a1-4a08aee8a600"
            userAgent = "search-sdk-android-internal"
            boundingBox = listOf(27.55140833333333, 53.911334999999994, 27.55140833333333, 53.911334999999994)
            autocomplete = true
            routing = false
            country = listOf("by")
            orientation = "portrait"
            fuzzyMatch = false
            endpoint = "mapbox.com"
            language = listOf("by")
            keyboardLocale = "by"
            mapZoom = 11.0f
            mapCenterLatitude = 10.1
            mapCenterLongitude = 11.1234567
            schema = "test-schema-1.0"
        }
    }
}
