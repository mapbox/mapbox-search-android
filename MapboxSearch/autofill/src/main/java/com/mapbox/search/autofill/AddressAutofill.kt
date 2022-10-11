package com.mapbox.search.autofill

import android.Manifest
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseSearchSdkInitializer
import com.mapbox.search.base.location.defaultLocationEngine

/**
 * Main entrypoint to the Mapbox Autofill API. Performs forward or reverse geocoding requests to fetch addresses.
 */
public interface AddressAutofill {

    /**
     * Performs reverse geocoding request.
     *
     * @param point Coordinate to resolve.
     * @param options Request options.
     * @return Result of the search request represented by [AddressAutofillResponse].
     */
    public suspend fun suggestions(
        point: Point,
        options: AddressAutofillOptions
    ): AddressAutofillResponse

    /**
     * Performs forward geocoding request.
     *
     * @param query Search query.
     * @param options Request options.
     * @return Result of the search request represented by [AddressAutofillResponse].
     */
    public suspend fun suggestions(
        query: Query,
        options: AddressAutofillOptions
    ): AddressAutofillResponse

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
            return AddressAutofillImpl(
                AutofillSearchEngine.create(
                    accessToken = accessToken,
                    app = BaseSearchSdkInitializer.app,
                    locationEngine = locationEngine
                )
            )
        }
    }
}
