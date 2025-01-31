@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreConnector
import com.mapbox.search.base.core.CoreConnectorFormat
import com.mapbox.search.base.core.CoreConnectorType
import com.mapbox.search.common.ev.EvConnector
import com.mapbox.search.common.ev.EvConnectorFormat
import com.mapbox.search.common.ev.EvConnectorType

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreConnector.mapToPlatform(): EvConnector {
    return EvConnector(
        id = id,
        standard = standard.mapToPlatform(),
        format = format.mapToPlatform(),
        powerType = powerType.mapToPlatform(),
        maxVoltage = maxVoltage,
        maxAmperage = maxAmperage,
        maxElectricPower = maxElectricPower,
        tariffIds = tariffIds,
        termsAndConditions = termsAndConditions,
        lastUpdated = lastUpdated,
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvConnectorFormat.Type
fun CoreConnectorFormat.mapToPlatform(): String {
    return when (this) {
        CoreConnectorFormat.SOCKET -> EvConnectorFormat.SOCKET
        CoreConnectorFormat.CABLE -> EvConnectorFormat.CABLE
        CoreConnectorFormat.UNKNOWN -> EvConnectorFormat.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun createCoreConnectorType(@EvConnectorType.Type type: String): CoreConnectorType? {
    return when (type) {
        EvConnectorType.CHADEMO -> CoreConnectorType.CHADEMO
        EvConnectorType.CHAOJI -> CoreConnectorType.CHAOJI
        EvConnectorType.DOMESTIC_A -> CoreConnectorType.DOMESTIC_A
        EvConnectorType.DOMESTIC_B -> CoreConnectorType.DOMESTIC_B
        EvConnectorType.DOMESTIC_C -> CoreConnectorType.DOMESTIC_C
        EvConnectorType.DOMESTIC_D -> CoreConnectorType.DOMESTIC_D
        EvConnectorType.DOMESTIC_E -> CoreConnectorType.DOMESTIC_E
        EvConnectorType.DOMESTIC_F -> CoreConnectorType.DOMESTIC_F
        EvConnectorType.DOMESTIC_G -> CoreConnectorType.DOMESTIC_G
        EvConnectorType.DOMESTIC_H -> CoreConnectorType.DOMESTIC_H
        EvConnectorType.DOMESTIC_I -> CoreConnectorType.DOMESTIC_I
        EvConnectorType.DOMESTIC_J -> CoreConnectorType.DOMESTIC_J
        EvConnectorType.DOMESTIC_K -> CoreConnectorType.DOMESTIC_K
        EvConnectorType.DOMESTIC_L -> CoreConnectorType.DOMESTIC_L
        EvConnectorType.DOMESTIC_M -> CoreConnectorType.DOMESTIC_M
        EvConnectorType.DOMESTIC_N -> CoreConnectorType.DOMESTIC_N
        EvConnectorType.DOMESTIC_O -> CoreConnectorType.DOMESTIC_O
        EvConnectorType.GBT_AC -> CoreConnectorType.GBT_AC
        EvConnectorType.GBT_DC -> CoreConnectorType.GBT_DC
        EvConnectorType.IEC_60309_2_SINGLE_16 -> CoreConnectorType.IEC_60309_2_SINGLE_16
        EvConnectorType.IEC_60309_2_THREE_16 -> CoreConnectorType.IEC_60309_2_THREE_16
        EvConnectorType.IEC_60309_2_THREE_32 -> CoreConnectorType.IEC_60309_2_THREE_32
        EvConnectorType.IEC_60309_2_THREE_64 -> CoreConnectorType.IEC_60309_2_THREE_64
        EvConnectorType.IEC_62196_T1 -> CoreConnectorType.IEC_62196_T1
        EvConnectorType.IEC_62196_T1_COMBO -> CoreConnectorType.IEC_62196_T1_COMBO
        EvConnectorType.IEC_62196_T2 -> CoreConnectorType.IEC_62196_T2
        EvConnectorType.IEC_62196_T2_COMBO -> CoreConnectorType.IEC_62196_T2_COMBO
        EvConnectorType.IEC_62196_T3_A -> CoreConnectorType.IEC_62196_T3_A
        EvConnectorType.IEC_62196_T3_C -> CoreConnectorType.IEC_62196_T3_C
        EvConnectorType.NEMA_5_20 -> CoreConnectorType.NEMA_5_20
        EvConnectorType.NEMA_6_30 -> CoreConnectorType.NEMA_6_30
        EvConnectorType.NEMA_6_50 -> CoreConnectorType.NEMA_6_50
        EvConnectorType.NEMA_10_30 -> CoreConnectorType.NEMA_10_30
        EvConnectorType.NEMA_10_50 -> CoreConnectorType.NEMA_10_50
        EvConnectorType.NEMA_14_30 -> CoreConnectorType.NEMA_14_30
        EvConnectorType.NEMA_14_50 -> CoreConnectorType.NEMA_14_50
        EvConnectorType.PANTOGRAPH_BOTTOM_UP -> CoreConnectorType.PANTOGRAPH_BOTTOM_UP
        EvConnectorType.PANTOGRAPH_TOP_DOWN -> CoreConnectorType.PANTOGRAPH_TOP_DOWN
        EvConnectorType.TESLA_R -> CoreConnectorType.TESLA_R
        EvConnectorType.TESLA_S -> CoreConnectorType.TESLA_S
        EvConnectorType.UNKNOWN -> CoreConnectorType.UNKNOWN
        else -> null
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvConnectorType.Type
fun CoreConnectorType.mapToPlatform(): String {
    return when (this) {
        CoreConnectorType.CHADEMO -> EvConnectorType.CHADEMO
        CoreConnectorType.CHAOJI -> EvConnectorType.CHAOJI
        CoreConnectorType.DOMESTIC_A -> EvConnectorType.DOMESTIC_A
        CoreConnectorType.DOMESTIC_B -> EvConnectorType.DOMESTIC_B
        CoreConnectorType.DOMESTIC_C -> EvConnectorType.DOMESTIC_C
        CoreConnectorType.DOMESTIC_D -> EvConnectorType.DOMESTIC_D
        CoreConnectorType.DOMESTIC_E -> EvConnectorType.DOMESTIC_E
        CoreConnectorType.DOMESTIC_F -> EvConnectorType.DOMESTIC_F
        CoreConnectorType.DOMESTIC_G -> EvConnectorType.DOMESTIC_G
        CoreConnectorType.DOMESTIC_H -> EvConnectorType.DOMESTIC_H
        CoreConnectorType.DOMESTIC_I -> EvConnectorType.DOMESTIC_I
        CoreConnectorType.DOMESTIC_J -> EvConnectorType.DOMESTIC_J
        CoreConnectorType.DOMESTIC_K -> EvConnectorType.DOMESTIC_K
        CoreConnectorType.DOMESTIC_L -> EvConnectorType.DOMESTIC_L
        CoreConnectorType.DOMESTIC_M -> EvConnectorType.DOMESTIC_M
        CoreConnectorType.DOMESTIC_N -> EvConnectorType.DOMESTIC_N
        CoreConnectorType.DOMESTIC_O -> EvConnectorType.DOMESTIC_O
        CoreConnectorType.GBT_AC -> EvConnectorType.GBT_AC
        CoreConnectorType.GBT_DC -> EvConnectorType.GBT_DC
        CoreConnectorType.IEC_60309_2_SINGLE_16 -> EvConnectorType.IEC_60309_2_SINGLE_16
        CoreConnectorType.IEC_60309_2_THREE_16 -> EvConnectorType.IEC_60309_2_THREE_16
        CoreConnectorType.IEC_60309_2_THREE_32 -> EvConnectorType.IEC_60309_2_THREE_32
        CoreConnectorType.IEC_60309_2_THREE_64 -> EvConnectorType.IEC_60309_2_THREE_64
        CoreConnectorType.IEC_62196_T1 -> EvConnectorType.IEC_62196_T1
        CoreConnectorType.IEC_62196_T1_COMBO -> EvConnectorType.IEC_62196_T1_COMBO
        CoreConnectorType.IEC_62196_T2 -> EvConnectorType.IEC_62196_T2
        CoreConnectorType.IEC_62196_T2_COMBO -> EvConnectorType.IEC_62196_T2_COMBO
        CoreConnectorType.IEC_62196_T3_A -> EvConnectorType.IEC_62196_T3_A
        CoreConnectorType.IEC_62196_T3_C -> EvConnectorType.IEC_62196_T3_C
        CoreConnectorType.NEMA_5_20 -> EvConnectorType.NEMA_5_20
        CoreConnectorType.NEMA_6_30 -> EvConnectorType.NEMA_6_30
        CoreConnectorType.NEMA_6_50 -> EvConnectorType.NEMA_6_50
        CoreConnectorType.NEMA_10_30 -> EvConnectorType.NEMA_10_30
        CoreConnectorType.NEMA_10_50 -> EvConnectorType.NEMA_10_50
        CoreConnectorType.NEMA_14_30 -> EvConnectorType.NEMA_14_30
        CoreConnectorType.NEMA_14_50 -> EvConnectorType.NEMA_14_50
        CoreConnectorType.PANTOGRAPH_BOTTOM_UP -> EvConnectorType.PANTOGRAPH_BOTTOM_UP
        CoreConnectorType.PANTOGRAPH_TOP_DOWN -> EvConnectorType.PANTOGRAPH_TOP_DOWN
        CoreConnectorType.TESLA_R -> EvConnectorType.TESLA_R
        CoreConnectorType.TESLA_S -> EvConnectorType.TESLA_S
        CoreConnectorType.UNKNOWN -> EvConnectorType.UNKNOWN
    }
}
