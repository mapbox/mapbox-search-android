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
import com.mapbox.search.category.Category;
import com.mapbox.search.category.CategoryOptions;
import com.mapbox.search.category.CategoryQuery;
import com.mapbox.search.category.CategoryResult;
import com.mapbox.search.sample.R;

import java.util.List;

public class CategoryJavaExampleActivity extends AppCompatActivity {

    private AsyncOperationTask searchTask;

    final CompletionCallback<List<CategoryResult>> callback = new CompletionCallback<List<CategoryResult>>() {
        @Override
        public void onComplete(List<CategoryResult> result) {
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

        final Category category = Category.create();

        final BoundingBox dcRegion = BoundingBox.fromPoints(
                Point.fromLngLat(-77.04482563320445, 38.89626984069077),
                Point.fromLngLat(-77.02584649998599, 38.907104458514695)
        );

        searchTask = category.search(
                CategoryQuery.Category.COFFEE_SHOP_CAFE,
                dcRegion,
                null,
                new CategoryOptions(),
                callback
        );
    }

    @Override
    protected void onDestroy() {
        searchTask.cancel();
        super.onDestroy();
    }
}
