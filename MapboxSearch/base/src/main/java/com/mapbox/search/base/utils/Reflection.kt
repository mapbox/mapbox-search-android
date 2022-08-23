package com.mapbox.search.base.utils

@get:JvmSynthetic
inline val <T : Any> Class<T>.printableName: String
    get() = if (isAnonymousClass) {
        name
    } else {
        simpleName
    }
