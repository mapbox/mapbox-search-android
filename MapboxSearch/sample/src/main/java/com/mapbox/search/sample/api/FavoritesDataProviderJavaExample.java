package com.mapbox.search.sample.api;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.Point;
import com.mapbox.search.AsyncOperationTask;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.record.FavoriteRecord;
import com.mapbox.search.record.FavoritesDataProvider;
import com.mapbox.search.CompletionCallback;
import com.mapbox.search.record.LocalDataProvider.OnDataChangedListener;
import com.mapbox.search.result.SearchAddress;
import com.mapbox.search.result.SearchResultType;

import java.util.List;
import java.util.UUID;

import kotlin.Unit;

public class FavoritesDataProviderJavaExample extends AppCompatActivity {

    private final FavoritesDataProvider favoritesDataProvider = MapboxSearchSdk.getServiceProvider().favoritesDataProvider();

    private AsyncOperationTask task = null;

    private final CompletionCallback<List<FavoriteRecord>> retrieveFavoritesCallback = new CompletionCallback<List<FavoriteRecord>>() {
        @Override
        public void onComplete(List<FavoriteRecord> result) {
            Log.i("SearchApiExample", "Favorite records: " + result);
        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("SearchApiExample", "Unable to retrieve favorite records", e);
        }
    };

    private final CompletionCallback<Unit> addFavoriteCallback = new CompletionCallback<Unit>() {
        @Override
        public void onComplete(Unit result) {
            Log.i("SearchApiExample", "Favorite record added");
            task = favoritesDataProvider.getAll(retrieveFavoritesCallback);
        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("SearchApiExample", "Unable to add a new favorite record", e);
        }
    };

    private final OnDataChangedListener<FavoriteRecord> onDataChangedListener = newData -> {
        Log.i("SearchApiExample", "Favorites data changed. New data: " + newData);
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        favoritesDataProvider.addOnDataChangedListener(onDataChangedListener);

        final FavoriteRecord newFavorite = new FavoriteRecord(
            UUID.randomUUID().toString(),
            "Paris Eiffel Tower",
            "Eiffel Tower, Paris, France",
            new SearchAddress(null, null, null, null, null, "Paris", null, null, "France"),
            null,
            null,
            null,
            Point.fromLngLat(2.294434, 48.858349),
            SearchResultType.PLACE,
            null
        );

        task = favoritesDataProvider.add(newFavorite, addFavoriteCallback);
    }

    @Override
    protected void onDestroy() {
        favoritesDataProvider.removeOnDataChangedListener(onDataChangedListener);
        task.cancel();
        super.onDestroy();
    }
}
