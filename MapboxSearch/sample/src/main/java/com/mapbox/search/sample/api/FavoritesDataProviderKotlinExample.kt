package com.mapbox.search.sample.api

import android.os.Bundle
import com.mapbox.geojson.Point
import com.mapbox.search.ServiceProvider
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.LocalDataProvider.OnDataChangedListener
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.sample.R
import java.util.UUID

class FavoritesDataProviderKotlinExample : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_open_favorites_data_provider_kt_example

    private val favoritesDataProvider = ServiceProvider.INSTANCE.favoritesDataProvider()

    private var task: AsyncOperationTask? = null

    private val retrieveFavoritesCallback: CompletionCallback<List<FavoriteRecord>> =
        object : CompletionCallback<List<FavoriteRecord>> {
            override fun onComplete(result: List<FavoriteRecord>) {
                logI("SearchApiExample", "Favorite records:", result)
                onFinished()
            }

            override fun onError(e: Exception) {
                logI("SearchApiExample", "Unable to retrieve favorite records", e)
                onFinished()
            }
        }

    private val addFavoriteCallback: CompletionCallback<Unit> = object : CompletionCallback<Unit> {
        override fun onComplete(result: Unit) {
            logI("SearchApiExample", "Favorite record added")
            task = favoritesDataProvider.getAll(retrieveFavoritesCallback)
        }

        override fun onError(e: Exception) {
            logI("SearchApiExample", "Unable to add a new favorite record", e)
        }
    }

    private val onDataChangedListener: OnDataChangedListener<FavoriteRecord> =
        object : OnDataChangedListener<FavoriteRecord> {
            override fun onDataChanged(newData: List<FavoriteRecord>) {
                logI("SearchApiExample", "Favorites data changed. New data:", newData)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        favoritesDataProvider.addOnDataChangedListener(onDataChangedListener)
    }

    override fun startExample() {
        val newFavorite = FavoriteRecord(
            id = UUID.randomUUID().toString(),
            name = "Paris Eiffel Tower",
            descriptionText = "Eiffel Tower, Paris, France",
            address = SearchAddress(place = "Paris", country = "France"),
            routablePoints = null,
            categories = null,
            makiIcon = null,
            coordinate = Point.fromLngLat(2.294434, 48.858349),
            type = SearchResultType.PLACE,
            metadata = null
        )

        task = favoritesDataProvider.upsert(newFavorite, addFavoriteCallback)
    }

    override fun onDestroy() {
        favoritesDataProvider.removeOnDataChangedListener(onDataChangedListener)
        task?.cancel()
        super.onDestroy()
    }
}
