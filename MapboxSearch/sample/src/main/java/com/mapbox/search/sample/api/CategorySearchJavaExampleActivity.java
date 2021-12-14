package com.mapbox.search.sample.api;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.search.CategorySearchEngine;
import com.mapbox.search.CategorySearchOptions;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchCallback;
import com.mapbox.search.SearchRequestTask;
import com.mapbox.search.result.SearchResult;

import java.util.List;

public class CategorySearchJavaExampleActivity extends AppCompatActivity {

    private CategorySearchEngine categorySearchEngine;
    private SearchRequestTask searchRequestTask;

    private final SearchCallback searchCallback = new SearchCallback() {

        @Override
        public void onResults(@NonNull List<? extends SearchResult> results, @NonNull ResponseInfo responseInfo) {
            if (results.isEmpty()) {
                Log.i("SearchApiExample", "No category search results");
            } else {
                Log.i("SearchApiExample", "Category search results: " + results);
            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("SearchApiExample", "Search error", e);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        categorySearchEngine = MapboxSearchSdk.getCategorySearchEngine();

        final CategorySearchOptions options = new CategorySearchOptions.Builder()
            .limit(1)
            .build();

        searchRequestTask = categorySearchEngine.search("cafe", options, searchCallback);
    }

    @Override
    public void onDestroy() {
        searchRequestTask.cancel();
        super.onDestroy();
    }
}
