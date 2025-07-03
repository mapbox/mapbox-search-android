@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.parking

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreParkingPaymentType
import com.mapbox.search.common.parking.ParkingPaymentType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ParkingPaymentTypeTest {

    @Test
    fun `CoreParkingPaymentType mapToPlatform() test`() {
        assertEquals(ParkingPaymentType.COINS, CoreParkingPaymentType.COINS.mapToPlatform())
        assertEquals(ParkingPaymentType.NOTES, CoreParkingPaymentType.NOTES.mapToPlatform())
        assertEquals(ParkingPaymentType.CONTACTLESS, CoreParkingPaymentType.CONTACTLESS.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS, CoreParkingPaymentType.CARDS.mapToPlatform())
        assertEquals(ParkingPaymentType.MOBILE, CoreParkingPaymentType.MOBILE.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_VISA, CoreParkingPaymentType.CARDS_VISA.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_MASTERCARD, CoreParkingPaymentType.CARDS_MASTERCARD.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_AMEX, CoreParkingPaymentType.CARDS_AMEX.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_MAESTRO, CoreParkingPaymentType.CARDS_MAESTRO.mapToPlatform())
        assertEquals(ParkingPaymentType.EFTPOS, CoreParkingPaymentType.EFTPOS.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_DINERS, CoreParkingPaymentType.CARDS_DINERS.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_GELDKARTE, CoreParkingPaymentType.CARDS_GELDKARTE.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_DISCOVER, CoreParkingPaymentType.CARDS_DISCOVER.mapToPlatform())
        assertEquals(ParkingPaymentType.CHEQUE, CoreParkingPaymentType.CHEQUE.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_ECASH, CoreParkingPaymentType.CARDS_ECASH.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_JCB, CoreParkingPaymentType.CARDS_JCB.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_OPERATORCARD, CoreParkingPaymentType.CARDS_OPERATORCARD.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_SMARTCARD, CoreParkingPaymentType.CARDS_SMARTCARD.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_TELEPEAGE, CoreParkingPaymentType.CARDS_TELEPEAGE.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_TOTALGR, CoreParkingPaymentType.CARDS_TOTALGR.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_MONEO, CoreParkingPaymentType.CARDS_MONEO.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_FLASHPAY, CoreParkingPaymentType.CARDS_FLASHPAY.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_CASHCARD, CoreParkingPaymentType.CARDS_CASHCARD.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_VCASHCARD, CoreParkingPaymentType.CARDS_VCASHCARD.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_CEPAS, CoreParkingPaymentType.CARDS_CEPAS.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_OCTOPUS, CoreParkingPaymentType.CARDS_OCTOPUS.mapToPlatform())
        assertEquals(ParkingPaymentType.ALIPAY, CoreParkingPaymentType.ALIPAY.mapToPlatform())
        assertEquals(ParkingPaymentType.WECHATPAY, CoreParkingPaymentType.WECHATPAY.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_EASYCARD, CoreParkingPaymentType.CARDS_EASYCARD.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_CARTEBLEUE, CoreParkingPaymentType.CARDS_CARTEBLEUE.mapToPlatform())
        assertEquals(ParkingPaymentType.CARDS_TOUCHNGO, CoreParkingPaymentType.CARDS_TOUCHNGO.mapToPlatform())
        assertEquals(ParkingPaymentType.UNKNOWN, CoreParkingPaymentType.UNKNOWN.mapToPlatform())
    }

    @Test
    fun `createCoreParkingPaymentType() test`() {
        assertEquals(CoreParkingPaymentType.COINS, createCoreParkingPaymentType(ParkingPaymentType.COINS))
        assertEquals(CoreParkingPaymentType.NOTES, createCoreParkingPaymentType(ParkingPaymentType.NOTES))
        assertEquals(CoreParkingPaymentType.CONTACTLESS, createCoreParkingPaymentType(ParkingPaymentType.CONTACTLESS))
        assertEquals(CoreParkingPaymentType.CARDS, createCoreParkingPaymentType(ParkingPaymentType.CARDS))
        assertEquals(CoreParkingPaymentType.MOBILE, createCoreParkingPaymentType(ParkingPaymentType.MOBILE))
        assertEquals(CoreParkingPaymentType.CARDS_VISA, createCoreParkingPaymentType(ParkingPaymentType.CARDS_VISA))
        assertEquals(CoreParkingPaymentType.CARDS_MASTERCARD, createCoreParkingPaymentType(ParkingPaymentType.CARDS_MASTERCARD))
        assertEquals(CoreParkingPaymentType.CARDS_AMEX, createCoreParkingPaymentType(ParkingPaymentType.CARDS_AMEX))
        assertEquals(CoreParkingPaymentType.CARDS_MAESTRO, createCoreParkingPaymentType(ParkingPaymentType.CARDS_MAESTRO))
        assertEquals(CoreParkingPaymentType.EFTPOS, createCoreParkingPaymentType(ParkingPaymentType.EFTPOS))
        assertEquals(CoreParkingPaymentType.CARDS_DINERS, createCoreParkingPaymentType(ParkingPaymentType.CARDS_DINERS))
        assertEquals(CoreParkingPaymentType.CARDS_GELDKARTE, createCoreParkingPaymentType(ParkingPaymentType.CARDS_GELDKARTE))
        assertEquals(CoreParkingPaymentType.CARDS_DISCOVER, createCoreParkingPaymentType(ParkingPaymentType.CARDS_DISCOVER))
        assertEquals(CoreParkingPaymentType.CHEQUE, createCoreParkingPaymentType(ParkingPaymentType.CHEQUE))
        assertEquals(CoreParkingPaymentType.CARDS_ECASH, createCoreParkingPaymentType(ParkingPaymentType.CARDS_ECASH))
        assertEquals(CoreParkingPaymentType.CARDS_JCB, createCoreParkingPaymentType(ParkingPaymentType.CARDS_JCB))
        assertEquals(CoreParkingPaymentType.CARDS_OPERATORCARD, createCoreParkingPaymentType(ParkingPaymentType.CARDS_OPERATORCARD))
        assertEquals(CoreParkingPaymentType.CARDS_SMARTCARD, createCoreParkingPaymentType(ParkingPaymentType.CARDS_SMARTCARD))
        assertEquals(CoreParkingPaymentType.CARDS_TELEPEAGE, createCoreParkingPaymentType(ParkingPaymentType.CARDS_TELEPEAGE))
        assertEquals(CoreParkingPaymentType.CARDS_TOTALGR, createCoreParkingPaymentType(ParkingPaymentType.CARDS_TOTALGR))
        assertEquals(CoreParkingPaymentType.CARDS_MONEO, createCoreParkingPaymentType(ParkingPaymentType.CARDS_MONEO))
        assertEquals(CoreParkingPaymentType.CARDS_FLASHPAY, createCoreParkingPaymentType(ParkingPaymentType.CARDS_FLASHPAY))
        assertEquals(CoreParkingPaymentType.CARDS_CASHCARD, createCoreParkingPaymentType(ParkingPaymentType.CARDS_CASHCARD))
        assertEquals(CoreParkingPaymentType.CARDS_VCASHCARD, createCoreParkingPaymentType(ParkingPaymentType.CARDS_VCASHCARD))
        assertEquals(CoreParkingPaymentType.CARDS_CEPAS, createCoreParkingPaymentType(ParkingPaymentType.CARDS_CEPAS))
        assertEquals(CoreParkingPaymentType.CARDS_OCTOPUS, createCoreParkingPaymentType(ParkingPaymentType.CARDS_OCTOPUS))
        assertEquals(CoreParkingPaymentType.ALIPAY, createCoreParkingPaymentType(ParkingPaymentType.ALIPAY))
        assertEquals(CoreParkingPaymentType.WECHATPAY, createCoreParkingPaymentType(ParkingPaymentType.WECHATPAY))
        assertEquals(CoreParkingPaymentType.CARDS_EASYCARD, createCoreParkingPaymentType(ParkingPaymentType.CARDS_EASYCARD))
        assertEquals(CoreParkingPaymentType.CARDS_CARTEBLEUE, createCoreParkingPaymentType(ParkingPaymentType.CARDS_CARTEBLEUE))
        assertEquals(CoreParkingPaymentType.CARDS_TOUCHNGO, createCoreParkingPaymentType(ParkingPaymentType.CARDS_TOUCHNGO))
        assertEquals(CoreParkingPaymentType.UNKNOWN, createCoreParkingPaymentType(ParkingPaymentType.UNKNOWN))

        assertNull(createCoreParkingPaymentType("UNKNOWN_PAYMENT_TYPE_123"))
    }
}
