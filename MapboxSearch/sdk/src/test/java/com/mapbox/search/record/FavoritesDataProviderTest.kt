package com.mapbox.search.record

import com.mapbox.search.common.tests.TestThreadExecutorService
import com.mapbox.test.dsl.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import java.util.concurrent.ExecutorService

internal class FavoritesDataProviderTest {

    private lateinit var dataProviderEngine: IndexableDataProviderEngine
    private lateinit var recordsStorage: RecordsFileStorage<FavoriteRecord>
    private lateinit var executorService: ExecutorService

    private lateinit var favoritesDataProvider: FavoritesDataProviderImpl

    @BeforeEach
    fun setUp() {
        dataProviderEngine = mockk(relaxed = true)

        recordsStorage = mockk(relaxed = true)
        every { recordsStorage.load() } returns emptyList()

        executorService = mockk<TestThreadExecutorService>(relaxed = true)

        favoritesDataProvider = FavoritesDataProviderImpl(recordsStorage, executorService)
    }

    @TestFactory
    fun `Check favorite provider name`() = TestCase {
        Given("FavoritesDataProvider with mocked dependencies") {
            When("get dataProviderName") {
                Then(
                    "Provider name should be ${FavoritesDataProvider.PROVIDER_NAME}",
                    FavoritesDataProvider.PROVIDER_NAME,
                    favoritesDataProvider.dataProviderName
                )
            }
        }
    }
}
