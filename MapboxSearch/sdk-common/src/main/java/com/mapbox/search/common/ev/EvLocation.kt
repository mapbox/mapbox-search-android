package com.mapbox.search.common.ev

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.search.common.BusinessDetails
import com.mapbox.search.common.metadata.ImageInfo
import kotlinx.parcelize.Parcelize

/**
 * OCPI Location. The Location object describes the location and its properties where a group of
 * EVSEs that belong together are installed.
 * Refer to this type in the [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#mod_locations_location_object)
 * for more details.
 *
 * @property partyId 3 letter identifier of the operator.
 * @property publish Defines if a Location may be published on a website or app etc.
 * @property publishAllowedTo PublishTokenType object. This field may only be used when the publish field is false.
 * @property relatedLocations AdditionalGeoLocation object representing geographical location of related points.
 * @property evses List of [EVSE] objects that belong to this Location.
 * @property operatorDetails BusinessDetails object representing information of the operator.
 * @property suboperatorDetails BusinessDetails object representing information of the suboperator if available.
 * @property ownerDetails BusinessDetails object representing information of the owner if available.
 * @property chargingWhenClosed Indicates if the EVSEs are still charging outside the opening hours of the location.
 * @property energyMix EnergyMix object representing details on the energy supplied at this location.
 */
@MapboxExperimental
@Parcelize
public class EvLocation @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    public val partyId: String,
    public val publish: Boolean,
    public val publishAllowedTo: List<EvsePublishTokenType>,
    public val relatedLocations: List<EvAdditionalGeoLocation>,
    public val evses: List<EVSE>,
    public val operatorDetails: BusinessDetails?,
    public val suboperatorDetails: BusinessDetails?,
    public val ownerDetails: BusinessDetails?,
    public val chargingWhenClosed: Boolean?,
    public val energyMix: EvEnergyMix?,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EvLocation

        if (partyId != other.partyId) return false
        if (publish != other.publish) return false
        if (publishAllowedTo != other.publishAllowedTo) return false
        if (relatedLocations != other.relatedLocations) return false
        if (evses != other.evses) return false
        if (operatorDetails != other.operatorDetails) return false
        if (suboperatorDetails != other.suboperatorDetails) return false
        if (ownerDetails != other.ownerDetails) return false
        if (chargingWhenClosed != other.chargingWhenClosed) return false
        if (energyMix != other.energyMix) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = partyId.hashCode()
        result = 31 * result + publish.hashCode()
        result = 31 * result + publishAllowedTo.hashCode()
        result = 31 * result + relatedLocations.hashCode()
        result = 31 * result + evses.hashCode()
        result = 31 * result + (operatorDetails?.hashCode() ?: 0)
        result = 31 * result + (suboperatorDetails?.hashCode() ?: 0)
        result = 31 * result + (ownerDetails?.hashCode() ?: 0)
        result = 31 * result + (chargingWhenClosed?.hashCode() ?: 0)
        result = 31 * result + (energyMix?.hashCode() ?: 0)
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "EvLocation(" +
                "partyId=$partyId, " +
                "publish=$publish, " +
                "publishAllowedTo=$publishAllowedTo, " +
                "relatedLocations=$relatedLocations, " +
                "evses=$evses, " +
                "operatorDetails=$operatorDetails, " +
                "suboperatorDetails=$suboperatorDetails, " +
                "ownerDetails=$ownerDetails, " +
                "chargingWhenClosed=$chargingWhenClosed, " +
                "energyMix=$energyMix, " +
                ")"
    }
}
