package com.mapbox.search.autofill

import android.Manifest
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseSearchSdkInitializer
import com.mapbox.search.base.location.defaultLocationEngine

/**
 * Main entrypoint to the Mapbox Autofill SDK. Performs forward or reverse geocoding requests to fetch addresses.
 */
public interface AddressAutofill {

    /**
     * Performs reverse geocoding request.
     *
     * @param point Coordinate to resolve.
     * @param options Request options.
     * @return Result of the search request, one of error or value.
     */
    public suspend fun suggestions(
        point: Point,
        options: AddressAutofillOptions
    ): Expected<Exception, List<AddressAutofillSuggestion>>

    /**
     * Performs forward geocoding request.
     *
     * @param query Search query.
     * @param options Request options.
     * @return Result of the search request, one of error or value.
     */
    public suspend fun suggestions(
        query: Query,
        options: AddressAutofillOptions
    ): Expected<Exception, List<AddressAutofillSuggestion>>

    /**
     * Retrieves detailed information about the [AddressAutofillSuggestion].
     * Use this function to end search session even if you don't need detailed information.
     *
     * Subject to change: in future, you may be charged for a suggestion call in case your UX flow
     * accepts one of suggestions as selected and uses the coordinates,
     * but you don’t call [select] method to confirm this. Other than that suggestions calls are not billed.
     *
     * @param suggestion Suggestion to select
     * @return Result of the select request, one of error or value.
     */
    public suspend fun select(suggestion: AddressAutofillSuggestion): Expected<Exception, AddressAutofillResult>

    /**
     * Companion object.
     */
    public companion object {

        /**
         * Creates a new instance of the [AddressAutofill].
         *
         * @param accessToken [Mapbox Access Token](https://docs.mapbox.com/help/glossary/access-token/).
         *
         * @param locationEngine The mechanism responsible for providing location approximations to the SDK.
         * By default [LocationEngine] is retrieved from [LocationEngineProvider.getBestLocationEngine].
         * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
         * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
         *
         * @return a new instance instance of [AddressAutofill].
         */
        @JvmStatic
        @JvmOverloads
        public fun create(
            accessToken: String,
            locationEngine: LocationEngine = defaultLocationEngine(),
        ): AddressAutofill {
            return AddressAutofillImpl.create(
                accessToken = accessToken,
                app = BaseSearchSdkInitializer.app,
                locationEngine = locationEngine
            )
        }
    }
}
