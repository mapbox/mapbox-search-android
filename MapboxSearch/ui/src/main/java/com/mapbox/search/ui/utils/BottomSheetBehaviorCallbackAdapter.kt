package com.mapbox.search.ui.utils

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

internal open class BottomSheetBehaviorCallbackAdapter : BottomSheetBehavior.BottomSheetCallback() {
    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        // Nothing to do by default
    }

    override fun onStateChanged(bottomSheet: View, @BottomSheetBehavior.State newState: Int) {
        // Nothing to do by default
    }
}
