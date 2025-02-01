package com.mapbox.search.base.factory

import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreChargingStatus
import com.mapbox.search.base.core.CoreEnergySourceCategory
import com.mapbox.search.base.core.CoreEnvironmentalImpactCategory
import com.mapbox.search.base.core.CoreEvMetadata
import com.mapbox.search.base.core.CoreEvseCapability
import com.mapbox.search.base.core.CoreEvseTokenType
import com.mapbox.search.base.core.CorePowerType
import com.mapbox.search.common.ev.EVSE
import com.mapbox.search.common.ev.EvAdditionalGeoLocation
import com.mapbox.search.common.ev.EvEnergyMix
import com.mapbox.search.common.ev.EvEnergySource
import com.mapbox.search.common.ev.EvEnergySourceCategory
import com.mapbox.search.common.ev.EvEnvironmentalImpact
import com.mapbox.search.common.ev.EvEnvironmentalImpactCategory
import com.mapbox.search.common.ev.EvLocation
import com.mapbox.search.common.ev.EvMetadata
import com.mapbox.search.common.ev.EvPowerType
import com.mapbox.search.common.ev.EvStatusSchedule
import com.mapbox.search.common.ev.EvseCapability
import com.mapbox.search.common.ev.EvsePublishTokenType
import com.mapbox.search.common.ev.EvseStatus
import com.mapbox.search.common.ev.EvseTokenType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(MapboxExperimental::class)
class EvTypesFactoryTest {

    @Test
    fun `CoreAdditionalCpoGeoLocation mapToPlatform() test`() {
        val core = TestObjects.CORE_ADDITIONAL_CPO_GEO_LOCATION

        val expected = EvAdditionalGeoLocation(
            position = core.position,
            name = core.name?.mapToPlatform()
        )

        assertEquals(expected, core.mapToPlatform())
    }

    @Test
    fun `CoreChargingStatusSchedule mapToPlatform() test`() {
        val core = TestObjects.CORE_CHARGING_STATUS_SCHEDULE

        val expected = EvStatusSchedule(
            periodBegin = core.periodBegin,
            periodEnd = core.periodEnd,
            status = core.status.mapToPlatform()
        )

        assertEquals(expected, core.mapToPlatform())
    }

    @Test
    fun `CoreEnergyMix mapToPlatform() test`() {
        val core = TestObjects.CORE_ENERGY_MIX

        val expected = EvEnergyMix(
            isGreenEnergy = core.isGreenEnergy,
            energySources = core.energySources.map { it.mapToPlatform() },
            environmentalImpact = core.environmentalImpact.map { it.mapToPlatform() },
            supplierName = core.supplierName,
            energyProductName = core.energyProductName
        )

        assertEquals(expected, core.mapToPlatform())
    }

    @Test
    fun `CoreEnergySource mapToPlatform() test`() {
        val core = TestObjects.CORE_ENERGY_SOURCE

        val expected = EvEnergySource(
            source = core.source.mapToPlatform(),
            percentage = core.percentage
        )

        assertEquals(expected, core.mapToPlatform())
    }

    @Test
    fun `CoreEnergySourceCategory mapToPlatform() test`() {
        assertEquals(
            EvEnergySourceCategory.NUCLEAR,
            CoreEnergySourceCategory.NUCLEAR.mapToPlatform()
        )
        assertEquals(
            EvEnergySourceCategory.GENERAL_FOSSIL,
            CoreEnergySourceCategory.GENERAL_FOSSIL.mapToPlatform()
        )
        assertEquals(EvEnergySourceCategory.COAL, CoreEnergySourceCategory.COAL.mapToPlatform())
        assertEquals(EvEnergySourceCategory.GAS, CoreEnergySourceCategory.GAS.mapToPlatform())
        assertEquals(
            EvEnergySourceCategory.GENERAL_GREEN,
            CoreEnergySourceCategory.GENERAL_GREEN.mapToPlatform()
        )
        assertEquals(EvEnergySourceCategory.SOLAR, CoreEnergySourceCategory.SOLAR.mapToPlatform())
        assertEquals(EvEnergySourceCategory.WIND, CoreEnergySourceCategory.WIND.mapToPlatform())
        assertEquals(EvEnergySourceCategory.WATER, CoreEnergySourceCategory.WATER.mapToPlatform())
        assertEquals(
            EvEnergySourceCategory.UNKNOWN,
            CoreEnergySourceCategory.UNKNOWN.mapToPlatform()
        )
    }

