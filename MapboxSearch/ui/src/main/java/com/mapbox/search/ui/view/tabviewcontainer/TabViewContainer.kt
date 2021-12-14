package com.mapbox.search.ui.view.tabviewcontainer

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mapbox.search.AsyncOperationTask
import com.mapbox.search.CompletionCallback
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.record.LocalDataProvider
import com.mapbox.search.ui.R
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.category.CategoryEntry
import com.mapbox.search.ui.view.category.CategoryViewCallback
import com.mapbox.search.ui.view.favorite.FavoriteTemplate
import com.mapbox.search.ui.view.favorite.FavoriteViewCallback
import com.mapbox.search.ui.view.favorite.model.UserFavoriteAdapterItem
import com.mapbox.search.ui.view.favorite.model.UserFavoriteAdapterItemsCreator

internal class TabViewContainer : LinearLayout {

    private val favoritesDataProvider = MapboxSearchSdk.serviceProvider.favoritesDataProvider()

    private val tabsAdapter = SearchViewTabsAdapter()

    private val tabLayout: TabLayout
    private val viewPager: ViewPager2

    private var viewPagerAdded = true

    private val favoritesManager: FavoritesManager

    private var favoritesLoadingTask: AsyncOperationTask? = null

    var favoriteViewCallback: FavoriteViewCallback?
        get() = tabsAdapter.favoriteViewCallback
        set(value) {
            tabsAdapter.favoriteViewCallback = value
        }

    var categoryViewCallback: CategoryViewCallback?
        get() = tabsAdapter.categoryViewCallback
        set(value) {
            tabsAdapter.categoryViewCallback = value
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet? = null) : super(context, attr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.mapbox_search_sdk_tab_view_container, this, true)

        favoritesManager = FavoritesManager(tabsAdapter)

        viewPager = findViewById(R.id.tab_view_viewpager)
        viewPager.adapter = tabsAdapter
        viewPager.children.find { it is RecyclerView }?.let {
            it as RecyclerView
            (it.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            it.overScrollMode = View.OVER_SCROLL_NEVER
        }

        tabLayout = findViewById(R.id.tab_view_tab_layout)

        tabsAdapter.setCategories(Category.PREDEFINED_CATEGORY_VALUES.map { CategoryEntry(it) })

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = context.getString(tabsAdapter.getItem(position).tabTitleRes)
        }.attach()
    }

    /**
     * BottomSheetBehavior supports only one scrollable view, so when we have multiple scrollable views,
     * behavior check state of the first one. Here we explicitly remove one of the scrolling view
     * so that BottomSheetBehavior finds another one.
     * There is another more flexible solution for this issue which requires more code and another workaround.
     */
    fun excludeViewPager(exclude: Boolean) {
        when {
            exclude && viewPagerAdded -> {
                removeView(viewPager)
                viewPagerAdded = false
            }
            !exclude && !viewPagerAdded -> {
                addView(viewPager)
                viewPagerAdded = true
            }
            else -> {
                // Nothing to do
            }
        }
    }

    fun setFavoriteTemplates(favoriteTemplates: List<FavoriteTemplate>) {
        favoritesManager.setFavoriteTemplates(favoriteTemplates)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        favoritesManager.showLoading()
        favoritesLoadingTask = favoritesDataProvider.getAll(object : CompletionCallback<List<FavoriteRecord>> {
            override fun onComplete(result: List<FavoriteRecord>) {
                favoritesManager.onDataChanged(result)
            }

            override fun onError(e: Exception) {
                favoritesManager.onDataChanged(emptyList())
                Toast.makeText(context, R.string.mapbox_search_sdk_favorite_loading_error, Toast.LENGTH_SHORT).show()
            }
        })

        favoritesDataProvider.addOnDataChangedListener(favoritesManager)
    }

    override fun onDetachedFromWindow() {
        favoritesLoadingTask?.cancel()
        favoritesDataProvider.removeOnDataChangedListener(favoritesManager)
        super.onDetachedFromWindow()
    }

    fun moveToInitialPage() {
        viewPager.adapter?.let {
            if (it.itemCount > 0) {
                viewPager.currentItem = 0
            }
        }
    }

    private class FavoritesManager(
        private val adapter: SearchViewTabsAdapter
    ) : LocalDataProvider.OnDataChangedListener<FavoriteRecord> {

        private val itemsCreator = UserFavoriteAdapterItemsCreator()

        private var latestFavoriteTemplates: List<FavoriteTemplate> = emptyList()
        private var latestFavorites: List<FavoriteRecord> = emptyList()

        fun showLoading() {
            adapter.setFavorites(listOf(UserFavoriteAdapterItem.Loading))
        }

        fun setFavoriteTemplates(favoriteTemplates: List<FavoriteTemplate>) {
            latestFavoriteTemplates = favoriteTemplates
            notifyAdapter()
        }

        override fun onDataChanged(newData: List<FavoriteRecord>) {
            latestFavorites = newData
            notifyAdapter()
        }

        private fun notifyAdapter() {
            val items = itemsCreator.createItems(
                favorites = latestFavorites,
                templates = latestFavoriteTemplates
            )
            adapter.setFavorites(items)
        }
    }
}
