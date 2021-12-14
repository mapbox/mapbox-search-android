package com.mapbox.search.ui.view.favorite

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.SearchEntityPresentation
import com.mapbox.search.ui.utils.StringToLongIdMapper
import com.mapbox.search.ui.utils.adapter.BaseRecyclerViewAdapter
import com.mapbox.search.ui.utils.adapter.BaseViewHolder
import com.mapbox.search.ui.utils.extenstion.getDrawableCompat
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.utils.extenstion.setTextAndHideIfBlank
import com.mapbox.search.ui.utils.extenstion.setTintCompat
import com.mapbox.search.ui.view.favorite.model.UserFavoriteAdapterItem

internal class FavoriteAdapter : BaseRecyclerViewAdapter<UserFavoriteAdapterItem, BaseViewHolder<UserFavoriteAdapterItem>>() {

    private val stringToLongIdMapper = StringToLongIdMapper()

    var favoriteViewCallback: FavoriteViewCallback? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        val stringId = when (val item = items[position]) {
            is UserFavoriteAdapterItem.Favorite -> "Favorite item ${item.id}"
            is UserFavoriteAdapterItem.AddFavorite -> "AddFavoriteItem"
            is UserFavoriteAdapterItem.Loading -> "Loading"
        }
        return stringToLongIdMapper.getId(stringId)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<UserFavoriteAdapterItem> {
        return when (viewType) {
            ITEM_VIEW_TYPE_FAVORITE_ITEM -> FavoriteItemViewHolder(parent)
            ITEM_VIEW_TYPE_ADD_FAVORITE_ACTION -> AddFavoriteViewHolder(parent)
            ITEM_VIEW_TYPE_LOADING -> FavoriteLoadingViewHolder(parent)
            else -> error("Unprocessed view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is UserFavoriteAdapterItem.Favorite -> ITEM_VIEW_TYPE_FAVORITE_ITEM
            is UserFavoriteAdapterItem.AddFavorite -> ITEM_VIEW_TYPE_ADD_FAVORITE_ACTION
            is UserFavoriteAdapterItem.Loading -> ITEM_VIEW_TYPE_LOADING
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<UserFavoriteAdapterItem>, position: Int) {
        holder.bind(items[position])
    }

    private inner class FavoriteItemViewHolder(parent: ViewGroup) :
        BaseViewHolder<UserFavoriteAdapterItem>(
            parent, R.layout.mapbox_search_sdk_favorite_item_layout
        ) {

        private val iconView: ImageView = findViewById(R.id.favorite_icon)
        private val nameView: TextView = findViewById(R.id.favorite_name)
        private val addressView: TextView = findViewById(R.id.favorite_address)
        private val moreActions: View = findViewById(R.id.favourite_more_actions)

        override fun bind(item: UserFavoriteAdapterItem) {
            val userFavoriteItem = item as UserFavoriteAdapterItem.Favorite

            when (userFavoriteItem) {
                is UserFavoriteAdapterItem.Favorite.Created -> bindForFavoriteRecord(userFavoriteItem)
                is UserFavoriteAdapterItem.Favorite.Template -> bindForTemplate(userFavoriteItem)
            }

            itemView.setOnClickListener {
                favoriteViewCallback?.onItemClick(userFavoriteItem)
            }
        }

        private fun bindForFavoriteRecord(item: UserFavoriteAdapterItem.Favorite.Created) {
            val favorite = item.favorite

            val favoriteIconId = if (item.isCreatedFromTemplate) {
                item.drawableId
            } else {
                SearchEntityPresentation.pickEntityDrawable(
                    favorite.makiIcon,
                    favorite.categories,
                    R.drawable.mapbox_search_sdk_ic_search_result_place
                )
            }

            iconView.setImageDrawable(
                context.getDrawableCompat(favoriteIconId)
                    ?.setTintCompat(context.resolveAttrOrThrow(R.attr.mapboxSearchSdkIconTintColor))
            )

            nameView.text = favorite.name
            addressView.setTextAndHideIfBlank(
                favorite.address?.formattedAddress(SearchAddress.FormatStyle.Long) ?: favorite.descriptionText
            )
            moreActions.isVisible = true
            moreActions.setOnClickListener {
                favoriteViewCallback?.onMoreActionsClick(item)
            }
        }

        private fun bindForTemplate(item: UserFavoriteAdapterItem.Favorite.Template) {
            val template = item.template

            iconView.setImageDrawable(
                context.getDrawableCompat(template.resourceId)
                    ?.setTintCompat(context.resolveAttrOrThrow(R.attr.mapboxSearchSdkIconTintColor))
            )

            nameView.text = resources.getString(template.nameId)
            moreActions.isVisible = false

            addressView.setTextAndHideIfBlank(context.getString(R.string.mapbox_search_sdk_favorite_tap_to_add))
        }
    }

    private inner class AddFavoriteViewHolder(parent: ViewGroup) : BaseViewHolder<UserFavoriteAdapterItem>(
        parent,
        R.layout.mapbox_search_sdk_add_favorite_item_layout
    ) {

        override fun bind(item: UserFavoriteAdapterItem) {
            itemView.setOnClickListener {
                favoriteViewCallback?.onAddFavoriteClick()
            }
        }
    }

    private inner class FavoriteLoadingViewHolder(parent: ViewGroup) : BaseViewHolder<UserFavoriteAdapterItem>(
        parent, R.layout.mapbox_search_sdk_loading_item_layout
    ) {
        override fun bind(item: UserFavoriteAdapterItem) {
            // Nothing to bind
        }
    }

    companion object {
        const val ITEM_VIEW_TYPE_FAVORITE_ITEM = 0
        const val ITEM_VIEW_TYPE_ADD_FAVORITE_ACTION = 1
        const val ITEM_VIEW_TYPE_LOADING = 2
    }
}
