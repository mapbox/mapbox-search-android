# Internal changelog for the Mapbox Search SDK for Android

## 1.0.0-beta.28-SNAPSHOT

### Mapbox dependencies
- Search Native SDK `0.50.0`
- Common SDK `21.2.0-beta.1`
- Telemetry SDK `8.1.1`
- Kotlin `1.5.31`



## 1.0.0-beta.27

### New features
- [CORE] Now `SearchSuggestion` has a new `categories` property.
- [UI] `SearchBottomSheetView` has a new callback `SearchBottomSheetView.OnSearchViewStateChangeListener` that notifies subscribers when the "Main search view" changes it's state.

### Breaking changes
- [CORE] Search SDK doesn't implicitly request `android.permission.ACCESS_COARSE_LOCATION` permission anymore. Default `LocationEngine` that's passed to `MapboxSearchSdk.initialize()` needs location access in order to work properly.
- [UI] `SearchResultsView.SearchListener` now has more callback functions: `SearchListener.onSuggestions()`, `SearchListener.onCategoryResult()`, and `SearchListener.onError()`.

### Mapbox dependencies
- Search Native SDK `0.50.0`
- Common SDK `21.2.0-beta.1`
- Telemetry SDK `8.1.1`
- Kotlin `1.5.31`



## 1.0.0-beta.26

### New features
- [CORE] New properties are available `SearchSuggestion.matchingName`, `SearchSuggestion.serverIndex`, `SearchResult.matchingName`, `SearchResult.serverIndex`, `ResponseInfo.responseUuid`.
- [CORE] Now customers can provide their own feedback IDs in `FeedbackEvent.feedbackId` and `MissingResultFeedbackEvent.feedbackId`.
- [CORE] Now `SearchSuggestion` provides optional `metadata` property.
- [CORE] Now `SearchResultMetadata` provides optional `countryIso1` and `countryIso2` properties that provide country codes in `ISO 3166-1` and `ISO 3166-2`. These properties are available for both `SearchResult` and `SearchSuggestion` types, in SBS and V5 APIs.

### Bug fixes
- [CORE] Fixed GSON serialization for `SearchResult` and `SearchSuggestion`

### Mapbox dependencies
- Search Native SDK `0.49.0`
- Common SDK `21.1.0`
- Telemetry SDK `8.1.1`
- Kotlin `1.5.31`



## 1.0.0-beta.25

### Breaking changes
- [CORE] Public interfaces `SearchResult`, `ServerSearchResult`, `IndexableRecordSearchResult`, `SearchSuggestion` have been marked as `sealed` as they were not supposed to be implemented by external code.
- [UI] Now `SearchCategoriesBottomSheetView.CategoryLoadingStateListener.onLoadingError()` callback provides one more parameter - `Exception` occurred during the request.
- [UI] Now `SearchCategoriesBottomSheetView.CategoryLoadingStateListener.onCategoryResultsLoaded()` callback provides one more parameter - `ResponseInfo` which contains response information.

### New features
- [CORE, UI] Now Search SDK documentation and source code is visible in Android Studio.

### Bug fixes
- [CORE] Now `SearchRequestException` contains original detail error message returned from the backend.

### Mapbox dependencies
- Search Native SDK `0.47.0`
- Common SDK `21.1.0-rc.1`
- Telemetry SDK `8.1.1`
- Kotlin `1.5.31`



## 1.0.0-beta.24

### Breaking changes
- [UI] Now initialization methods `SearchPlaceBottomSheetView.initialize()`, `SearchCategoriesBottomSheetView.initialize()`, `SearchResultsView.initialize()` have to be called in order to make these views work properly.

### New features
- [UI] Now you can provide distance unit type (imperial or metric) used for views visual information via initialization methods of the views: `SearchBottomSheetView.initializeSearch()`, `SearchPlaceBottomSheetView.initialize()`, `SearchCategoriesBottomSheetView.initialize()`, `SearchResultsView.initialize()`.

### Bug fixes
- [UI] Fixed a bug with uninitialized properties in created from `SearchBottomSheetView` favorite record.

### Mapbox dependencies
- Search Native SDK `0.46.1`
- Common SDK `21.0.1`
- Telemetry SDK `8.1.0`
- Kotlin `1.4.21`



## 1.0.0-beta.23

