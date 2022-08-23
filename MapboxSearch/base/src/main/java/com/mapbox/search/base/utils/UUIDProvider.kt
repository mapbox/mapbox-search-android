package com.mapbox.search.base.utils

import java.util.UUID

fun interface UUIDProvider {
    fun generateUUID(): String
}

class UUIDProviderImpl : UUIDProvider {
    override fun generateUUID(): String = UUID.randomUUID().toString()
}
