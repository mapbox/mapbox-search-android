package com.mapbox.search

import com.mapbox.search.base.core.CoreSearchEngine
import com.mapbox.search.base.logger.loge

internal interface HighlightsEngine {
    fun getHighlights(name: String, query: String): List<Int>
}

internal class DefaultHighlightsEngine : HighlightsEngine {
    override fun getHighlights(name: String, query: String): List<Int> {
        return CoreSearchEngine.getHighlights(name, query)
    }
}

/**
 * Used to calculate which parts of exact search result name will be highlighted.
 *
 * To obtain [HighlightsCalculator] instance, please, use [MapboxSearchSdk.serviceProvider].
 */
public interface HighlightsCalculator {
    /**
     * @return list of intervals [start, end) for the string [name] that was matched by search [query].
     */
    public fun highlights(name: String, query: String): List<Pair<Int, Int>>
}

internal class HighlightsCalculatorImpl(private val highlightsEngine: HighlightsEngine) : HighlightsCalculator {

    constructor() : this(DefaultHighlightsEngine())

    override fun highlights(name: String, query: String): List<Pair<Int, Int>> {
        val nativeHighlights = highlightsEngine.getHighlights(name, query)
        return if (nativeHighlights.size % 2 == 0) {
            (nativeHighlights.indices step 2)
                .asSequence()
                .map { nativeHighlights[it] to nativeHighlights[it + 1] }
                .filter { (start, end) -> start < end && start in 0..name.length && end in 0..name.length }
                .toList()
        } else {
            loge("Invalid highlights $nativeHighlights for name = $name and query = $query")
            emptyList()
        }
    }
}
