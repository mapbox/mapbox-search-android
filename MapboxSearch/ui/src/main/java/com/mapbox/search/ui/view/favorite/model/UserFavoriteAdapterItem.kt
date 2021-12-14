package com.mapbox.search.ui.view.favorite.model

import androidx.annotation.DrawableRes
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.ui.view.favorite.FavoriteTemplate

internal sealed class UserFavoriteAdapterItem {

    sealed class Favorite : UserFavoriteAdapterItem() {

        abstract val id: String

        data class Created(

            val favorite: FavoriteRecord,

            /**
             * True only if there's a matched [FavoriteTemplate] provided by user
             */
            val isCreatedFromTemplate: Boolean,

            @DrawableRes val drawableId: Int,
        ) : Favorite() {

            override val id: String
                get() = favorite.id
        }

        data class Template(
            val template: FavoriteTemplate,
        ) : Favorite() {

            override val id: String
                get() = template.id
        }
    }

    object AddFavorite : UserFavoriteAdapterItem()

    object Loading : UserFavoriteAdapterItem()
}
