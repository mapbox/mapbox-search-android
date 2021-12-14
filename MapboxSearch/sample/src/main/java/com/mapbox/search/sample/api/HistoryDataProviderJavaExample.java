package com.mapbox.search.sample.api;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.search.AsyncOperationTask;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.record.HistoryDataProvider;
import com.mapbox.search.record.HistoryRecord;
import com.mapbox.search.CompletionCallback;

import java.util.List;

public class HistoryDataProviderJavaExample extends AppCompatActivity {

    private final HistoryDataProvider historyDataProvider = MapboxSearchSdk.getServiceProvider().historyDataProvider();

    private AsyncOperationTask task = null;

    private final CompletionCallback<List<HistoryRecord>> callback = new CompletionCallback<List<HistoryRecord>>() {
        @Override
        public void onComplete(List<HistoryRecord> result) {
            Log.i("SearchApiExample", "History records: " + result);
        }

        @Override
        public void onError(@NonNull Exception e) {
            Log.i("SearchApiExample", "Unable to retrieve history records", e);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        task = historyDataProvider.getAll(callback);
    }

    @Override
    protected void onDestroy() {
        task.cancel();
        super.onDestroy();
    }
}
