package com.mapbox.search.ui.view.category

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mapbox.search.ui.R
import kotlinx.parcelize.Parcelize

/*
 * TODO(search-sdk/#708): Review categories list
 *
 * Internal doc:
 * SBS categories canonical names https://github.com/mapbox/tag-to-category/blob/master/reference/categories.csv
 */

/**
 * Experimental API, can be changed or removed in the next SDK releases.
 *
 * Represents a category of POI. This class also defines a list of commonly used categories.
 */
@Suppress("LargeClass")
@Parcelize
public class Category(

    /**
     * Category canonical name used in Geocoding API. Can be empty if you don't use SBS endpoint for search.
     * See [List of the most common POI categories](https://docs.mapbox.com/api/search/geocoding/#point-of-interest-category-coverage).
     */
    public val geocodingCanonicalName: String,

    /**
     * Category canonical name used in Single Box Search API. Can be empty if you don't use SBS endpoint for search.
     * Contact our team if you need more information about SBS categories.
     */
    public val sbsCanonicalName: String,

    /**
     * Category visual presentation.
     */
    public val presentation: Presentation
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Category

        if (geocodingCanonicalName != other.geocodingCanonicalName) return false
        if (sbsCanonicalName != other.sbsCanonicalName) return false
        if (presentation != other.presentation) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = geocodingCanonicalName.hashCode()
        result = 31 * result + sbsCanonicalName.hashCode()
        result = 31 * result + presentation.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "Category(" +
                "geocodingCanonicalName='$geocodingCanonicalName', " +
                "sbsCanonicalName='$sbsCanonicalName', " +
                "presentation=$presentation" +
                ")"
    }

    /**
     * Represents a category visual presentation like name and icon.
     * See [MAKI icons for common points of interest](https://labs.mapbox.com/maki-icons/).
     */
    @Parcelize
    public class Presentation(

        /**
         * The resource identifier of the string resource to be displayed as category name.
         */
        @StringRes public val displayName: Int,

        /**
         * The resource identifier of the drawable to be displayed as category icon.
         */
        @DrawableRes public val icon: Int,
    ) : Parcelable {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Presentation

            if (displayName != other.displayName) return false
            if (icon != other.icon) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            var result = displayName
            result = 31 * result + icon
            return result
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "CategoryPresentation(displayName=$displayName, icon=$icon)"
        }
    }

    /**
     * @suppress
     */
    public companion object {

        /**
         * Predefined category constant.
         */
        @JvmField
        public val RESTAURANTS: Category = Category(
            geocodingCanonicalName = "restaurant",
            sbsCanonicalName = "restaurant",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_restaurants,
                R.drawable.mapbox_search_sdk_ic_category_restaurant
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val BARS: Category = Category(
            geocodingCanonicalName = "bar",
            sbsCanonicalName = "bar",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_bars,
                R.drawable.mapbox_search_sdk_ic_category_bar
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val COFFEE_SHOP_CAFE: Category = Category(
            geocodingCanonicalName = "cafe",
            sbsCanonicalName = "cafe",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_coffee_shop_cafe,
                R.drawable.mapbox_search_sdk_ic_category_cafe
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val HOTEL: Category = Category(
            geocodingCanonicalName = "hotel",
            sbsCanonicalName = "hotel",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_hotel,
                R.drawable.mapbox_search_sdk_ic_category_hotel
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val GAS_STATION: Category = Category(
            geocodingCanonicalName = "fuel",
            sbsCanonicalName = "gas_station",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_gas_station,
                R.drawable.mapbox_search_sdk_ic_category_gas
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val EV_CHARGING_STATION: Category = Category(
            geocodingCanonicalName = "charging station",
            sbsCanonicalName = "charging_station",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_ev_charging_station,
                R.drawable.mapbox_search_sdk_ic_category_charging_station
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val PARKING: Category = Category(
            geocodingCanonicalName = "parking",
            sbsCanonicalName = "parking_lot",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_parking,
                R.drawable.mapbox_search_sdk_ic_category_parking
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val BUS_STATION: Category = Category(
            geocodingCanonicalName = "bus station",
            sbsCanonicalName = "bus_station",
            Presentation(
                R.string.mapbox_search_sdk_category_name_bus_station,
                R.drawable.mapbox_search_sdk_ic_category_bus
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val RAILWAY_STATION: Category = Category(
            geocodingCanonicalName = "train station",
            sbsCanonicalName = "railway_station",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_train_station,
                R.drawable.mapbox_search_sdk_ic_category_train
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val SHOPPING_MALLS: Category = Category(
            geocodingCanonicalName = "mall",
            sbsCanonicalName = "shopping_mall",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_shopping_malls,
                R.drawable.mapbox_search_sdk_ic_category_shopping_mall
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val SUPERMARKET_GROCERY: Category = Category(
            geocodingCanonicalName = "grocery",
            sbsCanonicalName = "grocery",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_supermarket_grocery,
                R.drawable.mapbox_search_sdk_ic_category_grocery
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val CLOTHING_STORE: Category = Category(
            geocodingCanonicalName = "clothes",
            sbsCanonicalName = "clothing_store",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_clothing_shoes,
                R.drawable.mapbox_search_sdk_ic_category_clothing_store
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val PHARMACY: Category = Category(
            geocodingCanonicalName = "pharmacy",
            sbsCanonicalName = "pharmacy",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_pharmacy,
                R.drawable.mapbox_search_sdk_ic_category_pharmacy
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val MUSEUMS: Category = Category(
            geocodingCanonicalName = "museum",
            sbsCanonicalName = "museum",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_museums,
                R.drawable.mapbox_search_sdk_ic_category_museum
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val PARKS: Category = Category(
            geocodingCanonicalName = "park",
            sbsCanonicalName = "park",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_parks,
                R.drawable.mapbox_search_sdk_ic_category_park
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val MOVIE_THEATERS: Category = Category(
            geocodingCanonicalName = "cinema",
            sbsCanonicalName = "cinema",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_movie_theaters,
                R.drawable.mapbox_search_sdk_ic_category_cinema
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val GYM_FITNESS: Category = Category(
            geocodingCanonicalName = "fitness center",
            sbsCanonicalName = "fitness_center",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_gym_fitness,
                R.drawable.mapbox_search_sdk_ic_category_gym
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val NIGHT_CLUBS: Category = Category(
            geocodingCanonicalName = "nightclub",
            sbsCanonicalName = "nightclub",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_night_clubs,
                R.drawable.mapbox_search_sdk_ic_category_night_club
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val AUTO_REPAIR_MECHANIC: Category = Category(
            geocodingCanonicalName = "auto repair",
            sbsCanonicalName = "auto_repair",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_auto_repair_mechanic,
                R.drawable.mapbox_search_sdk_ic_category_car_repair
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val ATM: Category = Category(
            geocodingCanonicalName = "atm",
            sbsCanonicalName = "atm",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_atm,
                R.drawable.mapbox_search_sdk_ic_category_atm
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val HOSPITAL: Category = Category(
            geocodingCanonicalName = "hospital",
            sbsCanonicalName = "hospital",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_hospital,
                R.drawable.mapbox_search_sdk_ic_category_hospital
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        public val EMERGENCY_ROOM: Category = Category(
            geocodingCanonicalName = "emergency room",
            sbsCanonicalName = "emergency_room",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_emergency_room,
                R.drawable.mapbox_search_sdk_ic_category_emergency_room
            )
        )

        /**
         * List of all predefined categories.
         */
        @JvmField
        internal val PREDEFINED_CATEGORY_VALUES: List<Category> = listOf(
            RESTAURANTS,
            BARS,
            COFFEE_SHOP_CAFE,
            HOTEL,
            GAS_STATION,
            EV_CHARGING_STATION,
            PARKING,
            BUS_STATION,
            RAILWAY_STATION,
            SHOPPING_MALLS,
            SUPERMARKET_GROCERY,
            CLOTHING_STORE,
            PHARMACY,
            MUSEUMS,
            PARKS,
            MOVIE_THEATERS,
            GYM_FITNESS,
            NIGHT_CLUBS,
            AUTO_REPAIR_MECHANIC,
            ATM,
            HOSPITAL,
            EMERGENCY_ROOM,
        )

        private val MAPPING = PREDEFINED_CATEGORY_VALUES.map { it.geocodingCanonicalName to it }.toMap()
        private val SBS_MAPPING = PREDEFINED_CATEGORY_VALUES.map { it.sbsCanonicalName to it }.toMap()

        /**
         * Looks up for a [Category] by any of SBS or geocoding canonical names.
         */
        @JvmStatic
        public fun findByCanonicalName(name: String): Category? = MAPPING[name] ?: SBS_MAPPING[name]
    }
}
