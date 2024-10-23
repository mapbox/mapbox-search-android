package com.mapbox.search.sample.api;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.search.ApiType;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchEngine;
import com.mapbox.search.SearchEngineSettings;
import com.mapbox.search.SearchMultipleSelectionCallback;
import com.mapbox.search.SearchOptions;
import com.mapbox.search.SearchSelectionCallback;
import com.mapbox.search.common.AsyncOperationTask;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.result.SearchSuggestion;
import com.mapbox.search.sample.R;

import java.util.List;

public class ForwardGeocodingBatchResolvingJavaExampleActivity extends AppCompatActivity {

    private SearchEngine searchEngine;
    private AsyncOperationTask searchRequestTask;

    private final SearchSelectionCallback searchCallback = new SearchSelectionCallback() {

        @Override
        public void onSuggestions(@NonNull List<SearchSuggestion> suggestions, @NonNull ResponseInfo responseInfo) {
            if (suggestions.isEmpty()) {
                Log.i("SearchApiExample", "No suggestions found");
            } else {
                Log.i("SearchApiExample", "Search suggestions: " + suggestions);
                searchRequestTask = searchEngine.select(suggestions, multipleSelection);
            }
        }

        @Override
        public void onResult(@NonNull SearchSuggestion suggestion, @NonNull SearchResult result, @NonNull ResponseInfo info) {
            Log.i("SearchApiExample", "Search result: " + result);
        }

        @Override
        public void onResults(@NonNull SearchSuggestion suggestion, @NonNull List<SearchResult> results, @NonNull ResponseInfo responseInfo) {
            Log.i("SearchApiExample", "Category search results: " + results);
        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("SearchApiExample", "Search error: ", e);
        }
    };

    private final SearchMultipleSelectionCallback multipleSelection = new SearchMultipleSelectionCallback() {

        @Override
        public void onResult(@NonNull List<SearchSuggestion> suggestions, @NonNull List<SearchResult> results, @NonNull ResponseInfo responseInfo) {
            Log.i("SearchApiExample", "Batch retrieve results: " + results);
        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("SearchApiExample", "Search error: ", e);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            ApiType.SEARCH_BOX,
            new SearchEngineSettings()
        );

        final SearchOptions options = new SearchOptions.Builder()
            .build();

        searchRequestTask = searchEngine.search("Paris Eiffel Tower", options, searchCallback);
    }

    @Override
    protected void onDestroy() {
        searchRequestTask.cancel();
        super.onDestroy();
    }
}
