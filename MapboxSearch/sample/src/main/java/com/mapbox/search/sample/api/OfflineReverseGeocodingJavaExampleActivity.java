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
import com.mapbox.search.common.AsyncOperationTask;
import com.mapbox.search.offline.OfflineResponseInfo;
import com.mapbox.search.offline.OfflineReverseGeoOptions;
import com.mapbox.search.offline.OfflineSearchCallback;
import com.mapbox.search.offline.OfflineSearchEngine;
import com.mapbox.search.offline.OfflineSearchEngineSettings;
import com.mapbox.search.offline.OfflineSearchResult;

import java.util.Collections;
import java.util.List;

public class OfflineReverseGeocodingJavaExampleActivity extends AppCompatActivity {

    private static final String TILE_REGION_ID = "Washington DC";
    private static final Point DC_LOCATION = Point.fromLngLat(-77.0339911055176, 38.899920004207516);

    private OfflineSearchEngine searchEngine;
    private Cancelable tilesLoadingTask;
    @Nullable
    private AsyncOperationTask searchRequestTask;

    private final OfflineSearchEngine.EngineReadyCallback engineReadyCallback =
        () -> Log.i("SearchApiExample", "Engine is ready");

    private final OfflineSearchCallback searchCallback = new OfflineSearchCallback() {

        @Override
        public void onResults(@NonNull List<OfflineSearchResult> results, @NonNull OfflineResponseInfo responseInfo) {
            Log.i("SearchApiExample", "Results: " + results);
        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("SearchApiExample", "Search error: ", e);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TileStore tileStore = TileStore.create();

        searchEngine = OfflineSearchEngine.create(
            new OfflineSearchEngineSettings(tileStore)
        );

        searchEngine.addEngineReadyCallback(engineReadyCallback);

        final List<TilesetDescriptor> descriptors = Collections.singletonList(OfflineSearchEngine.createTilesetDescriptor());

        final TileRegionLoadOptions tileRegionLoadOptions = new TileRegionLoadOptions.Builder()
            .descriptors(descriptors)
            .geometry(DC_LOCATION)
            .acceptExpired(true)
            .build();

        Log.i("SearchApiExample", "Loading tiles...");

        tilesLoadingTask = tileStore.loadTileRegion(
          TILE_REGION_ID,
            tileRegionLoadOptions,
            progress -> Log.i("SearchApiExample", "Loading progress: " + progress),
            region -> {
                if (region.isValue()) {
                    Log.i("SearchApiExample", "Tiles successfully loaded");
                    doSearch();
                } else {
                    Log.i("SearchApiExample", "Tiles loading error: " + region.getError());
                }
            }
        );
    }

    private void doSearch() {
        searchRequestTask = searchEngine.reverseGeocoding(
            new OfflineReverseGeoOptions(DC_LOCATION),
             searchCallback
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
