package com.mapbox.search.autofill

import com.mapbox.geojson.Point

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
         * @return a new instance instance of [AddressAutofill].
         */
        public fun create(accessToken: String): AddressAutofill {
            return AddressAutofillImpl(AutofillSearchEngine.create(accessToken))
        }
    }
}
