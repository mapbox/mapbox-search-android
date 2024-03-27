package com.mapbox.search.sample.api;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Point;
import com.mapbox.search.common.AsyncOperationTask;
import com.mapbox.search.common.CompletionCallback;
import com.mapbox.search.discover.Discover;
import com.mapbox.search.discover.DiscoverOptions;
import com.mapbox.search.discover.DiscoverQuery;
import com.mapbox.search.discover.DiscoverResult;
import com.mapbox.search.sample.R;

import java.util.List;

public class DiscoverJavaExampleActivity extends AppCompatActivity {

    private AsyncOperationTask searchTask;

    final CompletionCallback<List<DiscoverResult>> callback = new CompletionCallback<List<DiscoverResult>>() {
        @Override
        public void onComplete(List<DiscoverResult> result) {
            Log.i("SearchApiExample", "Search results: " + result);
        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("SearchApiExample", "Search error: ", e);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Discover discover = Discover.create();

        final BoundingBox dcRegion = BoundingBox.fromPoints(
                Point.fromLngLat(-77.04482563320445, 38.89626984069077),
                Point.fromLngLat(-77.02584649998599, 38.907104458514695)
        );

        searchTask = discover.search(
                DiscoverQuery.Category.COFFEE_SHOP_CAFE,
                dcRegion,
                null,
                new DiscoverOptions(),
                callback
        );
    }

    @Override
    protected void onDestroy() {
        searchTask.cancel();
        super.onDestroy();
    }
}
