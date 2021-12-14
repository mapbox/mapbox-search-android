package com.mapbox.search.ui.view.favorite

import com.mapbox.search.record.FavoriteRecord

internal interface FavoriteActionsCallback {

    fun onItemRenameAction(userFavorite: FavoriteRecord)

    fun onItemDeleteAction(userFavorite: FavoriteRecord)

    fun onItemEditLocationAction(userFavorite: FavoriteRecord)
}
