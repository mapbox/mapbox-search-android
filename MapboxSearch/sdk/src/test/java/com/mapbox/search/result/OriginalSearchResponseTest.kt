package com.mapbox.search.result

import com.mapbox.search.tests_support.createTestCoreSearchResponseCancelled
import com.mapbox.search.tests_support.createTestCoreSearchResponseConnectionError
import com.mapbox.search.tests_support.createTestCoreSearchResponseHttpError
import com.mapbox.search.tests_support.createTestCoreSearchResponseInternalError
import com.mapbox.search.tests_support.createTestCoreSearchResponseSuccess
import com.mapbox.search.tests_support.createTestCoreSearchResult
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class OriginalSearchResponseTest {

    @TestFactory
    fun `Check mapping CoreSearchResponse to platform`() = TestCase {
        Given("CoreSearchResponse extension") {
            When("Convert successful search response to platform type") {
                val searchResult = createTestCoreSearchResult()

                val coreResponse = createTestCoreSearchResponseSuccess(
                    results = listOf(searchResult), responseUUID = TEST_RESPONSE_UUID
                )

                val platformResponse = OriginalSearchResponse(
                    result = OriginalSearchResponse.Result.Success(listOf(searchResult.mapToPlatform())),
                    responseUUID = TEST_RESPONSE_UUID
                )

                Then("Mapped response should be correct", platformResponse, coreResponse.mapToPlatform())
            }

            When("Convert connection error search response to platform type") {
                val errorMessage = "Connection error happened"

                val coreResponse = createTestCoreSearchResponseConnectionError(
                    message = errorMessage,
                    responseUUID = TEST_RESPONSE_UUID
                )

                val platformResponse = OriginalSearchResponse(
                    result = OriginalSearchResponse.Result.Error.ConnectionError(errorMessage),
                    responseUUID = TEST_RESPONSE_UUID
                )

                Then("Mapped response should be correct", platformResponse, coreResponse.mapToPlatform())
            }

            When("Convert internal error search response to platform type") {
                val errorMessage = "Internal error happened"

                val coreResponse = createTestCoreSearchResponseInternalError(
                    message = errorMessage,
                    responseUUID = TEST_RESPONSE_UUID
                )

                val platformResponse = OriginalSearchResponse(
                    result = OriginalSearchResponse.Result.Error.InternalError(errorMessage),
                    responseUUID = TEST_RESPONSE_UUID
                )

                Then("Mapped response should be correct", platformResponse, coreResponse.mapToPlatform())
            }

            When("Convert http error search response to platform type") {
                val httpCode = 400
                val errorMessage = "Http error happened"

                val coreResponse = createTestCoreSearchResponseHttpError(
                    httpCode = httpCode,
                    message = errorMessage,
                    responseUUID = TEST_RESPONSE_UUID
                )

                val platformResponse = OriginalSearchResponse(
                    result = OriginalSearchResponse.Result.Error.HttpError(httpCode, errorMessage),
                    responseUUID = TEST_RESPONSE_UUID
                )

                Then("Mapped response should be correct", platformResponse, coreResponse.mapToPlatform())
            }

            When("Convert cancelled search response to platform type") {
                val errorMessage = "Request cancelled"

                val coreResponse = createTestCoreSearchResponseCancelled(
                    reason = errorMessage,
                    responseUUID = TEST_RESPONSE_UUID
                )

                val platformResponse = OriginalSearchResponse(
                    result = OriginalSearchResponse.Result.Error.RequestCancelled(errorMessage),
                    responseUUID = TEST_RESPONSE_UUID
                )

                Then("Mapped response should be correct", platformResponse, coreResponse.mapToPlatform())
            }
        }
    }

    private companion object {
        const val TEST_RESPONSE_UUID = "test-response-uuid"
    }
}
