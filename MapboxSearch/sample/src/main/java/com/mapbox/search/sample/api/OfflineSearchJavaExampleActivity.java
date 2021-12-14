package com.mapbox.search.sample.api;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.common.Cancelable;
import com.mapbox.common.TileRegionLoadOptions;
import com.mapbox.common.TileStore;
import com.mapbox.common.TilesetDescriptor;
import com.mapbox.geojson.Point;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.OfflineSearchEngine;
import com.mapbox.search.OfflineSearchEngine.EngineReadyCallback;
import com.mapbox.search.OfflineSearchOptions;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchRequestTask;
import com.mapbox.search.SearchSelectionCallback;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.result.SearchSuggestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OfflineSearchJavaExampleActivity extends AppCompatActivity {

    private OfflineSearchEngine searchEngine;
    private Cancelable tilesLoadingTask;
    @Nullable
    private SearchRequestTask searchRequestTask;

    private final EngineReadyCallback engineReadyCallback = new EngineReadyCallback() {
        @Override
        public void onEngineReady() {
            Log.i("SearchApiExample", "Engine is ready");
        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("SearchApiExample", "Error during engine initialization", e);
        }
    };

    private final SearchSelectionCallback searchCallback = new SearchSelectionCallback() {

        @Override
        public void onSuggestions(@NonNull List<? extends SearchSuggestion> suggestions, @NonNull ResponseInfo responseInfo) {
            if (suggestions.isEmpty()) {
                Log.i("SearchApiExample", "No suggestions found");
            } else {
                Log.i("SearchApiExample", "Search suggestions: " + suggestions + "\nSelecting first...");
                searchRequestTask = searchEngine.select(suggestions.get(0), this);
            }
        }

        @Override
        public void onResult(@NonNull SearchSuggestion suggestion, @NonNull SearchResult result, @NonNull ResponseInfo info) {
            Log.i("SearchApiExample", "Search result: " + result);
        }

        @Override
        public void onCategoryResult(@NonNull SearchSuggestion suggestion, @NonNull List<? extends SearchResult> results, @NonNull ResponseInfo responseInfo) {
            Log.i("SearchApiExample", "Category search results: " + results);
        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("SearchApiExample", "Search error: ", e);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchEngine = MapboxSearchSdk.getOfflineSearchEngine();
        searchEngine.addEngineReadyCallback(engineReadyCallback);

        final TileStore tileStore = searchEngine.getTileStore();

        final Point dcLocation = Point.fromLngLat(-77.0339911055176, 38.899920004207516);

        final List<TilesetDescriptor> descriptors = Collections.singletonList(searchEngine.createTilesetDescriptor());

        final TileRegionLoadOptions tileRegionLoadOptions = new TileRegionLoadOptions.Builder()
            .descriptors(descriptors)
            .geometry(dcLocation)
            .acceptExpired(true)
            .build();

        Log.i("SearchApiExample", "Loading tiles...");

        tilesLoadingTask = tileStore.loadTileRegion(
            "Washington DC",
            tileRegionLoadOptions,
            progress -> Log.i("SearchApiExample", "Loading progress: " + progress),
            region -> {
                if (region.isValue()) {
                    Log.i("SearchApiExample", "Tiles successfully loaded");

                    searchRequestTask = searchEngine.search(
                        "Cafe",
                        new OfflineSearchOptions(),
                        searchCallback
                    );
                } else {
                    Log.i("SearchApiExample", "Tiles loading error: " + region.getError());
                }
            }
        );
    }

    @Override
    protected void onDestroy() {
        searchEngine.removeEngineReadyCallback(engineReadyCallback);
        tilesLoadingTask.cancel();
        if (searchRequestTask != null) {
            searchRequestTask.cancel();
        }
        super.onDestroy();
    }
}
