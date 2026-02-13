package com.mapbox.search

/**
 * Special annotation for marking properties/functions/classes as reserved for internal
 * or special use.
 *
 * @property flags List of flags that indicate particular sets of restricted functionality.
 */
internal annotation class Reserved(vararg val flags: Flags) {

    enum class Flags {

        /**
         * Functionality related to [Single Box Search][ApiType.SBS].
         */
        SBS,

        /**
         * Functionality related to [Search Box Search][ApiType.SEARCH_BOX].
         */
        SEARCH_BOX,
    }
}
