package com.mapbox.search.base.core

import com.mapbox.geojson.Point
import com.mapbox.search.internal.bindgen.LonLatBBox
import com.mapbox.search.internal.bindgen.QueryType
import com.mapbox.search.internal.bindgen.ReverseGeoOptions
import com.mapbox.search.internal.bindgen.ReverseMode
import java.util.HashMap

fun createCoreSearchOptions(
    proximity: Point? = null,
    origin: Point? = null,
    navProfile: String? = null,
    etaType: String? = null,
    bbox: LonLatBBox? = null,
    countries: List<String>? = null,
    fuzzyMatch: Boolean? = null,
    language: List<String>? = null,
    limit: Int? = null,
    types: List<QueryType>? = null,
    ignoreUR: Boolean = false,
    urDistanceThreshold: Double? = null,
    requestDebounce: Int? = null,
    route: List<Point>? = null,
    sarType: String? = null,
    timeDeviation: Double? = null,
    addonAPI: Map<String, String>? = null,
): CoreSearchOptions = CoreSearchOptions(
    proximity,
    origin,
    navProfile,
    etaType,
    bbox,
    countries,
    fuzzyMatch,
    language,
    limit,
    types,
    ignoreUR,
    urDistanceThreshold,
    requestDebounce,
    route,
    sarType,
    timeDeviation,
    addonAPI?.let { it as? HashMap<String, String> ?: HashMap(it) }
)

fun createCoreReverseGeoOptions(
    point: Point,
    reverseMode: ReverseMode? = null,
    countries: List<String>? = null,
    language: List<String>? = null,
    limit: Int? = null,
    types: List<QueryType>? = null,
): ReverseGeoOptions = ReverseGeoOptions(
    point,
    reverseMode,
    countries,
    language,
    limit,
    types,
)

fun createCoreResultMetadata(
    reviewCount: Int? = null,
    phone: String? = null,
    website: String? = null,
    avRating: Double? = null,
    description: String? = null,
    openHours: CoreOpenHours? = null,
    primaryPhoto: List<CoreImageInfo>? = null,
    otherPhoto: List<CoreImageInfo>? = null,
    cpsJson: String? = null,
    parking: CoreParkingData? = null,
    children: List<CoreChildMetadata>? = null,
    data: HashMap<String, String>
): CoreResultMetadata = CoreResultMetadata(
    reviewCount,
    phone,
    website,
    avRating,
    description,
    openHours,
    primaryPhoto,
    otherPhoto,
    cpsJson,
    parking,
    children,
    data
)
