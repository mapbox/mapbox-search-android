@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.base.core.CoreAdditionalCpoGeoLocation
import com.mapbox.search.base.core.CoreChargingStatus
import com.mapbox.search.base.core.CoreChargingStatusSchedule
import com.mapbox.search.base.core.CoreEVSE
import com.mapbox.search.base.core.CoreEnergyMix
import com.mapbox.search.base.core.CoreEnergySource
import com.mapbox.search.base.core.CoreEnergySourceCategory
import com.mapbox.search.base.core.CoreEnvironmentalImpact
import com.mapbox.search.base.core.CoreEnvironmentalImpactCategory
import com.mapbox.search.base.core.CoreEvLocation
import com.mapbox.search.base.core.CoreEvMetadata
import com.mapbox.search.base.core.CoreEvseCapability
import com.mapbox.search.base.core.CoreEvsePublishTokenType
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

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreAdditionalCpoGeoLocation.mapToPlatform(): EvAdditionalGeoLocation {
    return EvAdditionalGeoLocation(
        position = position,
        name = name?.mapToPlatform(),
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreChargingStatusSchedule.mapToPlatform(): EvStatusSchedule {
    return EvStatusSchedule(
        periodBegin = periodBegin,
        periodEnd = periodEnd,
        status = status.mapToPlatform(),
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreEnergyMix.mapToPlatform(): EvEnergyMix {
    return EvEnergyMix(
        isGreenEnergy = this.isGreenEnergy,
        energySources = this.energySources.map { it.mapToPlatform() },
        environmentalImpact = this.environmentalImpact.map { it.mapToPlatform() },
        supplierName = this.supplierName,
        energyProductName = this.energyProductName,
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreEnergySource.mapToPlatform(): EvEnergySource {
    return EvEnergySource(
        source = this.source.mapToPlatform(),
        percentage = this.percentage,
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvEnergySourceCategory.Type
fun CoreEnergySourceCategory.mapToPlatform(): String {
    return when (this) {
        CoreEnergySourceCategory.NUCLEAR -> EvEnergySourceCategory.NUCLEAR
        CoreEnergySourceCategory.GENERAL_FOSSIL -> EvEnergySourceCategory.GENERAL_FOSSIL
        CoreEnergySourceCategory.COAL -> EvEnergySourceCategory.COAL
        CoreEnergySourceCategory.GAS -> EvEnergySourceCategory.GAS
        CoreEnergySourceCategory.GENERAL_GREEN -> EvEnergySourceCategory.GENERAL_GREEN
        CoreEnergySourceCategory.SOLAR -> EvEnergySourceCategory.SOLAR
        CoreEnergySourceCategory.WIND -> EvEnergySourceCategory.WIND
        CoreEnergySourceCategory.WATER -> EvEnergySourceCategory.WATER
        CoreEnergySourceCategory.UNKNOWN -> EvEnergySourceCategory.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreEnvironmentalImpact.mapToPlatform(): EvEnvironmentalImpact {
    return EvEnvironmentalImpact(
        category = category.mapToPlatform(),
        amount = amount,
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvEnvironmentalImpactCategory.Type
fun CoreEnvironmentalImpactCategory.mapToPlatform(): String {
    return when (this) {
        CoreEnvironmentalImpactCategory.CARBON_DIOXIDE -> EvEnvironmentalImpactCategory.CARBON_DIOXIDE
        CoreEnvironmentalImpactCategory.NUCLEAR_WASTE -> EvEnvironmentalImpactCategory.NUCLEAR_WASTE
        CoreEnvironmentalImpactCategory.UNKNOWN -> EvEnvironmentalImpactCategory.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvPowerType.Type
fun CorePowerType.mapToPlatform(): String {
    return when (this) {
        CorePowerType.AC_1_PHASE -> EvPowerType.AC_1_PHASE
        CorePowerType.AC_2_PHASE -> EvPowerType.AC_2_PHASE
        CorePowerType.AC_2_PHASE_SPLIT -> EvPowerType.AC_2_PHASE_SPLIT
        CorePowerType.AC_3_PHASE -> EvPowerType.AC_3_PHASE
        CorePowerType.DC -> EvPowerType.DC
        CorePowerType.UNKNOWN -> EvPowerType.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreEVSE.mapToPlatform(): EVSE {
    return EVSE(
        uid = this.uid,
        evseId = this.evseId,
        status = this.status.mapToPlatform(),
        statusSchedule = this.statusSchedule.map { it.mapToPlatform() },
        capabilities = this.capabilities.map { it.mapToPlatform() },
        connectors = this.connectors.map { it.mapToPlatform() },
        floorLevel = this.floorLevel,
        coordinate = this.coordinates,
        physicalReference = this.physicalReference,
        directions = this.directions.map { it.mapToPlatform() },
        parkingRestrictions = this.parkingRestrictions.map { it.mapToPlatform() },
        images = this.images.map { it.mapToPlatform() },
        lastUpdated = this.lastUpdated,
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvseCapability.Type
fun CoreEvseCapability.mapToPlatform(): String {
    return when (this) {
        CoreEvseCapability.CHARGING_PROFILE_CAPABLE -> EvseCapability.CHARGING_PROFILE_CAPABLE
        CoreEvseCapability.CHARGING_PREFERENCES_CAPABLE -> EvseCapability.CHARGING_PREFERENCES_CAPABLE
        CoreEvseCapability.CHIP_CARD_SUPPORT -> EvseCapability.CHIP_CARD_SUPPORT
        CoreEvseCapability.CONTACTLESS_CARD_SUPPORT -> EvseCapability.CONTACTLESS_CARD_SUPPORT
        CoreEvseCapability.CREDIT_CARD_PAYABLE -> EvseCapability.CREDIT_CARD_PAYABLE
        CoreEvseCapability.DEBIT_CARD_PAYABLE -> EvseCapability.DEBIT_CARD_PAYABLE
        CoreEvseCapability.PED_TERMINAL -> EvseCapability.PED_TERMINAL
        CoreEvseCapability.REMOTE_START_STOP_CAPABLE -> EvseCapability.REMOTE_START_STOP_CAPABLE
        CoreEvseCapability.RESERVABLE -> EvseCapability.RESERVABLE
        CoreEvseCapability.RFID_READER -> EvseCapability.RFID_READER
        CoreEvseCapability.START_SESSION_CONNECTOR_REQUIRED -> EvseCapability.START_SESSION_CONNECTOR_REQUIRED
        CoreEvseCapability.TOKEN_GROUP_CAPABLE -> EvseCapability.TOKEN_GROUP_CAPABLE
        CoreEvseCapability.UNLOCK_CAPABLE -> EvseCapability.UNLOCK_CAPABLE
        CoreEvseCapability.UNKNOWN -> EvseCapability.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreEvsePublishTokenType.mapToPlatform(): EvsePublishTokenType {
    return EvsePublishTokenType(
        uid = this.uid,
        tokenType = this.tokenType?.mapToPlatform(),
        visualNumber = this.visualNumber,
        issuer = this.issuer,
        groupId = this.groupId
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvseTokenType.Type
fun CoreEvseTokenType.mapToPlatform(): String {
    return when (this) {
        CoreEvseTokenType.AD_HOC_USER -> EvseTokenType.AD_HOC_USER
        CoreEvseTokenType.APP_USER -> EvseTokenType.APP_USER
        CoreEvseTokenType.RFID -> EvseTokenType.RFID
        CoreEvseTokenType.OTHER -> EvseTokenType.OTHER
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvseStatus.Type
fun CoreChargingStatus.mapToPlatform(): String {
    return when (this) {
        CoreChargingStatus.AVAILABLE -> EvseStatus.AVAILABLE
        CoreChargingStatus.BLOCKED -> EvseStatus.BLOCKED
        CoreChargingStatus.CHARGING -> EvseStatus.CHARGING
        CoreChargingStatus.INOPERATIVE -> EvseStatus.INOPERATIVE
        CoreChargingStatus.OUT_OF_ORDER -> EvseStatus.OUT_OF_ORDER
        CoreChargingStatus.PLANNED -> EvseStatus.PLANNED
        CoreChargingStatus.REMOVED -> EvseStatus.REMOVED
        CoreChargingStatus.RESERVED -> EvseStatus.RESERVED
        CoreChargingStatus.UNKNOWN -> EvseStatus.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreEvLocation.mapToPlatform(): EvLocation {
    return EvLocation(
        partyId = partyId,
        publish = publish,
        publishAllowedTo = publishAllowedTo.map { it.mapToPlatform() },
        relatedLocations = relatedLocations.map { it.mapToPlatform() },
        evses = evses.map { it.mapToPlatform() },
        operatorDetails = operatorDetails?.mapToPlatform(),
        suboperatorDetails = suboperatorDetails?.mapToPlatform(),
        ownerDetails = ownerDetails?.mapToPlatform(),
        chargingWhenClosed = chargingWhenClosed,
        energyMix = energyMix?.mapToPlatform(),
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreEvMetadata.mapToPlatform(): EvMetadata {
    return EvMetadata(
        evLocation = this.evLocation?.mapToPlatform(),
    )
}
