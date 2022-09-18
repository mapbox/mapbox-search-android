package com.mapbox.search.sample.api;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.Point;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.ReverseGeoOptions;
import com.mapbox.search.SearchCallback;
import com.mapbox.search.SearchEngine;
import com.mapbox.search.SearchEngineSettings;
import com.mapbox.search.common.AsyncOperationTask;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.sample.BuildConfig;

import java.util.List;

public class ReverseGeocodingJavaExampleActivity extends AppCompatActivity {

    private AsyncOperationTask searchRequestTask;

    private final SearchCallback searchCallback = new SearchCallback() {

        @Override
        public void onResults(@NonNull List<? extends SearchResult> results, @NonNull ResponseInfo responseInfo) {
            if (results.isEmpty()) {
                Log.i("SearchApiExample", "No reverse geocoding results");
            } else {
                Log.i("SearchApiExample", "Reverse geocoding results: " + results);
            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("SearchApiExample", "Reverse geocoding error", e);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SearchEngine searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            new SearchEngineSettings(BuildConfig.MAPBOX_API_TOKEN)
        );

        final ReverseGeoOptions options = new ReverseGeoOptions.Builder(Point.fromLngLat(2.294434, 48.858349))
            .limit(1)
            .build();

        searchRequestTask = searchEngine.search(options, searchCallback);
    }

    @Override
    public void onDestroy() {
        searchRequestTask.cancel();
        super.onDestroy();
    }
}
