@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreDisplayText
import com.mapbox.search.common.LocalizedText

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreDisplayText.mapToPlatform(): LocalizedText {
    return LocalizedText(
        language = this.language,
        text = this.text,
    )
}
