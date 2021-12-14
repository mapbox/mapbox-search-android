package com.mapbox.search.ui.view.favorite.rename

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.mapbox.search.AsyncOperationTask
import com.mapbox.search.CompletionCallback
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.common.throwDebug
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.ui.R
import com.mapbox.search.ui.utils.SearchEntityPresentation
import com.mapbox.search.ui.utils.TextWatcherAdapter
import com.mapbox.search.ui.utils.extenstion.doOnWindowFocus
import com.mapbox.search.ui.utils.extenstion.drawableStart
import com.mapbox.search.ui.utils.extenstion.hideKeyboard
import com.mapbox.search.ui.utils.extenstion.resolveAttrOrThrow
import com.mapbox.search.ui.utils.extenstion.setCompoundDrawableStartWithIntrinsicBounds
import com.mapbox.search.ui.utils.extenstion.setCursorAtTheEnd
import com.mapbox.search.ui.utils.extenstion.setTintCompat
import com.mapbox.search.ui.utils.extenstion.showKeyboard
import com.mapbox.search.ui.utils.wrapWithSearchTheme

internal class EditFavoriteView : LinearLayout {

    var onCloseClickListener: (() -> Unit)? = null
    var onDoneClickListener: (() -> Unit)? = null

    private val closeButton: View
    private val doneButton: View
    private val nameEditText: EditText
    private val clearTextButton: View
    private val addressTitle: TextView
    private val addressText: TextView
    private val toolbarTitle: TextView

    private var updateFavoriteTask: AsyncOperationTask? = null

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
        val context = if (isInEditMode) {
            wrapWithSearchTheme(context)
        } else {
            context
        }

        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.mapbox_search_sdk_screen_favorite_edit, this, true)

        id = R.id.favorite_edit_layout
        isClickable = true
        isFocusable = true

        closeButton = findViewById(R.id.close_button)
        doneButton = findViewById(R.id.done_button)
        nameEditText = findViewById(R.id.search_input_edit_text)
        clearTextButton = findViewById(R.id.clear_text_button)
        addressTitle = findViewById(R.id.address_title)
        addressText = findViewById(R.id.address)
        toolbarTitle = findViewById(R.id.toolbar_title)

        closeButton.setOnClickListener {
            onCloseClickListener?.invoke()
        }

        nameEditText.requestFocus()
        doOnWindowFocus {
            nameEditText.showKeyboard()
        }

        clearTextButton.setOnClickListener {
            nameEditText.setText("")
        }

        nameEditText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                nameEditText.hideKeyboard()
            }
        }

        nameEditText.addTextChangedListener(object : TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable) {
                onTextChanged(s.isBlank())
            }
        })
        onTextChanged(nameEditText.text.isBlank())
    }

    fun setFavorite(mode: Mode, favorite: FavoriteRecord) {
        toolbarTitle.text = resources.getText(
            when (mode) {
                Mode.ADD -> R.string.mapbox_search_sdk_favorite_screen_add_title
                Mode.RENAME -> R.string.mapbox_search_sdk_favorite_screen_rename_title
            }
        )

        nameEditText.apply {
            setText(favorite.name)
            setCursorAtTheEnd()

            setCompoundDrawableStartWithIntrinsicBounds(
                SearchEntityPresentation.pickEntityDrawable(
                    favorite.makiIcon,
                    favorite.categories,
                    R.drawable.mapbox_search_sdk_ic_search_result_place
                )
            )

            drawableStart = drawableStart
                ?.setTintCompat(context.resolveAttrOrThrow(R.attr.mapboxSearchSdkIconTintColor))
        }

        setAddressText(favorite.address?.formattedAddress(SearchAddress.FormatStyle.Full))
        doneButton.setOnClickListener {
            updateFavoriteTask = MapboxSearchSdk.serviceProvider.favoritesDataProvider().update(
                favorite.copy(name = nameEditText.text.trim().toString()),
                object : CompletionCallback<Unit> {
                    override fun onComplete(result: Unit) {
                        Log.d(LOG_TAG, "Favorite record has been updated")
                        onDoneClickListener?.invoke()
                    }

                    override fun onError(e: Exception) {
                        Toast.makeText(context, R.string.mapbox_search_sdk_favorite_update_error, Toast.LENGTH_SHORT).show()

                        throwDebug(e) {
                            "Unable to update favorite record"
                        }
                    }
                }
            )
        }
    }

    private fun onTextChanged(isBlank: Boolean) {
        clearTextButton.isVisible = !isBlank
        doneButton.isEnabled = !isBlank
        doneButton.alpha = if (isBlank) {
            // TODO temporary state until we get design
            DISABLED_ALPHA
        } else {
            NORMAL_ALPHA
        }
    }

    private fun setAddressText(address: String?) {
        addressTitle.isVisible = !address.isNullOrBlank()
        addressText.isVisible = !address.isNullOrBlank()
        addressText.text = address
    }

    override fun onDetachedFromWindow() {
        updateFavoriteTask?.cancel()
        updateFavoriteTask = null
        super.onDetachedFromWindow()
    }

    enum class Mode {
        ADD,
        RENAME
    }

    private companion object {

        const val LOG_TAG = "EditFavoriteView"

        const val NORMAL_ALPHA = 1f
        const val DISABLED_ALPHA = .5f
    }
}
