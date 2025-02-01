package com.mapbox.search.base.factory

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreConnectorFormat
import com.mapbox.search.base.core.CoreConnectorType
import com.mapbox.search.common.ev.EvConnector
import com.mapbox.search.common.ev.EvConnectorFormat
import com.mapbox.search.common.ev.EvConnectorType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
class EvConnectorTest {

    @Test
    fun `mapToPlatform test`() {
        val core = TestObjects.CORE_CONNECTOR

        val expected = EvConnector(
            id = core.id,
            standard = core.standard.mapToPlatform(),
            format = core.format.mapToPlatform(),
            powerType = core.powerType.mapToPlatform(),
            maxVoltage = core.maxVoltage,
            maxAmperage = core.maxAmperage,
            maxElectricPower = core.maxElectricPower,
            tariffIds = core.tariffIds,
            termsAndConditions = core.termsAndConditions,
            lastUpdated = core.lastUpdated
        )

        assertEquals(expected, core.mapToPlatform())
    }

    @Test
    fun `CoreConnectorFormat mapToPlatform() test`() {
        assertEquals(EvConnectorFormat.SOCKET, CoreConnectorFormat.SOCKET.mapToPlatform())
        assertEquals(EvConnectorFormat.CABLE, CoreConnectorFormat.CABLE.mapToPlatform())
        assertEquals(EvConnectorFormat.UNKNOWN, CoreConnectorFormat.UNKNOWN.mapToPlatform())
    }

