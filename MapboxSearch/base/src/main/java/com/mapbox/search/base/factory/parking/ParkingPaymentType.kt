@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingPaymentType
import com.mapbox.search.common.parking.ParkingPaymentType

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@ParkingPaymentType.Type
fun CoreParkingPaymentType.mapToPlatform(): String {
    return when (this) {
        CoreParkingPaymentType.COINS -> ParkingPaymentType.COINS
        CoreParkingPaymentType.NOTES -> ParkingPaymentType.NOTES
        CoreParkingPaymentType.CONTACTLESS -> ParkingPaymentType.CONTACTLESS
        CoreParkingPaymentType.CARDS -> ParkingPaymentType.CARDS
        CoreParkingPaymentType.MOBILE -> ParkingPaymentType.MOBILE
        CoreParkingPaymentType.CARDS_VISA -> ParkingPaymentType.CARDS_VISA
        CoreParkingPaymentType.CARDS_MASTERCARD -> ParkingPaymentType.CARDS_MASTERCARD
        CoreParkingPaymentType.CARDS_AMEX -> ParkingPaymentType.CARDS_AMEX
        CoreParkingPaymentType.CARDS_MAESTRO -> ParkingPaymentType.CARDS_MAESTRO
        CoreParkingPaymentType.EFTPOS -> ParkingPaymentType.EFTPOS
        CoreParkingPaymentType.CARDS_DINERS -> ParkingPaymentType.CARDS_DINERS
        CoreParkingPaymentType.CARDS_GELDKARTE -> ParkingPaymentType.CARDS_GELDKARTE
        CoreParkingPaymentType.CARDS_DISCOVER -> ParkingPaymentType.CARDS_DISCOVER
        CoreParkingPaymentType.CHEQUE -> ParkingPaymentType.CHEQUE
        CoreParkingPaymentType.CARDS_ECASH -> ParkingPaymentType.CARDS_ECASH
        CoreParkingPaymentType.CARDS_JCB -> ParkingPaymentType.CARDS_JCB
        CoreParkingPaymentType.CARDS_OPERATORCARD -> ParkingPaymentType.CARDS_OPERATORCARD
        CoreParkingPaymentType.CARDS_SMARTCARD -> ParkingPaymentType.CARDS_SMARTCARD
        CoreParkingPaymentType.CARDS_TELEPEAGE -> ParkingPaymentType.CARDS_TELEPEAGE
        CoreParkingPaymentType.CARDS_TOTALGR -> ParkingPaymentType.CARDS_TOTALGR
        CoreParkingPaymentType.CARDS_MONEO -> ParkingPaymentType.CARDS_MONEO
        CoreParkingPaymentType.CARDS_FLASHPAY -> ParkingPaymentType.CARDS_FLASHPAY
        CoreParkingPaymentType.CARDS_CASHCARD -> ParkingPaymentType.CARDS_CASHCARD
        CoreParkingPaymentType.CARDS_VCASHCARD -> ParkingPaymentType.CARDS_VCASHCARD
        CoreParkingPaymentType.CARDS_CEPAS -> ParkingPaymentType.CARDS_CEPAS
        CoreParkingPaymentType.CARDS_OCTOPUS -> ParkingPaymentType.CARDS_OCTOPUS
        CoreParkingPaymentType.ALIPAY -> ParkingPaymentType.ALIPAY
        CoreParkingPaymentType.WECHATPAY -> ParkingPaymentType.WECHATPAY
        CoreParkingPaymentType.CARDS_EASYCARD -> ParkingPaymentType.CARDS_EASYCARD
        CoreParkingPaymentType.CARDS_CARTEBLEUE -> ParkingPaymentType.CARDS_CARTEBLEUE
        CoreParkingPaymentType.CARDS_TOUCHNGO -> ParkingPaymentType.CARDS_TOUCHNGO
        CoreParkingPaymentType.UNKNOWN -> ParkingPaymentType.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun createCoreParkingPaymentType(@ParkingPaymentType.Type type: String): CoreParkingPaymentType? {
    return when (type) {
        ParkingPaymentType.COINS -> CoreParkingPaymentType.COINS
        ParkingPaymentType.NOTES -> CoreParkingPaymentType.NOTES
        ParkingPaymentType.CONTACTLESS -> CoreParkingPaymentType.CONTACTLESS
        ParkingPaymentType.CARDS -> CoreParkingPaymentType.CARDS
        ParkingPaymentType.MOBILE -> CoreParkingPaymentType.MOBILE
        ParkingPaymentType.CARDS_VISA -> CoreParkingPaymentType.CARDS_VISA
        ParkingPaymentType.CARDS_MASTERCARD -> CoreParkingPaymentType.CARDS_MASTERCARD
        ParkingPaymentType.CARDS_AMEX -> CoreParkingPaymentType.CARDS_AMEX
        ParkingPaymentType.CARDS_MAESTRO -> CoreParkingPaymentType.CARDS_MAESTRO
        ParkingPaymentType.EFTPOS -> CoreParkingPaymentType.EFTPOS
        ParkingPaymentType.CARDS_DINERS -> CoreParkingPaymentType.CARDS_DINERS
        ParkingPaymentType.CARDS_GELDKARTE -> CoreParkingPaymentType.CARDS_GELDKARTE
        ParkingPaymentType.CARDS_DISCOVER -> CoreParkingPaymentType.CARDS_DISCOVER
        ParkingPaymentType.CHEQUE -> CoreParkingPaymentType.CHEQUE
        ParkingPaymentType.CARDS_ECASH -> CoreParkingPaymentType.CARDS_ECASH
        ParkingPaymentType.CARDS_JCB -> CoreParkingPaymentType.CARDS_JCB
        ParkingPaymentType.CARDS_OPERATORCARD -> CoreParkingPaymentType.CARDS_OPERATORCARD
        ParkingPaymentType.CARDS_SMARTCARD -> CoreParkingPaymentType.CARDS_SMARTCARD
        ParkingPaymentType.CARDS_TELEPEAGE -> CoreParkingPaymentType.CARDS_TELEPEAGE
        ParkingPaymentType.CARDS_TOTALGR -> CoreParkingPaymentType.CARDS_TOTALGR
        ParkingPaymentType.CARDS_MONEO -> CoreParkingPaymentType.CARDS_MONEO
        ParkingPaymentType.CARDS_FLASHPAY -> CoreParkingPaymentType.CARDS_FLASHPAY
        ParkingPaymentType.CARDS_CASHCARD -> CoreParkingPaymentType.CARDS_CASHCARD
        ParkingPaymentType.CARDS_VCASHCARD -> CoreParkingPaymentType.CARDS_VCASHCARD
        ParkingPaymentType.CARDS_CEPAS -> CoreParkingPaymentType.CARDS_CEPAS
        ParkingPaymentType.CARDS_OCTOPUS -> CoreParkingPaymentType.CARDS_OCTOPUS
        ParkingPaymentType.ALIPAY -> CoreParkingPaymentType.ALIPAY
        ParkingPaymentType.WECHATPAY -> CoreParkingPaymentType.WECHATPAY
        ParkingPaymentType.CARDS_EASYCARD -> CoreParkingPaymentType.CARDS_EASYCARD
        ParkingPaymentType.CARDS_CARTEBLEUE -> CoreParkingPaymentType.CARDS_CARTEBLEUE
        ParkingPaymentType.CARDS_TOUCHNGO -> CoreParkingPaymentType.CARDS_TOUCHNGO
        ParkingPaymentType.UNKNOWN -> CoreParkingPaymentType.UNKNOWN
        else -> null
    }
}
