package com.mapbox.search.ui.view.common

import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.Controller
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.SearchMode

internal abstract class BaseSearchController : Controller {

    abstract val cardDraggingAllowed: Boolean

    private val onNetworkModeChangedListener: (SearchMode) -> Unit = { onNetworkModeChanged(it) }

    constructor() : super()
    constructor(args: Bundle?) : super(args)

    protected open fun onNetworkModeChanged(searchMode: SearchMode) {}

    override fun onAttach(view: View) {
        super.onAttach(view)
        val searchView = findSearchBottomSheetView(view) ?: return
        searchView.draggingAllowed = cardDraggingAllowed
        onNetworkModeChanged(searchView.searchMode)
        searchView.addOnSearchModeChangedListener(onNetworkModeChangedListener)
    }

    override fun onDetach(view: View) {
        findSearchBottomSheetView(view)?.removeOnSearchModeChangedListener(onNetworkModeChangedListener)
        super.onDetach(view)
    }

    protected fun findSearchBottomSheetView(view: View): SearchBottomSheetView? {
        val parent = view.parent
        return when {
            view is SearchBottomSheetView -> view
            parent == null || parent !is View -> null
            else -> findSearchBottomSheetView(parent)
        }
    }
}
