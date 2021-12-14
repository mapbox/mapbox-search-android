package com.mapbox.search.ui.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RouterTransaction
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchOptions
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.DebounceClickListener
import com.mapbox.search.ui.utils.extenstion.withHorizontalAnimation
import com.mapbox.search.ui.utils.extenstion.withVerticalAnimation
import com.mapbox.search.ui.view.CardStateListener
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.SearchMode
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.category.CategorySuggestionSearchViewController
import com.mapbox.search.ui.view.common.BaseSearchController
import com.mapbox.search.ui.view.favorite.model.UserFavoriteAdapterItem
import com.mapbox.search.ui.view.favorite.rename.EditFavoriteView
import com.mapbox.search.ui.view.favorite.rename.EditFavoriteViewController
import com.mapbox.search.ui.view.search.address.AddressSearchView
import com.mapbox.search.ui.view.search.address.AddressSearchViewController

internal class MainScreenViewController : BaseSearchController {

    override val cardDraggingAllowed: Boolean = true

    var onCategoryClickListener: ((Category) -> Unit)? = null
    var onSearchResultClickListener: ((SearchResult, ResponseInfo) -> Unit)? = null
    var onHistoryItemClickListener: ((HistoryRecord) -> Unit)? = null
    var onFavoriteClickListener: ((FavoriteRecord) -> Unit)? = null
    var cardStateListener: CardStateListener? = null
    var onFeedbackClickListener: ((ResponseInfo) -> Unit)? = null

    var configuration: SearchBottomSheetView.Configuration
        set(value) {
            field = value
            mainScreenView()?.initializeSearch(value)
        }

    var searchOptions: SearchOptions
        set(value) {
            field = value
            mainScreenView()?.searchOptions = value
        }

    private var isIgnoreNextRestoreInstanceState = false

    init {
        retainViewMode = RetainViewMode.RETAIN_DETACH
    }

    constructor(
        configuration: SearchBottomSheetView.Configuration,
        searchOptions: SearchOptions
    ) : super(bundleConfig(configuration, searchOptions)) {
        this.configuration = configuration
        this.searchOptions = searchOptions
    }

    // Android Studio marks this constructor as unused, but it's needed for Controller
    constructor(configBundle: Bundle) : super(configBundle) {
        configuration = requireNotNull(restoreConfigFromBundle(configBundle))
        searchOptions = requireNotNull(restoreSearchOptionsFromBundle(configBundle))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return MainScreenView(container.context).apply {
            id = R.id.main_screen_view
            initializeSearch(configuration)

            ignoreNextRestoreInstanceState = isIgnoreNextRestoreInstanceState
            isIgnoreNextRestoreInstanceState = false

            searchOptions = this@MainScreenViewController.searchOptions
            bindActions(this)
        }
    }

    private fun bindActions(view: MainScreenView) {
        view.onEditLocationClickListener = {
            openSearchScreen(AddressSearchView.Mode.EditLocation.ForFavorite(it, searchOptions))
        }
        view.onCategoryClickListener = { onCategoryClickListener?.invoke(it) }
        view.onFavoriteAddListener = { openSearchScreen(AddressSearchView.Mode.AddFavorite(searchOptions)) }
        view.onFavoriteRenameListener = { openFavoriteRenameScreen(it) }
        view.onSearchResultClickListener = { searchResult, responseInfo ->
            onSearchResultClickListener?.invoke(searchResult, responseInfo)
        }
        view.onCategorySuggestionClickListener = object : DebounceClickListener<SearchSuggestion>() {
            // We use DebounceClickListener only for category suggestion clicks because
            // category search opens in a separate view and it takes some time to animate view transitions
            // what's enough for the user to click multiple times and open multiple view instances.
            override fun onClick(arg: SearchSuggestion) {
                openCategorySuggestionSearchScreen(arg)
            }
        }
        view.onHistoryItemClickListener = { onHistoryItemClickListener?.invoke(it) }
        view.onFavoriteClickListener = { userFavoriteItem ->
            when (userFavoriteItem) {
                is UserFavoriteAdapterItem.Favorite.Created -> {
                    onFavoriteClickListener?.invoke(userFavoriteItem.favorite)
                }
                is UserFavoriteAdapterItem.Favorite.Template -> {
                    openSearchScreen(
                        AddressSearchView.Mode.EditLocation.ForTemplate(userFavoriteItem.template, searchOptions)
                    )
                }
            }
        }
        view.onFeedbackClickListener = {
            onFeedbackClickListener?.invoke(it)
        }
        view.cardStateListener = object : CardStateListener {
            override fun onExpandCard() {
                cardStateListener?.onExpandCard()
            }

            override fun onCollapseCard() {
                cardStateListener?.onCollapseCard()
            }
        }
    }

