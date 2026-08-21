package com.mapbox.search

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.Reserved.Flags.SEARCH_BOX
import com.mapbox.search.base.core.CoreRetrieveOptions
import kotlinx.parcelize.Parcelize
import java.util.Objects

/**
 * Bunch of options used by the [SearchEngine.retrieve] function.
 *
 * Note: this class is only supported for [ApiType.SEARCH_BOX].
 *
 * @see SearchEngine.retrieve
 */
@MapboxExperimental
@Reserved(SEARCH_BOX)
@Parcelize
public class RetrieveOptions @JvmOverloads public constructor(

    /**
     * Besides the basic metadata attributes, developers can request additional
     * attributes by setting attribute_sets parameter with attribute set values,
     * for example &attribute_sets=basic,photos,visit.
     * The requested metadata will be provided in metadata object in the response.
     */
    public val attributeSets: List<AttributeSet>? = null,

    /**
     * Non-verified query parameters, that will be added to the server API request.
     *
     * Note: Incorrect usage of this parameter may cause failed or malformed response.
     * Do not use it without SDK developers agreement.
     */
    public val unsafeParameters: Map<String, String>? = null,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RetrieveOptions

        if (attributeSets != other.attributeSets) return false
        if (unsafeParameters != other.unsafeParameters) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return Objects.hash(attributeSets, unsafeParameters)
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "RetrieveOptions(" +
                "attributeSets=$attributeSets, " +
                "unsafeParameters=$unsafeParameters" +
                ")"
    }
}

@OptIn(MapboxExperimental::class)
@JvmSynthetic
internal fun RetrieveOptions.mapToCore(): CoreRetrieveOptions = CoreRetrieveOptions(
    attributeSets?.map { it.mapToCore() },
    unsafeParameters?.let { (it as? HashMap) ?: HashMap(it) },
)
