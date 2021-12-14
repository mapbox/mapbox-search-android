package com.mapbox.search.sample

import com.mapbox.search.sample.robots.searchBottomSheet
import org.junit.Test

class MainActivityTest : MockServerSearchActivityTest() {

    @Test
    fun searchViewActivityTest() {
        searchBottomSheet {
            verifyQueryHintIsVisible()
            verifySearchInputNotInFocus()
            verifyRootElevation(R.dimen.search_card_elevation)
        }
    }
}
