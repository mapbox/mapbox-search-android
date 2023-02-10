package com.mapbox.search.autocomplete

import android.Manifest
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseSearchSdkInitializer
import com.mapbox.search.base.location.defaultLocationEngine

/**
 * Main entrypoint to the Mapbox Place Autocomplete SDK.
 */
public interface PlaceAutocomplete {

    /**
     * Performs forward geocoding request.
     *
     * @param query Search query.
     * @param options Request options.
     * @return Result of the search request, one of error or value.
     */
    public suspend fun suggestions(
        query: TextQuery,
        options: PlaceAutocompleteOptions = PlaceAutocompleteOptions()
    ): Expected<Exception, List<PlaceAutocompleteSuggestion>>

    /**
     * Performs reverse geocoding request.
     *
     * @param point Coordinate to resolve.
     * @param options Request options.
     * @return Result of the search request, one of error or value.
     */
    public suspend fun suggestions(
        point: Point,
        options: PlaceAutocompleteOptions = PlaceAutocompleteOptions()
    ): Expected<Exception, List<PlaceAutocompleteSuggestion>>

    /**
     * @suppress
     */
    public companion object {

        /**
         * Creates a new instance of the [PlaceAutocomplete].
         *
         * @param accessToken [Mapbox Access Token](https://docs.mapbox.com/help/glossary/access-token/).
         *
         * @param locationEngine The mechanism responsible for providing location approximations to the SDK.
         * By default [LocationEngine] is retrieved from [LocationEngineProvider.getBestLocationEngine].
         * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
         * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
         *
         * @return a new instance instance of [PlaceAutocomplete].
         */
        @JvmStatic
        @JvmOverloads
        public fun create(
            accessToken: String,
            locationEngine: LocationEngine = defaultLocationEngine(),
        ): PlaceAutocomplete {
            return PlaceAutocompleteImpl.create(
                accessToken = accessToken,
                app = BaseSearchSdkInitializer.app,
                locationEngine = locationEngine
            )
        }
    }
}
