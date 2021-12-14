package com.mapbox.search.utils

import java.util.UUID

internal fun interface UUIDProvider {
    fun generateUUID(): String
}

internal class UUIDProviderImpl : UUIDProvider {
    override fun generateUUID(): String = UUID.randomUUID().toString()
}
