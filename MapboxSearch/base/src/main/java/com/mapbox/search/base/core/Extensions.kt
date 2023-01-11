package com.mapbox.search.base.core

val CoreResultMetadata.countryIso1: String?
    get() = data["iso_3166_1"]

val CoreResultMetadata.countryIso2: String?
    get() = data["iso_3166_2"]
