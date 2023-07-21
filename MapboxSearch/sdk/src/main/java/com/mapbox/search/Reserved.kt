package com.mapbox.search

/**
 * Special annotation for marking properties/functions/classes as reserved for internal
 * or special use.
 *
 * @param flags list of flags, that indicates particular sets of restricted functionality.
 */
internal annotation class Reserved(vararg val flags: Flags) {

    enum class Flags {

        /**
         * Functionality related to [Single Box Search][ApiType.SearchBox].
         */
        SBS,
    }
}
