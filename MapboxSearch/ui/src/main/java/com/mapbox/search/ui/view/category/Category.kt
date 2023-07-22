package com.mapbox.search.ui.view.category

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mapbox.search.ui.R
import kotlinx.parcelize.Parcelize

/**
 * Represents a category of POI. This class also defines a list of commonly used categories.
 * Internal: Search Box category canonical names https://github.com/mapbox/tag-to-category/blob/master/reference/categories.csv
 */
@Suppress("LargeClass")
@Parcelize
internal class Category(

    /**
     * Category canonical name used in Geocoding API.
     * See [List of the most common POI categories](https://docs.mapbox.com/api/search/geocoding/#point-of-interest-category-coverage).
     */
    val geocodingName: String,

    /**
     * Category canonical name used in Search Box API.
     */
    val searchBoxName: String,

    /**
     * Category visual presentation.
     */
    val presentation: Presentation
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Category

        if (geocodingName != other.geocodingName) return false
        if (searchBoxName != other.searchBoxName) return false
        if (presentation != other.presentation) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = geocodingName.hashCode()
        result = 31 * result + searchBoxName.hashCode()
        result = 31 * result + presentation.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "Category(" +
                "geocodingName='$geocodingName', " +
                "searchBoxName='$searchBoxName', " +
                "presentation=$presentation" +
                ")"
    }

    /**
     * Represents a category visual presentation like name and icon.
     * See [MAKI icons for common points of interest](https://labs.mapbox.com/maki-icons/).
     */
    @Parcelize
    class Presentation(

        /**
         * The resource identifier of the string resource to be displayed as category name.
         */
        @StringRes val displayName: Int,

        /**
         * The resource identifier of the drawable to be displayed as category icon.
         */
        @DrawableRes val icon: Int,
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
     * Companion object.
     */
    companion object {

        /**
         * Predefined category constant.
         */
        @JvmField
        val RESTAURANTS: Category = Category(
            geocodingName = "restaurant",
            searchBoxName = "restaurant",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_restaurants,
                R.drawable.mapbox_search_sdk_ic_category_restaurant
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val BARS: Category = Category(
            geocodingName = "bar",
            searchBoxName = "bar",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_bars,
                R.drawable.mapbox_search_sdk_ic_category_bar
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val COFFEE_SHOP_CAFE: Category = Category(
            geocodingName = "cafe",
            searchBoxName = "cafe",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_coffee_shop_cafe,
                R.drawable.mapbox_search_sdk_ic_category_cafe
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val HOTEL: Category = Category(
            geocodingName = "hotel",
            searchBoxName = "hotel",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_hotel,
                R.drawable.mapbox_search_sdk_ic_category_hotel
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val GAS_STATION: Category = Category(
            geocodingName = "fuel",
            searchBoxName = "gas_station",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_gas_station,
                R.drawable.mapbox_search_sdk_ic_category_gas
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val EV_CHARGING_STATION: Category = Category(
            geocodingName = "charging station",
            searchBoxName = "charging_station",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_ev_charging_station,
                R.drawable.mapbox_search_sdk_ic_category_charging_station
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val PARKING: Category = Category(
            geocodingName = "parking",
            searchBoxName = "parking_lot",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_parking,
                R.drawable.mapbox_search_sdk_ic_category_parking
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val BUS_STATION: Category = Category(
            geocodingName = "bus station",
            searchBoxName = "bus_station",
            Presentation(
                R.string.mapbox_search_sdk_category_name_bus_station,
                R.drawable.mapbox_search_sdk_ic_category_bus
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val RAILWAY_STATION: Category = Category(
            geocodingName = "train station",
            searchBoxName = "railway_station",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_train_station,
                R.drawable.mapbox_search_sdk_ic_category_train
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val SHOPPING_MALLS: Category = Category(
            geocodingName = "mall",
            searchBoxName = "shopping_mall",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_shopping_malls,
                R.drawable.mapbox_search_sdk_ic_category_shopping_mall
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val SUPERMARKET_GROCERY: Category = Category(
            geocodingName = "grocery",
            searchBoxName = "grocery",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_supermarket_grocery,
                R.drawable.mapbox_search_sdk_ic_category_grocery
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val CLOTHING_STORE: Category = Category(
            geocodingName = "clothes",
            searchBoxName = "clothing_store",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_clothing_shoes,
                R.drawable.mapbox_search_sdk_ic_category_clothing_store
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val PHARMACY: Category = Category(
            geocodingName = "pharmacy",
            searchBoxName = "pharmacy",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_pharmacy,
                R.drawable.mapbox_search_sdk_ic_category_pharmacy
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val MUSEUMS: Category = Category(
            geocodingName = "museum",
            searchBoxName = "museum",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_museums,
                R.drawable.mapbox_search_sdk_ic_category_museum
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val PARKS: Category = Category(
            geocodingName = "park",
            searchBoxName = "park",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_parks,
                R.drawable.mapbox_search_sdk_ic_category_park
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val MOVIE_THEATERS: Category = Category(
            geocodingName = "cinema",
            searchBoxName = "cinema",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_movie_theaters,
                R.drawable.mapbox_search_sdk_ic_category_cinema
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val GYM_FITNESS: Category = Category(
            geocodingName = "fitness center",
            searchBoxName = "fitness_center",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_gym_fitness,
                R.drawable.mapbox_search_sdk_ic_category_gym
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val NIGHT_CLUBS: Category = Category(
            geocodingName = "nightclub",
            searchBoxName = "nightclub",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_night_clubs,
                R.drawable.mapbox_search_sdk_ic_category_night_club
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val AUTO_REPAIR_MECHANIC: Category = Category(
            geocodingName = "auto repair",
            searchBoxName = "auto_repair",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_auto_repair_mechanic,
                R.drawable.mapbox_search_sdk_ic_category_car_repair
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val ATM: Category = Category(
            geocodingName = "atm",
            searchBoxName = "atm",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_atm,
                R.drawable.mapbox_search_sdk_ic_category_atm
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val HOSPITAL: Category = Category(
            geocodingName = "hospital",
            searchBoxName = "hospital",
            presentation = Presentation(
                R.string.mapbox_search_sdk_category_name_hospital,
                R.drawable.mapbox_search_sdk_ic_category_hospital
            )
        )

        /**
         * Predefined category constant.
         */
        @JvmField
        val EMERGENCY_ROOM: Category = Category(
            geocodingName = "emergency room",
            searchBoxName = "emergency_room",
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

        private val MAPPING = PREDEFINED_CATEGORY_VALUES.associateBy { it.geocodingName }
        private val SEARCH_BOX_MAPPING = PREDEFINED_CATEGORY_VALUES.associateBy { it.searchBoxName }

        /**
         * Looks up for a [Category] by any of Search Box or geocoding canonical names.
         */
        @JvmStatic
        fun findByCanonicalName(name: String): Category? = MAPPING[name] ?: SEARCH_BOX_MAPPING[name]
    }
}
