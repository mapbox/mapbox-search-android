package com.mapbox.search

import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.SearchRequestContextProvider
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreRequestOptions
import com.mapbox.search.base.core.CoreResultType
import com.mapbox.search.base.core.CoreSearchCallback
import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.base.core.CoreSearchOptions
import com.mapbox.search.base.core.CoreSearchResult
import com.mapbox.search.base.logger.reinitializeLogImpl
import com.mapbox.search.base.logger.resetLogImpl
import com.mapbox.search.base.record.BaseIndexableRecord
import com.mapbox.search.base.record.IndexableRecordResolver
import com.mapbox.search.base.record.SearchHistoryService
import com.mapbox.search.base.result.BaseGeocodingCompatSearchSuggestion
import com.mapbox.search.base.result.BaseIndexableRecordSearchResultImpl
import com.mapbox.search.base.result.BaseIndexableRecordSearchSuggestion
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.BaseSearchResultType
import com.mapbox.search.base.result.BaseSearchSuggestion
import com.mapbox.search.base.result.BaseServerSearchResultImpl
import com.mapbox.search.base.result.BaseServerSearchSuggestion
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.base.result.SearchResultFactory
import com.mapbox.search.base.result.mapToBase
import com.mapbox.search.base.task.AsyncOperationTaskImpl
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.SearchCancellationException
import com.mapbox.search.common.SearchRequestException
import com.mapbox.search.common.TestConstants.ASSERTIONS_KT_CLASS_NAME
import com.mapbox.search.common.TestExecutor
import com.mapbox.search.common.TestThreadExecutorService
import com.mapbox.search.common.createTestCoreSearchResponseCancelled
import com.mapbox.search.common.createTestCoreSearchResponseHttpError
import com.mapbox.search.common.createTestCoreSearchResponseSuccess
import com.mapbox.search.common.createTestCoreSearchResult
import com.mapbox.search.common.createTestCoreSuggestAction
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.FavoritesDataProvider
import com.mapbox.search.record.mapToBase
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.mapToCore
import com.mapbox.search.result.mapToPlatform
import com.mapbox.search.tests_support.catchThrowable
import com.mapbox.search.tests_support.createTestRequestOptions
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import java.util.concurrent.Executor

/**
 * Contains only forward-geocoding related functionality tests.
 * See [CategorySearchTest], [ReverseGeocodingSearchTest] for more tests.
 */
@Suppress("LargeClass")
internal class SearchEngineTest {

    private lateinit var coreEngine: CoreSearchEngineInterface
    private lateinit var indexableRecordResolver: IndexableRecordResolver
    private lateinit var historyService: SearchHistoryService
    private lateinit var searchResultFactory: SearchResultFactory
    private lateinit var executor: Executor
    private lateinit var requestContextProvider: SearchRequestContextProvider
    private lateinit var indexableDataProvidersRegistry: IndexableDataProvidersRegistry

    private lateinit var searchEngine: SearchEngine

    @BeforeEach
    fun setUp() {
        coreEngine = mockk(relaxed = true)
        historyService = mockk(relaxed = true)

        indexableRecordResolver = mockk()

        val slotResolverCallback = slot<(Result<BaseIndexableRecord>) -> Unit>()
        every { indexableRecordResolver.resolve(any(), any(), any(), capture(slotResolverCallback)) } answers {
            slotResolverCallback.captured(Result.failure(Exception()))
            AsyncOperationTaskImpl.COMPLETED
        }

        searchResultFactory = spyk(SearchResultFactory(indexableRecordResolver))

        val slotSuggestionCallback = slot<(Result<BaseSearchSuggestion>) -> Unit>()
        every { searchResultFactory.createSearchSuggestionAsync(any(), any(), CoreApiType.SBS, any(), capture(slotSuggestionCallback)) } answers {
            slotSuggestionCallback.captured(Result.success(TEST_GEOCODING_SEARCH_SUGGESTION))
            AsyncOperationTaskImpl.COMPLETED
        }

        executor = spyk(TestExecutor())

        requestContextProvider = mockk()
        every { requestContextProvider.provide(CoreApiType.SBS) } returns TEST_SEARCH_REQUEST_CONTEXT

        indexableDataProvidersRegistry = mockk()

        searchEngine = SearchEngineImpl(
            apiType = ApiType.SBS,
            settings = mockk(),
            analyticsService = mockk(relaxed = true),
            coreEngine = coreEngine,
            historyService = historyService,
            requestContextProvider = requestContextProvider,
            searchResultFactory = searchResultFactory,
            engineExecutorService = TestThreadExecutorService(),
            indexableDataProvidersRegistry = indexableDataProvidersRegistry,
        )
    }

