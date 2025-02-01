package com.mapbox.search.common.ev

import androidx.annotation.StringDef
import com.mapbox.annotation.MapboxExperimental

/**
 * OCPI ConnectorType. The socket or plug standard of the charge point.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#145-connectortype-enum)
 * for more details.
 */
@MapboxExperimental
public object EvConnectorType {

    /**
     * The connector type is CHAdeMO, DC.
     */
    public const val CHADEMO: String = "CHADEMO"

    /**
     * The ChaoJi connector. The new generation charging connector, harmonized between CHAdeMO and GB/T. DC.
     */
    public const val CHAOJI: String = "CHAOJI"

    /**
     * Standard/Domestic household, type "A", NEMA 1-15, 2 pins.
     */
    public const val DOMESTIC_A: String = "DOMESTIC_A"

    /**
     * Standard/Domestic household, type "B", NEMA 5-15, 3 pins.
     */
    public const val DOMESTIC_B: String = "DOMESTIC_B"

    /**
     * Standard/Domestic household, type "C", CEE 7/17, 2 pins.
     */
    public const val DOMESTIC_C: String = "DOMESTIC_C"

    /**
     * Standard/Domestic household, type "D", 3 pins.
     */
    public const val DOMESTIC_D: String = "DOMESTIC_D"

    /**
     * Standard/Domestic household, type "E", CEE 7/5 3 pins.
     */
    public const val DOMESTIC_E: String = "DOMESTIC_E"

    /**
     * Standard/Domestic household, type "F", CEE 7/4, Schuko, 3 pins.
     */
    public const val DOMESTIC_F: String = "DOMESTIC_F"

    /**
     * Standard/Domestic household, type "G", BS 1363, Commonwealth, 3 pins.
     */
    public const val DOMESTIC_G: String = "DOMESTIC_G"

    /**
     * Standard/Domestic household, type "H", SI-32, 3 pins.
     */
    public const val DOMESTIC_H: String = "DOMESTIC_H"

    /**
     * Standard/Domestic household, type "I", AS 3112, 3 pins.
     */
    public const val DOMESTIC_I: String = "DOMESTIC_I"

    /**
     * Standard/Domestic household, type "J", SEV 1011, 3 pins.
     */
    public const val DOMESTIC_J: String = "DOMESTIC_J"

    /**
     * Standard/Domestic household, type "K", DS 60884-2-D1, 3 pins.
     */
    public const val DOMESTIC_K: String = "DOMESTIC_K"

    /**
     * Standard/Domestic household, type "L", CEI 23-16-VII, 3 pins.
     */
    public const val DOMESTIC_L: String = "DOMESTIC_L"

    /**
     * Standard/Domestic household, type "M", BS 546, 3 pins.
     */
    public const val DOMESTIC_M: String = "DOMESTIC_M"

    /**
     * Standard/Domestic household, type "N", NBR 14136, 3 pins.
     */
    public const val DOMESTIC_N: String = "DOMESTIC_N"

    /**
     * Standard/Domestic household, type "O", TIS 166-2549, 3 pins.
     */
    public const val DOMESTIC_O: String = "DOMESTIC_O"

    /**
     * Guobiao GB/T 20234.2 AC socket/connector.
     */
    public const val GBT_AC: String = "GBT_AC"

    /**
     * Guobiao GB/T 20234.3 DC connector.
     */
    public const val GBT_DC: String = "GBT_DC"

    /**
     * IEC 60309-2 Industrial Connector single phase 16 amperes (usually blue).
     */
    public const val IEC_60309_2_SINGLE_16: String = "IEC_60309_2_SINGLE_16"

    /**
     * IEC 60309-2 Industrial Connector three phases 16 amperes (usually red).
     */
    public const val IEC_60309_2_THREE_16: String = "IEC_60309_2_THREE_16"

    /**
     * IEC 60309-2 Industrial Connector three phases 32 amperes (usually red).
     */
    public const val IEC_60309_2_THREE_32: String = "IEC_60309_2_THREE_32"

    /**
     * IEC 60309-2 Industrial Connector three phases 64 amperes (usually red).
     */
    public const val IEC_60309_2_THREE_64: String = "IEC_60309_2_THREE_64"

