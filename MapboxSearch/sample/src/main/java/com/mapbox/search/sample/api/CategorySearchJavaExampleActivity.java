package com.mapbox.search.sample.api;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.search.ApiType;
import com.mapbox.search.CategorySearchOptions;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchCallback;
import com.mapbox.search.SearchEngine;
import com.mapbox.search.SearchEngineSettings;
import com.mapbox.search.common.AsyncOperationTask;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.sample.BuildConfig;
import com.mapbox.search.sample.R;

import java.util.List;

public class CategorySearchJavaExampleActivity extends AppCompatActivity {

    private AsyncOperationTask searchRequestTask;

    private final SearchCallback searchCallback = new SearchCallback() {

        @Override
        public void onResults(@NonNull List<SearchResult> results, @NonNull ResponseInfo responseInfo) {
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

        final SearchEngine searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            ApiType.SEARCH_BOX,
            new SearchEngineSettings()
        );

        final CategorySearchOptions options = new CategorySearchOptions.Builder()
            .limit(1)
            .build();

        searchRequestTask = searchEngine.search("cafe", options, searchCallback);
    }

    @Override
    public void onDestroy() {
        searchRequestTask.cancel();
        super.onDestroy();
    }
}
