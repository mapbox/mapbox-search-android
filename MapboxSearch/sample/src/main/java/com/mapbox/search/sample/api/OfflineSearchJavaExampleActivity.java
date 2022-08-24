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
import com.mapbox.search.offline.OfflineIndexChangeEvent;
import com.mapbox.search.offline.OfflineIndexChangeEvent.EventType;
import com.mapbox.search.offline.OfflineIndexErrorEvent;
import com.mapbox.search.offline.OfflineResponseInfo;
import com.mapbox.search.offline.OfflineSearchCallback;
import com.mapbox.search.offline.OfflineSearchEngine;
import com.mapbox.search.offline.OfflineSearchEngineSettings;
import com.mapbox.search.offline.OfflineSearchOptions;
import com.mapbox.search.offline.OfflineSearchResult;
import com.mapbox.search.sample.BuildConfig;

import java.util.Collections;
import java.util.List;

public class OfflineSearchJavaExampleActivity extends AppCompatActivity {

    private OfflineSearchEngine searchEngine;
    private Cancelable tilesLoadingTask;
    @Nullable
    private AsyncOperationTask searchRequestTask;

    private final OfflineSearchEngine.EngineReadyCallback engineReadyCallback =
        () -> Log.i("SearchApiExample", "Engine is ready");

    private final OfflineSearchCallback searchCallback = new OfflineSearchCallback() {

        @Override
        public void onResults(@NonNull List<OfflineSearchResult> results, @NonNull OfflineResponseInfo responseInfo) {
            Log.i("SearchApiExample", "Search results: " + results);
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
            new OfflineSearchEngineSettings(
                BuildConfig.MAPBOX_API_TOKEN,
                tileStore
            )
        );

        searchEngine.addEngineReadyCallback(engineReadyCallback);

        final Point dcLocation = Point.fromLngLat(-77.0339911055176, 38.899920004207516);

        final List<TilesetDescriptor> descriptors = Collections.singletonList(searchEngine.createTilesetDescriptor());

        final TileRegionLoadOptions tileRegionLoadOptions = new TileRegionLoadOptions.Builder()
            .descriptors(descriptors)
            .geometry(dcLocation)
            .acceptExpired(true)
            .build();

        Log.i("SearchApiExample", "Loading tiles...");

        final String tileRegionId = "Washington DC";

        searchEngine.addOnIndexChangeListener(new OfflineSearchEngine.OnIndexChangeListener() {
            @Override
            public void onIndexChange(@NonNull OfflineIndexChangeEvent event) {
                if (event.getRegionId().equals(tileRegionId) && (event.getType() == EventType.ADD || event.getType() == EventType.UPDATE)) {
                    Log.i("SearchApiExample", tileRegionId + " was successfully added or updated");

                    searchRequestTask = searchEngine.search(
                        "Cafe",
                        new OfflineSearchOptions(),
                        searchCallback
                    );
                }
            }

            @Override
            public void onError(@NonNull OfflineIndexErrorEvent event) {
                Log.i("SearchApiExample", "Offline index error: $event");
            }
        });

        tilesLoadingTask = tileStore.loadTileRegion(
            tileRegionId,
            tileRegionLoadOptions,
            progress -> Log.i("SearchApiExample", "Loading progress: " + progress),
            result -> {
                final Object printableResult;
                if (result.isValue()) {
                    printableResult = result.getValue();
                } else {
                    printableResult = result.getError();
                }
                Log.i("SearchApiExample", tileRegionId + " loading result: " + printableResult);
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
