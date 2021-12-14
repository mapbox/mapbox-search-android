package com.mapbox.search.ui.view.favorite

import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.wrapWithSearchPopupDialogThemeOverlay
import com.mapbox.search.ui.view.favorite.model.UserFavoriteAdapterItem

internal class FavoriteActionsDialogFactory {

    fun create(
        context: Context,
        favoriteActionsCallback: FavoriteActionsCallback,
        favoriteItem: UserFavoriteAdapterItem.Favorite.Created
    ): BottomSheetDialog {
        val themedPopupContext = wrapWithSearchPopupDialogThemeOverlay(context)
        val dialogView = View.inflate(themedPopupContext, R.layout.mapbox_search_sdk_favorite_actions_layout, null)
        val dialog = BottomSheetDialog(themedPopupContext, R.style.MapboxSearchSdk_Internal_Theme_BottomSheetDialog)
        dialog.setContentView(dialogView)

        val deleteActionView = dialogView.findViewById<TextView>(R.id.favorite_delete_action)
        val renameActionView = dialogView.findViewById<TextView>(R.id.favorite_rename_layout_action)

        if (favoriteItem.isCreatedFromTemplate) {
            deleteActionView.setText(R.string.mapbox_search_sdk_favorite_action_remove_location)
        } else {
            deleteActionView.setText(R.string.mapbox_search_sdk_favorite_action_delete)
        }

        renameActionView.setOnClickListener {
            favoriteActionsCallback.onItemRenameAction(favoriteItem.favorite)
            dialog.dismiss()
        }

        deleteActionView.setOnClickListener {
            favoriteActionsCallback.onItemDeleteAction(favoriteItem.favorite)
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.favorite_edit_location_action).setOnClickListener {
            favoriteActionsCallback.onItemEditLocationAction(favoriteItem.favorite)
            dialog.dismiss()
        }
        return dialog
    }
}
