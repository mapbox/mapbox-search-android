package com.mapbox.search.base.factory

import com.mapbox.geojson.Point
import com.mapbox.search.base.core.CoreAdditionalCpoGeoLocation
import com.mapbox.search.base.core.CoreBusinessDetails
import com.mapbox.search.base.core.CoreChargingStatus
import com.mapbox.search.base.core.CoreChargingStatusSchedule
import com.mapbox.search.base.core.CoreConnector
import com.mapbox.search.base.core.CoreConnectorFormat
import com.mapbox.search.base.core.CoreConnectorType
import com.mapbox.search.base.core.CoreDisplayText
import com.mapbox.search.base.core.CoreEVSE
import com.mapbox.search.base.core.CoreEnergyMix
import com.mapbox.search.base.core.CoreEnergySource
import com.mapbox.search.base.core.CoreEnergySourceCategory
import com.mapbox.search.base.core.CoreEnvironmentalImpact
import com.mapbox.search.base.core.CoreEnvironmentalImpactCategory
import com.mapbox.search.base.core.CoreEvLocation
import com.mapbox.search.base.core.CoreEvseCapability
import com.mapbox.search.base.core.CoreEvsePublishTokenType
import com.mapbox.search.base.core.CoreEvseTokenType
import com.mapbox.search.base.core.CoreParkingRestriction
import com.mapbox.search.base.core.CorePowerType

internal object TestObjects {

    val CORE_CHARGING_STATUS_SCHEDULE = CoreChargingStatusSchedule(
        periodBegin = "2025-01-01T00:00:00Z",
        periodEnd = "2025-02-01T01:00:00Z",
        status = CoreChargingStatus.AVAILABLE,
    )

    val CORE_ENERGY_SOURCE = CoreEnergySource(
        source = CoreEnergySourceCategory.SOLAR,
        percentage = 70,
    )

    val CORE_ENV_IMPACT = CoreEnvironmentalImpact(
        category = CoreEnvironmentalImpactCategory.CARBON_DIOXIDE,
        amount = 100.0f,
    )

    val CORE_CONNECTOR = CoreConnector(
        id = "connector-123",
        standard = CoreConnectorType.CHADEMO,
        format = CoreConnectorFormat.SOCKET,
        powerType = CorePowerType.AC_1_PHASE,
        maxVoltage = 400,
        maxAmperage = 32,
        maxElectricPower = 12000,
        tariffIds = listOf("tariff-1", "tariff-2"),
        termsAndConditions = "https://example.com/terms",
        lastUpdated = "2025-01-01"
    )

    val CORE_EVSE_PUBLISH_TOKEN_TYPE = CoreEvsePublishTokenType(
        uid = "12345",
        tokenType = CoreEvseTokenType.RFID,
        visualNumber = "RFID number",
        issuer = "test-issue",
        groupId = "test-group"
    )

    val CORE_ADDITIONAL_CPO_GEO_LOCATION = CoreAdditionalCpoGeoLocation(
        position = Point.fromLngLat(10.0, 20.0),
        name = CoreDisplayText("en", "Turn right"),
    )

    val CORE_EVSE = CoreEVSE(
        uid = "12345",
        evseId = "EVSE-1",
        status = CoreChargingStatus.AVAILABLE,
        statusSchedule = listOf(CORE_CHARGING_STATUS_SCHEDULE),
        capabilities = listOf(CoreEvseCapability.CONTACTLESS_CARD_SUPPORT),
        connectors = listOf(CORE_CONNECTOR),
        floorLevel = "1",
        coordinates = Point.fromLngLat(10.0, 20.0),
        physicalReference = "test-ref-1",
        directions = listOf(CoreDisplayText("en", "Turn right")),
        parkingRestrictions = listOf(CoreParkingRestriction.EV_ONLY),
        images = listOf(createCoreImageInfo("https://test.com/img.pln", 500, 300)),
        lastUpdated = "2025-01-01"
    )

    val CORE_ENERGY_MIX = CoreEnergyMix(
        isGreenEnergy = true,
        energySources = listOf(CORE_ENERGY_SOURCE),
        environmentalImpact = listOf(CORE_ENV_IMPACT),
        supplierName = "Test supplier",
        energyProductName = "Test product",
    )

    val CORE_EV_LOCATION = CoreEvLocation(
        partyId = "test-party-id",
        publish = true,
        publishAllowedTo = listOf(CORE_EVSE_PUBLISH_TOKEN_TYPE),
        relatedLocations = listOf(CORE_ADDITIONAL_CPO_GEO_LOCATION),
        evses = listOf(CORE_EVSE),
        operatorDetails = CoreBusinessDetails("Operator", "https://test-operator.com", null),
        suboperatorDetails = null,
        ownerDetails = CoreBusinessDetails("Owner", "https://test-owner.com", null),
        chargingWhenClosed = false,
        energyMix = CORE_ENERGY_MIX
    )
}
