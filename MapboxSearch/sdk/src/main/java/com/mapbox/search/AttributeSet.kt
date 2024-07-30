package com.mapbox.search

import com.mapbox.search.base.core.CoreAttributeSet

public enum class AttributeSet {
    /**
     * Essential information about a location such as name, address and coordinates. This is the default value for attribute_sets parameter, and will be provided when attribute_sets is not provided in the request.
     */
    BASIC,

    /**
     * A collection of photos related to the location.
     */
    PHOTOS,

    /**
     * Specific information about the location including a detailed description text, user reviews, price level and popularity.
     */
    VENUE,

    /**
     * Visiting information for the location like website, phone number and social media handles.
     */
    VISIT
}

@JvmSynthetic
internal fun AttributeSet.mapToCore(): CoreAttributeSet {
    return when (this) {
        AttributeSet.BASIC -> CoreAttributeSet.BASIC
        AttributeSet.PHOTOS -> CoreAttributeSet.PHOTOS
        AttributeSet.VENUE -> CoreAttributeSet.VENUE
        AttributeSet.VISIT -> CoreAttributeSet.VISIT
    }
}

@JvmSynthetic
internal fun CoreAttributeSet.mapToPlatform(): AttributeSet {
    return when (this) {
        CoreAttributeSet.BASIC -> AttributeSet.BASIC
        CoreAttributeSet.PHOTOS -> AttributeSet.PHOTOS
        CoreAttributeSet.VENUE -> AttributeSet.VENUE
        CoreAttributeSet.VISIT -> AttributeSet.VISIT
    }
}
