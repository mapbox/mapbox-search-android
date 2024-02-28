package com.mapbox.search.sample.api

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.search.category.Category
import com.mapbox.search.category.CategoryQuery

class CategoryKotlinExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set your Access Token here if it's not already set in some other way
        // MapboxOptions.accessToken = "<my-access-token>"
        val category = Category.create()

        lifecycleScope.launchWhenCreated {
            val dcRegion = BoundingBox.fromPoints(
                Point.fromLngLat(-77.04482563320445, 38.89626984069077),
                Point.fromLngLat(-77.02584649998599, 38.907104458514695)
            )

            val response = category.search(
                query = CategoryQuery.Category.COFFEE_SHOP_CAFE,
                region = dcRegion
            )

            response.onValue { results ->
                Log.i("SearchApiExample", "Category results: $results")
            }.onError { e ->
                Log.i("SearchApiExample", "Category error", e)
            }
        }
    }
}
