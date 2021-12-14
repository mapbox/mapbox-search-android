# Forward geocoding

## Search SDK

The Search SDK's forward geocoding API represented by the `SearchEngine` interface. The instance of the `SearchEngine` can be retrieved from `MapboxSearchSdk`

```kotlin
val searchEngine = MapboxSearchSdk.createSearchEngine()
```
or for Java
```java
final SearchEngine searchEngine = MapboxSearchSdk.createSearchEngine();
```

The `SearchEngine` interface has 2 functions `SearchEngine.search()` and `SearchEngine.select()`. Each of this functions accept a callback object to provide search suggestions and results and returns `SearchRequestTask` which represents current request.
`SearchEngine.search(query: String, options: SearchOptions, callback: SearchSuggestionsCallback)` should be called to initiate search with the given query and returns list of suggestions in the `SearchSuggestionsCallback.onSuggestions(suggestions: List<SearchSuggestion>)` callback. When a user has chosen a suggestion, `SearchEngine.select(suggestion: SearchSuggestion, callback: SearchSelectionCallback)` function should be called to get updated suggestions or a `SearchResult` with location field available. Resolved search result will be returned in `SearchSelectionCallback.onResult(searchResult: SearchResult)`.

To prevent memory leaks when user leaves a screen or application and you are sure that search results are not needed anymore, call `SearchRequestTask.cancel()` function which releases a reference to a callback object.

See demo app for full code examples.



## Search SDK UI

`SearchBottomSheetView` view implements a forward geocoding. It notifies developers via listeners when a user clicks on a `SearchResult`, favorite record or wants to initiate a category search.

```kotlin
searchBottomSheetView.addOnSearchResultClickListener {
    ...
}

searchBottomSheetView.addOnFavoriteClickListener {
    ...
}

searchBottomSheetView.addOnCategoryClickListener {
    ...
}
```
or for Java
```java
searchBottomSheetView.addOnSearchResultClickListener(new SearchBottomSheetView.OnSearchResultClickListener() {
    @Override
    public void onSearchResultClick(@NotNull SearchResult searchResult) {
        ...
    }
});

searchBottomSheetView.addOnCategoryClickListener(new SearchBottomSheetView.OnCategoryClickListener() {
    @Override
    public void onCategoryClick(@NotNull Category category) {
        ...
    }
});

searchBottomSheetView.addOnFavoriteClickListener(new SearchBottomSheetView.OnFavoriteClickListener() {
    @Override
    public void onFavoriteClick(@NotNull FavoriteRecord favorite) {
        ...
    }
});
```

`SearchPlaceBottomSheetView` view can be used to show a selected in `OnSearchResultClickListener` or `OnFavoriteClickListener` listeners object. `SearchCategoriesBottomSheetView` view can be used to continue category search flow from `OnCategoryClickListener` listener.
