package com.mapbox.search.base.tests_support

import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.core.CoreSearchResponse
import com.mapbox.search.common.CommonSdkTypeObjectCreators
import com.mapbox.search.common.tests.CustomTypeObjectCreator
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import java.io.IOException
import java.net.URI

internal object SdkCustomTypeObjectCreators {

    internal val CORE_SEARCH_RESPONSE_CREATOR = CustomTypeObjectCreatorImpl(CoreSearchResponse::class) { mode ->
        listOf(
            createTestCoreSearchResponseSuccess(responseUUID = "test-response-uuid-1"),
            createTestCoreSearchResponseSuccess(responseUUID = "test-response-uuid-2"),
        )[mode.ordinal]
    }

    internal val BASE_REQUEST_OPTIONS_CREATOR = CustomTypeObjectCreatorImpl(BaseRequestOptions::class) { mode ->
        listOf(
            createTestBaseRequestOptions(createTestCoreRequestOptions(sessionID = "session-id-1")),
            createTestBaseRequestOptions(createTestCoreRequestOptions(sessionID = "session-id-2")),
        )[mode.ordinal]
    }

    internal val URI_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(URI::class) { mode ->
        listOf(
            URI.create("https://api.mapbox.com"),
            URI.create("https://api-offline-search-staging.tilestream.net")
        )[mode.ordinal]
    }

    internal val EXCEPTION_CREATOR = CustomTypeObjectCreatorImpl(Exception::class) { mode ->
        listOf(Exception(), IOException())[mode.ordinal]
    }

    internal val ALL_CREATORS = listOf<CustomTypeObjectCreator>(
        CORE_SEARCH_RESPONSE_CREATOR,
        BASE_REQUEST_OPTIONS_CREATOR,
        URI_OBJECT_CREATOR,
        EXCEPTION_CREATOR,
    ) + CommonSdkTypeObjectCreators.ALL_CREATORS
}
