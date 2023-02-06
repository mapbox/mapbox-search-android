package com.mapbox.search.sample.api

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.discover.Discover
import com.mapbox.search.discover.DiscoverQuery
import com.mapbox.search.sample.R

class DiscoverKotlinExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val discover = Discover.create(
            accessToken = getString(R.string.mapbox_access_token),
        )

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
                Log.i("SearchApiExample", "Discover results: $results")
            }.onError { e ->
                Log.i("SearchApiExample", "Discover error", e)
            }
        }
    }
}
