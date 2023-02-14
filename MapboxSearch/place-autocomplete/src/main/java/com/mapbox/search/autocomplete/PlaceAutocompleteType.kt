package com.mapbox.search.autocomplete

import android.os.Parcelable
import com.mapbox.search.base.core.CoreQueryType
import kotlinx.parcelize.Parcelize

/**
 * Values to filter Place Autocomplete results to include only a subset (one or more) of the available feature types.
 */
public abstract class PlaceAutocompleteType constructor(
    @JvmSynthetic
    internal val coreType: CoreQueryType,
) : Parcelable {

    /**
     * Poi query type.
     */
    @Parcelize
    public object Poi : PlaceAutocompleteType(CoreQueryType.POI)

    /**
     * Administrative unit types.
     */
    public abstract class AdministrativeUnit(
        type: CoreQueryType,
    ) : PlaceAutocompleteType(type) {

        /**
         * Generally recognized countries or, in some cases like Hong Kong, an area of quasi-national administrative status
         * that has been given a designated country code under ISO 3166-1.
         */
        @Parcelize
        public object Country : AdministrativeUnit(CoreQueryType.COUNTRY)

        /**
         * Top-level sub-national administrative features,
         * such as states in the United States or provinces in Canada or China.
         */
        @Parcelize
        public object Region : AdministrativeUnit(CoreQueryType.REGION)

        /**
         * Postal codes used in country-specific national addressing systems.
         */
        @Parcelize
        public object Postcode : AdministrativeUnit(CoreQueryType.POSTCODE)

        /**
         * Features that are smaller than top-level administrative features but typically larger than cities,
         * in countries that use such an additional layer in postal addressing (for example, prefectures in China).
         */
        @Parcelize
        public object District : AdministrativeUnit(CoreQueryType.DISTRICT)

        /**
         * Typically these are cities, villages, municipalities, etc. They’re usually features used in postal addressing,
         * and are suitable for display in ambient end-user applications where current-location context is needed
         * (for example, in weather displays).
         */
        @Parcelize
        public object Place : AdministrativeUnit(CoreQueryType.PLACE)

        /**
         * Official sub-city features present in countries where such an additional administrative layer
         * is used in postal addressing, or where such features are commonly referred to in local parlance.
         * Examples include city districts in Brazil and Chile and arrondissements in France.
         */
        @Parcelize
        public object Locality : AdministrativeUnit(CoreQueryType.LOCALITY)

        /**
         * Colloquial sub-city features often referred to in local parlance. Unlike locality features,
         * these typically lack official status and may lack universally agreed-upon boundaries.
         * Note: Not available for reverse geocoding requests.
         */
        @Parcelize
        public object Neighborhood : AdministrativeUnit(CoreQueryType.NEIGHBORHOOD)

        /**
         * Street.
         */
        @Parcelize
        public object Street : AdministrativeUnit(CoreQueryType.STREET)

        /**
         * Individual residential or business addresses as a street with house number. In a Japanese context,
         * this is the block number and the house number. All components smaller than `chome` are designated as an `address`.
         */
        @Parcelize
        public object Address : AdministrativeUnit(CoreQueryType.ADDRESS)
    }

    internal companion object {

        internal val ALL_DECLARED_TYPES: List<PlaceAutocompleteType> by lazy {
            listOf(
                Poi,
                AdministrativeUnit.Country,
                AdministrativeUnit.Region,
                AdministrativeUnit.Postcode,
                AdministrativeUnit.District,
                AdministrativeUnit.Place,
                AdministrativeUnit.Locality,
                AdministrativeUnit.Neighborhood,
                AdministrativeUnit.Street,
                AdministrativeUnit.Address,
            )
        }
    }
}
