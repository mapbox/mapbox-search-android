# Reverse geocoding

## Search SDK

Reverse geocoding is represented by the Search SDK's `ReverseGeocodingSearchEngine` interface. The instance of the `ReverseGeocodingSearchEngine` can be retrieved from `MapboxSearchSdk`

```kotlin
val reverseGeocoding = MapboxSearchSdk.createReverseGeocodingSearchEngine()
```
or for Java
```java
final ReverseGeocodingSearchEngine reverseGeocoding = MapboxSearchSdk.createReverseGeocodingSearchEngine();
```

The `ReverseGeocodingSearchEngine` interface has 1 function `ReverseGeocodingSearchEngine.search()` which accepts a callback object to provide search results and returns `SearchRequestTask` which represents current request.
To prevent memory leaks when user leaves a screen or application and you are sure that search results are not needed anymore, call `SearchRequestTask.cancel()` function which releases a reference to a callback object. If multiple requests have been submitted, every uncomplete `SearchRequestTask` should be canceled.

See demo app for full code examples.



## Search SDK UI

Search SDK UI does not implement reverse geocoding functionality.
