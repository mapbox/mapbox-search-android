package com.mapbox.search.ui.view.favorite.model

import com.mapbox.search.common.assertDebug
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.ui.R
import com.mapbox.search.ui.view.favorite.FavoriteTemplate

internal class UserFavoriteAdapterItemsCreator {

    fun createItems(
        favorites: List<FavoriteRecord>,
        templates: List<FavoriteTemplate>
    ): List<UserFavoriteAdapterItem> {
        return mutableListOf<UserFavoriteAdapterItem>().apply {
            addAll(
                createItemsFromTemplates(favorites, templates)
            )

            addAll(
                createItemsFromFavorites(favorites, templates)
            )

            add(UserFavoriteAdapterItem.AddFavorite)
        }
    }

    private fun createItemsFromTemplates(
        favorites: List<FavoriteRecord>,
        templates: List<FavoriteTemplate>
    ): List<UserFavoriteAdapterItem> {
        val favoritesMap = favorites.groupBy { it.id }

        return templates.map { template ->
            val groupedFavorites = favoritesMap[template.id]
            assertDebug(groupedFavorites.isNullOrEmpty() || groupedFavorites.size == 1) {
                "Favorites list with duplicated ids"
            }

            if (groupedFavorites.isNullOrEmpty()) {
                UserFavoriteAdapterItem.Favorite.Template(
                    template = template
                )
            } else {
                UserFavoriteAdapterItem.Favorite.Created(
                    favorite = groupedFavorites.first(),
                    isCreatedFromTemplate = true,
                    drawableId = template.resourceId
                )
            }
        }
    }

    private fun createItemsFromFavorites(
        favorites: List<FavoriteRecord>,
        templates: List<FavoriteTemplate>
    ): List<UserFavoriteAdapterItem> {
        // Favorites with matched by id templates are processed in createItemsFromTemplates()
        val templateIds = templates.map { it.id }.toHashSet()
        return favorites.filter {
            !templateIds.contains(it.id)
        }.map {
            UserFavoriteAdapterItem.Favorite.Created(
                favorite = it,
                isCreatedFromTemplate = false,
                drawableId = R.drawable.mapbox_search_sdk_ic_favorite_uncategorized
            )
        }
    }
}
