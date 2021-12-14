package com.mapbox.search.sample.api;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.geojson.Point;
import com.mapbox.search.AsyncOperationTask;
import com.mapbox.search.CompletionCallback;
import com.mapbox.search.IndexableDataProvidersRegistry;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.SearchEngine;
import com.mapbox.search.SearchOptions;
import com.mapbox.search.SearchRequestTask;
import com.mapbox.search.SearchSelectionCallback;
import com.mapbox.search.record.FavoriteRecord;
import com.mapbox.search.record.IndexableDataProvider;
import com.mapbox.search.record.IndexableDataProviderEngineLayer;
import com.mapbox.search.record.IndexableRecord;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.result.SearchResultType;
import com.mapbox.search.result.SearchSuggestion;
import com.mapbox.search.utils.concurrent.SearchSdkMainThreadWorker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

import kotlin.Unit;

public class CustomIndexableDataProviderJavaExample extends AppCompatActivity {

    private SearchEngine searchEngine;
    private AsyncOperationTask registerProviderTask;
    @Nullable
    private SearchRequestTask searchRequestTask = null;

    private final InMemoryDataProvider<IndexableRecord> customDataProvider = new InMemoryDataProvider<>(
        Arrays.asList(
            createRecord("Let it be", Point.fromLngLat(27.575321258282806, 53.89025545661358)),
            createRecord("Laŭka", Point.fromLngLat(27.574862357961212, 53.88998973246244)),
            createRecord("Underdog", Point.fromLngLat(27.57573285942709, 53.89020312748444))
        )
    );

