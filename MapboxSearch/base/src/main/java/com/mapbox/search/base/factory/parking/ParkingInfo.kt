@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingInfo
import com.mapbox.search.base.factory.createCoreParkingRestriction
import com.mapbox.search.base.factory.mapToPlatform
import com.mapbox.search.common.parking.ParkingInfo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreParkingInfo.mapToPlatform(): ParkingInfo {
    return ParkingInfo(
        capacity = capacity,
        rateInfo = rateInfo?.mapToPlatform(),
        availability = availability,
        availabilityLevel = availabilityLevel?.mapToPlatform(),
        availabilityUpdatedAt = availabilityAt,
        trend = trend?.mapToPlatform(),
        paymentMethods = paymentMethods?.mapNotNull { it?.mapToPlatform() },
        paymentTypes = paymentTypes?.mapNotNull { it?.mapToPlatform() },
        restrictions = restrictions?.mapNotNull { it?.mapToPlatform() },
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun ParkingInfo.mapToCore(): CoreParkingInfo {
    return CoreParkingInfo(
        capacity = capacity,
        rateInfo = rateInfo?.mapToCore(),
        availability = availability,
        availabilityLevel = availabilityLevel?.let {
            createCoreParkingAvailabilityLevel(it)
        },
        availabilityAt = availabilityUpdatedAt,
        trend = trend?.let {
            createCoreParkingTrend(it)
        },
        paymentMethods = paymentMethods?.map {
            createCoreParkingPaymentMethod(it)
        },
        paymentTypes = paymentTypes?.map {
            createCoreParkingPaymentType(it)
        },
        restrictions = restrictions?.map {
            createCoreParkingRestriction(it)
        },
    )
}
