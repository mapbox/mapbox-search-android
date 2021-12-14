package com.mapbox.search.sample

import com.mapbox.search.sample.Constants.Assets
import com.mapbox.search.sample.robots.builders.SearchResultsRecyclerBuilder

object SearchResultsInflater {

    fun SearchResultsRecyclerBuilder.inflateSearchResults(mockResponseAssetName: String) {
        return when (mockResponseAssetName) {
            Assets.RANELAGH_SUGGESTIONS_ASSET -> inflateForRanelaghSuggestion()
            Assets.CATEGORY_CAFE_RESULTS_ASSET -> inflateForCafeCategory()
            else -> error("No inflatable search results found for \"$mockResponseAssetName\" asset file.")
        }
    }
}

private fun SearchResultsRecyclerBuilder.inflateForRanelaghSuggestion() {
    result(
        name = "62 Ranelagh Terrace",
        address = "Royal Leamington Spa, Royal Leamington Spa, CV31 3BS, United Kingdom",
        distance = "5281.7 mi"
    )
    result(
        name = "62 Ranelagh Terrace",
        address = "Karori, Wellington 6012, New Zealand",
        distance = "6741.9 mi"
    )
    result(
        name = "62 Ranelagh Terrace",
        address = "Huntsbury, Christchurch 8022, New Zealand",
        distance = "6928.3 mi"
    )
    result(
        name = "Rusina Ct Ranelagh Terrac E",
        address = "Royal Leamington Spa, Royal Leamington Spa, CV31 3BS, United Kingdom",
        distance = "5281.7 mi"
    )
    result(
        name = "62 Ranelagh",
        address = "Dublin, Dublin D06, Ireland",
        distance = "5089 mi"
    )
    result(
        name = "62 Ranelagh Street",
        address = "Bellville, Western Cape 7530, South Africa",
        distance = "10234 mi"
    )
    result(
        name = "62 Ranelagh Road",
        address = "Cape Town, Western Cape 7700, South Africa",
        distance = "10227.8 mi"
    )
    result(
        name = "Ranelagh Terrace",
        address = "South Perth Western Australia 6151, Australia",
        distance = "9171.4 mi"
    )
    result(
        name = "62 Ranelagh Street",
        address = "Ranelagh Tasmania 7109, Australia",
        distance = "7947.3 mi"
    )
}

private fun SearchResultsRecyclerBuilder.inflateForCafeCategory() {
    result(
        name = "Googleplex - Big Table Cafe",
        address = "1900 Charleston Rd, Mountain View, California 94043, United States of America",
        distance = "0.2 mi"
    )
    result(
        name = "Krispy Kreme Doughnuts",
        address = "2146 Leghorn St, Mountain View, California 94043, United States of America",
        distance = "0.6 mi"
    )
    result(
        name = "Starbucks",
        address = "1380 Pear Ave, Mountain View, California 94043, United States of America",
        distance = "0.6 mi"
    )
    result(
        name = "Googleplex - Coffee Lab",
        address = "1345 Shorebird Way, Mountain View, California 94043, United States of America",
        distance = "0.7 mi"
    )
    result(
        name = "Starbucks",
        address = "2410 Charleston Rd, Mountain View, California 94043, United States of America",
        distance = "0.7 mi"
    )
    result(
        name = "Shoreline Lake American Bistro",
        address = "3160 N Shoreline Blvd, Palo Alto, California 94303, United States of America",
        distance = "0.8 mi"
    )
    result(
        name = "Cloud Bistro",
        address = "1401 N Shoreline Blvd, Mountain View, California 94043, United States of America",
        distance = "0.6 mi"
    )
    result(
        name = "Bajis Cafe",
        address = "2423 Old Middlefield Way, Mountain View, California 94043, United States of America",
        distance = "0.9 mi"
    )
    result(
        name = "The Art of Tea",
        address = "Mountain View, California 94043, United States of America",
        distance = "0.2 mi"
    )
    result(
        name = "Starbucks",
        address = "580 N Rengstorff Ave, Mountain View, California 94043, United States of America",
        distance = "0.9 mi"
    )
}