    private final SearchSelectionCallback searchCallback = new SearchSelectionCallback() {
        @Override
        public void onSuggestions(@NonNull List<? extends SearchSuggestion> suggestions, @NonNull ResponseInfo responseInfo) {
            if (suggestions.isEmpty()) {
                Log.i("SearchApiExample", "No suggestions found");
            } else {
                Log.i("SearchApiExample", "Search suggestions: " + suggestions + ".\nSelecting first suggestion...");
                searchRequestTask = searchEngine.select(suggestions.get(0), this);
            }
        }

        @Override
        public void onResult(@NonNull SearchSuggestion suggestion, @NonNull SearchResult result, @NonNull ResponseInfo responseInfo) {
            Log.i("SearchApiExample", "Search result: " + result);
        }

        @Override
        public void onCategoryResult(@NonNull SearchSuggestion suggestion, @NonNull List<? extends SearchResult> results, @NonNull ResponseInfo responseInfo) {
            Log.i("SearchApiExample", "Category search results: " + results);
        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("SearchApiExample", "Search error", e);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchEngine = MapboxSearchSdk.getSearchEngine();

        Log.i("SearchApiExample", "Start CustomDataProvider registering...");
        registerProviderTask = MapboxSearchSdk.getServiceProvider().globalDataProvidersRegistry().register(
            customDataProvider,
            200,
            new IndexableDataProvidersRegistry.Callback() {
                @Override
                public void onSuccess() {
                    Log.i("SearchApiExample", "CustomDataProvider is registered");
                    searchRequestTask = searchEngine.search(
                        "Underdog",
                        new SearchOptions.Builder()
                            .proximity(Point.fromLngLat(27.574862357961212, 53.88998973246244))
                            .build(),
                        searchCallback
                    );
                }

                @Override
                public void onError(@NonNull Exception e) {
                    Log.i("SearchApiExample", "Error during registering", e);
                }
            }
        );
    }

    @Override
    protected void onDestroy() {
        registerProviderTask.cancel();

        if (searchRequestTask != null) {
            searchRequestTask.cancel();
        }
        MapboxSearchSdk.getServiceProvider().globalDataProvidersRegistry().unregister(
            customDataProvider,
            new IndexableDataProvidersRegistry.Callback() {
                @Override
                public void onSuccess() {
                    Log.i("SearchApiExample", "CustomDataProvider is unregistered");
                }

                @Override
                public void onError(@NonNull Exception e) {
                    Log.i("SearchApiExample", "Error during unregistering", e);
                }
            }
        );

        super.onDestroy();
    }

    private IndexableRecord createRecord(String name, Point coordinate) {
        return new FavoriteRecord(
            UUID.randomUUID().toString(),
            name,
            null,
            null,
            null,
            Collections.emptyList(),
            null,
            coordinate,
            SearchResultType.POI,
            null
        );
    }

    private static class InMemoryDataProvider<R extends IndexableRecord> implements IndexableDataProvider<R> {

        private final List<IndexableDataProviderEngineLayer> dataProviderEngineLayers = new ArrayList<>();
        private final Map<String, R> records = new LinkedHashMap<>();

        private final Executor mainThreadExecutor = SearchSdkMainThreadWorker.INSTANCE.getMainExecutor();

        InMemoryDataProvider(List<R> records) {
            for (R record : records) {
                this.records.put(record.getId(), record);
            }
        }

        @NonNull
        @Override
        public String getDataProviderName() {
            return "SAMPLE_APP_CUSTOM_DATA_PROVIDER";
        }

        @NonNull
        @Override
        public AsyncOperationTask registerIndexableDataProviderEngineLayer(
            @NonNull IndexableDataProviderEngineLayer dataProviderEngine,
            @NonNull Executor executor,
            @NonNull CompletionCallback<Unit> callback
        ) {
            dataProviderEngine.addAll(records.values());
            dataProviderEngineLayers.add(dataProviderEngine);
            executor.execute(() -> callback.onComplete(Unit.INSTANCE));
            return CompletedAsyncOperationTask.getInstance();
        }

        @NonNull
        @Override
        public AsyncOperationTask registerIndexableDataProviderEngineLayer(
            @NonNull IndexableDataProviderEngineLayer dataProviderEngine,
            @NonNull CompletionCallback<Unit> callback
        ) {
            return registerIndexableDataProviderEngineLayer(dataProviderEngine, mainThreadExecutor, callback);
        }

        @NonNull
        @Override
        public AsyncOperationTask unregisterIndexableDataProviderEngineLayer(
            @NonNull IndexableDataProviderEngineLayer dataProviderEngine,
            @NonNull Executor executor,
            @NonNull CompletionCallback<Boolean> callback
        ) {
            boolean isRemoved = dataProviderEngineLayers.remove(dataProviderEngine);
            if (isRemoved) {
                dataProviderEngine.clear();
            }
            executor.execute(() -> callback.onComplete(isRemoved));
            return CompletedAsyncOperationTask.getInstance();
        }

        @NonNull
        @Override
        public AsyncOperationTask unregisterIndexableDataProviderEngineLayer(
            @NonNull IndexableDataProviderEngineLayer dataProviderEngine,
            @NonNull CompletionCallback<Boolean> callback
        ) {
            return unregisterIndexableDataProviderEngineLayer(dataProviderEngine, mainThreadExecutor, callback);
        }

        @NonNull
        @Override
        public AsyncOperationTask get(
            @NonNull String id,
            @NonNull Executor executor,
            @NonNull CompletionCallback<? super R> callback
        ) {
            executor.execute(() -> callback.onComplete(records.get(id)));
            return CompletedAsyncOperationTask.getInstance();
        }

        @NonNull
        @Override
        public AsyncOperationTask get(@NonNull String id, @NonNull CompletionCallback<? super R> callback) {
            return get(id, mainThreadExecutor, callback);
        }

        @NonNull
        @Override
        public AsyncOperationTask getAll(@NonNull Executor executor, @NonNull CompletionCallback<List<R>> callback) {
            executor.execute(() -> callback.onComplete(new ArrayList<>(records.values())));
            return CompletedAsyncOperationTask.getInstance();
        }

        @NonNull
        @Override
        public AsyncOperationTask getAll(@NonNull CompletionCallback<List<R>> callback) {
            return getAll(mainThreadExecutor, callback);
        }

        @NonNull
        @Override
        public AsyncOperationTask contains(
            @NonNull String id,
            @NonNull Executor executor,
            @NonNull CompletionCallback<Boolean> callback
        ) {
            executor.execute(() -> callback.onComplete(records.get(id) != null));
            return CompletedAsyncOperationTask.getInstance();
        }

        @NonNull
        @Override
        public AsyncOperationTask contains(@NonNull String id, @NonNull CompletionCallback<Boolean> callback) {
            return contains(id, mainThreadExecutor, callback);
        }

        @NonNull
        @Override
        public AsyncOperationTask add(
            @NonNull R record,
            @NonNull Executor executor,
            @NonNull CompletionCallback<Unit> callback
        ) {
            for (IndexableDataProviderEngineLayer layer : dataProviderEngineLayers) {
                layer.add(record);
            }
            records.put(record.getId(), record);
            executor.execute(() -> callback.onComplete(Unit.INSTANCE));
            return CompletedAsyncOperationTask.getInstance();
        }

        @NonNull
        @Override
        public AsyncOperationTask add(@NonNull R record, @NonNull CompletionCallback<Unit> callback) {
            return add(record, mainThreadExecutor, callback);
        }

        @NonNull
        @Override
        public AsyncOperationTask addAll(
            @NonNull List<? extends R> records,
            @NonNull Executor executor,
            @NonNull CompletionCallback<Unit> callback
        ) {
            for (IndexableDataProviderEngineLayer layer : dataProviderEngineLayers) {
                layer.addAll(records);
            }
            for (R record : records) {
                this.records.put(record.getId(), record);
            }
            executor.execute(() -> callback.onComplete(Unit.INSTANCE));
            return CompletedAsyncOperationTask.getInstance();
        }

        @NonNull
        @Override
        public AsyncOperationTask addAll(@NonNull List<? extends R> records, @NonNull CompletionCallback<Unit> callback) {
            return addAll(records, mainThreadExecutor, callback);
        }

        @NonNull
        @Override
        public AsyncOperationTask update(
            @NonNull R record,
            @NonNull Executor executor,
            @NonNull CompletionCallback<Unit> callback
        ) {
            for (IndexableDataProviderEngineLayer layer : dataProviderEngineLayers) {
                layer.update(record);
            }
            records.put(record.getId(), record);
            executor.execute(() -> callback.onComplete(Unit.INSTANCE));
            return CompletedAsyncOperationTask.getInstance();
        }

        @NonNull
        @Override
        public AsyncOperationTask update(@NonNull R record, @NonNull CompletionCallback<Unit> callback) {
            return update(record, mainThreadExecutor, callback);
        }

        @NonNull
        @Override
        public AsyncOperationTask remove(
            @NonNull String id,
            @NonNull Executor executor,
            @NonNull CompletionCallback<Boolean> callback
        ) {
            for (IndexableDataProviderEngineLayer layer : dataProviderEngineLayers) {
                layer.remove(id);
            }
            boolean isRemoved = records.remove(id) != null;
            executor.execute(() -> callback.onComplete(isRemoved));
            return CompletedAsyncOperationTask.getInstance();
        }

        @NonNull
        @Override
        public AsyncOperationTask remove(@NonNull String id, @NonNull CompletionCallback<Boolean> callback) {
            return remove(id, mainThreadExecutor, callback);
        }

        @NonNull
        @Override
        public AsyncOperationTask clear(@NonNull Executor executor, @NonNull CompletionCallback<Unit> callback) {
            for (IndexableDataProviderEngineLayer layer : dataProviderEngineLayers) {
                layer.clear();
            }
            records.clear();
            executor.execute(() -> callback.onComplete(Unit.INSTANCE));
            return CompletedAsyncOperationTask.getInstance();
        }

        @NonNull
        @Override
        public AsyncOperationTask clear(@NonNull CompletionCallback<Unit> callback) {
            return clear(mainThreadExecutor, callback);
        }
    }

    private static class CompletedAsyncOperationTask implements AsyncOperationTask {

        private static final CompletedAsyncOperationTask INSTANCE = new CompletedAsyncOperationTask();

        public static CompletedAsyncOperationTask getInstance() {
            return INSTANCE;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void cancel() {
            // Do nothing
        }
    }
}
