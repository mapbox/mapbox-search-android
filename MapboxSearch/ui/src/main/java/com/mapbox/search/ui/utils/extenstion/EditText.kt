package com.mapbox.search.ui.utils.extenstion

import android.widget.EditText

internal fun EditText.setCursorAtTheEnd() {
    setSelection(text.length)
}