    @Test
    fun `CoreEnvironmentalImpact mapToPlatform() test`() {
        val core = TestObjects.CORE_ENV_IMPACT

        val expected = EvEnvironmentalImpact(
            category = core.category.mapToPlatform(),
            amount = core.amount,
        )

        assertEquals(expected, core.mapToPlatform())
    }

    @Test
    fun `oreEnvironmentalImpactCategory mapToPlatform() test`() {
        assertEquals(
            EvEnvironmentalImpactCategory.CARBON_DIOXIDE,
            CoreEnvironmentalImpactCategory.CARBON_DIOXIDE.mapToPlatform()
        )
        assertEquals(
            EvEnvironmentalImpactCategory.NUCLEAR_WASTE,
            CoreEnvironmentalImpactCategory.NUCLEAR_WASTE.mapToPlatform()
        )
        assertEquals(
            EvEnvironmentalImpactCategory.UNKNOWN,
            CoreEnvironmentalImpactCategory.UNKNOWN.mapToPlatform()
        )
    }

    @Test
    fun `CorePowerType mapToPlatform() test`() {
        assertEquals(EvPowerType.AC_1_PHASE, CorePowerType.AC_1_PHASE.mapToPlatform())
        assertEquals(EvPowerType.AC_2_PHASE, CorePowerType.AC_2_PHASE.mapToPlatform())
        assertEquals(EvPowerType.AC_2_PHASE_SPLIT, CorePowerType.AC_2_PHASE_SPLIT.mapToPlatform())
        assertEquals(EvPowerType.AC_3_PHASE, CorePowerType.AC_3_PHASE.mapToPlatform())
        assertEquals(EvPowerType.DC, CorePowerType.DC.mapToPlatform())
        assertEquals(EvPowerType.UNKNOWN, CorePowerType.UNKNOWN.mapToPlatform())
    }

    @Test
    fun `CoreEVSE mapToPlatform() test`() {
        val coreEVSE = TestObjects.CORE_EVSE

        val expected = EVSE(
            uid = coreEVSE.uid,
            evseId = coreEVSE.evseId,
            status = coreEVSE.status.mapToPlatform(),
            statusSchedule = coreEVSE.statusSchedule.map { it.mapToPlatform() },
            capabilities = coreEVSE.capabilities.map { it.mapToPlatform() },
            connectors = coreEVSE.connectors.map { it.mapToPlatform() },
            floorLevel = coreEVSE.floorLevel,
            coordinate = coreEVSE.coordinates,
            physicalReference = coreEVSE.physicalReference,
            directions = coreEVSE.directions.map { it.mapToPlatform() },
            parkingRestrictions = coreEVSE.parkingRestrictions.map { it.mapToPlatform() },
            images = coreEVSE.images.map { it.mapToPlatform() },
            lastUpdated = coreEVSE.lastUpdated,
        )

        assertEquals(expected, coreEVSE.mapToPlatform())
    }

    @Test
    fun `CoreEvseCapability mapToPlatform() test`() {
        assertEquals(
            EvseCapability.CHARGING_PROFILE_CAPABLE,
            CoreEvseCapability.CHARGING_PROFILE_CAPABLE.mapToPlatform()
        )
        assertEquals(
            EvseCapability.CHARGING_PREFERENCES_CAPABLE,
            CoreEvseCapability.CHARGING_PREFERENCES_CAPABLE.mapToPlatform()
        )
        assertEquals(
            EvseCapability.CHIP_CARD_SUPPORT,
            CoreEvseCapability.CHIP_CARD_SUPPORT.mapToPlatform()
        )
        assertEquals(
            EvseCapability.CONTACTLESS_CARD_SUPPORT,
            CoreEvseCapability.CONTACTLESS_CARD_SUPPORT.mapToPlatform()
        )
        assertEquals(
            EvseCapability.CREDIT_CARD_PAYABLE,
            CoreEvseCapability.CREDIT_CARD_PAYABLE.mapToPlatform()
        )
        assertEquals(
            EvseCapability.DEBIT_CARD_PAYABLE,
            CoreEvseCapability.DEBIT_CARD_PAYABLE.mapToPlatform()
        )
        assertEquals(EvseCapability.PED_TERMINAL, CoreEvseCapability.PED_TERMINAL.mapToPlatform())
        assertEquals(
            EvseCapability.REMOTE_START_STOP_CAPABLE,
            CoreEvseCapability.REMOTE_START_STOP_CAPABLE.mapToPlatform()
        )
        assertEquals(EvseCapability.RESERVABLE, CoreEvseCapability.RESERVABLE.mapToPlatform())
        assertEquals(EvseCapability.RFID_READER, CoreEvseCapability.RFID_READER.mapToPlatform())
        assertEquals(
            EvseCapability.START_SESSION_CONNECTOR_REQUIRED,
            CoreEvseCapability.START_SESSION_CONNECTOR_REQUIRED.mapToPlatform()
        )
        assertEquals(
            EvseCapability.TOKEN_GROUP_CAPABLE,
            CoreEvseCapability.TOKEN_GROUP_CAPABLE.mapToPlatform()
        )
        assertEquals(
            EvseCapability.UNLOCK_CAPABLE,
            CoreEvseCapability.UNLOCK_CAPABLE.mapToPlatform()
        )
        assertEquals(EvseCapability.UNKNOWN, CoreEvseCapability.UNKNOWN.mapToPlatform())
    }

