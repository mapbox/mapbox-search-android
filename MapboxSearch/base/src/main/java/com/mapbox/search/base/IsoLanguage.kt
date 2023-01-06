package com.mapbox.search.base

import com.mapbox.search.common.IsoLanguage
import java.util.Locale

fun defaultLocaleLanguage(): IsoLanguage {
    return IsoLanguage(Locale.getDefault().language)
}
