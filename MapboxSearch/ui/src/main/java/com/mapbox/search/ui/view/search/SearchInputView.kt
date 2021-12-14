package com.mapbox.search.ui.view.search

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.view.updatePaddingRelative
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.TextWatcherAdapter
import com.mapbox.search.ui.utils.extenstion.drawableStart
import com.mapbox.search.ui.utils.extenstion.getPixelSize
import com.mapbox.search.ui.utils.extenstion.hideKeyboard
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.utils.extenstion.setCompoundDrawableStartWithIntrinsicBounds
import com.mapbox.search.ui.utils.extenstion.setCursorAtTheEnd
import com.mapbox.search.ui.utils.extenstion.setTintCompat

internal class SearchInputView : FrameLayout {

    var searchInputCallback: SearchInputCallback? = null

    internal val editText: EditText
    private val clearTextButton: View

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.mapbox_search_sdk_search_input_view_layout, this)

        editText = findViewById(R.id.search_edit_text)
        clearTextButton = findViewById(R.id.clear_text_button)

        setClearButtonVisible(editText.text.isNotEmpty())
        editText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            searchInputCallback?.onFocusChange(hasFocus)
            val drawableRes = if (hasFocus) {
                R.drawable.mapbox_search_sdk_ic_mdi_search
            } else {
                0
            }
            editText.setCompoundDrawableStartWithIntrinsicBounds(drawableRes)
            editText.drawableStart = editText.drawableStart
                ?.setTintCompat(context.resolveAttrOrThrow(R.attr.mapboxSearchSdkPrimaryTextInactiveColor))
            changeHintColor(hasFocus)
        }

        editText.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable) {
                searchInputCallback?.onQuery(s.toString())
                setClearButtonVisible(s.isNotEmpty())
            }
        })

        editText.setOnClickListener {
            searchInputCallback?.onTextFieldClick()
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    editText.hideKeyboard()
                    true
                }
                else -> false
            }
        }

        clearTextButton.setOnClickListener {
            editText.text.clear()
        }
    }

    private fun setClearButtonVisible(isVisible: Boolean) {
        clearTextButton.isVisible = isVisible
        editText.updatePaddingRelative(
            end = context.getPixelSize(
                if (isVisible) {
                    R.dimen.mapbox_search_sdk_search_input_view_edit_text_padding_end
                } else {
                    R.dimen.mapbox_search_sdk_list_item_icon_horizontal_margin
                }
            )
        )
    }

    fun setHint(@StringRes resId: Int) {
        editText.setHint(resId)
    }

    fun setQuery(query: String) {
        searchInputCallback?.onQuery(query)
        editText.setText(query)
        if (query.isNotEmpty()) {
            editText.setCursorAtTheEnd()
        }
    }

    fun hasTextFocus(): Boolean {
        return editText.isFocused
    }

    fun requestTextFocus(): Boolean {
        return editText.requestFocus()
    }

    fun clearTextFocus() {
        editText.clearFocus()
    }

    private fun changeHintColor(hasFocus: Boolean) {
        val attrId = if (hasFocus) {
            R.attr.mapboxSearchSdkPrimaryTextInactiveColor
        } else {
            R.attr.mapboxSearchSdkPrimaryTextColor
        }
        editText.setHintTextColor(context.resolveAttrOrThrow(attrId))
    }

    interface SearchInputCallback {

        fun onFocusChange(hasFocus: Boolean)

        fun onTextFieldClick()

        fun onQuery(query: String)
    }
}