### Breaking changes
- [CORE] Signature of `OfflineSearchEngine.select(SearchSuggestion, Executor, SearchSelectionCallback): SearchRequestTask` function has been changed, now it also accepts `SelectOptions`.
- [CORE] Functions `MapboxSearchSdk.createCategorySearchEngine()`, `MapboxSearchSdk.createReverseGeocodingSearchEngine()` and `MapboxSearchSdk.createSearchEngine()` have been replaced with `MapboxSearchSdk.getCategorySearchEngine()`, `MapboxSearchSdk.getReverseGeocodingSearchEngine()` and `MapboxSearchSdk.getSearchEngine()` functions accordingly. Now SDK consumers can access only single instance of each search engine.
- [CORE] `MapboxSearchSdk.initialize()` now accepts `com.mapbox.android.core.location.LocationEngine` instead of `com.mapbox.search.location.LocationProvider`, argument name has also been changed from `locationProvider` to `locationEngine`. `com.mapbox.search.location.LocationProvider`, `com.mapbox.search.location.DefaultLocationProvider`, `com.mapbox.search.location.PointLocationProvider` have been removed. Now you can use `com.mapbox.android.core.location.LocationEngineProvider.getBestLocationEngine(Context)` to get a default implementation of `LocationEngine`.
- [CORE] Function `com.mapbox.search.ServiceProvider.locationProvider()` has been removed, now `ServiceProvider.locationEngine()` is available which returns instance of `com.mapbox.android.core.location.LocationEngine`.
- [CORE] `OfflineSearchEngine.createBoundariesTilesetDescriptor()` has been renamed to `createPlacesTilesetDescriptor()`.
- [UI] `SearchPlace` has a new property `feedback`, constructor and `copy` function signature have been changed.
- [UI] Now `SearchPlace.createFromSearchResult()` requires `ResponseInfo` as an additional argument.
- [UI] `SearchBottomSheetView.OnSearchResultClickListener.onSearchResultClick()` and `SearchCategoriesBottomSheetView.OnSearchResultClickListener.onSearchResultClick()` receive `ResponseInfo` as an additional argument.
- [UI] `SearchResultsView.SearchListener`'s functions `onSearchResult()` and `onPopulateQueryClicked()` now receive `ResponseInfo` as an additional argument.

### New features
- [CORE] `IndexableDataProviderEngineLayer` class now can be called from any thread.
- [CORE] Now selected search suggestions from `OfflineSarchEngine` are added to `HistoryDataProvider` by default. You can manage this behavior by passing custom `SelectOptions` to `select()` function.
- [CORE] New functions `IndexableDataProviderEngineLayer.removeAll()` and `IndexableDataProviderEngineLayer.executeBatchUpdate()` and new interface `IndexableDataProviderEngineLayer.BatchUpdateOperation` have been introduced. Now user can remove several records in one go and also specify several operations that should be executed atomically for a given engine layer.
- [CORE] Now `OfflineSearchEngine` has a new functions for selecting preferable tileset - `selectTileset()`.
- [CORE] Also, `OfflineSearchEngine` can notify users of index change events. Add a listener via `addOnIndexChangeListener()` to get these events.
- [UI] Now `SearchPlaceBottomSheetView` class notifies users about adding specific `SearchPlace` to favorites via `SearchPlaceBottomSheetView.OnSearchPlaceAddedToFavoritesListener`.
- [UI] `SearchPlaceBottomSheetView` has a new `Feedback` button that enables users to report issues and send feedback on search quality. With this functionality also come `OnFeedbackClickListener` and functions to add/remove feedback listeners. Customers have to use `SearchFeedbackBottomSheetView` or implement feedback UI on their own.
- [UI] Now a new view `SearchFeedbackBottomSheetView` which implements feedback workflow is available. Customers can add their own listener via `addOnFeedbackSubmitClickListener()` to hook feedback sending to define their own behavior.
- [UI] Now `SearchBottomSheetView` has new interfaces `OnFeedbackClickListener`, and `OnFeedbackSubmitClickListener`. Customers can add their own listeners via `addOnFeedbackClickListener`, and `addOnFeedbackSubmitClickListener()` to hook feedback sending and define custom behavior.

### Bug fixes
- [CORE] Fixed bug related to unexpected cancellation of scheduled `SearchEngine` request when another search request was fired from `CategorySearchEngine` or from another `SearchEngine`.
- [CORE] Fixed bug in `HistoryDataProvider` when records weren't removed from `IndexableDataProviderEngineLayer` during the trimming of obsolete records.
- [UI] Fixed several bugs related to state restoration, keyboard showing and UI lagginess. 

### Mapbox dependencies
- Search Native SDK `0.44.1`
- Common SDK `20.1.1`
- Telemetry SDK `8.1.0`



## 1.0.0-beta.22

### Breaking changes
- [UI] `SearchMode` now has a new value `AUTO`. If search mode set to `AUTO`, UI will determine online/offline mode automatically based on the device's network reachability.
- [CORE] `OfflineSearchEngine` functions `getTileRegions()`, `getGroupInfo()`, `loadTileRegion()`, `updateTilesGroup()`, `removeTilesGroup()` have been removed. Now you should use `TileStore` directly to work with offline data.
- [CORE] Signature of `EngineReadyCallback.onEngineReady()` has been changed. Now this function has no arguments.
- [CORE] Properties `OfflineSearchSettings.tilesDataset` and `OfflineSearchSettings.tilesVersion` and their corresponding Builder properties have been remove. Now dataset and version parameters can be specified during `TilesetDescriptor` creation.
- [CORE] `OfflineSearchEngine.addOflineRegion()` function and `AddRegionCallback` interface have been removed. Now you can use `TileStore` to manage offline data.

