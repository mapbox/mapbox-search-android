package com.mapbox.search.autofill

/**
 * Text query used for autofill forward geocoding.
 */
public class Query internal constructor(

    /**
     * Query text.
     */
    public val query: String
) {

    init {
        if (query.length < MIN_QUERY_LENGTH) {
            throw IllegalArgumentException("Query must be at least $MIN_QUERY_LENGTH characters long")
        }
    }

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Query

        if (query != other.query) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return query.hashCode()
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "Query(query='$query')"
    }

    /**
     * @suppress
     */
    public companion object {

        /**
         * Minimal allowed query length for address autofill.
         */
        public const val MIN_QUERY_LENGTH: Int = 3

        /**
         * Creates [Query] instance.
         * @param query Query text.
         * @return [Query] instance or `null` if [query]'s length is less than [MIN_QUERY_LENGTH].
         */
        @JvmStatic
        public fun create(query: String): Query? {
            return when {
                query.length >= MIN_QUERY_LENGTH -> Query(query)
                else -> null
            }
        }
    }
}
