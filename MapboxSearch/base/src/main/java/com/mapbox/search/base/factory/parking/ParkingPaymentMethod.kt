@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingPaymentMethod
import com.mapbox.search.common.parking.ParkingPaymentMethod

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@ParkingPaymentMethod.Type
fun CoreParkingPaymentMethod.mapToPlatform(): String {
    return when (this) {
        CoreParkingPaymentMethod.PAY_ON_FOOT -> ParkingPaymentMethod.PAY_ON_FOOT
        CoreParkingPaymentMethod.PAY_AND_DISPLAY -> ParkingPaymentMethod.PAY_AND_DISPLAY
        CoreParkingPaymentMethod.PAY_ON_EXIT -> ParkingPaymentMethod.PAY_ON_EXIT
        CoreParkingPaymentMethod.PAY_ON_ENTRY -> ParkingPaymentMethod.PAY_ON_ENTRY
        CoreParkingPaymentMethod.PARKING_METER -> ParkingPaymentMethod.PARKING_METER
        CoreParkingPaymentMethod.MULTI_SPACE_METER -> ParkingPaymentMethod.MULTI_SPACE_METER
        CoreParkingPaymentMethod.HONESTY_BOX -> ParkingPaymentMethod.HONESTY_BOX
        CoreParkingPaymentMethod.ATTENDANT -> ParkingPaymentMethod.ATTENDANT
        CoreParkingPaymentMethod.PAY_BY_PLATE -> ParkingPaymentMethod.PAY_BY_PLATE
        CoreParkingPaymentMethod.PAY_AT_RECEPTION -> ParkingPaymentMethod.PAY_AT_RECEPTION
        CoreParkingPaymentMethod.PAY_BY_PHONE -> ParkingPaymentMethod.PAY_BY_PHONE
        CoreParkingPaymentMethod.PAY_BY_COUPON -> ParkingPaymentMethod.PAY_BY_COUPON
        CoreParkingPaymentMethod.ELECTRONIC_PARKING_SYSTEM ->
            ParkingPaymentMethod.ELECTRONIC_PARKING_SYSTEM
        CoreParkingPaymentMethod.UNKNOWN -> ParkingPaymentMethod.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun createCoreParkingPaymentMethod(
    @ParkingPaymentMethod.Type type: String?,
): CoreParkingPaymentMethod? {
    return when (type) {
        ParkingPaymentMethod.PAY_ON_FOOT -> CoreParkingPaymentMethod.PAY_ON_FOOT
        ParkingPaymentMethod.PAY_AND_DISPLAY -> CoreParkingPaymentMethod.PAY_AND_DISPLAY
        ParkingPaymentMethod.PAY_ON_EXIT -> CoreParkingPaymentMethod.PAY_ON_EXIT
        ParkingPaymentMethod.PAY_ON_ENTRY -> CoreParkingPaymentMethod.PAY_ON_ENTRY
        ParkingPaymentMethod.PARKING_METER -> CoreParkingPaymentMethod.PARKING_METER
        ParkingPaymentMethod.MULTI_SPACE_METER -> CoreParkingPaymentMethod.MULTI_SPACE_METER
        ParkingPaymentMethod.HONESTY_BOX -> CoreParkingPaymentMethod.HONESTY_BOX
        ParkingPaymentMethod.ATTENDANT -> CoreParkingPaymentMethod.ATTENDANT
        ParkingPaymentMethod.PAY_BY_PLATE -> CoreParkingPaymentMethod.PAY_BY_PLATE
        ParkingPaymentMethod.PAY_AT_RECEPTION -> CoreParkingPaymentMethod.PAY_AT_RECEPTION
        ParkingPaymentMethod.PAY_BY_PHONE -> CoreParkingPaymentMethod.PAY_BY_PHONE
        ParkingPaymentMethod.PAY_BY_COUPON -> CoreParkingPaymentMethod.PAY_BY_COUPON
        ParkingPaymentMethod.ELECTRONIC_PARKING_SYSTEM ->
            CoreParkingPaymentMethod.ELECTRONIC_PARKING_SYSTEM
        ParkingPaymentMethod.UNKNOWN -> CoreParkingPaymentMethod.UNKNOWN
        else -> null
    }
}
