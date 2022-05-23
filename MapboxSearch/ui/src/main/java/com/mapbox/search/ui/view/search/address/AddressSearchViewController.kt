package com.mapbox.search.ui.view.search.address

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bluelinelabs.conductor.RouterTransaction
import com.mapbox.geojson.Point
import com.mapbox.search.AsyncOperationTask
import com.mapbox.search.CompletionCallback
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.ResponseInfo
import com.mapbox.search.common.logger.logd
import com.mapbox.search.common.logger.loge
import com.mapbox.search.common.throwDebug
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.extenstion.withVerticalAnimation
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.SearchMode
import com.mapbox.search.ui.view.common.BaseSearchController
import com.mapbox.search.ui.view.favorite.rename.EditFavoriteView
import com.mapbox.search.ui.view.favorite.rename.EditFavoriteViewController
import java.util.UUID

internal class AddressSearchViewController : BaseSearchController {

    var onFeedbackClickListener: ((ResponseInfo) -> Unit)? = null

    private val mode: AddressSearchView.Mode
    private val configuration: SearchBottomSheetView.Configuration

    override val cardDraggingAllowed = false

    private var updateFavoriteTask: AsyncOperationTask? = null

    constructor(
        mode: AddressSearchView.Mode,
        configuration: SearchBottomSheetView.Configuration
    ) : super(bundleSearchMode(mode, configuration)) {
        this.mode = mode
        this.configuration = configuration
    }

    // Android Studio marks this constructor as unused, but it's needed for Controller
    constructor(bundle: Bundle) : super(bundle) {
        mode = requireNotNull(bundle.getParcelable(BUNDLE_KEY_SEARCH_MODE))
        configuration = requireNotNull(bundle.getParcelable(BUNDLE_KEY_CONFIGURATION))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return AddressSearchView(container.context).apply {
            initialize(mode, configuration)

            onCloseClickListener = {
                router.popCurrentController()
            }

            searchResultListener = { searchResult ->
                when (mode) {
                    is AddressSearchView.Mode.AddFavorite -> openAddFavoriteScreen(searchResult)
                    is AddressSearchView.Mode.EditLocation -> changeLocationAndCloseScreen(
                        context = container.context,
                        mode = mode,
                        searchResult = searchResult
                    )
                }
            }

            onFeedbackClickListener = {
                this@AddressSearchViewController.onFeedbackClickListener?.invoke(it)
            }
        }
    }

    private fun changeLocationAndCloseScreen(
        context: Context,
        mode: AddressSearchView.Mode.EditLocation,
        searchResult: SearchResult,
    ) {
        val coordinate = searchResult.coordinate
        if (coordinate == null) {
            // TODO should we show error view?
            loge("Search result without coordinate")
            router.popToRoot()
            return
        }

        val newFavorite = when (mode) {
            is AddressSearchView.Mode.EditLocation.ForFavorite -> {
                mode.favorite.copy(
                    address = searchResult.address,
                    coordinate = coordinate,
                )
            }
            is AddressSearchView.Mode.EditLocation.ForTemplate -> {
                val template = mode.template
                createFavoriteRecord(coordinate, searchResult).copy(
                    id = template.id,
                    name = context.resources.getString(template.nameId)
                )
            }
        }

        updateFavoriteTask = MapboxSearchSdk.serviceProvider.favoritesDataProvider().upsert(
            newFavorite,
            object : CompletionCallback<Unit> {
                override fun onComplete(result: Unit) {
                    logd("Favorite record has been updated")
                    router.popToRoot()
                }

                override fun onError(e: Exception) {
                    view?.context.let { context ->
                        Toast.makeText(context, R.string.mapbox_search_sdk_favorite_update_error, Toast.LENGTH_SHORT).show()
                    }

                    throwDebug(e) {
                        "Unable to update favorite record"
                    }
                    router.popToRoot()
                }
            }
        )
    }

    override fun onNetworkModeChanged(searchMode: SearchMode) {
        (view as? AddressSearchView)?.searchMode = searchMode
    }

    private fun openAddFavoriteScreen(searchResult: SearchResult) {
        val coordinate = searchResult.coordinate
        if (coordinate == null) {
            // TODO should we show error view?
            loge("Search result without coordinate")
            router.popToRoot()
            return
        }

        val record = createFavoriteRecord(coordinate, searchResult)

        val transaction = RouterTransaction
            .with(EditFavoriteViewController(EditFavoriteView.Mode.ADD, record))
            .withVerticalAnimation(animationBackgroundRes = R.drawable.mapbox_search_sdk_search_view_background)
        router.pushController(transaction)
    }

    override fun onDetach(view: View) {
        updateFavoriteTask?.cancel()
        updateFavoriteTask = null
        super.onDetach(view)
    }

    private companion object {

        const val BUNDLE_KEY_SEARCH_MODE = "key.AddressSearchViewController.Mode"
        const val BUNDLE_KEY_CONFIGURATION = "key.AddressSearchViewController.Configuration"

        fun bundleSearchMode(
            mode: AddressSearchView.Mode,
            configuration: SearchBottomSheetView.Configuration
        ): Bundle {
            return Bundle().apply {
                putParcelable(BUNDLE_KEY_SEARCH_MODE, mode)
                putParcelable(BUNDLE_KEY_CONFIGURATION, configuration)
            }
        }

        fun createFavoriteRecord(coordinate: Point, searchResult: SearchResult): FavoriteRecord {
            return FavoriteRecord(
                id = "${searchResult.id} - ${UUID.randomUUID()}",
                name = searchResult.name,
                coordinate = coordinate,
                descriptionText = searchResult.descriptionText,
                address = searchResult.address,
                type = searchResult.types.first(),
                makiIcon = searchResult.makiIcon,
                categories = searchResult.categories,
                routablePoints = searchResult.routablePoints,
                metadata = searchResult.metadata
            )
        }
    }
}
