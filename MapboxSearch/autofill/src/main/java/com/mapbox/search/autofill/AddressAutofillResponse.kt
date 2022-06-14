package com.mapbox.search.autofill

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Wrapper around the [AddressAutofill] response, which is either success or error.
 */
public sealed class AddressAutofillResponse : Parcelable {

    /**
     * Successful [AddressAutofill] result.
     */
    @Parcelize
    public class Suggestions(
        /**
         * List of [AddressAutofillSuggestion]. Might be empty.
         */
        public val suggestions: List<AddressAutofillSuggestion>
    ) : AddressAutofillResponse() {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Suggestions

            if (suggestions != other.suggestions) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return suggestions.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "Suggestions(suggestion=$suggestions)"
        }
    }

    /**
     * Error [AddressAutofill] result.
     */
    @Parcelize
    public class Error(
        /**
         * Exception, occurred during request.
         */
        public val error: Exception
    ) : AddressAutofillResponse() {

        /**
         * @suppress
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Error

            if (error != other.error) return false

            return true
        }

        /**
         * @suppress
         */
        override fun hashCode(): Int {
            return error.hashCode()
        }

        /**
         * @suppress
         */
        override fun toString(): String {
            return "Error(error=$error)"
        }
    }
}
