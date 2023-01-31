package com.mapbox.search.sample.api

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.discover.DiscoverApi
import com.mapbox.search.discover.DiscoverApiQuery
import com.mapbox.search.sample.R

class DiscoverApiKotlinExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val discoverApi = DiscoverApi.create(
            accessToken = getString(R.string.mapbox_access_token),
        )

        lifecycleScope.launchWhenCreated {
            val dcRegion = BoundingBox.fromPoints(
                Point.fromLngLat(-77.04482563320445, 38.89626984069077),
                Point.fromLngLat(-77.02584649998599, 38.907104458514695)
            )

            val response = discoverApi.search(
                query = DiscoverApiQuery.Category.COFFEE_SHOP_CAFE,
                region = dcRegion
            )

            response.onValue { results ->
                Log.i("SearchApiExample", "Discover API results: $results")
            }.onError { e ->
                Log.i("SearchApiExample", "Discover API error", e)
            }
        }
    }
}
