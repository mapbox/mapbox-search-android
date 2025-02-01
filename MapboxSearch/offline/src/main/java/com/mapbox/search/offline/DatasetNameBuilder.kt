package com.mapbox.search.offline

internal object DatasetNameBuilder {

    private val REGEX_LANGUAGE_CODE = "^[a-z]{2}\$".toRegex(RegexOption.IGNORE_CASE)
    private val REGEX_COUNTRY_CODE = "^[a-z]{2}\$".toRegex(RegexOption.IGNORE_CASE)

    fun buildDatasetName(
        dataset: String,
        language: String?,
        worldview: String? = null,
    ): String {
        require(language != null || worldview == null) { "Language must be present when worldview is specified" }

        language?.let {
            require(REGEX_LANGUAGE_CODE.matches(it)) { "Language should be an ISO 639-1 code" }
        }

        worldview?.let {
            require(REGEX_COUNTRY_CODE.matches(it)) { "Worldview should be an ISO 3166 alpha-2 code" }
        }

        return buildString {
            append(dataset)
            language?.let { append("_${it.lowercase()}") }
            worldview?.let { append("-${it.lowercase()}") }
        }
    }
}