    @Test
    fun `CoreEvsePublishTokenType mapToPlatform() test`() {
        val core = TestObjects.CORE_EVSE_PUBLISH_TOKEN_TYPE

        val expected = EvsePublishTokenType(
            uid = core.uid,
            tokenType = core.tokenType?.mapToPlatform(),
            visualNumber = core.visualNumber,
            issuer = core.issuer,
            groupId = core.groupId
        )

        assertEquals(expected, core.mapToPlatform())
    }

    @Test
    fun `CoreEvseTokenType mapToPlatform() test`() {
        assertEquals(EvseTokenType.AD_HOC_USER, CoreEvseTokenType.AD_HOC_USER.mapToPlatform())
        assertEquals(EvseTokenType.APP_USER, CoreEvseTokenType.APP_USER.mapToPlatform())
        assertEquals(EvseTokenType.RFID, CoreEvseTokenType.RFID.mapToPlatform())
        assertEquals(EvseTokenType.OTHER, CoreEvseTokenType.OTHER.mapToPlatform())
    }

    @Test
    fun `CoreChargingStatus mapToPlatform() test`() {
        assertEquals(EvseStatus.AVAILABLE, CoreChargingStatus.AVAILABLE.mapToPlatform())
        assertEquals(EvseStatus.BLOCKED, CoreChargingStatus.BLOCKED.mapToPlatform())
        assertEquals(EvseStatus.CHARGING, CoreChargingStatus.CHARGING.mapToPlatform())
        assertEquals(EvseStatus.INOPERATIVE, CoreChargingStatus.INOPERATIVE.mapToPlatform())
        assertEquals(EvseStatus.OUT_OF_ORDER, CoreChargingStatus.OUT_OF_ORDER.mapToPlatform())
        assertEquals(EvseStatus.PLANNED, CoreChargingStatus.PLANNED.mapToPlatform())
        assertEquals(EvseStatus.REMOVED, CoreChargingStatus.REMOVED.mapToPlatform())
        assertEquals(EvseStatus.RESERVED, CoreChargingStatus.RESERVED.mapToPlatform())
        assertEquals(EvseStatus.UNKNOWN, CoreChargingStatus.UNKNOWN.mapToPlatform())
    }

    @Test
    fun `CoreEvLocation mapToPlatform() test`() {
        val core = TestObjects.CORE_EV_LOCATION

        val expected = EvLocation(
            partyId = core.partyId,
            publish = core.publish,
            publishAllowedTo = core.publishAllowedTo.map { it.mapToPlatform() },
            relatedLocations = core.relatedLocations.map { it.mapToPlatform() },
            evses = core.evses.map { it.mapToPlatform() },
            operatorDetails = core.operatorDetails?.mapToPlatform(),
            suboperatorDetails = null,
            ownerDetails = core.ownerDetails?.mapToPlatform(),
            chargingWhenClosed = core.chargingWhenClosed,
            images = core.images.map { it.mapToPlatform() },
            energyMix = core.energyMix?.mapToPlatform()
        )

        assertEquals(expected, core.mapToPlatform())
    }

    @Test
    fun `CoreEvMetadata mapToPlatform() test`() {
        val core = CoreEvMetadata(TestObjects.CORE_EV_LOCATION)
        val expected = EvMetadata(core.evLocation?.mapToPlatform())
        assertEquals(expected, core.mapToPlatform())
    }
}
