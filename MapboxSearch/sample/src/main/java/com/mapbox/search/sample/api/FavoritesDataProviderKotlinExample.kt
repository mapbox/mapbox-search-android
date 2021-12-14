package com.mapbox.search.sample.api

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Point
import com.mapbox.search.AsyncOperationTask
import com.mapbox.search.CompletionCallback
import com.mapbox.search.MapboxSearchSdk.serviceProvider
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.LocalDataProvider.OnDataChangedListener
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResultType
import java.util.UUID

class FavoritesDataProviderKotlinExample : AppCompatActivity() {

    private val favoritesDataProvider = serviceProvider.favoritesDataProvider()

    private lateinit var task: AsyncOperationTask

    private val retrieveFavoritesCallback: CompletionCallback<List<FavoriteRecord>> =
        object : CompletionCallback<List<FavoriteRecord>> {
            override fun onComplete(result: List<FavoriteRecord>) {
                Log.i("SearchApiExample", "Favorite records: $result")
            }

            override fun onError(e: Exception) {
                Log.i("SearchApiExample", "Unable to retrieve favorite records", e)
            }
        }

    private val addFavoriteCallback: CompletionCallback<Unit> = object : CompletionCallback<Unit> {
        override fun onComplete(result: Unit) {
            Log.i("SearchApiExample", "Favorite record added")
            task = favoritesDataProvider.getAll(retrieveFavoritesCallback)
        }

        override fun onError(e: Exception) {
            Log.i("SearchApiExample", "Unable to add a new favorite record", e)
        }
    }

    private val onDataChangedListener: OnDataChangedListener<FavoriteRecord> =
        object : OnDataChangedListener<FavoriteRecord> {
            override fun onDataChanged(newData: List<FavoriteRecord>) {
                Log.i("SearchApiExample", "Favorites data changed. New data: $newData")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        favoritesDataProvider.addOnDataChangedListener(onDataChangedListener)

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

        task = favoritesDataProvider.add(newFavorite, addFavoriteCallback)
    }

    override fun onDestroy() {
        favoritesDataProvider.removeOnDataChangedListener(onDataChangedListener)
        task.cancel()
        super.onDestroy()
    }
}
