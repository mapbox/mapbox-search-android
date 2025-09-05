package com.mapbox.search.details

import android.os.Parcelable
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.AttributeSet
import com.mapbox.search.base.defaultLocaleLanguage
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.common.IsoLanguageCode
import com.mapbox.search.internal.bindgen.DetailsOptions
import com.mapbox.search.mapToCore
import kotlinx.parcelize.Parcelize

/**
 * Options, used for the [DetailsApi.retrieveDetails].
 */
@MapboxExperimental
@Parcelize
public class RetrieveDetailsOptions @JvmOverloads constructor(

    /**
     * Request additional metadata attributes besides the basic ones.
     */
    public val attributeSets: List<AttributeSet>? = null,

    /**
     * Specify the user’s language. This parameter controls the language of the text supplied in responses.
     * If language is not set explicitly, then language from default system locale will be used.
     */
    public val language: IsoLanguageCode = defaultLocaleLanguage(),

    /**
     * The ISO country code to requests a worldview for the location data,
     * if applicable data is available.
     * This parameters will only be applicable for Boundaries and Places feature types.
     */
    public val worldview: IsoCountryCode? = null,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RetrieveDetailsOptions

        if (attributeSets != other.attributeSets) return false
        if (language != other.language) return false
        if (worldview != other.worldview) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = attributeSets?.hashCode() ?: 0
        result = 31 * result + language.hashCode()
        result = 31 * result + (worldview?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "RetrieveDetailsOptions(" +
                "attributeSets=$attributeSets, " +
                "language=$language, " +
                "worldview=$worldview" +
                ")"
    }
}

@OptIn(MapboxExperimental::class)
@JvmSynthetic
internal fun RetrieveDetailsOptions.mapToCore(): DetailsOptions {
    return DetailsOptions(
        attributeSets?.fixedAttributesOption()?.map { it.mapToCore() },
        language.code,
        worldview?.code,
    )
}

@JvmSynthetic
private fun List<AttributeSet>.fixedAttributesOption(): List<AttributeSet> {
    return if (isNotEmpty() && !contains(AttributeSet.BASIC)) {
        this + AttributeSet.BASIC
    } else {
        this
    }
}
