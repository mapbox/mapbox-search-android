package com.mapbox.search.utils

import android.app.Application
import android.content.Context
import android.os.Build
import android.view.inputmethod.InputMethodManager
import java.util.Locale

internal fun interface KeyboardLocaleProvider {
    fun provideKeyboardLocale(): Locale?
}

internal class AndroidKeyboardLocaleProvider(private val app: Application) : KeyboardLocaleProvider {

    override fun provideKeyboardLocale(): Locale? {
        val inputMethodSubtype = (app.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.currentInputMethodSubtype ?: return null

        var locale: Locale? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = inputMethodSubtype.languageTag
                .takeIf { it.isNotEmpty() }
                ?.let { Locale.forLanguageTag(it) }
        }
        if (locale == null) {
            @Suppress("DEPRECATION")
            locale = inputMethodSubtype.locale
                .takeIf { it.isNotEmpty() }
                ?.let { Locale(it) }
        }
        return locale
    }
}
