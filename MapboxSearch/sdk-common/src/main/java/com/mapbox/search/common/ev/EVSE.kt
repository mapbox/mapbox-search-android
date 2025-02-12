package com.mapbox.search.common.ev

import android.os.Parcelable
import androidx.annotation.RestrictTo
import com.mapbox.annotation.MapboxExperimental
import com.mapbox.geojson.Point
import com.mapbox.search.common.LocalizedText
import com.mapbox.search.common.metadata.ImageInfo
import kotlinx.parcelize.Parcelize

/**
 * OCPI EVSE. The EVSE object describes the part that controls the power supply to a single EV
 * in a single session. It always belongs to a Location object. Refer to this type in the
 * [OCPI GitHub repository](https://github.com/ocpi/ocpi/blob/2.2.1/mod_locations.asciidoc#mod_locations_evse_object)
 * for more details.
 *
 * @property uid Uniquely identifies the EVSE within the operators platform (and suboperator platforms).
 * For example a database ID or the actual "EVSE ID". This field can never be changed,
 * modified or renamed. This is the technical identification of the EVSE, not to be used as
 * human readable identification, use the field evse_id for that.
 * @property evseId Compliant with the following specification for EVSE ID from
 * "eMI3 standard version V1.0" (http://emi3group.com/documents-links/) "Part 2: business objects."
 * Optional because: If an EVSE ID is to be reused in the real world, you can remove
 * the evse_id from an EVSE object if the status after setting its status to REMOVED.
 * @property status [EvseStatus.Type] value that indicates the current status of the EVSE.
 * @property statusSchedule List of [EvStatusSchedule] representing a planned status update of the EVSE.
 * @property capabilities List of [EvseCapability] that the EVSE is capable of.
 * @property connectors List of available [EvConnector] objects on the EVSE.
 * @property floorLevel Level on which the Charge Point is located (in garage buildings)
 * in the locally displayed numbering scheme.
 * @property coordinate Coordinate of the EVSE.
 * @property physicalReference A number/string printed on the outside of the EVSE for visual identification.
 * @property directions [LocalizedText] objects representing multi-language human-readable directions
 * when more detailed information on how to reach the EVSE from the Location is required.
 * @property parkingRestrictions List of [EvParkingRestriction.Type] values that apply to the parking spot.
 * @property images List of [ImageInfo] objects representing links to images related to the EVSE such as photos or logos.
 * @property lastUpdated Timestamp in RFC 3339 format when this EVSE or
 * one of its Connectors was last updated (or created).
 */
@MapboxExperimental
@Parcelize
public class EVSE @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX) constructor(
    public val uid: String?,
    public val evseId: String? = null,
    @EvseStatus.Type public val status: String,
    public val statusSchedule: List<EvStatusSchedule>,
    public val capabilities: List<String>,
    public val connectors: List<EvConnector>,
    public val floorLevel: String? = null,
    public val coordinate: Point? = null,
    public val physicalReference: String? = null,
    public val directions: List<LocalizedText>,
    public val parkingRestrictions: List<String>,
    public val images: List<ImageInfo>,
    public val lastUpdated: String?,
) : Parcelable {

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EVSE

        if (uid != other.uid) return false
        if (evseId != other.evseId) return false
        if (status != other.status) return false
        if (statusSchedule != other.statusSchedule) return false
        if (capabilities != other.capabilities) return false
        if (connectors != other.connectors) return false
        if (floorLevel != other.floorLevel) return false
        if (coordinate != other.coordinate) return false
        if (physicalReference != other.physicalReference) return false
        if (directions != other.directions) return false
        if (parkingRestrictions != other.parkingRestrictions) return false
        if (images != other.images) return false
        if (lastUpdated != other.lastUpdated) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + (evseId?.hashCode() ?: 0)
        result = 31 * result + status.hashCode()
        result = 31 * result + statusSchedule.hashCode()
        result = 31 * result + capabilities.hashCode()
        result = 31 * result + connectors.hashCode()
        result = 31 * result + (floorLevel?.hashCode() ?: 0)
        result = 31 * result + (coordinate?.hashCode() ?: 0)
        result = 31 * result + (physicalReference?.hashCode() ?: 0)
        result = 31 * result + directions.hashCode()
        result = 31 * result + parkingRestrictions.hashCode()
        result = 31 * result + images.hashCode()
        result = 31 * result + lastUpdated.hashCode()
        return result
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "EVSE(" +
                "uid=$uid, " +
                "evseId=$evseId, " +
                "status=$status, " +
                "statusSchedule=$statusSchedule, " +
                "capabilities=$capabilities, " +
                "connectors=$connectors, " +
                "floorLevel=$floorLevel, " +
                "coordinates=$coordinate, " +
                "physicalReference=$physicalReference, " +
                "directions=$directions, " +
                "parkingRestrictions=$parkingRestrictions, " +
                "images=$images, " +
                "lastUpdated=$lastUpdated" +
                ")"
    }
}
