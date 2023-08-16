package com.mapbox.search.autocomplete

import android.Manifest
import com.mapbox.bindgen.Expected
import com.mapbox.common.location.LocationService
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseSearchSdkInitializer
import com.mapbox.search.base.location.defaultLocationService

/**
 * Main entrypoint to the Mapbox Place Autocomplete SDK.
 */
public interface PlaceAutocomplete {

    /**
     * Performs forward geocoding request.
     *
     * @param query Search query.
     * @param region Limit results to only those contained within the supplied bounding box.
     * @param proximity Optional geographic point that bias the response to favor results that are closer to this location.
     * If not specified the SDK will try to get user location from the [LocationService] that was provided in the [PlaceAutocomplete.create].
     * @param options Request options.
     * @return Result of the search request, one of error or value.
     */
    public suspend fun suggestions(
        query: String,
        region: BoundingBox? = null,
        proximity: Point? = null,
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
     * Retrieves detailed information about the [PlaceAutocompleteSuggestion].
     * Use this function to end search session even if you don't need detailed information.
     *
     * Subject to change: in future, you may be charged for a suggestion call in case your UX flow
     * accepts one of suggestions as selected and uses the coordinates,
     * but you don’t call [select] method to confirm this. Other than that suggestions calls are not billed.
     *
     * @param suggestion Suggestion to select
     * @return Result of the select request, one of error or value.
     */
    public suspend fun select(suggestion: PlaceAutocompleteSuggestion): Expected<Exception, PlaceAutocompleteResult>

    /**
     * Companion object.
     */
    public companion object {

        /**
         * Creates a new instance of the [PlaceAutocomplete].
         *
         * @param accessToken [Mapbox Access Token](https://docs.mapbox.com/help/glossary/access-token/).
         *
         * @param locationService The mechanism responsible for providing location approximations to the SDK.
         * By default [LocationService] is retrieved from [LocationServiceFactory.getOrCreate].
         * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
         * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
         *
         * @return a new instance instance of [PlaceAutocomplete].
         */
        @JvmStatic
        @JvmOverloads
        public fun create(
            accessToken: String,
            locationService: LocationService = defaultLocationService(),
        ): PlaceAutocomplete {
            return PlaceAutocompleteImpl.create(
                accessToken = accessToken,
                app = BaseSearchSdkInitializer.app,
                locationService = locationService
            )
        }
    }
}
