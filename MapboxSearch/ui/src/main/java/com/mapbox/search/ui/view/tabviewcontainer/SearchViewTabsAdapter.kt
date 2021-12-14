package com.mapbox.search.ui.view.tabviewcontainer

import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.OffsetItemDecoration
import com.mapbox.search.ui.utils.adapter.BaseViewHolder
import com.mapbox.search.ui.view.category.CategoryAdapter
import com.mapbox.search.ui.view.category.CategoryEntry
import com.mapbox.search.ui.view.category.CategoryViewCallback
import com.mapbox.search.ui.view.favorite.FavoriteAdapter
import com.mapbox.search.ui.view.favorite.FavoriteViewCallback
import com.mapbox.search.ui.view.favorite.model.UserFavoriteAdapterItem

internal class SearchViewTabsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    internal var favoriteViewCallback: FavoriteViewCallback? = null
    internal var categoryViewCallback: CategoryViewCallback? = null

    private var categories = Item.Categories(emptyList())
    private var favorites = Item.Favorites.Data(emptyList())

    private val innerCategoryViewCallback = object : CategoryViewCallback {
        override fun onItemClick(categoryEntry: CategoryEntry) {
            categoryViewCallback?.onItemClick(categoryEntry)
        }
    }

    private val innerFavoriteViewCallback = object : FavoriteViewCallback {
        override fun onItemClick(userFavoriteItem: UserFavoriteAdapterItem.Favorite) {
            favoriteViewCallback?.onItemClick(userFavoriteItem)
        }

        override fun onMoreActionsClick(userFavoriteItem: UserFavoriteAdapterItem.Favorite.Created) {
            favoriteViewCallback?.onMoreActionsClick(userFavoriteItem)
        }

        override fun onAddFavoriteClick() {
            favoriteViewCallback?.onAddFavoriteClick()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_POSITION_CATEGORIES -> CategoriesViewHolder(parent, innerCategoryViewCallback)
            ITEM_POSITION_FAVORITES -> FavoritesViewHolder(parent, innerFavoriteViewCallback)
            else -> throw IllegalStateException("Unknown item type: $viewType")
        }
    }

    override fun getItemCount() = ITEMS_SIZE

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Item.Categories -> {
                holder as CategoriesViewHolder
                holder.bind(item.categories)
            }
            is Item.Favorites -> {
                holder as FavoritesViewHolder
                holder.bind(item.favorites)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun getItem(position: Int): Item {
        return when (position) {
            ITEM_POSITION_CATEGORIES -> categories
            ITEM_POSITION_FAVORITES -> favorites
            else -> throw IllegalArgumentException("Illegal items position $position")
        }
    }

    fun setCategories(categories: List<CategoryEntry>) {
        this.categories = Item.Categories(categories)
        notifyItemChanged(ITEM_POSITION_CATEGORIES)
    }

    fun setFavorites(favorites: List<UserFavoriteAdapterItem>) {
        this.favorites = Item.Favorites.Data(favorites)
        notifyItemChanged(ITEM_POSITION_FAVORITES)
    }

    internal sealed class Item(@StringRes val tabTitleRes: Int) {

        data class Categories(val categories: List<CategoryEntry>) : Item(
            R.string.mapbox_search_sdk_search_tab_view_container_category_page
        )

        sealed class Favorites : Item(R.string.mapbox_search_sdk_search_tab_view_container_favorite_page) {

            abstract val favorites: List<UserFavoriteAdapterItem>

            data class Data(override val favorites: List<UserFavoriteAdapterItem>) : Favorites()

            object Loading : Favorites() {
                override val favorites: List<UserFavoriteAdapterItem> = listOf(UserFavoriteAdapterItem.Loading)
            }
        }
    }

    private class CategoriesViewHolder(
        parent: ViewGroup,
        private val callback: CategoryViewCallback
    ) : BaseViewHolder<List<CategoryEntry>>(parent, R.layout.mapbox_search_sdk_search_tab_recycler) {

        private val recyclerView = findViewById<RecyclerView>(R.id.recycler)
        private var categoryAdapter = CategoryAdapter().apply {
            categoryViewCallback = callback
        }

        init {
            with(recyclerView) {
                id = R.id.search_tab_category_recycler
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = categoryAdapter
                itemAnimator = null
                setHasFixedSize(true)

                addItemDecoration(OffsetItemDecoration(context))
            }
        }

        override fun bind(item: List<CategoryEntry>) {
            categoryAdapter.items = item
        }
    }

    private class FavoritesViewHolder(
        parent: ViewGroup,
        private val callback: FavoriteViewCallback
    ) : BaseViewHolder<List<UserFavoriteAdapterItem>>(parent, R.layout.mapbox_search_sdk_search_tab_recycler) {

        private val recyclerView = findViewById<RecyclerView>(R.id.recycler)
        private var favoriteAdapter = FavoriteAdapter().apply {
            favoriteViewCallback = callback
        }

        init {
            with(recyclerView) {
                id = R.id.search_tab_favorites_recycler
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = favoriteAdapter
                setHasFixedSize(true)

                addItemDecoration(OffsetItemDecoration(context))
            }
        }

        override fun bind(item: List<UserFavoriteAdapterItem>) {
            favoriteAdapter.items = item
        }
    }

    private companion object {
        const val ITEMS_SIZE = 2
        const val ITEM_POSITION_CATEGORIES = 0
        const val ITEM_POSITION_FAVORITES = 1
    }
}
