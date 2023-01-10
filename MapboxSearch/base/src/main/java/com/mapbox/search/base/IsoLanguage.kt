package com.mapbox.search.base

import com.mapbox.search.common.IsoLanguageCode
import java.util.Locale

fun defaultLocaleLanguage(): IsoLanguageCode {
    return IsoLanguageCode(Locale.getDefault().language)
}