### New features
- [CORE] `DefaultLocationProvider` class now can be called from any thread.
- [CORE] A new property `OfflineSearchEngine.tileStore` is available. It returns `TileStore` object used for offline tiles management.

### Mapbox dependencies
- Common SDK `20.1.0`
- Telemetry SDK `8.1.0`



## 1.0.0-beta.21

### Breaking changes
- [CORE] Enum `QueryType` has a new type - `CATEGORY`. Now users can receive category-only search results. Please note: this type is restricted for internal or special use.
- [CORE] `SearchEngine.select(SearchSuggestion, Executor, SearchSelectionCallback)` method signature has been changed to `SearchEngine.select(SearchSuggestion, SelectOptions, Executor, SearchSelectionCallback)`: now user can control whether `SearchEngine` should automatically add final result to the history or not.

### New features
- [CORE] `@Deprecated` annotations for Geocoding-specific search options have been removed.
- [CORE] New function `SearchEngine.select(SearchSuggestion, SelectOptions, SearchSelectionCallback)` is available.
- [CORE] `SelectOptions` class has been introduced.

### Bug fixes
- [CORE] Fixed a bug in `OfflineSearchEngine` when tiles modifying functions such as `loadTileRegion()`, `updateTilesGroup()`, `removeTilesGroup()` were called before engine got initialized.

### Mapbox dependencies
- Common SDK `20.0.0`
- Telemetry SDK `8.1.0`



## 1.0.0-beta.20

### Breaking changes
- [CORE] `MainThreadWorker` has a new property `mainExecutor` that returns an executor working on the main thread. All the subclasses now have to implement this property.
- [CORE] Similarly to search engines, asynchronous functions of `IndexableDataProvider` may accept optional parameter `Executor`. All the subclasses now have to implement these functions.
- [CORE] `OfflineTileRegion`'s constructor has been made internal. Also, its `copy` function has been removed.
- [CORE] Signature of the function `OfflineSearchEngine.loadTileRegion()` has been changed, now it accepts additional `progressCallback` parameter. `callback` parameter has been renamed to `completionCallback`.

### New features
- [CORE] Now you can pass `OfflineSearchSettings` to `MapboxSearchSdk.initialize()` to override offline search settings.
- [CORE] Now asynchronous functions of `SearchEngine`, `ReverseGeocodingSearchEngine`, `CategorySearchEngine`, `OfflineSearchEngine`, `IndexableDataProvider`, `IndexableDataProvidersRegistry` may accept optional parameter `Executor` used for events dispatching. By default events are still dispatched on the main thread.
- [CORE] New function `OfflineSearchEngine.getGroupInfo()` is available. This function provides group meta info.
- [CORE] New function `OfflineSearchEngine.updateTilesGroup()` is available. This function updates the existing tiles group.

### Mapbox dependencies
- Common SDK `19.0.0`
- Telemetry SDK `8.1.0`



## 1.0.0-beta.19.1

### Mapbox dependencies
- Common SDK `19.0.0`
- Telemetry SDK `8.1.0`



## 1.0.0-beta.19

### Breaking changes
- [CORE] `IndexableDataProvider.CompletionCallback` has been moved to upper level package `com.mapbox.search`.

