# Installation

## Search SDK

### 1. Set up access
1. From your account's [tokens page](https://account.mapbox.com/access-tokens/) create a token with the `Downloads:Read` scope.
2. Declare the Mapbox Downloads API's `v2/releases/maven` endpoint in the repositories block.

```groovy
allprojects {
   repositories {
     maven {
       url 'https://api.mapbox.com/downloads/v2/releases/maven'
       authentication {
         basic(BasicAuthentication)
       }
       credentials {
         username = "mapbox"
         password = "{secret Mapbox token with DOWNLOADS:READ scope}"
       }
     }
   }
}
```

For more information and advices, see similar description for Maps SDK https://docs.mapbox.com/android/maps/overview/#install-the-maps-sdk.

### 2. Add the dependency
1. Make sure that your project's `minSdkVersion` is at API 19 or higher.
2. Add the Search SDK dependency to your `build.gradle` file

```groovy
dependencies {
    implementation "com.mapbox.search:mapbox-search-android:VERSION"
}
```


### 3. Get an access token
If you don't have a Mapbox account, [sign up](https://account.mapbox.com/auth/signup/), and then navigate to your [Account page](https://www.mapbox.com/account/). Copy your default public token to your clipboard. After you've added the Search SDK as a dependency inside of your Android project, as an option you can save the token in the `R.strings.xml` file, create a new String resource, and paste the access token.
```xml
<string name="mapbox_access_token">MAPBOX_ACCESS_TOKEN</string>
```
Make sure to read [Mapbox's suggestions on keeping access tokens private in open-source Android projects](https://docs.mapbox.com/help/troubleshooting/private-access-token-android-and-ios/#non-git-option) if you're working on an open-source project.


### 4. Initialize the SDK

Initialize the Search SDK with your Mapbox API token. The SDK should be initialized only once, so it makes sense to initialize it in your project’s Application class.
```kotlin
override fun onCreate() {
    super.onCreate()
    MapboxSearchSdk.initialize(this, getString(R.string.mapbox_access_token), DefaultLocationProvider(this))
}
```
or for Java
```java
@Override
public void onCreate() {
    super.onCreate();
    MapboxSearchSdk.initialize(this, getString(R.string.mapbox_access_token), new DefaultLocationProvider(this));
}
```

The Search SDK also provides a `setAccessToken()` method in case you want to switch the Mapbox access token at runtime.
```kotlin
MapboxSearchSdk.setAccessToken(newToken)
```
or for Java
```java
MapboxSearchSdk.setAccessToken(newToken);
```

### 5. Setup permissions

While the Search SDK can work without location permissions, we strongly recommend that you request either `android.permission.ACCESS_FINE_LOCATION` or `android.permission.ACCESS_COARSE_LOCATION` permission in order to have more accurate and relevant search results. See [Request App Permissions](https://developer.android.com/training/permissions/requesting) for more information.
For Android below Android 6.0 (API level 23) `android.permission.ACCESS_COARSE_LOCATION` permission requested automatically by Search SDK via Manifest merge feature. Also for all the API levels SDK requests `android.permission.INTERNET` permission.


### 6. Use Java 8 language features and APIs
Search SDK uses some features from Java 8 so it is also required to update your `build.gradle` file as described [here](https://developer.android.com/studio/write/java8-support).



## Search SDK UI

### 1. Add the dependency

```groovy
implementation "com.mapbox.search:mapbox-search-android-ui:VERSION"
```

### 2. Declare view in the layout

There are 3 available views that can be used in the UI:
- `SearchBottomSheetView` is a view that implements common search flow including search history and favorites
- `SearchPlaceBottomSheetView` is a view that shows extended information about the place
- `SearchCategoriesBottomSheetView` is a view for category search

These views can be used all together or independently from each other. Place them inside a CoordinatorLayout:

```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >

    <com.mapbox.search.ui.view.SearchBottomSheetView
        android:id="@+id/search_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:elevation="@dimen/search_card_elevation"
        />

    <com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
        android:id="@+id/search_place_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:elevation="@dimen/search_card_elevation"
        />

    <com.mapbox.search.ui.view.category.SearchCategoriesBottomSheetView
        android:id="@+id/search_categories_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:elevation="@dimen/search_card_elevation"
        />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

`SearchBottomSheetView` must be initialized before use

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_search_view)

    val searchBottomSheetView = findViewById<SearchBottomSheetView>(R.id.search_view)
    searchBottomSheetView.initializeSearch(savedInstanceState, SearchBottomSheetView.Configuration())
}
```
or for Java
```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final SearchBottomSheetView searchBottomSheetView = findViewById(R.id.search_view);
    searchBottomSheetView.initializeSearch(savedInstanceState, new SearchBottomSheetView.Configuration());
}
```

To ensure the best behavior for the `SearchBottomSheetView`, you should declare the [android:windowSoftInputMode](https://developer.android.com/guide/topics/manifest/activity-element#wsoft) attribute in your manifest's `<activity>` element with `adjustNothing` value.

### 3. Simple UI view

Search SDK UI also provides simple UI mode - a standalone view for search that is a simplified version of `SearchBottomSheetView` without categories and favorites. Unlike `SearchBottomSheetView`, `SearchResultsView` doesn't need to be declared inside CoordinatorLayout and can be used anywhere. To add the simple UI mode, declare view in your layout xml

```xml
<com.mapbox.search.ui.view.SearchResultsView
    android:id="@+id/search_results_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    />
```

and similarly to `SearchBottomSheetView` initialize it

```kotlin
val searchResultsView = findViewById<SearchResultsView>(R.id.search_results_view)
searchResultsView.addSearchListener(listener)
```
or for Java
```java
final SearchResultsView searchResultsView = findViewById(R.id.search_results_view);
searchResultsView.addSearchListener(listener);
```