    @TestFactory
    fun `Check initial successful search call`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            val slotSearchCallback = slot<CoreSearchCallback>()

            every { coreEngine.search(any(), any(), any(), capture(slotSearchCallback)) } answers {
                slotSearchCallback.captured.run(TEST_SUCCESSFUL_CORE_RESPONSE)
            }

            When("Initial search called") {
                val callback = mockk<SearchSuggestionsCallback>(relaxed = true)

                val task = searchEngine.search(
                    query = TEST_QUERY,
                    options = TEST_SEARCH_OPTIONS,
                    executor = executor,
                    callback = callback
                )

                Then("SearchRequestTask is executed", true, task.isDone)

                Verify("Callbacks called inside executor") {
                    executor.execute(any())
                }

                Verify("CoreSearchEngine.search() called") {
                    coreEngine.search(
                        eq(TEST_QUERY),
                        eq(emptyList()),
                        eq(TEST_SEARCH_OPTIONS.mapToCore()),
                        slotSearchCallback.captured
                    )
                }

                Verify("Results passed to callback") {
                    callback.onSuggestions(
                        listOf(TEST_GEOCODING_SEARCH_SUGGESTION.mapToPlatform()),
                        ResponseInfo(
                            TEST_REQUEST_OPTIONS.copy(requestContext = TEST_SEARCH_REQUEST_CONTEXT.copy(responseUuid = TEST_RESPONSE_UUID)),
                            TEST_SUCCESSFUL_CORE_RESPONSE.mapToBase(),
                            isReproducible = true,
                        )
                    )
                }
            }
        }
    }

    @TestFactory
    fun `Check initial error search call`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            val slotSearchCallback = slot<CoreSearchCallback>()
            val slotSearchOptions = slot<CoreSearchOptions>()

            every { coreEngine.search(any(), any(), capture(slotSearchOptions), capture(slotSearchCallback)) } answers {
                slotSearchCallback.captured.run(TEST_ERROR_CORE_RESPONSE)
            }

            When("Initial search called") {
                val callback = mockk<SearchSuggestionsCallback>(relaxed = true)

                val task = searchEngine.search(
                    query = TEST_QUERY,
                    options = TEST_SEARCH_OPTIONS,
                    executor = executor,
                    callback = callback
                )

                Then("Task is executed", true, task.isDone)

                Verify("CoreSearchEngine.search() called") {
                    coreEngine.search(
                        eq(TEST_QUERY),
                        eq(emptyList()),
                        slotSearchOptions.captured,
                        slotSearchCallback.captured
                    )
                }

                Verify("Callbacks called inside executor") {
                    executor.execute(any())
                }

                Verify("Error passed to callback") {
                    callback.onError(
                        SearchRequestException(
                            message = TEST_ERROR_CORE_RESPONSE_MESSAGE,
                            code = TEST_ERROR_CORE_RESPONSE_HTTP_CODE,
                        )
                    )
                }
            }
        }
    }

    @TestFactory
    fun `Check initial internal error search call`() = TestCase {
        Given("SearchEngine with failing SearchResultFactory") {
            val exception = RuntimeException("Test error")
            every { searchResultFactory.createSearchSuggestionAsync(any(), any(), any(), any(), any()) } throws exception
            val slotSearchCallback = slot<CoreSearchCallback>()
            every {
                coreEngine.search(any(), any(), any(), capture(slotSearchCallback))
            } answers {
                slotSearchCallback.captured.run(TEST_SUCCESSFUL_CORE_RESPONSE)
            }

            When("Initial search called") {
                val callback = mockk<SearchSuggestionsCallback>(relaxed = true)
                val slotCallbackError = slot<Exception>()
                every { callback.onError(capture(slotCallbackError)) } returns Unit

                searchEngine.search(
                    query = TEST_QUERY,
                    options = TEST_SEARCH_OPTIONS,
                    executor = executor,
                    callback = callback
                )

                Then("Exception from SearchResultFactory should be forwarded to callback", slotCallbackError.captured, exception)
            }
        }
    }

    @TestFactory
    fun `Check search call cancellation`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            val cancellationReason = "Request cancelled"

            val slotSearchCallback = slot<CoreSearchCallback>()
            every { coreEngine.search(eq(TEST_QUERY), any(), any(), capture(slotSearchCallback)) } answers {
                slotSearchCallback.captured.run(createTestCoreSearchResponseCancelled(cancellationReason))
            }

            When("Search request cancelled by the Search SDK") {
                val callback = mockk<SearchSuggestionsCallback>(relaxed = true)

                val task = searchEngine.search(TEST_QUERY, TEST_SEARCH_OPTIONS, callback)

                Then("Task is cancelled", true, task.isCancelled)

                VerifyOnce("Callback called with cancellation error") {
                    callback.onError(eq(SearchCancellationException(cancellationReason)))
                }
            }
        }
    }

    @TestFactory
    fun `Check request marked as executed when internal error happens`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            val slotSearchCallback = slot<CoreSearchCallback>()

            every { coreEngine.search(any(), any(), any(), capture(slotSearchCallback)) } returns Unit

            val callback = mockk<SearchSuggestionsCallback>(relaxed = true)
            every { callback.onSuggestions(any(), any()) } throws IllegalStateException()

            var task: AsyncOperationTask? = null
            When("Search function called for the first time") {
                val throwable = catchThrowable<IllegalStateException> {
                    task = searchEngine.search(
                        query = TEST_QUERY,
                        options = TEST_SEARCH_OPTIONS,
                        executor = executor,
                        callback = callback
                    )

                    slotSearchCallback.captured.run(TEST_SUCCESSFUL_CORE_RESPONSE)
                }

                Then("Task failed with IllegalStateException", true, throwable != null)

                Then("Task is executed", true, task?.isDone)
            }
        }
    }

    @TestFactory
    fun `Check successful search selection with geocoding suggestion`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            val slotRequestOptions = slot<CoreRequestOptions>()
            val slotSearchResult = slot<CoreSearchResult>()
            every { coreEngine.onSelected(capture(slotRequestOptions), capture(slotSearchResult)) } returns Unit

            val historyServiceCompletionCallbackSlot = slot<(Result<Boolean>) -> Unit>()
            every { historyService.addToHistoryIfNeeded(any(), any(), capture(historyServiceCompletionCallbackSlot)) } answers {
                historyServiceCompletionCallbackSlot.captured(Result.success(true))
                AsyncOperationTaskImpl.COMPLETED
            }

            When("Suggestion selected") {
                val callback = mockk<SearchSelectionCallback>(relaxed = true)

                val task = searchEngine.select(
                    suggestion = TEST_GEOCODING_SEARCH_SUGGESTION.mapToPlatform(),
                    options = SelectOptions(),
                    executor = executor,
                    callback = callback
                )

                Then("Task is executed", true, task.isDone)

                Verify("Callbacks called inside executor") {
                    executor.execute(any())
                }

                Verify("CoreSearchEngine.onSelected() called") {
                    coreEngine.onSelected(slotRequestOptions.captured, slotSearchResult.captured)
                }

                Verify("Suggestion added to history") {
                    historyService.addToHistoryIfNeeded(
                        TEST_SEARCH_RESULT,
                        any(),
                        historyServiceCompletionCallbackSlot.captured
                    )
                }

                Verify("Results passed to callback") {
                    callback.onResult(
                        TEST_GEOCODING_SEARCH_SUGGESTION.mapToPlatform(),
                        TEST_SEARCH_RESULT.mapToPlatform(),
                        ResponseInfo(
                            TEST_REQUEST_OPTIONS,
                            null,
                            isReproducible = false,
                        )
                    )
                }
            }
        }
    }

    @TestFactory
    fun `Check successful search selection with SBS suggestion`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            val slotSelectRequestOptions = slot<CoreRequestOptions>()
            val slotSelectSearchResult = slot<CoreSearchResult>()
            every { coreEngine.onSelected(capture(slotSelectRequestOptions), capture(slotSelectSearchResult)) } returns Unit

            val slotRetrieveSearchCallback = slot<CoreSearchCallback>()
            every {
                coreEngine.retrieve(any(), any(), capture(slotRetrieveSearchCallback))
            } answers {
                slotRetrieveSearchCallback.captured.run(TEST_SUCCESSFUL_CORE_RESPONSE)
            }

            val historyServiceCompletionCallbackSlot = slot<(Result<Boolean>) -> Unit>()
            every { historyService.addToHistoryIfNeeded(any(), any(), capture(historyServiceCompletionCallbackSlot)) } answers {
                historyServiceCompletionCallbackSlot.captured(Result.success(true))
                AsyncOperationTaskImpl.COMPLETED
            }

            When("Suggestion selected") {
                val callback = mockk<SearchSelectionCallback>(relaxed = true)

                val task = searchEngine.select(
                    suggestion = TEST_SBS_SERVER_SEARCH_SUGGESTION.mapToPlatform(),
                    options = SelectOptions(),
                    executor = executor,
                    callback = callback,
                )

                Verify("CoreSearchEngine.retrieve() Called") {
                    coreEngine.retrieve(any(), any(), any())
                }

                Then("Task is executed", true, task.isDone)

                Verify("Callbacks called inside executor") {
                    executor.execute(any())
                }

                val expectedSearchRequestContext = requestContextProvider.provide(CoreApiType.SBS)
                    .copy(responseUuid = TEST_SUCCESSFUL_CORE_RESPONSE.responseUUID)

                val expectedRequestOptions = BaseRequestOptions(
                    TEST_SUCCESSFUL_CORE_RESPONSE.request,
                    expectedSearchRequestContext
                )

                val selectedResult = TEST_SUCCESSFUL_CORE_RESPONSE.results.value?.first()!!

                val expectedResult = searchResultFactory.createSearchResult(
                    selectedResult.mapToBase(),
                    expectedRequestOptions
                )!!

                Verify("CoreSearchEngine.onSelected() called") {
                    coreEngine.onSelected(TEST_SUCCESSFUL_CORE_RESPONSE.request, selectedResult)
                }

                Verify("Suggestion added to history") {
                    historyService.addToHistoryIfNeeded(
                        expectedResult,
                        any(),
                        historyServiceCompletionCallbackSlot.captured
                    )
                }

                Verify("Results passed to callback") {
                    callback.onResult(
                        TEST_SBS_SERVER_SEARCH_SUGGESTION.mapToPlatform(),
                        expectedResult.mapToPlatform(),
                        ResponseInfo(
                            TEST_REQUEST_OPTIONS.copy(
                                requestContext = TEST_SEARCH_REQUEST_CONTEXT.copy(responseUuid = TEST_RESPONSE_UUID)
                            ),
                            null,
                            isReproducible = false,
                        )
                    )
                }
            }
        }
    }

    @TestFactory
    fun `Check successful search selection with data layer suggestion`() = TestCase {
        Given("SearchEngine with mocked dependencies") {
            val slotRequestOptions = slot<CoreRequestOptions>()
            val slotSearchResult = slot<CoreSearchResult>()
            every { coreEngine.onSelected(capture(slotRequestOptions), capture(slotSearchResult)) } returns Unit

            val indexableRecordCallbackSlot = slot<(Result<BaseIndexableRecord>) -> Unit>()
            every { indexableRecordResolver.resolve(
                FavoritesDataProvider.PROVIDER_NAME,
                TEST_USER_LAYER_RECORD_ID,
                any(),
                capture(indexableRecordCallbackSlot))
            } answers {
                indexableRecordCallbackSlot.captured(Result.success(TEST_FAVORITE_RECORD.mapToBase()))
                AsyncOperationTaskImpl.COMPLETED
            }

            val historyServiceCompletionCallbackSlot = slot<(Result<Boolean>) -> Unit>()
            every { historyService.addToHistoryIfNeeded(any(), any(), capture(historyServiceCompletionCallbackSlot)) } answers {
                historyServiceCompletionCallbackSlot.captured(Result.success(true))
                AsyncOperationTaskImpl.COMPLETED
            }

            When("Suggestion selected") {
                val callback = mockk<SearchSelectionCallback>(relaxed = true)

                val task = searchEngine.select(
                    suggestion = TEST_USER_RECORD_SEARCH_SUGGESTION.mapToPlatform(),
                    options = SelectOptions(),
                    executor = executor,
                    callback = callback
                )

                Then("Task is executed", true, task.isDone)

                Verify("Callbacks called inside executor") {
                    executor.execute(any())
                }

                Verify("CoreSearchEngine.onSelected() called") {
                    coreEngine.onSelected(slotRequestOptions.captured, slotSearchResult.captured)
                }

                Verify("Suggestion added to history") {
                    historyService.addToHistoryIfNeeded(
                        TEST_FAVORITE_RECORD_SEARCH_RESULT,
                        any(),
                        historyServiceCompletionCallbackSlot.captured
                    )
                }

                Verify("Results passed to callback") {
                    callback.onResult(
                        TEST_USER_RECORD_SEARCH_SUGGESTION.mapToPlatform(),
                        TEST_FAVORITE_RECORD_SEARCH_RESULT.mapToPlatform(),
                        ResponseInfo(
                            TEST_REQUEST_OPTIONS.mapToBase().mapToPlatform(),
                            null,
                            isReproducible = false,
                        )
                    )
                }
            }
        }
    }

    @TestFactory
    fun `Check search selection with initial internal error`() = TestCase {
        Given("SearchEngine with failing SearchResultFactory") {
            val exception = RuntimeException("Test error")
            every { searchResultFactory.createSearchResult(any(), any()) } throws exception

            every { coreEngine.onSelected(any(), any()) } returns Unit

            val slotRetrieveSearchCallback = slot<CoreSearchCallback>()
            every {
                coreEngine.retrieve(any(), any(), capture(slotRetrieveSearchCallback))
            } answers {
                slotRetrieveSearchCallback.captured.run(TEST_SUCCESSFUL_CORE_RESPONSE)
            }

            When("Suggestion selected") {
                val callback = mockk<SearchSelectionCallback>(relaxed = true)
                val slotCallbackError = slot<Exception>()
                every { callback.onError(capture(slotCallbackError)) } returns Unit

                searchEngine.select(
                    suggestion = TEST_SBS_SERVER_SEARCH_SUGGESTION.mapToPlatform(),
                    options = SelectOptions(),
                    executor = executor,
                    callback = callback
                )

                Then("Exception from SearchResultFactory should be forwarded to callback", slotCallbackError.captured, exception)
            }
        }
    }

    private companion object {

        const val TEST_QUERY = "Minsk"
        val TEST_SEARCH_OPTIONS = SearchOptions()
        const val TEST_RESPONSE_UUID = "UUID test"
        const val TEST_RESPONSE_UUID_2 = "UUID test 2"
        const val TEST_DESCRIPTION_TEXT = "Test description text"
        val TEST_USER_LOCATION: Point = Point.fromLngLat(10.0, 11.0)
        val TEST_SEARCH_ADDRESS = SearchAddress(null, null, null, null, null, null, null, null, null)
        val TEST_SEARCH_REQUEST_CONTEXT = SearchRequestContext(CoreApiType.SBS, responseUuid = TEST_RESPONSE_UUID_2)

        val TEST_REQUEST_OPTIONS = createTestRequestOptions(
            query = TEST_QUERY,
            options = SearchOptions(proximity = TEST_USER_LOCATION),
            requestContext = TEST_SEARCH_REQUEST_CONTEXT
        )

        val TEST_CORE_SEARCH_RESULT = createTestCoreSearchResult(
            id = "test result id",
            types = listOf(CoreResultType.ADDRESS),
            names = listOf("Result name"),
            languages = listOf("Default"),
            addresses = listOf(TEST_SEARCH_ADDRESS.mapToCore()),
            distanceMeters = 123.0,
            center = Point.fromLngLat(20.0, 30.0),
            categories = emptyList(),
            descriptionAddress = "Test result description"
        )

        val TEST_CORE_SEARCH_SUGGESTION = createTestCoreSearchResult(
            id = "test result id",
            types = listOf(CoreResultType.ADDRESS),
            names = listOf("Result name"),
            languages = listOf("Default"),
            addresses = listOf(TEST_SEARCH_ADDRESS.mapToCore()),
            distanceMeters = 123.0,
            center = Point.fromLngLat(20.0, 30.0),
            categories = emptyList(),
            descriptionAddress = "Test result description",
        )

        val TEST_SUCCESSFUL_CORE_RESPONSE = createTestCoreSearchResponseSuccess(
            TEST_REQUEST_OPTIONS.mapToCore(),
            listOf(TEST_CORE_SEARCH_RESULT),
            TEST_RESPONSE_UUID
        )

        const val TEST_ERROR_CORE_RESPONSE_MESSAGE = "Auth failed"

        const val TEST_ERROR_CORE_RESPONSE_HTTP_CODE = 401

        val TEST_ERROR_CORE_RESPONSE = createTestCoreSearchResponseHttpError(
            TEST_ERROR_CORE_RESPONSE_HTTP_CODE,
            TEST_ERROR_CORE_RESPONSE_MESSAGE,
            TEST_REQUEST_OPTIONS.mapToCore(),
            TEST_RESPONSE_UUID
        )

        val TEST_SEARCH_RESULT = BaseServerSearchResultImpl(
            listOf(BaseSearchResultType.ADDRESS),
            TEST_CORE_SEARCH_RESULT.mapToBase(),
            TEST_REQUEST_OPTIONS.mapToBase()
        )

        val TEST_GEOCODING_SEARCH_SUGGESTION = BaseGeocodingCompatSearchSuggestion(
            TEST_CORE_SEARCH_SUGGESTION.mapToBase(),
            TEST_REQUEST_OPTIONS.mapToBase()
        )

        val BASE_TEST_REQUEST_OPTIONS = BaseRequestOptions(
            TEST_REQUEST_OPTIONS.mapToCore(),
            TEST_SEARCH_REQUEST_CONTEXT
        )

        val TEST_SBS_SERVER_SEARCH_SUGGESTION = BaseServerSearchSuggestion(
            TEST_CORE_SEARCH_SUGGESTION.mapToBase().copy(
                types = listOf(BaseRawResultType.POI),
                action = createTestCoreSuggestAction().mapToBase()
            ),
            BASE_TEST_REQUEST_OPTIONS
        )

        const val TEST_USER_LAYER_RECORD_ID = "test user layer record id"

        val TEST_USER_RECORD_SEARCH_RESULT = createTestCoreSearchResult(
            id = TEST_USER_LAYER_RECORD_ID,
            types = listOf(CoreResultType.ADDRESS),
            names = listOf("Result name"),
            languages = listOf("Default"),
            addresses = listOf(TEST_SEARCH_ADDRESS.mapToCore()),
            distanceMeters = 123.0,
            center = Point.fromLngLat(20.0, 30.0),
            categories = emptyList(),
            layerId = FavoritesDataProvider.PROVIDER_NAME,
            userRecordId = TEST_USER_LAYER_RECORD_ID
        )

        val TEST_FAVORITE_RECORD = FavoriteRecord(
            id = TEST_USER_LAYER_RECORD_ID,
            name = "Test favorite",
            coordinate = TEST_USER_LOCATION,
            descriptionText = TEST_DESCRIPTION_TEXT,
            address = TEST_SEARCH_ADDRESS,
            type = SearchResultType.ADDRESS,
            makiIcon = null,
            categories = emptyList(),
            routablePoints = null,
            metadata = null
        )

        val TEST_USER_RECORD_SEARCH_SUGGESTION = BaseIndexableRecordSearchSuggestion(
            TEST_FAVORITE_RECORD.mapToBase(),
            rawSearchResult = TEST_USER_RECORD_SEARCH_RESULT.mapToBase().copy(
                types = listOf(BaseRawResultType.USER_RECORD)
            ),
            requestOptions = BASE_TEST_REQUEST_OPTIONS
        )

        val TEST_FAVORITE_RECORD_SEARCH_RESULT = BaseIndexableRecordSearchResultImpl(
            record = TEST_FAVORITE_RECORD.mapToBase(),
            rawSearchResult = TEST_USER_RECORD_SEARCH_SUGGESTION.rawSearchResult,
            requestOptions = TEST_USER_RECORD_SEARCH_SUGGESTION.requestOptions
        )

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @BeforeAll
        @JvmStatic
        fun setUpAll() {
            resetLogImpl()
            mockkStatic(ASSERTIONS_KT_CLASS_NAME)
        }

        @Suppress("JVM_STATIC_IN_PRIVATE_COMPANION")
        @AfterAll
        @JvmStatic
        fun tearDownAll() {
            reinitializeLogImpl()
            unmockkStatic(ASSERTIONS_KT_CLASS_NAME)
        }
    }
}
