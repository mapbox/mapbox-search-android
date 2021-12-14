package com.mapbox.search.ui.utils.extenstion

import android.view.View
import androidx.core.text.layoutDirection
import java.util.Locale

internal val isLayoutDirectionLtr: Boolean
    get() = Locale.getDefault().layoutDirection == View.LAYOUT_DIRECTION_LTR
