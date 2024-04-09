package com.mapbox.search.sample.api

import androidx.lifecycle.lifecycleScope
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.discover.Discover
import com.mapbox.search.discover.DiscoverQuery
import com.mapbox.search.sample.R

class DiscoverKotlinExampleActivity : BaseKotlinExampleActivity() {

    override val titleResId: Int = R.string.action_discover_kotlin_example

    override fun startExample() {
        // Set your Access Token here if it's not already set in some other way
        // MapboxOptions.accessToken = "<my-access-token>"
        val discover = Discover.create()

        lifecycleScope.launchWhenCreated {
            val dcRegion = BoundingBox.fromPoints(
                Point.fromLngLat(-77.04482563320445, 38.89626984069077),
                Point.fromLngLat(-77.02584649998599, 38.907104458514695)
            )

            val response = discover.search(
                query = DiscoverQuery.Category.COFFEE_SHOP_CAFE,
                region = dcRegion
            )

            response.onValue { results ->
                logI("SearchApiExample", "Discover results", results)
                onFinished()
            }.onError { e ->
                logE("SearchApiExample", "Discover error", e)
                onFinished()
            }
        }
    }
}
