# Category search

## Search SDK

Category search is represented by the Search SDK's `CategorySearchEngine` interface. The instance of the `CategorySearchEngine` can be retrieved from `MapboxSearchSdk`

```kotlin
val searchEngine = MapboxSearchSdk.createCategorySearchEngine()
```
or for Java
```java
final CategorySearchEngine searchEngine = MapboxSearchSdk.createCategorySearchEngine();
```

The `CategorySearchEngine` interface has 1 function `CategorySearchEngine.search()` which accepts a callback object to provide search results and returns `SearchRequestTask` which represents current request.
To prevent memory leaks when user leaves a screen or application and you are sure that search results are not needed anymore, call `SearchRequestTask.cancel()` function which releases a reference to a callback object.

See demo app for full code examples.



## Search SDK UI

`SearchCategoriesBottomSheetView` view implements a category search. It notifies developers via listeners when a user clicks on a `SearchResult`.

```kotlin
searchCategoriesView.addOnSearchResultClickListener {
    ...
}
```
or for Java
```java
searchCategoriesView.addOnSearchResultClickListener(new SearchCategoriesBottomSheetView.OnSearchResultClickListener() {
    @Override
    public void onSearchResultClick(@NotNull SearchResult searchResult) {
        ...
    }
});
```

`SearchPlaceBottomSheetView` view can be used to show a selected in a `OnSearchResultClickListener` search result.
