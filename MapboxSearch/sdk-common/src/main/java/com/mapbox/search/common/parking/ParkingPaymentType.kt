package com.mapbox.search.common.parking

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * Available payment types
 */
@MapboxExperimental
public object ParkingPaymentType {

    /**
     * Pay using coins.
     */
    public const val COINS: String = "COINS"

    /**
     * Pay using banknotes.
     */
    public const val NOTES: String = "NOTES"

    /**
     * Contactless payment methods (e.g., NFC cards, smart devices).
     */
    public const val CONTACTLESS: String = "CONTACTLESS"

    /**
     * Generic payment with cards.
     */
    public const val CARDS: String = "CARDS"

    /**
     * Mobile payment applications.
     */
    public const val MOBILE: String = "MOBILE"

    /**
     * Visa card payment.
     */
    public const val CARDS_VISA: String = "CARDS_VISA"

    /**
     * Mastercard card payment.
     */
    public const val CARDS_MASTERCARD: String = "CARDS_MASTERCARD"

    /**
     * American Express card payment.
     */
    public const val CARDS_AMEX: String = "CARDS_AMEX"

    /**
     * Maestro card payment.
     */
    public const val CARDS_MAESTRO: String = "CARDS_MAESTRO"

    /**
     * EFTPOS payment system.
     */
    public const val EFTPOS: String = "EFTPOS"

    /**
     * Diners Club card payment.
     */
    public const val CARDS_DINERS: String = "CARDS_DINERS"

    /**
     * GeldKarte payment system.
     */
    public const val CARDS_GELDKARTE: String = "CARDS_GELDKARTE"

    /**
     * Discover card payment.
     */
    public const val CARDS_DISCOVER: String = "CARDS_DISCOVER"

    /**
     * Pay using cheques.
     */
    public const val CHEQUE: String = "CHEQUE"

    /**
     * ECash payment system.
     */
    public const val CARDS_ECASH: String = "CARDS_ECASH"

    /**
     * JCB card payment.
     */
    public const val CARDS_JCB: String = "CARDS_JCB"

    /**
     * Operator-issued payment card.
     */
    public const val CARDS_OPERATORCARD: String = "CARDS_OPERATORCARD"

    /**
     * Generic smart card payment.
     */
    public const val CARDS_SMARTCARD: String = "CARDS_SMARTCARD"

    /**
     * Télépéage electronic toll collection.
     */
    public const val CARDS_TELEPEAGE: String = "CARDS_TELEPEAGE"

    /**
     * TotalGR card payment.
     */
    public const val CARDS_TOTALGR: String = "CARDS_TOTALGR"

    /**
     * Moneo card payment system.
     */
    public const val CARDS_MONEO: String = "CARDS_MONEO"

    /**
     * FlashPay contactless smart card.
     */
    public const val CARDS_FLASHPAY: String = "CARDS_FLASHPAY"

    /**
     * CashCard payment system.
     */
    public const val CARDS_CASHCARD: String = "CARDS_CASHCARD"

    /**
     * VCashCard payment system.
     */
    public const val CARDS_VCASHCARD: String = "CARDS_VCASHCARD"

    /**
     * CEPAS card payment system.
     */
    public const val CARDS_CEPAS: String = "CARDS_CEPAS"

    /**
     * Octopus card payment system.
     */
    public const val CARDS_OCTOPUS: String = "CARDS_OCTOPUS"

    /**
     * Alipay mobile payment.
     */
    public const val ALIPAY: String = "ALIPAY"

    /**
     * WeChat Pay mobile payment.
     */
    public const val WECHATPAY: String = "WECHATPAY"

    /**
     * EasyCard payment system.
     */
    public const val CARDS_EASYCARD: String = "CARDS_EASYCARD"

    /**
     * Carte Bleue card payment system.
     */
    public const val CARDS_CARTEBLEUE: String = "CARDS_CARTEBLEUE"

    /**
     * Touch 'n Go card payment.
     */
    public const val CARDS_TOUCHNGO: String = "CARDS_TOUCHNGO"

    /**
     * Payment type is unknown.
     */
    public const val UNKNOWN: String = "UNKNOWN"

    /**
     * Retention policy for [ParkingPaymentType] values.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        COINS,
        NOTES,
        CONTACTLESS,
        CARDS,
        MOBILE,
        CARDS_VISA,
        CARDS_MASTERCARD,
        CARDS_AMEX,
        CARDS_MAESTRO,
        EFTPOS,
        CARDS_DINERS,
        CARDS_GELDKARTE,
        CARDS_DISCOVER,
        CHEQUE,
        CARDS_ECASH,
        CARDS_JCB,
        CARDS_OPERATORCARD,
        CARDS_SMARTCARD,
        CARDS_TELEPEAGE,
        CARDS_TOTALGR,
        CARDS_MONEO,
        CARDS_FLASHPAY,
        CARDS_CASHCARD,
        CARDS_VCASHCARD,
        CARDS_CEPAS,
        CARDS_OCTOPUS,
        ALIPAY,
        WECHATPAY,
        CARDS_EASYCARD,
        CARDS_CARTEBLEUE,
        CARDS_TOUCHNGO,
        UNKNOWN,
    )
    public annotation class Type
}
