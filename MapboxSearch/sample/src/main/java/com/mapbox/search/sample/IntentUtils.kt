package com.mapbox.search.sample

import android.content.Intent
import android.net.Uri
import com.mapbox.geojson.Point
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.ui.view.place.SearchPlace

object IntentUtils {

    fun geoIntent(point: Point): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${point.latitude()}, ${point.longitude()}"))
    }

    fun shareIntent(searchPlace: SearchPlace): Intent {
        val text = "${searchPlace.name}. " +
                "Address: ${searchPlace.address?.formattedAddress(SearchAddress.FormatStyle.Short) ?: "unknown"}. " +
                "Geo coordinate: (lat=${searchPlace.coordinate.latitude()}, lon=${searchPlace.coordinate.longitude()})"

        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }
}
