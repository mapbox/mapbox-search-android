package com.mapbox.search.autofill

import com.mapbox.geojson.Point
import com.mapbox.search.SearchEngine

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
        query: String,
        options: AddressAutofillOptions
    ): AddressAutofillResponse

    /**
     * @suppress
     */
    public companion object {

        /**
         * Creates a new instance of the [AddressAutofill].
         *
         * @param searchEngine [SearchEngine] that will be used as a backend for address search.
         * @return a new instance instance of [AddressAutofill].
         */
        public fun create(searchEngine: SearchEngine): AddressAutofill = AddressAutofillImpl(searchEngine)
    }
}