    /**
     * IEC 62196 Type 1 "SAE J1772".
     */
    public const val IEC_62196_T1: String = "IEC_62196_T1"

    /**
     * Combo Type 1 based, DC.
     */
    public const val IEC_62196_T1_COMBO: String = "IEC_62196_T1_COMBO"

    /**
     * IEC 62196 Type 2 "Mennekes".
     */
    public const val IEC_62196_T2: String = "IEC_62196_T2"

    /**
     * Combo Type 2 based, DC.
     */
    public const val IEC_62196_T2_COMBO: String = "IEC_62196_T2_COMBO"

    /**
     * IEC 62196 Type 3A.
     */
    public const val IEC_62196_T3_A: String = "IEC_62196_T3_A"

    /**
     * IEC 62196 Type 3C "Scame".
     */
    public const val IEC_62196_T3_C: String = "IEC_62196_T3_C"

    /**
     * NEMA 5-20, 3 pins.
     */
    public const val NEMA_5_20: String = "NEMA_5_20"

    /**
     * NEMA 6-30, 3 pins.
     */
    public const val NEMA_6_30: String = "NEMA_6_30"

    /**
     * NEMA 6-50, 3 pins.
     */
    public const val NEMA_6_50: String = "NEMA_6_50"

    /**
     * NEMA 10-30, 3 pins.
     */
    public const val NEMA_10_30: String = "NEMA_10_30"

    /**
     * NEMA 10-50, 3 pins.
     */
    public const val NEMA_10_50: String = "NEMA_10_50"

    /**
     * NEMA 14-30, 3 pins, rating of 30 A.
     */
    public const val NEMA_14_30: String = "NEMA_14_30"

    /**
     * NEMA 14-50, 3 pins, rating of 50 A.
     */
    public const val NEMA_14_50: String = "NEMA_14_50"

    /**
     * On-board Bottom up Pantograph typically for bus charging.
     */
    public const val PANTOGRAPH_BOTTOM_UP: String = "PANTOGRAPH_BOTTOM_UP"

    /**
     * Off-board Top down Pantograph typically for bus charging.
     */
    public const val PANTOGRAPH_TOP_DOWN: String = "PANTOGRAPH_TOP_DOWN"

    /**
     * Tesla Connector "Roadster"-type (round, 4 pin).
     */
    public const val TESLA_R: String = "TESLA_R"

    /**
     * Tesla Connector "Model-S"-type (oval, 5 pin).
     */
    public const val TESLA_S: String = "TESLA_S"

    /**
     * Connector type is unknown.
     */
    public const val UNKNOWN: String = "UNKNOWN"

    /**
     * Retention policy for the ConnectorType.
     */
    @Retention(AnnotationRetention.BINARY)
    @StringDef(
        CHADEMO,
        CHAOJI,
        DOMESTIC_A,
        DOMESTIC_B,
        DOMESTIC_C,
        DOMESTIC_D,
        DOMESTIC_E,
        DOMESTIC_F,
        DOMESTIC_G,
        DOMESTIC_H,
        DOMESTIC_I,
        DOMESTIC_J,
        DOMESTIC_K,
        DOMESTIC_L,
        DOMESTIC_M,
        DOMESTIC_N,
        DOMESTIC_O,
        GBT_AC,
        GBT_DC,
        IEC_60309_2_SINGLE_16,
        IEC_60309_2_THREE_16,
        IEC_60309_2_THREE_32,
        IEC_60309_2_THREE_64,
        IEC_62196_T1,
        IEC_62196_T1_COMBO,
        IEC_62196_T2,
        IEC_62196_T2_COMBO,
        IEC_62196_T3_A,
        IEC_62196_T3_C,
        NEMA_5_20,
        NEMA_6_30,
        NEMA_6_50,
        NEMA_10_30,
        NEMA_10_50,
        NEMA_14_30,
        NEMA_14_50,
        PANTOGRAPH_BOTTOM_UP,
        PANTOGRAPH_TOP_DOWN,
        TESLA_R,
        TESLA_S,
        UNKNOWN,
    )
    public annotation class Type
}