    @Test
    fun `createCoreConnectorType() test`() {
        assertEquals(CoreConnectorType.CHADEMO, createCoreConnectorType(EvConnectorType.CHADEMO))
        assertEquals(CoreConnectorType.CHAOJI, createCoreConnectorType(EvConnectorType.CHAOJI))
        assertEquals(CoreConnectorType.DOMESTIC_A, createCoreConnectorType(EvConnectorType.DOMESTIC_A))
        assertEquals(CoreConnectorType.DOMESTIC_B, createCoreConnectorType(EvConnectorType.DOMESTIC_B))
        assertEquals(CoreConnectorType.DOMESTIC_C, createCoreConnectorType(EvConnectorType.DOMESTIC_C))
        assertEquals(CoreConnectorType.DOMESTIC_D, createCoreConnectorType(EvConnectorType.DOMESTIC_D))
        assertEquals(CoreConnectorType.DOMESTIC_E, createCoreConnectorType(EvConnectorType.DOMESTIC_E))
        assertEquals(CoreConnectorType.DOMESTIC_F, createCoreConnectorType(EvConnectorType.DOMESTIC_F))
        assertEquals(CoreConnectorType.DOMESTIC_G, createCoreConnectorType(EvConnectorType.DOMESTIC_G))
        assertEquals(CoreConnectorType.DOMESTIC_H, createCoreConnectorType(EvConnectorType.DOMESTIC_H))
        assertEquals(CoreConnectorType.DOMESTIC_I, createCoreConnectorType(EvConnectorType.DOMESTIC_I))
        assertEquals(CoreConnectorType.DOMESTIC_J, createCoreConnectorType(EvConnectorType.DOMESTIC_J))
        assertEquals(CoreConnectorType.DOMESTIC_K, createCoreConnectorType(EvConnectorType.DOMESTIC_K))
        assertEquals(CoreConnectorType.DOMESTIC_L, createCoreConnectorType(EvConnectorType.DOMESTIC_L))
        assertEquals(CoreConnectorType.DOMESTIC_M, createCoreConnectorType(EvConnectorType.DOMESTIC_M))
        assertEquals(CoreConnectorType.DOMESTIC_N, createCoreConnectorType(EvConnectorType.DOMESTIC_N))
        assertEquals(CoreConnectorType.DOMESTIC_O, createCoreConnectorType(EvConnectorType.DOMESTIC_O))
        assertEquals(CoreConnectorType.GBT_AC, createCoreConnectorType(EvConnectorType.GBT_AC))
        assertEquals(CoreConnectorType.GBT_DC, createCoreConnectorType(EvConnectorType.GBT_DC))
        assertEquals(CoreConnectorType.IEC_60309_2_SINGLE_16, createCoreConnectorType(EvConnectorType.IEC_60309_2_SINGLE_16))
        assertEquals(CoreConnectorType.IEC_60309_2_THREE_16, createCoreConnectorType(EvConnectorType.IEC_60309_2_THREE_16))
        assertEquals(CoreConnectorType.IEC_60309_2_THREE_32, createCoreConnectorType(EvConnectorType.IEC_60309_2_THREE_32))
        assertEquals(CoreConnectorType.IEC_60309_2_THREE_64, createCoreConnectorType(EvConnectorType.IEC_60309_2_THREE_64))
        assertEquals(CoreConnectorType.IEC_62196_T1, createCoreConnectorType(EvConnectorType.IEC_62196_T1))
        assertEquals(CoreConnectorType.IEC_62196_T1_COMBO, createCoreConnectorType(EvConnectorType.IEC_62196_T1_COMBO))
        assertEquals(CoreConnectorType.IEC_62196_T2, createCoreConnectorType(EvConnectorType.IEC_62196_T2))
        assertEquals(CoreConnectorType.IEC_62196_T2_COMBO, createCoreConnectorType(EvConnectorType.IEC_62196_T2_COMBO))
        assertEquals(CoreConnectorType.IEC_62196_T3_A, createCoreConnectorType(EvConnectorType.IEC_62196_T3_A))
        assertEquals(CoreConnectorType.IEC_62196_T3_C, createCoreConnectorType(EvConnectorType.IEC_62196_T3_C))
        assertEquals(CoreConnectorType.NEMA_5_20, createCoreConnectorType(EvConnectorType.NEMA_5_20))
        assertEquals(CoreConnectorType.NEMA_6_30, createCoreConnectorType(EvConnectorType.NEMA_6_30))
        assertEquals(CoreConnectorType.NEMA_6_50, createCoreConnectorType(EvConnectorType.NEMA_6_50))
        assertEquals(CoreConnectorType.NEMA_10_30, createCoreConnectorType(EvConnectorType.NEMA_10_30))
        assertEquals(CoreConnectorType.NEMA_10_50, createCoreConnectorType(EvConnectorType.NEMA_10_50))
        assertEquals(CoreConnectorType.NEMA_14_30, createCoreConnectorType(EvConnectorType.NEMA_14_30))
        assertEquals(CoreConnectorType.NEMA_14_50, createCoreConnectorType(EvConnectorType.NEMA_14_50))
        assertEquals(CoreConnectorType.PANTOGRAPH_BOTTOM_UP, createCoreConnectorType(EvConnectorType.PANTOGRAPH_BOTTOM_UP))
        assertEquals(CoreConnectorType.PANTOGRAPH_TOP_DOWN, createCoreConnectorType(EvConnectorType.PANTOGRAPH_TOP_DOWN))
        assertEquals(CoreConnectorType.TESLA_R, createCoreConnectorType(EvConnectorType.TESLA_R))
        assertEquals(CoreConnectorType.TESLA_S, createCoreConnectorType(EvConnectorType.TESLA_S))
        assertEquals(CoreConnectorType.UNKNOWN, createCoreConnectorType(EvConnectorType.UNKNOWN))
        assertNull(createCoreConnectorType("UNKNOWN_TYPE"))
    }

