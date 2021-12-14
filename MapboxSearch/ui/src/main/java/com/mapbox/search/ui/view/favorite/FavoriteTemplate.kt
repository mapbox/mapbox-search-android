package com.mapbox.search.ui.view.favorite

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

/**
 * Template for favorite object. Used to add predefined favorites object, such as Home or Work, that user can only edit but can not delete.
 */
@Parcelize
public class FavoriteTemplate(

    /**
     * Unique template id.
     */
    public val id: String,

    /**
     * String resource id for template name.
     */
    @StringRes
    public val nameId: Int,

    /**
     * Drawable resource id for template icon.
     */
    @DrawableRes
    public val resourceId: Int
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FavoriteTemplate

        if (id != other.id) return false
        if (nameId != other.nameId) return false
        if (resourceId != other.resourceId) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + nameId
        result = 31 * result + resourceId
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "FavoriteTemplate(id='$id', nameId=$nameId, resourceId=$resourceId)"
    }

    /**
     * @suppress
     */
    public companion object {

        /**
         * Id for HOME predefined favorite.
         */
        public const val HOME_DEFAULT_TEMPLATE_ID: String = "HOME_DEFAULT_TEMPLATE_ID"

        /**
         * Id for WORK predefined favorite.
         */
        public const val WORK_DEFAULT_TEMPLATE_ID: String = "WORK_DEFAULT_TEMPLATE_ID"
    }
}
