package com.mapbox.search.common.ev

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.geojson.Point
import kotlinx.parcelize.Parcelize

/**
 * OCPI Location. The Location object describes the location and its properties where a group of
 * EVSEs that belong together are installed.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#mod_locations_location_object)
 * for more details.
 *
 * @property countryCode ISO-3166 alpha-2 country code of the operator that 'owns' this Location.
 * @property partyId 3 letter identifier of the operator.
 * @property id Uniquely identifies the location within the operator's platform (and suboperator platforms).
 * @property publish Defines if a Location may be published on a website or app etc.
 * @property publishAllowedTo PublishTokenType object. This field may only be used when the publish field is false.
 * @property name Display name of the location.
 * @property address Street/block name and house number if available.
 * @property city City or town.
 * @property postalCode Postal code of the location, may only be omitted when the location has no postal code.
 * @property state State or province of the location, only to be used when relevant.
 * @property country ISO 3166-1 alpha-3 code for the country of this location.
 * @property coordinates GeoLocation object representing coordinates of the location.
 * @property relatedLocations AdditionalGeoLocation object representing geographical location of related points.
 * @property parkingType ParkingType value. The general type of parking at the charge point location.
 * @property evses List of [EVSE] objects that belong to this Location.
 * @property directions DisplayText object representing human-readable directions on how to reach the location.
 * @property operatorDetails BusinessDetails object representing information of the operator.
 * @property suboperatorDetails BusinessDetails object representing information of the suboperator if available.
 * @property ownerDetails BusinessDetails object representing information of the owner if available.
 * @property facilities List of [EvFacility.Type] this charging location directly belongs to.
 * @property timezone One of IANA time zone data’s TZ-values representing the time zone of the location.
 * @property chargingWhenClosed Indicates if the EVSEs are still charging outside the opening hours of the location.
 * @property images Image object representing links to images related to the location such as photos or logos.
 * @property energyMix EnergyMix object representing details on the energy supplied at this location.
 * @property lastUpdated Timestamp in OCPI DateTime format when this Location or one of its EVSEs or Connectors were last updated (or created).
 */
@MapboxExperimental
@Parcelize
public class EvLocation @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    public val countryCode: String,
    public val partyId: String,
    public val id: String,
    public val publish: Boolean,
    public val publishAllowedTo: List<EvsePublishTokenType>,
    public val name: String?,
    public val address: String,
    public val city: String,
    public val postalCode: String?,
    public val state: String?,
    public val country: String,
    public val coordinates: Point,
    public val relatedLocations: List<EvAdditionalGeoLocation>,
    @EvParkingType.Type public val parkingType: String?,
    public val evses: List<EVSE>,
    public val directions: List<EvDisplayText>,
    public val operatorDetails: EvBusinessDetails?,
    public val suboperatorDetails: EvBusinessDetails?,
    public val ownerDetails: EvBusinessDetails?,
    public val facilities: List<String>,
    public val timezone: String,
    public val chargingWhenClosed: Boolean?,
    public val images: List<EvImage>,
    public val energyMix: EvEnergyMix?,
    public val lastUpdated: String
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EvLocation

        if (countryCode != other.countryCode) return false
        if (partyId != other.partyId) return false
        if (id != other.id) return false
        if (publish != other.publish) return false
        if (publishAllowedTo != other.publishAllowedTo) return false
        if (name != other.name) return false
        if (address != other.address) return false
        if (city != other.city) return false
        if (postalCode != other.postalCode) return false
        if (state != other.state) return false
        if (country != other.country) return false
        if (coordinates != other.coordinates) return false
        if (relatedLocations != other.relatedLocations) return false
        if (parkingType != other.parkingType) return false
        if (evses != other.evses) return false
        if (directions != other.directions) return false
        if (operatorDetails != other.operatorDetails) return false
        if (suboperatorDetails != other.suboperatorDetails) return false
        if (ownerDetails != other.ownerDetails) return false
        if (facilities != other.facilities) return false
        if (timezone != other.timezone) return false
        if (chargingWhenClosed != other.chargingWhenClosed) return false
        if (images != other.images) return false
        if (energyMix != other.energyMix) return false
        if (lastUpdated != other.lastUpdated) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = countryCode.hashCode()
        result = 31 * result + partyId.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + publish.hashCode()
        result = 31 * result + publishAllowedTo.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + address.hashCode()
        result = 31 * result + city.hashCode()
        result = 31 * result + (postalCode?.hashCode() ?: 0)
        result = 31 * result + (state?.hashCode() ?: 0)
        result = 31 * result + country.hashCode()
        result = 31 * result + coordinates.hashCode()
        result = 31 * result + relatedLocations.hashCode()
        result = 31 * result + (parkingType?.hashCode() ?: 0)
        result = 31 * result + evses.hashCode()
        result = 31 * result + directions.hashCode()
        result = 31 * result + (operatorDetails?.hashCode() ?: 0)
        result = 31 * result + (suboperatorDetails?.hashCode() ?: 0)
        result = 31 * result + (ownerDetails?.hashCode() ?: 0)
        result = 31 * result + facilities.hashCode()
        result = 31 * result + timezone.hashCode()
        result = 31 * result + (chargingWhenClosed?.hashCode() ?: 0)
        result = 31 * result + images.hashCode()
        result = 31 * result + (energyMix?.hashCode() ?: 0)
        result = 31 * result + lastUpdated.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "EvLocation(" +
                "countryCode=$countryCode, " +
                "partyId=$partyId, " +
                "id=$id, " +
                "publish=$publish, " +
                "publishAllowedTo=$publishAllowedTo, " +
                "name=$name, " +
                "address=$address, " +
                "city=$city, " +
                "postalCode=$postalCode, " +
                "state=$state, " +
                "country=$country, " +
                "coordinates=$coordinates, " +
                "relatedLocations=$relatedLocations, " +
                "parkingType=$parkingType, " +
                "evses=$evses, " +
                "directions=$directions, " +
                "operatorDetails=$operatorDetails, " +
                "suboperatorDetails=$suboperatorDetails, " +
                "ownerDetails=$ownerDetails, " +
                "facilities=$facilities, " +
                "timezone=$timezone, " +
                "chargingWhenClosed=$chargingWhenClosed, " +
                "images=$images, " +
                "energyMix=$energyMix, " +
                "lastUpdated=$lastUpdated" +
                ")"
    }
}
