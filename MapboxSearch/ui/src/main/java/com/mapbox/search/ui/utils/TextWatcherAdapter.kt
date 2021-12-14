package com.mapbox.search.ui.utils

import android.text.Editable
import android.text.TextWatcher

internal abstract class TextWatcherAdapter : TextWatcher {
    override fun afterTextChanged(s: Editable) {
        // Nothing to do by default
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // Nothing to do by default
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        // Nothing to do by default
    }
}
