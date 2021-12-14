package com.mapbox.search.ui.view.favorite.rename

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.ui.view.common.BaseSearchController
import com.mapbox.search.ui.view.favorite.rename.EditFavoriteView.Mode

internal class EditFavoriteViewController : BaseSearchController {

    override val cardDraggingAllowed: Boolean = false

    private val mode: Mode
    private val favorite: FavoriteRecord

    constructor(mode: Mode, favorite: FavoriteRecord) : super(bundleFavoriteAndAction(favorite, mode)) {
        this.mode = mode
        this.favorite = favorite
    }

    // Android Studio marks this constructor as unused, but it's needed for Controller
    constructor(favoriteBundle: Bundle) : super(favoriteBundle) {
        mode = favoriteBundle.getSerializable(BUNDLE_KEY_MODE) as Mode
        favorite = requireNotNull(favoriteBundle.getParcelable(BUNDLE_KEY_FAVORITE))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return EditFavoriteView(container.context).apply {
            setFavorite(mode, favorite)
            onCloseClickListener = {
                router.popCurrentController()
            }
            onDoneClickListener = {
                router.popToRoot()
            }
        }
    }

    private companion object {

        const val BUNDLE_KEY_MODE = "key.RenameFavoriteView.mode"
        const val BUNDLE_KEY_FAVORITE = "key.RenameFavoriteView.favorite"

        fun bundleFavoriteAndAction(favorite: FavoriteRecord, mode: Mode): Bundle {
            return Bundle().apply {
                putSerializable(BUNDLE_KEY_MODE, mode)
                putParcelable(BUNDLE_KEY_FAVORITE, favorite)
            }
        }
    }
}
