package com.mapbox.search.analytics.events

import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class AnalyticsEventsTest {

    @TestFactory
    fun `Check test search feedback events for validity`() = TestCase {
        TEST_SEARCH_FEEDBACK_EVENTS.forEach { (inputValue, expectedValue) ->
            Given("SearchFeedbackEvent = $inputValue") {
                When("SearchFeedbackEvent = $inputValue") {
                    val actualValue = inputValue.isValid
                    Then("Event is valid should be <$expectedValue>", expectedValue, actualValue)
                }
            }
        }
    }

    @TestFactory
    fun `Check test app metadata for validity`() = TestCase {
        TEST_APP_METADATAS.forEach { (inputValue, expectedValue) ->
            Given("AppMetadata = $inputValue") {
                When("AppMetadata = $inputValue") {
                    val actualValue = inputValue.isValid
                    Then("Event is valid should be <$expectedValue>", expectedValue, actualValue)
                }
            }
        }
    }

    private companion object {
        const val TEST_SESSION_IDENTIFIER = "123456"
        const val TEST_TIME_IN_CORRECT_FORMAT = "2020-11-23T11:03:08+0300"
        const val TEST_RESULT_INDEX = 0
        const val TEST_FEEDBACK_REASON = "Other reason"
        const val TEST_FEEDBACK_TEXT = "Incorrect coordinates"
        const val TEST_RESPONSE_UUID = "e0a2b1d6-3621-11eb-adc1-0242ac120002"
        const val SEARCH_FEEDBACK_EVENT_NAME = "search.feedback"

        val TEST_SEARCH_FEEDBACK_EVENTS: Map<SearchFeedbackEvent, Boolean>
            get() = mapOf(
                SearchFeedbackEvent().apply { event = null; queryString = "a"; sessionIdentifier = TEST_SESSION_IDENTIFIER; created = TEST_TIME_IN_CORRECT_FORMAT; resultIndex = TEST_RESULT_INDEX; feedbackReason = TEST_FEEDBACK_REASON; feedbackText = TEST_FEEDBACK_TEXT; selectedItemName = "A"; responseUuid = TEST_RESPONSE_UUID } to false,
                SearchFeedbackEvent().apply { event = ""; queryString = "a"; sessionIdentifier = TEST_SESSION_IDENTIFIER; created = TEST_TIME_IN_CORRECT_FORMAT; resultIndex = TEST_RESULT_INDEX; feedbackReason = TEST_FEEDBACK_REASON; feedbackText = TEST_FEEDBACK_TEXT; selectedItemName = "A"; responseUuid = TEST_RESPONSE_UUID } to false,
                SearchFeedbackEvent().apply { event = SEARCH_FEEDBACK_EVENT_NAME; queryString = null; sessionIdentifier = TEST_SESSION_IDENTIFIER; created = TEST_TIME_IN_CORRECT_FORMAT; resultIndex = TEST_RESULT_INDEX; feedbackReason = TEST_FEEDBACK_REASON; feedbackText = TEST_FEEDBACK_TEXT; selectedItemName = "A"; responseUuid = TEST_RESPONSE_UUID } to false,
                SearchFeedbackEvent().apply { event = SEARCH_FEEDBACK_EVENT_NAME; queryString = "a"; sessionIdentifier = null; created = TEST_TIME_IN_CORRECT_FORMAT; resultIndex = TEST_RESULT_INDEX; feedbackReason = TEST_FEEDBACK_REASON; feedbackText = TEST_FEEDBACK_TEXT; selectedItemName = "A"; responseUuid = TEST_RESPONSE_UUID } to false,
                SearchFeedbackEvent().apply { event = SEARCH_FEEDBACK_EVENT_NAME; queryString = "a"; sessionIdentifier = TEST_SESSION_IDENTIFIER; created = null; resultIndex = TEST_RESULT_INDEX; feedbackReason = TEST_FEEDBACK_REASON; feedbackText = TEST_FEEDBACK_TEXT; selectedItemName = "A"; responseUuid = TEST_RESPONSE_UUID } to false,
                SearchFeedbackEvent().apply { event = SEARCH_FEEDBACK_EVENT_NAME; queryString = "a"; sessionIdentifier = TEST_SESSION_IDENTIFIER; created = TEST_TIME_IN_CORRECT_FORMAT; resultIndex = null; feedbackReason = TEST_FEEDBACK_REASON; feedbackText = TEST_FEEDBACK_TEXT; selectedItemName = "A"; responseUuid = TEST_RESPONSE_UUID } to false,
                SearchFeedbackEvent().apply { event = SEARCH_FEEDBACK_EVENT_NAME; queryString = "a"; sessionIdentifier = TEST_SESSION_IDENTIFIER; created = TEST_TIME_IN_CORRECT_FORMAT; resultIndex = TEST_RESULT_INDEX; feedbackReason = null; feedbackText = TEST_FEEDBACK_TEXT; selectedItemName = "A"; responseUuid = TEST_RESPONSE_UUID } to false,
                SearchFeedbackEvent().apply { event = SEARCH_FEEDBACK_EVENT_NAME; queryString = "a"; sessionIdentifier = TEST_SESSION_IDENTIFIER; created = TEST_TIME_IN_CORRECT_FORMAT; resultIndex = TEST_RESULT_INDEX; feedbackReason = ""; feedbackText = TEST_FEEDBACK_TEXT; selectedItemName = "A"; responseUuid = TEST_RESPONSE_UUID } to false,
                SearchFeedbackEvent().apply { event = SEARCH_FEEDBACK_EVENT_NAME; queryString = "a"; sessionIdentifier = TEST_SESSION_IDENTIFIER; created = TEST_TIME_IN_CORRECT_FORMAT; resultIndex = TEST_RESULT_INDEX; feedbackReason = null; feedbackText = null; selectedItemName = "A"; responseUuid = TEST_RESPONSE_UUID } to false,
                SearchFeedbackEvent().apply { event = SEARCH_FEEDBACK_EVENT_NAME; queryString = "a"; sessionIdentifier = TEST_SESSION_IDENTIFIER; created = TEST_TIME_IN_CORRECT_FORMAT; resultIndex = TEST_RESULT_INDEX; feedbackReason = ""; feedbackText = null; selectedItemName = "A"; responseUuid = TEST_RESPONSE_UUID } to false,
                SearchFeedbackEvent().apply { event = SEARCH_FEEDBACK_EVENT_NAME; queryString = "a"; sessionIdentifier = TEST_SESSION_IDENTIFIER; created = TEST_TIME_IN_CORRECT_FORMAT; resultIndex = TEST_RESULT_INDEX; feedbackReason = TEST_FEEDBACK_REASON; feedbackText = TEST_FEEDBACK_TEXT; selectedItemName = null; responseUuid = TEST_RESPONSE_UUID } to false,
                SearchFeedbackEvent().apply { event = SEARCH_FEEDBACK_EVENT_NAME; queryString = "a"; sessionIdentifier = TEST_SESSION_IDENTIFIER; created = TEST_TIME_IN_CORRECT_FORMAT; resultIndex = TEST_RESULT_INDEX; feedbackReason = TEST_FEEDBACK_REASON; feedbackText = TEST_FEEDBACK_TEXT; selectedItemName = "A"; responseUuid = null } to true,
                SearchFeedbackEvent().apply { event = SEARCH_FEEDBACK_EVENT_NAME; queryString = "a"; sessionIdentifier = TEST_SESSION_IDENTIFIER; created = TEST_TIME_IN_CORRECT_FORMAT; resultIndex = TEST_RESULT_INDEX; feedbackReason = TEST_FEEDBACK_REASON; feedbackText = TEST_FEEDBACK_TEXT; selectedItemName = "A"; responseUuid = "" } to true,
                SearchFeedbackEvent().apply { event = SEARCH_FEEDBACK_EVENT_NAME; queryString = "a"; sessionIdentifier = TEST_SESSION_IDENTIFIER; created = TEST_TIME_IN_CORRECT_FORMAT; resultIndex = TEST_RESULT_INDEX; feedbackReason = TEST_FEEDBACK_REASON; feedbackText = null; selectedItemName = "A"; responseUuid = TEST_RESPONSE_UUID } to true,
                SearchFeedbackEvent().apply { event = SEARCH_FEEDBACK_EVENT_NAME; queryString = "a"; sessionIdentifier = TEST_SESSION_IDENTIFIER; created = TEST_TIME_IN_CORRECT_FORMAT; resultIndex = TEST_RESULT_INDEX; feedbackReason = TEST_FEEDBACK_REASON; feedbackText = TEST_FEEDBACK_TEXT; selectedItemName = "A"; responseUuid = TEST_RESPONSE_UUID } to true,
            )

        val TEST_APP_METADATAS: Map<AppMetadata, Boolean>
            get() = mapOf(
                AppMetadata(name = null, version = null, userId = null, sessionId = null) to true,
                AppMetadata(name = null) to true,
                AppMetadata(name = "") to false,
                AppMetadata(name = "name") to true,
                AppMetadata(version = null) to true,
                AppMetadata(version = "") to false,
                AppMetadata(version = "v2.1") to true,
                AppMetadata(sessionId = "test-session-id") to true,
            )
    }
}
