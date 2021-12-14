package com.mapbox.search.ui.utils.extenstion

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.search.ui.utils.BottomSheetBehaviorCallbackAdapter

internal val BottomSheetBehavior<*>.isHidden: Boolean
    get() = state == BottomSheetBehavior.STATE_HIDDEN

internal val BottomSheetBehavior<*>.isCollapsed: Boolean
    get() = state == BottomSheetBehavior.STATE_COLLAPSED

internal val BottomSheetBehavior<*>.isExpanded: Boolean
    get() = state == BottomSheetBehavior.STATE_EXPANDED

internal fun BottomSheetBehavior<*>.hide() {
    state = BottomSheetBehavior.STATE_HIDDEN
}

internal fun BottomSheetBehavior<*>.collapse() {
    state = BottomSheetBehavior.STATE_COLLAPSED
}

internal fun BottomSheetBehavior<*>.expand() {
    state = BottomSheetBehavior.STATE_EXPANDED
}

internal inline fun BottomSheetBehavior<*>.addOnStateChangedCallback(crossinline callback: (Int) -> Unit) {
    addBottomSheetCallback(object : BottomSheetBehaviorCallbackAdapter() {
        override fun onStateChanged(bottomSheet: View, @BottomSheetBehavior.State newState: Int) {
            callback(newState)
        }
    })
}

internal fun bottomSheetBehaviorStateToString(@BottomSheetBehavior.State state: Int): String {
    return when (state) {
        BottomSheetBehavior.STATE_DRAGGING -> "STATE_DRAGGING"
        BottomSheetBehavior.STATE_SETTLING -> "STATE_SETTLING"
        BottomSheetBehavior.STATE_EXPANDED -> "STATE_EXPANDED"
        BottomSheetBehavior.STATE_COLLAPSED -> "STATE_COLLAPSED"
        BottomSheetBehavior.STATE_HIDDEN -> "STATE_HIDDEN"
        BottomSheetBehavior.STATE_HALF_EXPANDED -> "STATE_HALF_EXPANDED"
        else -> "Unknown BottomSheetBehavior state: $state"
    }
}
