@file:OptIn(ExperimentalPreviewMapboxEvAPI::class)

package com.mapbox.search.extensions.utils

import com.mapbox.geojson.Point
import com.mapbox.navigation.ev.ExperimentalPreviewMapboxEvAPI
import com.mapbox.navigation.ev.model.ChargingStation
import com.mapbox.navigation.ev.model.GeoLocation
import com.mapbox.search.CategorySearchOptions
import com.mapbox.search.base.BaseRequestOptions
import com.mapbox.search.base.core.CoreApiType
import com.mapbox.search.base.core.CoreImageInfo
import com.mapbox.search.base.core.CoreRequestOptions
import com.mapbox.search.base.core.CoreResultMetadata
import com.mapbox.search.base.core.createCoreResultMetadata
import com.mapbox.search.base.defaultLocaleLanguage
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.result.BaseSearchAddress
import com.mapbox.search.base.result.BaseSearchResultType
import com.mapbox.search.base.result.BaseServerSearchResultImpl
import com.mapbox.search.base.result.SearchRequestContext
import com.mapbox.search.common.DistanceCalculator
import com.mapbox.search.internal.SearchResultFactory
import com.mapbox.search.mapToCoreCategory
import com.mapbox.search.result.SearchResult

internal class SearchResultFactory {

    fun createFromChargingStation(
        station: ChargingStation,
        proximity: Point?,
        options: CategorySearchOptions,
    ): SearchResult {
        val base = BaseServerSearchResultImpl(
            types = listOf(BaseSearchResultType.POI),
            rawSearchResult = createRawSearchResult(station, proximity),
            requestOptions = BaseRequestOptions(
                createRequestOptions(options), createRequestContext()
            ),
        )

        return SearchResultFactory.create(base)
    }


    private fun createMetadata(station: ChargingStation): CoreResultMetadata {
        val otherData = HashMap<String, String>().apply {
            /**
             * TODO Serialize
             * ChargingStation.tariffs, ChargingStation.location
             */
        }

        return createCoreResultMetadata(
            data = otherData,
            primaryPhoto = null,
            otherPhoto = station.imageInfos(),
            website = station.website(),
            // TODO discuss and agree on format
            openHours = null,
            parkingAvailable = station.location.parkingType != null,
            streetParking = station.streetParking(),
            parking = null,
            valetParking = null,
            cpsJson = null,
            reviewCount = null,
            phone = null,
            facebookId = null,
            fax = null,
            email = null,
            instagram = null,
            twitter = null,
            avRating = null,
            popularity = null,
            rating = null,
            delivery = null,
            driveThrough = null,
            reservable = null,
            servesVegan = null,
            servesVegetarian = null,
            servesBreakfast = null,
            servesBrunch = null,
            servesDinner = null,
            servesLunch = null,
            servesWine = null,
            servesBeer = null,
            takeout = null,
            priceLevel = null,
            wheelchairAccessible = null,
        )
    }

    private fun ChargingStation.streetParking(): Boolean? {
        return when (location.parkingType) {
            "ALONG_MOTORWAY", // Location on a parking facility/rest area along a motorway, freeway, interstate, highway etc.
            "PARKING_LOT", // A cleared area that is intended for parking vehicles, as in at super markets, bars, etc.
            "ON_DRIVEWAY", // Location is on the driveway of a house/building.
            "ON_STREET" -> true // Parking in public space along a street.


            "PARKING_GARAGE", // Multistorey car park.
            "UNDERGROUND_GARAGE" -> false // Multistorey car park, mainly underground.

            else -> null
        }
    }

    private fun ChargingStation.imageInfos(): List<CoreImageInfo> {
        return location.images.mapNotNull {
            val width = it.width
            val height = it.height

            if (width != null && height != null) {
                CoreImageInfo(
                    url = it.url,
                    width = width,
                    height = height,
                )
            } else {
                null
            }
        }
    }

    private fun ChargingStation.website() = with(location) {
        owner?.website ?: operator?.website ?: subOperator?.website
    }

    private fun createRawSearchResult(
        station: ChargingStation,
        proximity: Point?,
    ): BaseRawSearchResult {
        val baseRawSearchResult = BaseRawSearchResult(
            id = station.location.id,
            mapboxId = null,
            types = listOf(BaseRawResultType.POI),
            names = listOf(station.nonNullName()),
            languages = listOf(defaultLocaleLanguage().code),
            addresses = listOf(station.address()),
            descriptionAddress = station.location.address,
            fullAddress = station.location.address,
            distanceMeters = distanceToStationMeters(station, proximity),
            center = station.location.coordinates.toPoint(),
            categories = listOf("charging station", "transportation"),
            categoryIds = listOf("charging_station", "transportation"),
            icon = "charging-station",
            metadata = createMetadata(station),
            brand = null,
            brandId = null,
            accuracy = null,
            routablePoints = null,
            externalIDs = null,
            layerId = null,
            userRecordId = null,
            userRecordPriority = -1,
            action = null,
            serverIndex = null,
            etaMinutes = null,
            matchingName = null,
        )

        return baseRawSearchResult
    }

    private fun ChargingStation.nonNullName() = with(location) {
        name ?: owner?.name ?: operator?.name ?: subOperator?.name ?: id
    }

    private fun ChargingStation.address(): BaseSearchAddress {
        return BaseSearchAddress(
            postcode = location.postalCode,
            place = location.city,
            region = location.state,
            country = location.country,
        )
    }

    private fun distanceToStationMeters(
        station: ChargingStation,
        proximity: Point?,
    ): Double? {
        return station.proximity?.distance?.kmToMeters() ?: proximity?.let {
            DistanceCalculator.distanceOnSphere(it, station.location.coordinates.toPoint())
        }
    }

    private fun createRequestContext() = SearchRequestContext(
        apiType = CoreApiType.SEARCH_BOX,
        keyboardLocale = null,
        screenOrientation = null,
        responseUuid = null
    )

    private fun createRequestOptions(options: CategorySearchOptions) = CoreRequestOptions(
        query = "charging_station",
        endpoint = "ev/v1",
        options = options.mapToCoreCategory(),
        proximityRewritten = false,
        originRewritten = false,
        sessionID = "<NO SESSION IDENTIFIER>"
    )

    private fun GeoLocation.toPoint() = Point.fromLngLat(longitude, latitude)

    private fun Double.kmToMeters() = this * 1000.0
}