package com.mapbox.search.tests_support

import com.mapbox.search.result.SearchAddress

internal fun SearchAddress.toStringFull(): String {
    return "SearchAddress(houseNumber=$houseNumber, street=$street, neighborhood=$neighborhood, locality=$locality, postcode=$postcode, place=$place, district=$district, region=$region, country=$country)"
}