    fun mainScreenView() = view as? MainScreenView

    override fun onNetworkModeChanged(searchMode: SearchMode) {
        mainScreenView()?.searchMode = searchMode
    }

    fun ignoreNextRestoreInstanceState() {
        val mainScreenView = mainScreenView()
        if (mainScreenView != null) {
            mainScreenView.ignoreNextRestoreInstanceState = true
            isIgnoreNextRestoreInstanceState = false
        } else {
            isIgnoreNextRestoreInstanceState = true
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        findSearchBottomSheetView(view)?.let { bottomSheet ->
            mainScreenView()?.onAttachedToBottomSheetView(bottomSheet)
        }
    }

    override fun onDetach(view: View) {
        findSearchBottomSheetView(view)?.let { bottomSheet ->
            mainScreenView()?.onDetachedFromBottomSheetView(bottomSheet)
        }
        super.onDetach(view)
    }

    private fun openFavoriteRenameScreen(favorite: FavoriteRecord) {
        val transaction = RouterTransaction.with(
            EditFavoriteViewController(EditFavoriteView.Mode.RENAME, favorite)
        ).withVerticalAnimation(animationBackgroundRes = R.drawable.mapbox_search_sdk_search_view_background)
        router.pushController(transaction)
    }

    private fun openSearchScreen(mode: AddressSearchView.Mode) {
        val controller = AddressSearchViewController(mode, configuration).apply {
            onFeedbackClickListener = {
                this@MainScreenViewController.onFeedbackClickListener?.invoke(it)
            }
        }

        val transaction = RouterTransaction.with(controller)
            .withVerticalAnimation(animationBackgroundRes = R.drawable.mapbox_search_sdk_search_view_background)

        router.pushController(transaction)
    }

    private fun openCategorySuggestionSearchScreen(searchSuggestion: SearchSuggestion) {
        val controller = CategorySuggestionSearchViewController(searchSuggestion, configuration).apply {
            onBackClickListener = {
                router.popCurrentController()
                mainScreenView()?.requestTextFocus()
            }
            onCloseSearchClickListener = { cardStateListener?.onCollapseCard() }
            onSearchResultClickListener = { searchResult, responseInfo ->
                this@MainScreenViewController.onSearchResultClickListener?.invoke(searchResult, responseInfo)
            }
            onFeedbackClickListener = {
                this@MainScreenViewController.onFeedbackClickListener?.invoke(it)
            }
        }

        val transaction = RouterTransaction.with(controller)
            .tag(CategorySuggestionSearchViewController.TAG)
            .withHorizontalAnimation()
        router.pushController(transaction)
    }

    companion object {

        const val TAG = "controller.tag.MainScreenViewController"

        private const val BUNDLE_KEY_CONFIG = "key.SearchView.Config"
        private const val BUNDLE_KEY_SEARCH_OPTIONS = "key.SearchView.SearchOptions"

        private fun bundleConfig(
            configuration: SearchBottomSheetView.Configuration,
            searchOptions: SearchOptions
        ): Bundle {
            return Bundle().apply {
                putParcelable(BUNDLE_KEY_CONFIG, configuration)
                putParcelable(BUNDLE_KEY_SEARCH_OPTIONS, searchOptions)
            }
        }

        private fun restoreConfigFromBundle(bundle: Bundle): SearchBottomSheetView.Configuration? {
            return bundle.getParcelable(BUNDLE_KEY_CONFIG)
        }

        private fun restoreSearchOptionsFromBundle(bundle: Bundle): SearchOptions? {
            return bundle.getParcelable(BUNDLE_KEY_SEARCH_OPTIONS)
        }
    }
}
