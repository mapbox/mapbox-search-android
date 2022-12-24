package com.mapbox.search.sample.api;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.search.ApiType;
import com.mapbox.search.common.IsoCountry;
import com.mapbox.search.common.IsoLanguage;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchEngine;
import com.mapbox.search.SearchEngineSettings;
import com.mapbox.search.SearchOptions;
import com.mapbox.search.SearchSelectionCallback;
import com.mapbox.search.common.AsyncOperationTask;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.result.SearchSuggestion;
import com.mapbox.search.sample.R;

import java.util.List;

public class JapanSearchJavaExampleActivity extends AppCompatActivity {

    private SearchEngine searchEngine;
    private AsyncOperationTask searchRequestTask;

    private final SearchSelectionCallback searchCallback = new SearchSelectionCallback() {

        @Override
        public void onSuggestions(@NonNull List<SearchSuggestion> suggestions, @NonNull ResponseInfo responseInfo) {
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
        public void onCategoryResult(@NonNull SearchSuggestion suggestion, @NonNull List<SearchResult> results, @NonNull ResponseInfo responseInfo) {
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

        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            ApiType.SBS,
            new SearchEngineSettings(getString(R.string.mapbox_access_token))
        );

        final SearchOptions options = new SearchOptions.Builder()
            .countries(IsoCountry.JAPAN)
            .languages(IsoLanguage.JAPANESE)
            .build();

        searchRequestTask = searchEngine.search("東京", options, searchCallback);
    }

    @Override
    protected void onDestroy() {
        searchRequestTask.cancel();
        super.onDestroy();
    }
}
