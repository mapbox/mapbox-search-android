@file:OptIn(MapboxExperimental::class)

package com.mapbox.search.base.factory.ev

import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
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
import com.mapbox.search.base.core.CoreEvMetadata
import com.mapbox.search.base.core.CoreEvseCapability
import com.mapbox.search.base.core.CoreEvsePublishTokenType
import com.mapbox.search.base.core.CoreEvseTokenType
import com.mapbox.search.base.core.CoreFacility
import com.mapbox.search.base.core.CoreImage
import com.mapbox.search.base.core.CoreImageCategory
import com.mapbox.search.base.core.CoreParkingRestriction
import com.mapbox.search.base.core.CoreParkingType
import com.mapbox.search.base.core.CorePowerType
import com.mapbox.search.common.ev.EVSE
import com.mapbox.search.common.ev.EvAdditionalGeoLocation
import com.mapbox.search.common.ev.EvBusinessDetails
import com.mapbox.search.common.ev.EvConnector
import com.mapbox.search.common.ev.EvConnectorFormat
import com.mapbox.search.common.ev.EvConnectorType
import com.mapbox.search.common.ev.EvDisplayText
import com.mapbox.search.common.ev.EvEnergyMix
import com.mapbox.search.common.ev.EvEnergySource
import com.mapbox.search.common.ev.EvEnergySourceCategory
import com.mapbox.search.common.ev.EvEnvironmentalImpact
import com.mapbox.search.common.ev.EvEnvironmentalImpactCategory
import com.mapbox.search.common.ev.EvFacility
import com.mapbox.search.common.ev.EvImage
import com.mapbox.search.common.ev.EvImageCategory
import com.mapbox.search.common.ev.EvLocation
import com.mapbox.search.common.ev.EvMetadata
import com.mapbox.search.common.ev.EvParkingRestriction
import com.mapbox.search.common.ev.EvParkingType
import com.mapbox.search.common.ev.EvPowerType
import com.mapbox.search.common.ev.EvStatusSchedule
import com.mapbox.search.common.ev.EvseCapability
import com.mapbox.search.common.ev.EvsePublishTokenType
import com.mapbox.search.common.ev.EvseStatus
import com.mapbox.search.common.ev.EvseTokenType

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreAdditionalCpoGeoLocation.toPlatform(): EvAdditionalGeoLocation {
    return EvAdditionalGeoLocation(
        position = position,
        name = name?.toPlatform()
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreChargingStatusSchedule.toPlatform(): EvStatusSchedule {
    return EvStatusSchedule(
        periodBegin = periodBegin,
        periodEnd = periodEnd,
        status = status.toPlatform(),
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreBusinessDetails.toPlatform(): EvBusinessDetails {
    return EvBusinessDetails(
        name = name,
        website = website,
        logo = logo?.toPlatform()
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreConnector.toPlatform(): EvConnector {
    return EvConnector(
        id = id,
        standard = standard.toPlatform(),
        format = format.toPlatform(),
        powerType = powerType.toPlatform(),
        maxVoltage = maxVoltage,
        maxAmperage = maxAmperage,
        maxElectricPower = maxElectricPower,
        tariffIds = tariffIds,
        termsAndConditions = termsAndConditions,
        lastUpdated = lastUpdated
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvConnectorFormat.Type
fun CoreConnectorFormat.toPlatform(): String {
    return when (this) {
        CoreConnectorFormat.SOCKET -> EvConnectorFormat.SOCKET
        CoreConnectorFormat.CABLE -> EvConnectorFormat.CABLE
        CoreConnectorFormat.UNKNOWN -> EvConnectorFormat.UNKNOWN
    }
}

/**
 * Factory function to create platform-type [EvConnectorType.Type] from core-type [CoreConnectorType].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvConnectorType.Type
fun CoreConnectorType.toPlatform(): String {
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

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreDisplayText.toPlatform(): EvDisplayText {
    return EvDisplayText(
        language = this.language,
        text = this.text
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreEnergyMix.toPlatform(): EvEnergyMix {
    return EvEnergyMix(
        isGreenEnergy = this.isGreenEnergy,
        energySources = this.energySources.map { it.toPlatform() },
        environmentalImpact = this.environmentalImpact.map { it.toPlatform() },
        supplierName = this.supplierName,
        energyProductName = this.energyProductName
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreEnergySource.toPlatform(): EvEnergySource {
    return EvEnergySource(
        source = this.source.toPlatform(),
        percentage = this.percentage
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvEnergySourceCategory.Type
fun CoreEnergySourceCategory.toPlatform(): String {
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
fun CoreEnvironmentalImpact.toPlatform(): EvEnvironmentalImpact {
    return EvEnvironmentalImpact(
        category = category.toPlatform(),
        amount = amount,
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvEnvironmentalImpactCategory.Type
fun CoreEnvironmentalImpactCategory.toPlatform(): String {
    return when (this) {
        CoreEnvironmentalImpactCategory.CARBON_DIOXIDE -> EvEnvironmentalImpactCategory.CARBON_DIOXIDE
        CoreEnvironmentalImpactCategory.NUCLEAR_WASTE -> EvEnvironmentalImpactCategory.NUCLEAR_WASTE
        CoreEnvironmentalImpactCategory.UNKNOWN -> EvEnvironmentalImpactCategory.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvFacility.Type
fun CoreFacility.toPlatform(): String {
    return when (this) {
        CoreFacility.HOTEL -> EvFacility.HOTEL
        CoreFacility.RESTAURANT -> EvFacility.RESTAURANT
        CoreFacility.CAFE -> EvFacility.CAFE
        CoreFacility.MALL -> EvFacility.MALL
        CoreFacility.SUPERMARKET -> EvFacility.SUPERMARKET
        CoreFacility.SPORT -> EvFacility.SPORT
        CoreFacility.RECREATION_AREA -> EvFacility.RECREATION_AREA
        CoreFacility.NATURE -> EvFacility.NATURE
        CoreFacility.MUSEUM -> EvFacility.MUSEUM
        CoreFacility.BIKE_SHARING -> EvFacility.BIKE_SHARING
        CoreFacility.BUS_STOP -> EvFacility.BUS_STOP
        CoreFacility.TAXI_STAND -> EvFacility.TAXI_STAND
        CoreFacility.TRAM_STOP -> EvFacility.TRAM_STOP
        CoreFacility.METRO_STATION -> EvFacility.METRO_STATION
        CoreFacility.TRAIN_STATION -> EvFacility.TRAIN_STATION
        CoreFacility.AIRPORT -> EvFacility.AIRPORT
        CoreFacility.PARKING_LOT -> EvFacility.PARKING_LOT
        CoreFacility.CARPOOL_PARKING -> EvFacility.CARPOOL_PARKING
        CoreFacility.FUEL_STATION -> EvFacility.FUEL_STATION
        CoreFacility.WIFI -> EvFacility.WIFI
        CoreFacility.UNKNOWN -> EvFacility.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreImage.toPlatform(): EvImage {
    return EvImage(
        url = this.url,
        thumbnail = this.thumbnail,
        category = this.category.toPlatform(),
        type = this.type,
        width = this.width,
        height = this.height
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvParkingRestriction.Type
fun CoreParkingRestriction.toPlatform(): String {
    return when (this) {
        CoreParkingRestriction.EV_ONLY -> EvParkingRestriction.EV_ONLY
        CoreParkingRestriction.PLUGGED -> EvParkingRestriction.PLUGGED
        CoreParkingRestriction.DISABLED -> EvParkingRestriction.DISABLED
        CoreParkingRestriction.CUSTOMERS -> EvParkingRestriction.CUSTOMERS
        CoreParkingRestriction.MOTOR_CYCLES -> EvParkingRestriction.MOTOR_CYCLES
        CoreParkingRestriction.UNKNOWN -> EvParkingRestriction.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvParkingType.Type
fun CoreParkingType.toPlatform(): String {
    return when (this) {
        CoreParkingType.ALONG_MOTORWAY -> EvParkingType.ALONG_MOTORWAY
        CoreParkingType.PARKING_GARAGE -> EvParkingType.PARKING_GARAGE
        CoreParkingType.PARKING_LOT -> EvParkingType.PARKING_LOT
        CoreParkingType.ON_DRIVEWAY -> EvParkingType.ON_DRIVEWAY
        CoreParkingType.ON_STREET -> EvParkingType.ON_STREET
        CoreParkingType.UNDERGROUND_GARAGE -> EvParkingType.UNDERGROUND_GARAGE
        CoreParkingType.UNKNOWN -> EvParkingType.UNKNOWN
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvPowerType.Type
fun CorePowerType.toPlatform(): String {
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
fun CoreEVSE.toPlatform(): EVSE {
    return EVSE(
        uid = this.uid,
        evseId = this.evseId,
        status = this.status.toPlatform(),
        statusSchedule = this.statusSchedule.map { it.toPlatform() },
        capabilities = this.capabilities.map { it.toPlatform() },
        connectors = this.connectors.map { it.toPlatform() },
        floorLevel = this.floorLevel,
        coordinate = this.coordinates,
        physicalReference = this.physicalReference,
        directions = this.directions.map { it.toPlatform() },
        parkingRestrictions = this.parkingRestrictions.map { it.toPlatform() },
        images = this.images.map { it.toPlatform() },
        lastUpdated = this.lastUpdated
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvseCapability.Type
fun CoreEvseCapability.toPlatform(): String {
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
fun CoreEvsePublishTokenType.toPlatform(): EvsePublishTokenType {
    return EvsePublishTokenType(
        uid = this.uid,
        tokenType = this.tokenType?.toPlatform(),
        visualNumber = this.visualNumber,
        issuer = this.issuer,
        groupId = this.groupId
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvseTokenType.Type
fun CoreEvseTokenType.toPlatform(): String {
    return when (this) {
        CoreEvseTokenType.AD_HOC_USER -> EvseTokenType.AD_HOC_USER
        CoreEvseTokenType.APP_USER -> EvseTokenType.APP_USER
        CoreEvseTokenType.RFID -> EvseTokenType.RFID
        CoreEvseTokenType.OTHER -> EvseTokenType.OTHER
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
@EvseStatus.Type
fun CoreChargingStatus.toPlatform(): String {
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
@EvImageCategory.Type
fun CoreImageCategory.toPlatform(): String {
    return when (this) {
        CoreImageCategory.CHARGER -> EvImageCategory.CHARGER
        CoreImageCategory.ENTRANCE -> EvImageCategory.ENTRANCE
        CoreImageCategory.LOCATION -> EvImageCategory.LOCATION
        CoreImageCategory.NETWORK -> EvImageCategory.NETWORK
        CoreImageCategory.OPERATOR -> EvImageCategory.OPERATOR
        CoreImageCategory.OTHER -> EvImageCategory.OTHER
        CoreImageCategory.OWNER -> EvImageCategory.OWNER
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreEvLocation.toPlatform(): EvLocation {
    return EvLocation(
        countryCode = countryCode,
        partyId = partyId,
        id = id,
        publish = publish,
        publishAllowedTo = publishAllowedTo.map { it.toPlatform() },
        name = name,
        address = address,
        city = city,
        postalCode = postalCode,
        state = state,
        country = country,
        coordinates = coordinates,
        relatedLocations = relatedLocations.map { it.toPlatform() },
        parkingType = parkingType?.toPlatform(),
        evses = evses.map { it.toPlatform() },
        directions = directions.map { it.toPlatform() },
        operatorDetails = operatorDetails?.toPlatform(),
        suboperatorDetails = suboperatorDetails?.toPlatform(),
        ownerDetails = ownerDetails?.toPlatform(),
        facilities = facilities.map { it.toPlatform() },
        timezone = timezone,
        chargingWhenClosed = chargingWhenClosed,
        images = images.map { it.toPlatform() },
        energyMix = energyMix?.toPlatform(),
        lastUpdated = lastUpdated
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
fun CoreEvMetadata.toPlatform(): EvMetadata {
    return EvMetadata(
        evLocation = this.evLocation?.toPlatform(),
    )
}