    @Test
    fun `CoreConnectorType mapToPlatform() test`() {
        assertEquals(EvConnectorType.CHADEMO, CoreConnectorType.CHADEMO.mapToPlatform())
        assertEquals(EvConnectorType.CHAOJI, CoreConnectorType.CHAOJI.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_A, CoreConnectorType.DOMESTIC_A.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_B, CoreConnectorType.DOMESTIC_B.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_C, CoreConnectorType.DOMESTIC_C.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_D, CoreConnectorType.DOMESTIC_D.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_E, CoreConnectorType.DOMESTIC_E.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_F, CoreConnectorType.DOMESTIC_F.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_G, CoreConnectorType.DOMESTIC_G.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_H, CoreConnectorType.DOMESTIC_H.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_I, CoreConnectorType.DOMESTIC_I.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_J, CoreConnectorType.DOMESTIC_J.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_K, CoreConnectorType.DOMESTIC_K.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_L, CoreConnectorType.DOMESTIC_L.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_M, CoreConnectorType.DOMESTIC_M.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_N, CoreConnectorType.DOMESTIC_N.mapToPlatform())
        assertEquals(EvConnectorType.DOMESTIC_O, CoreConnectorType.DOMESTIC_O.mapToPlatform())
        assertEquals(EvConnectorType.GBT_AC, CoreConnectorType.GBT_AC.mapToPlatform())
        assertEquals(EvConnectorType.GBT_DC, CoreConnectorType.GBT_DC.mapToPlatform())
        assertEquals(EvConnectorType.IEC_60309_2_SINGLE_16, CoreConnectorType.IEC_60309_2_SINGLE_16.mapToPlatform())
        assertEquals(EvConnectorType.IEC_60309_2_THREE_16, CoreConnectorType.IEC_60309_2_THREE_16.mapToPlatform())
        assertEquals(EvConnectorType.IEC_60309_2_THREE_32, CoreConnectorType.IEC_60309_2_THREE_32.mapToPlatform())
        assertEquals(EvConnectorType.IEC_60309_2_THREE_64, CoreConnectorType.IEC_60309_2_THREE_64.mapToPlatform())
        assertEquals(EvConnectorType.IEC_62196_T1, CoreConnectorType.IEC_62196_T1.mapToPlatform())
        assertEquals(EvConnectorType.IEC_62196_T1_COMBO, CoreConnectorType.IEC_62196_T1_COMBO.mapToPlatform())
        assertEquals(EvConnectorType.IEC_62196_T2, CoreConnectorType.IEC_62196_T2.mapToPlatform())
        assertEquals(EvConnectorType.IEC_62196_T2_COMBO, CoreConnectorType.IEC_62196_T2_COMBO.mapToPlatform())
        assertEquals(EvConnectorType.IEC_62196_T3_A, CoreConnectorType.IEC_62196_T3_A.mapToPlatform())
        assertEquals(EvConnectorType.IEC_62196_T3_C, CoreConnectorType.IEC_62196_T3_C.mapToPlatform())
        assertEquals(EvConnectorType.NEMA_5_20, CoreConnectorType.NEMA_5_20.mapToPlatform())
        assertEquals(EvConnectorType.NEMA_6_30, CoreConnectorType.NEMA_6_30.mapToPlatform())
        assertEquals(EvConnectorType.NEMA_6_50, CoreConnectorType.NEMA_6_50.mapToPlatform())
        assertEquals(EvConnectorType.NEMA_10_30, CoreConnectorType.NEMA_10_30.mapToPlatform())
        assertEquals(EvConnectorType.NEMA_10_50, CoreConnectorType.NEMA_10_50.mapToPlatform())
        assertEquals(EvConnectorType.NEMA_14_30, CoreConnectorType.NEMA_14_30.mapToPlatform())
        assertEquals(EvConnectorType.NEMA_14_50, CoreConnectorType.NEMA_14_50.mapToPlatform())
        assertEquals(EvConnectorType.PANTOGRAPH_BOTTOM_UP, CoreConnectorType.PANTOGRAPH_BOTTOM_UP.mapToPlatform())
        assertEquals(EvConnectorType.PANTOGRAPH_TOP_DOWN, CoreConnectorType.PANTOGRAPH_TOP_DOWN.mapToPlatform())
        assertEquals(EvConnectorType.TESLA_R, CoreConnectorType.TESLA_R.mapToPlatform())
        assertEquals(EvConnectorType.TESLA_S, CoreConnectorType.TESLA_S.mapToPlatform())
        assertEquals(EvConnectorType.UNKNOWN, CoreConnectorType.UNKNOWN.mapToPlatform())
    }
}
