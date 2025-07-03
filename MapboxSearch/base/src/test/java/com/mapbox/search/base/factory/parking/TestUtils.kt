package com.mapbox.search.base.factory.parking

import com.mapbox.search.base.core.CoreParkingInfo
import com.mapbox.search.base.core.CoreParkingRate
import com.mapbox.search.base.core.CoreParkingRateInfo
import com.mapbox.search.base.core.CoreParkingRatePrice
import com.mapbox.search.base.core.CoreParkingRateValue

fun equals(v1: CoreParkingRateValue?, v2: CoreParkingRateValue?): Boolean {
    return if (v1 == null || v2 == null) {
        v1 == v2
    } else if (v1.isString && v2.isString) {
        v1.string == v2.string
    } else if (v1.isParkingRateCustomValue && v2.isParkingRateCustomValue) {
        v1.parkingRateCustomValue == v2.parkingRateCustomValue
    } else {
        false
    }
}

fun equals(v1: CoreParkingRatePrice?, v2: CoreParkingRatePrice?): Boolean {
    if (v1 == null || v2 == null) {
        return v1 == v2
    }

    if (v1 === v2) return true
    if (v1.type != v2.type) return false
    if (v1.amount != v2.amount) return false
    if (!equals(v1.value, v2.value)) return false
    return true
}

fun equals(v1: CoreParkingRate?, v2: CoreParkingRate?): Boolean {
    if (v1 == null || v2 == null) {
        return v1 == v2
    }

    if (v1.maxStay != v2.maxStay) return false
    if (v1.times != v2.times) return false

    if (v1.prices?.size != v2.prices?.size) {
        return false
    }

    v1.prices?.mapIndexed { idx, price ->
        if (!equals(price, v2.prices?.getOrNull(idx))) {
            return false
        }
    }

    return true
}

fun equals(v1: CoreParkingRateInfo?, v2: CoreParkingRateInfo?): Boolean {
    if (v1 == null || v2 == null) {
        return v1 == v2
    }

    if (v1.currencySymbol != v2.currencySymbol) return false
    if (v1.currencyCode != v2.currencyCode) return false

    if (v1.rates?.size != v2.rates?.size) {
        return false
    }

    v1.rates?.mapIndexed { idx, rate ->
        if (!equals(rate, v2.rates?.getOrNull(idx))) {
            return false
        }
    }

    return true
}

fun equals(v1: CoreParkingInfo?, v2: CoreParkingInfo?): Boolean {
    if (v1 == null || v2 == null) {
        return v1 == v2
    }

    if (v1.capacity != v2.capacity) return false
    if (!equals(v1.rateInfo, v2.rateInfo)) return false
    if (v1.availability != v2.availability) return false
    if (v1.availabilityLevel != v2.availabilityLevel) return false
    if (v1.availabilityAt != v2.availabilityAt) return false
    if (v1.trend != v2.trend) return false
    if (v1.paymentMethods != v2.paymentMethods) return false
    if (v1.paymentTypes != v2.paymentTypes) return false
    if (v1.restrictions != v2.restrictions) return false

    return true
}
