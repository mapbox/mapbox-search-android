package com.mapbox.search.ui.view.common

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.mapbox.search.ui.R
import com.mapbox.search.ui.view.UiError

internal class MapboxSdkUiErrorView : LinearLayout {

    var onRetryClickListener: (() -> Unit)? = null
    var uiError: UiError = UiError.NoInternetConnectionError
        set(value) {
            if (field != value) {
                field = value
                updateErrorView()
            }
        }

    private val errorTitleTextView: TextView
    private val errorSubtitleTextView: TextView

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.mapbox_search_sdk_error_view, this)
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL

        errorTitleTextView = findViewById(R.id.error_title)
        errorSubtitleTextView = findViewById(R.id.error_subtitle)
        findViewById<View>(R.id.retry_button).setOnClickListener {
            onRetryClickListener?.invoke()
        }

        updateErrorView()
    }

    private fun updateErrorView() {
        when (uiError) {
            UiError.NoInternetConnectionError -> {
                errorTitleTextView.setText(R.string.mapbox_search_sdk_no_internet_connection_title)
                errorSubtitleTextView.setText(R.string.mapbox_search_sdk_no_internet_connection_subtitle)
            }
            UiError.ServerError -> {
                errorTitleTextView.setText(R.string.mapbox_search_sdk_server_error_title)
                errorSubtitleTextView.setText(R.string.mapbox_search_sdk_server_error_subtitle)
            }
            UiError.ClientError,
            UiError.UnknownError -> {
                errorTitleTextView.setText(R.string.mapbox_search_sdk_unknown_error_title)
                errorSubtitleTextView.setText(R.string.mapbox_search_sdk_unknown_error_subtitle)
            }
        }
    }
}
