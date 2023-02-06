package com.mapbox.search.discover

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Discover Query type.
 */
public abstract class DiscoverQuery internal constructor() : Parcelable {

    /**
     * Raw query canonical name.
     */
    public abstract val canonicalName: String

    /**
     * Query type representing category name.
     */
    @Parcelize
    public class Category internal constructor(
        override val canonicalName: String,
    ) : DiscoverQuery() {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DiscoverQuery

            if (canonicalName != other.canonicalName) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return canonicalName.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "Category(canonicalName='$canonicalName')"
        }

        /**
         * Companion object.
         */
        public companion object {

            /**
             * Predefined category query.
             */
            @JvmField
            public val RESTAURANTS: Category = Category(
                canonicalName = "restaurant"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val BARS: Category = Category(
                canonicalName = "bar"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val COFFEE_SHOP_CAFE: Category = Category(
                canonicalName = "cafe"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val HOTEL: Category = Category(
                canonicalName = "hotel"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val GAS_STATION: Category = Category(
                canonicalName = "gas_station"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val EV_CHARGING_STATION: Category = Category(
                canonicalName = "charging_station"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val PARKING: Category = Category(
                canonicalName = "parking_lot"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val BUS_STATION: Category = Category(
                canonicalName = "bus_station"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val RAILWAY_STATION: Category = Category(
                canonicalName = "railway_station"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val SHOPPING_MALLS: Category = Category(
                canonicalName = "shopping_mall"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val SUPERMARKET_GROCERY: Category = Category(
                canonicalName = "grocery"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val CLOTHING_STORE: Category = Category(
                canonicalName = "clothing_store"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val PHARMACY: Category = Category(
                canonicalName = "pharmacy"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val MUSEUMS: Category = Category(
                canonicalName = "museum"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val PARKS: Category = Category(
                canonicalName = "park"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val MOVIE_THEATERS: Category = Category(
                canonicalName = "cinema"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val GYM_FITNESS: Category = Category(
                canonicalName = "fitness_center"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val NIGHT_CLUBS: Category = Category(
                canonicalName = "nightclub"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val AUTO_REPAIR_MECHANIC: Category = Category(
                canonicalName = "auto_repair"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val ATM: Category = Category(
                canonicalName = "atm"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val HOSPITAL: Category = Category(
                canonicalName = "hospital"
            )

            /**
             * Predefined category query.
             */
            @JvmField
            public val EMERGENCY_ROOM: Category = Category(
                canonicalName = "emergency_room"
            )

            /**
             * Creates a new [Category] instance with a raw category canonical name
             */
            @JvmStatic
            public fun create(canonicalName: String): Category {
                return Category(canonicalName)
            }
        }
    }
}
