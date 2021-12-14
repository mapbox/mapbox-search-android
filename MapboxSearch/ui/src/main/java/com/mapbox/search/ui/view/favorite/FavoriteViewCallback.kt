package com.mapbox.search.ui.view.favorite

import com.mapbox.search.ui.view.favorite.model.UserFavoriteAdapterItem

internal interface FavoriteViewCallback {

    fun onItemClick(userFavoriteItem: UserFavoriteAdapterItem.Favorite)

    fun onMoreActionsClick(userFavoriteItem: UserFavoriteAdapterItem.Favorite.Created)

    fun onAddFavoriteClick()
}
