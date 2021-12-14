package com.mapbox.search.sample.api;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.Point;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.ReverseGeoOptions;
import com.mapbox.search.ReverseGeocodingSearchEngine;
import com.mapbox.search.SearchCallback;
import com.mapbox.search.SearchRequestTask;
import com.mapbox.search.result.SearchResult;

import java.util.List;

public class ReverseGeocodingJavaExampleActivity extends AppCompatActivity {

    private ReverseGeocodingSearchEngine reverseGeocoding;
    private SearchRequestTask searchRequestTask;

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

        reverseGeocoding = MapboxSearchSdk.getReverseGeocodingSearchEngine();

        final ReverseGeoOptions options = new ReverseGeoOptions.Builder(Point.fromLngLat(2.294434, 48.858349))
            .limit(1)
            .build();

        searchRequestTask = reverseGeocoding.search(options, searchCallback);
    }

    @Override
    public void onDestroy() {
        searchRequestTask.cancel();
        super.onDestroy();
    }
}
