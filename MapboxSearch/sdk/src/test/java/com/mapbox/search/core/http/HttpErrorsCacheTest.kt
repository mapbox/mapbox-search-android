package com.mapbox.search.core.http

import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class HttpErrorsCacheTest {

    @TestFactory
    fun `Test HttpErrorsCache`() = TestCase {
        Given("HttpErrorsCache") {
            val httpErrorsCache = HttpErrorsCacheImpl()

            When("Save error to cache and retrieve error with existing request id") {
                val requestId = 0
                val exception = Exception()
                httpErrorsCache.put(requestId, exception)
                Then("Saved error should be returned", exception, httpErrorsCache.getAndRemove(requestId))
            }

            When("Save error to cache, retrieve error with existing request id and try to retrieve it again") {
                val requestId = 0
                val exception = Exception()
                httpErrorsCache.put(requestId, exception)
                Then("First retrieve should return correct saved error", exception, httpErrorsCache.getAndRemove(requestId))
                Then("Next retrieves should return null", null, httpErrorsCache.getAndRemove(requestId))
            }

            When("Save error to cache and retrieve error with unknown request id") {
                val requestId = 0
                httpErrorsCache.put(requestId, Exception())
                Then("Returned error should be null", null, httpErrorsCache.getAndRemove(requestId + 1))
            }
        }
    }
}
