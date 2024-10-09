package com.mapbox.search

import android.os.Parcelable
import com.mapbox.search.base.core.CoreRetrieveOptions
import kotlinx.parcelize.Parcelize
import java.util.Objects

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

    /**
     * Besides the basic metadata attributes, developers can request additional
     * attributes by setting attribute_sets parameter with attribute set values,
     * for example &attribute_sets=basic,photos,visit.
     * The requested metadata will be provided in metadata object in the response.
     *
     * Note: this method is only used supported for [ApiType.SEARCH_BOX]
     */
    @Reserved(Reserved.Flags.SEARCH_BOX)
    public val attributeSets: List<AttributeSet>? = null,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SelectOptions

        if (addResultToHistory != other.addResultToHistory) return false
        if (attributeSets != other.attributeSets) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return Objects.hash(addResultToHistory, attributeSets)
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SelectOptions(" +
                "addResultToHistory=$addResultToHistory" +
                "attributeSets=$attributeSets" +
                ")"
    }
}

@JvmSynthetic
internal fun SelectOptions.mapToCore(): CoreRetrieveOptions = CoreRetrieveOptions(
    attributeSets?.map { it.mapToCore() }
)
