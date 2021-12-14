package com.mapbox.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Bunch of options used by [SearchEngine.select] function.
 * @see SearchEngine
 */
@Parcelize
public class SelectOptions public constructor(

    /**
     * Flag to control whether search result should be added to history automatically. Defaults to true.
     */
    public val addResultToHistory: Boolean = true,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SelectOptions

        if (addResultToHistory != other.addResultToHistory) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return addResultToHistory.hashCode()
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SelectOptions(" +
            "addResultToHistory=$addResultToHistory" +
            ")"
    }
}