### New features
- [CORE] Now customers can override logger. See [Mapbox Android Modularization](https://github.com/mapbox/mapbox-base-android/blob/master/MODULARIZATION.md) for more information.
- [CORE] A new functionality to work with offline data has been added to `OfflineSearchEngine`.
- [CORE] Now customers can add custom data providers. As a result, new classes (`IndexableDataProvidersRegistry` and `IndexableDataProviderEngineLayer`) have been added and also few extra methods and properties have been added to exisitng API. Take a look at new samples for more information.
- [CORE] New functions `MapboxSearchSdk.addDataProviderInitializationCallback()` and `MapboxSearchSdk.removeDataProviderInitializationCallback()` have been introduced. Now users can receive notifications about default data providers (history and favorites) successful or failed initialization.

### Bug fixes
- [CORE] Fix failed Search SDK requests for cases, when application label contains non-ASCII symbols.

### Mapbox dependencies
- Common SDK `18.0.0`
- Telemetry SDK `8.1.0`



## 1.0.0-beta.18

### Breaking changes
- [CORE] Now `OfflineSearchEngine.addOfflineRegion()` doesn't implicitly add `.cont` extensions for offline data files.

### New features
- [CORE] Now customers can override native library loader. See [Mapbox Android Modularization](https://github.com/mapbox/mapbox-base-android/blob/master/MODULARIZATION.md) for more information.

### Mapbox dependencies
- Common SDK `17.0.0`
- Telemetry SDK `8.1.0`



## v1.0.0-beta.17

### Breaking changes
- [CORE] `MapboxSearchSdk.initialize()` function's signature has been changed: `geocodingEndpointBaseUrl` and `singleBoxSearchBaseUrl` parameters have been removed and new parameter `searchSdkSettings: SearchSdkSettings` has been added. With new `SearchSdkSettings` class user can specify not only search endpoints, but also maximum allowed `HistoryRecords` amount in `HistoryDataProvider`.
- [CORE] `SearchSuggestionType.IndexableRecordItem`'s constructor has been changed: new parameter `type: SearchResultType` has been added. Now user can get the type of resolved `IndexableRecord` before the selection of `SearchSuggestion`, associated with given `IndexableRecordItem`.
- [CORE] `SearchSuggestionType.SearchResultSuggestion`, `SearchSuggestionType.Category` and `SearchSuggestionType.IndexableRecordItem` constructors visibility have been reduced to `internal`.

### New features
- [CORE] `MainThreadWorker` and `SearchSdkMainThreadWorker` classes are now public, they can be used to override main thread job handler which can be useful for testing.
- [UI] Now history records matched with saved favorites will be marked as favorites in the search history view.

### Bug fixes
- [UI] Fix for suggestion clicks processing.

### Mapbox dependencies
- Common SDK `16.2.0`
- Telemetry SDK `8.1.0`



## v1.0.0-beta.16

### Breaking changes
- [UI] Functions `Category.findBySBSName()` and `Category.findByName()` have been removed, now you can use `Category.findByCanonicalName()` which looks up for a Category by any of SBS or geocoding canonical names.

### New features
- [UI] Now `com.mapbox.search.ui.view.category.Category`'s constructor and properties are public, you can instantiate a new category to be used in the UI Search SDK.
- [UI] Expose `maxWidth` layout parameter for `SearchSdkFrameLayout` class and its subclasses in public API.

### Mapbox dependencies
- Common SDK `16.0.0`
- Telemetry SDK `8.1.0`



## v1.0.0-beta.15

### Breaking changes
- [CORE] Async functions in `IndexableDataProvider` and `OfflineSearchEngine.addOfflineRegion()` now return `AsyncOperationTask` object instead of `Future`. 
- [CORE] Function `DistanceCalculator.squareDistance()` has been removed.
- [UI] Property `SearchBottomSheetView.Configuration.searchOptions` and corresponding `Configuration.Builder` function have been removed. Now you can provide `SearchOptions` through `SearchBottomSheetView.searchOptions` and override them for each new request.
- [UI] New `distanceMeters` property has been added to `SearchPlace`. Therefore, `copy()`, `createFromSearchResult()` and `createFromIndexableRecord()` methods and `SearchPlace` constructor accept `distanceMeters` parameter now.

### New features
- [CORE] New `distanceMeters` property has been added to `SearchResult`.
- [CORE] Now `SearchOptions.indexableRecordsDistanceThresholdMeters` and `CategorySearchOptions.indexableRecordsDistanceThresholdMeters` options are available that allow to look up for indexable records only within specified distance threshold.
- [UI] Now user can edit name and can delete favorite, created from template (e.g. Work template).
- [UI] Now you can provide `CategorySearchOptions` that will be used for search request in `SearchCategoriesBottomSheetView`. Options can be provided via `fun open(category: Category, searchOptions: CategorySearchOptions)`.
- [UI] New `updateDistance()` method has been added to `SearchPlaceBottomSheetView`.

### Bug fixes
- [CORE] Fixed a bug with uninitialized in some cases `SearchResultMetadata` properties.

### Mapbox dependencies
- Common SDK `14.2.0`
- Telemetry SDK `8.1.0`



## v1.0.0-beta.14

### Breaking changes
- [CORE] All operations in `IndexableDataProvider` classes now are asynchronous.
- [CORE] Property `LocalDataProvider.size` has been removed, now it's recommended to get size of the provider by calling `IndexableDataProvider.getAll()`.
- [CORE] Now `OfflineSearchEngine.addOfflineRegion()` returns a `Future<Boolean>` representing pending completion of the task. This change may break existing code in rare cases.

### New features
- [CORE] `OfflineSearchEngine` has a new function `fun searchAddressesNearby(street: String, proximity: Point, radiusMeters: Double, callback: SearchCallback)` that searches for addresses nearby.



## v1.0.0-beta.13

### Breaking changes
- [CORE] Offline data format has been changed, you'll need to obtain new offline data to work with offline.
- [CORE] In the previous versions, some internal classes were available publicly, they had the `internal` keyword in the package name. Now such classes are not available anymore. Normally, external customers shouldn't have accessed internal classes in their projects, however, this change may break customer's code.

### New features
- [UI] Landscape support.
- [UI] `SearchBottomSheetView`, `SearchCategoriesBottomSheetView` and `SearchPlaceBottomSheetView` classes extend from `SearchSdkFrameLayout` class now. This new class allows users to specify `maxWidth` of the view.

### Bug fixes
- [CORE] Fixed a bug with uninitialized properties in search suggestions and search results based on indexable records.



## v1.0.0-beta.12

### New features
- [UI] Dark Mode support.
- [UI] Color palette customization has been added. Take a look at new sample for more information.
- [UI] Now `SearchBottomSheetView` returns clicked `HistoryRecords` via `SearchBottomSheetView.OnHistoryClickListener`. In the previous SDK versions when a user clicked on a `HistoryRecords`, a new search started using the record's name as a query.

### Breaking changes
- [UI] `SearchPlace.createFromUserFavorite(FavoriteRecord)` removed, use `SearchPlace.createFromIndexableRecord(IndexableRecord, Point)` instead.



## v1.0.0-beta.11

### Breaking changes
- [CORE] The following methods now require extra `responseInfo: ResponseInfo` parameter:
    - `AnalyticsService.createRawFeedbackEvent(searchResult: SearchResult)`
    - `AnalyticsService.createRawFeedbackEvent(searchSuggestion: SearchSuggestion)`
    - `AnalyticsService.sendFeedback(searchResult: SearchResult)`
    - `AnalyticsService.sendFeedback(searchSuggestion: SearchSuggestion)`

### New features
- [CORE] `FeedbackEvent.FeedbackReason` annotation class has been added which helps us to enforce developers to use one of predefined constants for `FeedbackEvent.reason` property. Currently available constants are:
    - `FeedbackReason.INCORRECT_NAME`
    - `FeedbackReason.INCORRECT_ADDRESS`
    - `FeedbackReason.INCORRECT_LOCATION`
    - `FeedbackReason.OTHER`


## v1.0.0-beta.10

### Breaking changes
- [CORE] `OfflineSearchEngine.addOfflineRegion()` function's signature has been changed, now it accepts callback to be invoked when the region has been added.
- [UI] Data classes `SearchPlace` and `FavoriteTemplate` have been replaced with regular classes. As a result, all `componentN()` methods have been removed and `FavoriteTemplate.copy()` method has been removed.
- [UI] Enum class `Category` has been replaced with a regular class. All predefined `Category` values can be retrieved with `Category.PREDEFINED_CATEGORY_VALUES` property.
- [UI] Enum classes `SearchBottomSheetView.BottomSheetState`, `SearchCategoriesBottomSheetView.BottomSheetState`, `SearchPlaceBottomSheetView.BottomSheetState` have been replaced with annotation classes, annotated with `@IntDef`.



## v1.0.0-beta.9

### Breaking changes
- [CORE] Almost all public `data classes`, `sealed classes` and `enum classes` API have been changed.

### New features
- [CORE, UI] `OfflineSearchEngine` has been added which provides offline search functionality. Contact our team to get more information on how to use it and where to get offline data.

### Bug fixes
- [CORE] Fixed NPE that could be thrown on some specific Android devices.



## v1.0.0-beta.8

### Breaking changes
- [CORE, UI] Minimum Android SDK version 21, OkHttp 4.9.0.
- [CORE] `SearchResultType` enum types order have been changed. Now all types are sorted in descending order by the size of the associated geofeature.

### New features

### Bug fixes
- [CORE] Fixed a bug with `SearchSuggestionType.Category()` where property `canonicalName` was incorrect.
- [UI] Functions `findByName()` and `findBySBSName()` from `Category` class now work properly: `findByName()` finds categories for names associated with Geocoding endpoint, `findBySBSName()` - for names associated with SBS endpoints.



## v1.0.0-beta.7

### Breaking changes
- [CORE] `WeekTimestamp` class API has been changed:
    - `hour` and `minute` properties type has been changed from `Byte` to `Int`.
    - `day` property type has been changed from `Byte` to `WeekDay`. Also, new `WeekDay` enum class has been added. This class represents one of days of the week (Monday, ..., Sunday).
- [CORE] `HistoryRecord` and `FavoriteRecord` constructor signatures have been changed.
- [CORE] `FavoriteRecord.categories` property now is nullable.
- [UI] `SearchPlace.categories` property now is nullable.

### New features
- [CORE] Now `IndexableRecord` interface provides new properties: `routablePoints`, `metadata`, `descriptionText`, `categories`, `makiIcon`. Their semantic is the same as in the corresponding fields of the `SearchResult`.



## v1.0.0-beta.6.1

### New features
- [CORE] New `AnalyticsService.createRawFeedbackEvent()` methods have been added. This methods allow users to create raw feedback event, that can be cached and used later. Also, `AnalyticsService.sendRawFeedbackEvent()` method has been added, so users could send feedback event, based on provided raw feedback event.



## v1.0.0-beta.6

### Breaking changes
- [CORE] `SearchCallback.onResults()`, `SearchSuggestionsCallback.onSuggestions()`, `SearchSelectionCallback.onResult()`, `SearchMultipleSelectionCallback.onResult()` function signatures have been changed: now each function accepts `ResponseInfo` parameter, the information about search response and associated search request.
- [CORE] Batch retrieve method `SearchEngine.select(List<SearchSuggestion>,SearchMultipleSelectionCallback): SearchRequestTask` now requires provided list of suggestions to be non-empty and also each `SearchSuggestion` should have same `RequestOptions`, so request will be processed correctly.
- [CORE] `CategorySearchOptions.types` property and its corresponding builder field and functions have been removed.
- [CORE] Enum `QueryType` has a new type - `STREET`.
- [CORE] `SearchResultSuggestion` property `type: SearchResultType` has been replaced with property `types: List<SearchResultType>`. Also, `SearchResult` property `type: SearchResultType` has been replaced with property `types: List<SearchResultType>` and `SearchPlace` property `resultType: SearchResultType` has been replaced with property `resultTypes: List<SearchResultType>`.
- [CORE] Now `RouteOptions.Deviation.Time` constructor requires extra `sarType: SarType?` parameter.
- [CORE] `SearchOptions.navigationProfile: SearchNavigationProfile` property and its corresponding builder field and function have been removed. Instead, new `SearchOptions.navigationOptions: SearchNavigationOptions` property and corresponding `SearchOptions.Builder.navigationOptions` builder field and `SearchOptions.Builder.navigationOptions()` function have been added.
- [UI] Now `SearchPlace` also provides `descriptionText` field. `SearchPlace`'s constructor signature has been changed.

### New features
- [CORE] New `AnalyticsService.sendMissingResultFeedback()` method has been added. This method allows users to send feedback for use cases, when expected POI / place wasn't found in search results. Also new `MissingResultFeedbackEvent` class has been added.
- [CORE] New `sessionId` property has been added to `FeedbackEvent`. 
- [CORE] New `EtaType`, `SearchNavigationOptions`, `RouteOptions.Deviation.SarType` classes have been added. Also, `RouteOptions.Deviation` and its subclasses has new `sarType: SarType?` property.
- [CORE] Similarly to the `SearchSuggestion` type, now `SearchResult` also provides `descriptionText` field.
- [UI] All types of `SearchSuggestion/SearchResult` will be shown in the description label (previously only first was shown).



## v1.0.0-beta.5

### Breaking changes
- [CORE] `RequestOptions` primary constructor visibility has been reduced to `internal`. Now you can not create `RequestOptions` objects.
- [CORE] `com.mapbox.search.NavProfile` has been renamed to `com.mapbox.search.SearchNavigationProfile`. Consequently, `com.mapbox.search.SearchOptions.navProfile` and `com.mapbox.search.SearchOptions.Builder.navProfile` properties have been renamed to `navigationProfile`. Also, `com.mapbox.search.SearchOptions.Builder.navProfile()` function have been renamed to `navigationProfile()` and accepts `SearchNavigationProfile` instead of the old type.
- [CORE] Property `searchResultType` in `com.mapbox.search.record.HistoryRecord` and `com.mapbox.search.record.FavoriteRecord` classes has been renamed to `type`. Now `type` became common property for all the Indexable Records.
- [CORE] `CategorySearchEngine` function signature has been changed: now `search()` accepts `options` parameter of `CategorySearchOptions` type. Class `CategorySearchOptions` contains search options, used for category search (previously those options were stored inside `SearchOptions`). Both `CategorySearchOptions` and `SearchOptions` class signatures are identical.
- [CORE] `ReverseGeoOptions.countries` and `ReverseGeoOptions.reverseMode` options have been marked as deprecated along with `ReverseMode` enum. These entities will be removed in the future updates.

### New features
- [CORE] New `AnalyticsService` and `FeedbackEvent` classes have been added. `AnalyticsService` helps users track analytics events. For now only feedback events can be tracked. User should provide `SearchResult` / `SearchSuggestion` / `IndexableRecord` object and `FeedbackEvent` object, describing user's feedback.
- [CORE] New`ServiceProvider.analyticsService()` method has been added, which returns instance of `AnalyticsService`.
- [CORE] New `unsafeParameters` property has been added to `SearchOptions`. The corresponding `unsafeParameters` property and `unsafeParameters()` method have been added to `SearchOptions.Builder`.
- [CORE] `openHours`, `parking`, `cpsJson` properties have been added to `SearchResultMetadata`. Also `OpenHours`, `OpenPeriod`, `WeekTimestamp`, `ParkingData` classes have been added.
- [CORE] New `ignoreIndexableRecords` property now available in `SearchOptions`, and `CategorySearchOptions`. This property specifies whether to ignore `IndexableRecord` results or not, default is false. Semantic is the same for both `SearchOptions`, and `CategorySearchOptions`.

### Bug fixes
- [CORE] Fixed a bug due to which search results based on Indexable Records could be with types not specified in `SearchOptions`.



## v1.0.0-beta.4.1

### Bug fixes
- [CORE] Fixed a bug that caused to fail in some cases search request with `IndexableRecords` included.
- [UI] Category canonical name updated.

## v1.0.0-beta.4

### Breaking changes
- [CORE] New `SearchSuggestionType` subtype has been added, which may break existing code in some cases.
- [CORE] `proximity`, `origin` and `navProfile` properties have been removed from `RequestOptions`.
- [CORE] `SearchResultMetadata`'s API has been significantly changed.
    - Function `SearchResultMetadata.getAllData()` has been removed. Instead `extraData` property is available. This property provides the data that is not available via the other properties of the class.
    - Constants `KEY_PHONE`, `KEY_WEBSITE`, `KEY_REVIEW_COUNT`, `KEY_AVERAGE_RATING` were removed. Instead, use `phone`, `website`, `reviewCount`, `averageRating` properties directly.

### New features
- [CORE] New `SearchSuggestionType` subtype has been added. Now `SearchSuggestion` can have `Query` type, which means that selection of this suggestion type will result in new suggestions.
- [CORE] Now you can search along a route, pass `RouteOptions` to `SearchOptions` to configure route parameters.
- [CORE] Now `SearchResultMetadata` provides associated with search result photos and also provides a new `description` property.
- [CORE] Now `SearchSuggestion` has an optional `address` property.
- [CORE] Now `RequestOptions` has new `options` property and additional `proximityRewritten` and `originRewritten` flags that denote whether `options` have been modified by the Search SDK.

### Bug fixes
- [CORE] Fixed a bug that happened when backend sent suggestion with `POSTCODE` type.
- [UI] Now UI SDK always shows address if it's available.



## v1.0.0-beta.3

### Breaking changes
- [CORE] `SearchSelectionCallback` functions signatures have been changed, now `onResult` and `onCategoryResult` accept `SearchSuggestion` parameter, the suggestion from which the result was resolved.
- [CORE] New fields have been added to `HistoryRecord` and `FavoriteRecord` data classes which may break existing code.
- [CORE] String resource `app_name` has been removed from the core SDK. This resource was public by mistake and external modules normally shouldn't have used this.

### New features
- [CORE] Batch retrieve for search suggestions has been added. Call `SearchEngine.select(List<SearchSuggestion>,SearchMultipleSelectionCallback): SearchRequestTask` to resolve multiple suggestions at once.
- [CORE] Now if you have a token with special permissions, you'll be receiving POI metadata if it's available. Metadata is available in `SearchResult`, also, it can be stored in `HistoryRecord` and `FavoriteRecord`.
- [CORE] Now you can request ETA to search suggestions and results for forward geocoding. Specify `origin` point and `navProfile` type in `SearchOptions` to request ETA. ETA for reverse geocoding is to be added.
- [CORE] Now routable points are available for search results, also, it can be stored in `HistoryRecord` and `FavoriteRecord`.

### Bug fixes
- [CORE] Fixed a crash that happened after consecutive selection of `SearchSuggestion` within one session.
- [UI] Now `recent search history` view doesn't show duplicating history entries.
- [UI] Prevent `SearchCategoriesBottomSheetView` and `SearchResultsView` from leaking, when used by `Fragment`.



## v1.0.0-beta.2

### Breaking changes
- [CORE] Parameter `endpointBaseUrl` of the `MapboxSearchSdk.initialize()` method has been renamed to `geocodingEndpointBaseUrl`. In addition, new parameter named `singleBoxSearchBaseUrl` has been added to specify `Single Box Search` API endpoint.
- [CORE] Enum `SearchResultType` now has two more types - `PLACE` and `STREET`.
- [CORE] `SearchSuggestion.TypeDescription` has been transformed to a new type called `SearchSuggestionType`. See documentation of `SearchSuggestionType` for more details. Also property `typeDescription` of the `SearchSuggestion` type has been renamed to `type`.
- [CORE] `SearchCategorySuggestion` type has been removed. Use `SearchSuggestion.type` to check if a suggestion is a category.
- [CORE] `SearchSelectionCallback.onCategoryResult(results: List<SearchResult>)` callback has been added.
- [UI] From `SearchBottomSheetView`, `SearchPlaceBottomSheetView`, `SearchCategoriesBottomSheetView` classes removed functions that were available to simplify interaction with Kotlin functional interfaces.
With Kotlin 1.4.0 [SAM conversions for Kotlin interfaces](https://kotlinlang.org/docs/reference/whatsnew14.html#sam-conversions-for-kotlin-interfaces) are available and these functions are not needed anymore.
Removing these functions from the SDK may bring breaking changes if you were accessing those functions from Java code (which is usually pointless) or if you are using older versions of Kotlin.

### New features
- [ALL] Support of the new search backend (will be announced soon).
- [ALL] Bump Kotlin version to 1.4.0.

### Bug fixes
We are bug-free :wink: (almost).



## v1.0.0-beta.1
No changes, the code is the same as v0.7.1.



## v0.7.1

### New features

### Bug fixes
- [CORE] Fixed serialization bug that caused history and favorites data loss.
- [CORE] Now it's possible for external customers to instantiate HistoryRecord instance.

### Breaking changes
- [CORE] SearchRequestException constructor arguments order changed.



## v0.7.0

### New features
- [UI] Now if `SearchCategoriesBottomSheetView.open(Category)` is called with the latest set category, listeners will be notified about loaded results if they already loaded or loading will be initiated if the card's state is error.
- [UI] New properties `isNavigateButtonVisible`, `isShareButtonVisible`, `isFavoriteButtonVisible` for `SearchPlaceBottomSheetView` are available that manage buttons visible to user.
- [UI] Now it's allowed to set any number of items for hot categories section including zero. Hot categories section will be hidden in case of an empty list. However, to ensure the best behavior for the view, we recommend setting at most four items. Hot categories section does not support scrolling, if items don't fit into the view width, they will be flattened.
- [UI] Now `SearchResultsView` has `defaultSearchOptions` property which defines default options for search. To override these options, use `search(query: String, options: SearchOptions)` function.
- [UI] Now you can specify collapsed state anchor for `SearchBottomSheetView`. Desired anchor can be provided with `SearchBottomSheetView.Configuration`.

### Bug fixes
- [UI] Fixed a bug with `SearchBottomSheetView` when new configuration was ignored after activity recreation.

### Breaking changes
- [CORE] Some classes that used to be available in external modules have been moved to `com.mapbox.search.internal` package. These files should not be accessed in the customer's code and we do not guarantee their availability in the future.
- [CORE] Core SDK has been fully refactored, see new samples for more information.
- [UI] Removed `HotCategory` class. Now it's allowed to choose any category for hot categories section.
- [UI] Removed `SearchBottomSheetView.Configuration.categorySearchResultsLimit` property and its corresponding builder fields.
- [UI] We do not longer guarantee that returned by `SearchBottomSheetBehavior`, `SearchPlaceBottomSheetView`, and `SearchCategoriesBottomSheetView`'s `getBehavior()` function returns an instance of `com.google.android.material.bottomsheet.BottomSheetBehavior`. Use `addOnBottomSheetStateChangedListener` functions for watching card state changes and `state` property for getting current state.
- [UI] `SearchViewBottomSheetsMediator` is no longer part of the UI SDK and has been moved to the sample app.
- [UI] `SearchResultsView.initialize()` function has been removed. Now view can work without explicit initialization call. To provide search options, including results limit, use `search(query: String, options: SearchOptions)` function.
- [UI] `SearchBottomSheetView.Configuration.searchResultsLimit` property and its corresponding builder functions have been removed. Now you can provide `SearchOptions` that includes results limit.



## v0.6.1

### Bug fixes
- [UI] Fixed a bug with SearchBottomSheetView when it reset its state after `hide()` function call.



## v0.6.0

### New features
- [UI] Added `SearchBottomSheetView.isHideableByDrag` property that sets whether the bottom sheet can hide when it is swiped down.

### Bug fixes
- [UI] Fixed a bug with BottomSheetBehavior when it came to the incorrect state after state restoration in some cases.

### Breaking changes
- [CORE] SearchError has been renamed to SearchNetworkRequestException. Now it extends RuntimeException() and contains error cause.
- [UI] `SearchPlaceBottomSheetView.addOnNavigateClickListener()` and `SearchPlaceBottomSheetView.addOnShareClickListener()` functions now accept lambdas which take argument of type `SearchPlace`.



## v0.5.0

### New features
- [UI] Added SearchPlaceBottomSheetView.
- [UI] Added SearchCategoriesBottomSheetView.
- [UI] Added auxiliary functions for SearchBottomSheet state management: `open()`, `expand()`, `hide()`, `restorePreviousNonHiddenState()`.
- [UI] Miles support. Now UI SDK detects measurement system from current device locale and formats distances in corresponding distance unit.
- [CORE] Added `SearchEngine.setAccessToken(String)` function.

### Bug fixes
- [CORE] In some cases SDK couldn't get last known location.

### Breaking changes
- [UI] Removed SearchResultListener, use `SearchBottomSheetView.OnSearchResultClickListener` and `SearchBottomSheetView.OnCategoryClickListener` instead.
    Category search functionality has been removed from `SearchBottomSheetView`, instead now `SearchCategoriesBottomSheetView` implements category search.
- [UI] Removed `UserFavoriteOnClickListener`, use `SearchBottomSheetView.OnFavoriteClickListener` instead.
- [UI] Removed `SearchCloseListener`. Now `SearchBottomSheetView` does not have close button, instead the card resets its state when it collapses.
- [CORE] Removed `SearchResult.distance()` function, instead `DistanceCalculator` class is available, also you can use `LatLng.distanceTo(LatLng)` function.
- [CORE] Removed `SearchEngine.destroy()` function. Now `SearchEngine` must be initialized only once, in case of reinitialization attempt `IllegalStateException` will be thrown.
