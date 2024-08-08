package com.mapbox.search.sample.api

import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.search.CategorySearchOptions
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.record.IndexableDataProvider
import com.mapbox.search.record.IndexableDataProviderEngine
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.sample.MainActivity
import com.mapbox.search.sample.R
import com.mapbox.search.sample.databinding.ActivityDynamicUserDataExampleBinding
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.Executor

class DynamicUserDataExampleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDynamicUserDataExampleBinding
    private lateinit var viewModel: DynamicUserDataViewModel
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inputStream = applicationContext.assets.open("sample-user-data.json")

        binding = ActivityDynamicUserDataExampleBinding.inflate(layoutInflater)
        viewModel = DynamicUserDataViewModel(inputStream)
        setContentView(binding.root)

        mapView = binding.mapView
        mapView.mapboxMap.also { mapboxMap ->
            mapboxMap.loadStyle(getMapStyleUri())

            mapView.location.updateSettings {
                enabled = true
            }

            mapView.location.addOnIndicatorPositionChangedListener(object :
                OnIndicatorPositionChangedListener {
                override fun onIndicatorPositionChanged(point: Point) {
                    mapView.mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(3.0)
                            .build()
                    )

                    mapView.location.removeOnIndicatorPositionChangedListener(this)
                }
            })
        }

        viewModel.uiState().observe(this, Observer { uiState ->
            if (uiState != null) {
                render(uiState)
            }
        })
    }

    private fun getMapStyleUri(): String {
        return when (val darkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> Style.DARK
            Configuration.UI_MODE_NIGHT_NO,
            Configuration.UI_MODE_NIGHT_UNDEFINED -> Style.MAPBOX_STREETS
            else -> error("Unknown mode: $darkMode")
        }
    }

    private fun render(uiState: UiState) {
        when (uiState) {
            is UiState.Loading -> {
                onLoad(uiState)
            }
            is UiState.Ready -> {
                onReady(uiState)
            }
            is UiState.Error -> {
                onError(uiState)
            }
            is UiState.Searching -> {
                onSearching(uiState)
            }
            is UiState.Success -> {
                onSuccess(uiState)
            }
        }
    }

    private fun onLoad(uiState: UiState.Loading) {
        binding.textView.text = "Loading..."
    }

    private fun onReady(uiState: UiState.Ready) {
        binding.textView.text = "Loaded."
        viewModel.search("charging_station")
    }

    private fun onError(uiState: UiState.Error) {
        Toast.makeText(applicationContext, uiState.message, Toast.LENGTH_LONG).show()
    }

    private fun onSearching(uiState: UiState.Searching) {
        binding.textView.text = "Search for: ${uiState.query}"
    }

    private fun onSuccess(uiState: UiState.Success) {
        binding.textView.text = "Search completed"

        val circleAnnotationManager = mapView.annotations.createCircleAnnotationManager(null)

        uiState.results.forEach { result ->
            val markerColor = if (result.categories?.contains("my_charging_station") == true) "#4caf50" else "#ee4e8b"

            val circleAnnotationOptions: CircleAnnotationOptions = CircleAnnotationOptions()
                .withPoint(result.coordinate)
                .withCircleRadius(8.0)
                .withCircleColor(markerColor)
                .withCircleStrokeWidth(2.0)
                .withCircleStrokeColor("#ffffff")
            circleAnnotationManager.create(circleAnnotationOptions)
        }

    }

    class DynamicUserDataViewModel(inputStream: InputStream) : ViewModel() {
        private val uiState = MutableLiveData<UiState>()
        private val searchEngine: SearchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            SearchEngineSettings()
        )

        init {
            try {
                loadData(inputStream)
            } finally {
                inputStream.close()
            }
        }

        fun uiState() = uiState

        fun search(category: String) {
            uiState.value = UiState.Searching(category)

            searchEngine.search(
                category,
                CategorySearchOptions(
                    limit = 200
                ), object: SearchCallback {
                override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                    uiState.postValue(UiState.Success(results))
                }

                override fun onError(e: Exception) {
                    TODO("Not yet implemented")
                }
            })
        }

        private fun loadData(inputStream: InputStream) {
            uiState.value = UiState.Loading

            viewModelScope.launch {
                val typeToken = object : TypeToken<List<UserRecord>>() {}.type
                val userRecords = Gson().fromJson<List<UserRecord>>(InputStreamReader(inputStream), typeToken)
                val userDataProvider = UserDataProvider(userRecords)
                searchEngine.registerDataProvider(userDataProvider, object : CompletionCallback<Unit> {
                    override fun onComplete(result: Unit) {
                        uiState.postValue(UiState.Ready)
                    }

                    override fun onError(e: Exception) {
                        uiState.postValue(UiState.Error("Unable to load data: ${e.message}"))
                    }

                })
            }
        }
    }

    class UserDataProvider(userRecords: List<UserRecord>) : IndexableDataProvider<UserRecord> {
        private val dataProviderEngines: MutableList<IndexableDataProviderEngine> = mutableListOf()
        private val userRecordIndex = mutableMapOf<String, UserRecord>()

        init {
            userRecords.forEach { record ->
                userRecordIndex[record.id] = record
            }
        }

        override val dataProviderName: String
            get() = PROVIDER_NAME

        override val priority: Int
            get() = PROVIDER_PRIORITY

        override fun registerIndexableDataProviderEngine(
            dataProviderEngine: IndexableDataProviderEngine,
            executor: Executor,
            callback: CompletionCallback<Unit>
        ): AsyncOperationTask = makeRequest(executor, callback) {
            dataProviderEngine.upsertAll(userRecordIndex.values.toList())
            dataProviderEngines.add(dataProviderEngine)
        }

        override fun unregisterIndexableDataProviderEngine(
            dataProviderEngine: IndexableDataProviderEngine,
            executor: Executor,
            callback: CompletionCallback<Boolean>
        ): AsyncOperationTask = makeRequest(executor, callback) {
            val isRemoved = dataProviderEngines.remove(dataProviderEngine)
            if (isRemoved) {
                dataProviderEngine.clear()
            }
            isRemoved
        }

        override fun contains(
            id: String,
            executor: Executor,
            callback: CompletionCallback<Boolean>
        ): AsyncOperationTask = makeRequest(executor, callback) {
            userRecordIndex.contains(id)
        }

        override fun remove(
            id: String,
            executor: Executor,
            callback: CompletionCallback<Boolean>
        ): AsyncOperationTask = makeRequest(executor, callback) {
            userRecordIndex.remove(id) != null
        }

        override fun clear(
            executor: Executor,
            callback: CompletionCallback<Unit>
        ): AsyncOperationTask = makeRequest(executor, callback) {
            userRecordIndex.clear()
            return@makeRequest Unit
        }

        override fun upsertAll(
            records: List<UserRecord>,
            executor: Executor,
            callback: CompletionCallback<Unit>
        ): AsyncOperationTask = makeRequest(executor, callback) {
            records.forEach { record ->
                userRecordIndex[record.id] = record
            }
        }

        override fun upsert(
            record: UserRecord,
            executor: Executor,
            callback: CompletionCallback<Unit>
        ): AsyncOperationTask = makeRequest(executor, callback) {
            userRecordIndex[record.id] = record
        }

        override fun getAll(
            executor: Executor,
            callback: CompletionCallback<List<UserRecord>>
        ): AsyncOperationTask = makeRequest(executor, callback) {
            userRecordIndex.values.toList()
        }

        override fun get(
            id: String,
            executor: Executor,
            callback: CompletionCallback<in UserRecord?>
        ): AsyncOperationTask = makeRequest(executor, callback) {
            userRecordIndex[id]
        }

        companion object {
            const val PROVIDER_NAME: String = "com.mapbox.search.sample.user-data"

            const val PROVIDER_PRIORITY: Int = 101

            fun <T> makeRequest(executor: Executor, callback: CompletionCallback<T>, func: () -> T): AsyncOperationTask {
                val result = func()
                executor.execute {
                    callback.onComplete(result)
                }
                return AsyncOperationTask.COMPLETED
            }
        }
    }

    sealed class UiState {

        object Loading : UiState()

        object Ready : UiState()

        data class Searching(val query: String) : UiState()

        data class Success(val results: List<SearchResult>) : UiState()

        data class Error(val message: String) : UiState()
    }

    class UserRecord(
        override val id: String,
        override val name: String,
        override val descriptionText: String,
        override val categories: List<String>,
        private val latitude: Double,
        private val longitude: Double,
        private val country: String
    ) : IndexableRecord, Parcelable
    {
        override val coordinate: Point
            get() = Point.fromLngLat(longitude, latitude)

        override val address: SearchAddress
            get() = SearchAddress(country = country)

        override val indexTokens: List<String>
            get() = listOf()

        override val makiIcon: String?
            get() = null

        override val metadata: SearchResultMetadata?
            get() = null

        override val routablePoints: List<RoutablePoint>?
            get() = null

        override val type: SearchResultType
            get() = SearchResultType.POI

        override fun describeContents(): Int {
            TODO("Not yet implemented")
        }

        override fun writeToParcel(p0: Parcel, p1: Int) {
            TODO("Not yet implemented")
        }
    }
}