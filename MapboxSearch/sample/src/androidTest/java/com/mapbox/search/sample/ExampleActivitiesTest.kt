package com.mapbox.search.sample

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import com.mapbox.search.sample.api.CategorySearchJavaExampleActivity
import com.mapbox.search.sample.api.CategorySearchKotlinExampleActivity
import com.mapbox.search.sample.api.CustomIndexableDataProviderJavaExample
import com.mapbox.search.sample.api.CustomIndexableDataProviderKotlinExample
import com.mapbox.search.sample.api.FavoritesDataProviderJavaExample
import com.mapbox.search.sample.api.FavoritesDataProviderKotlinExample
import com.mapbox.search.sample.api.ForwardGeocodingBatchResolvingJavaExampleActivity
import com.mapbox.search.sample.api.ForwardGeocodingBatchResolvingKotlinExampleActivity
import com.mapbox.search.sample.api.ForwardGeocodingJavaExampleActivity
import com.mapbox.search.sample.api.ForwardGeocodingKotlinExampleActivity
import com.mapbox.search.sample.api.HistoryDataProviderJavaExample
import com.mapbox.search.sample.api.HistoryDataProviderKotlinExample
import com.mapbox.search.sample.api.OfflineReverseGeocodingJavaExampleActivity
import com.mapbox.search.sample.api.OfflineReverseGeocodingKotlinExampleActivity
import com.mapbox.search.sample.api.OfflineSearchJavaExampleActivity
import com.mapbox.search.sample.api.OfflineSearchKotlinExampleActivity
import com.mapbox.search.sample.api.ReverseGeocodingJavaExampleActivity
import com.mapbox.search.sample.api.ReverseGeocodingKotlinExampleActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ExampleActivitiesTest(private val clazz: Class<out Activity>) : MockServerSearchActivityTest() {

    @Test
    fun testActivityLaunchesWithoutCrash() {
        ActivityScenario.launch(clazz)
    }

    companion object {

        @Parameterized.Parameters
        @JvmStatic
        fun testParameters(): Collection<Class<out Activity>> = listOf(
            SimpleUiSearchActivity::class.java,

            CustomThemeActivity::class.java,

            // TODO enable MapsIntegrationExampleActivity when Maps SDK will be available with Common 21
            // MapsIntegrationExampleActivity::class.java,

            CustomIndexableDataProviderKotlinExample::class.java,
            CustomIndexableDataProviderJavaExample::class.java,

            ForwardGeocodingKotlinExampleActivity::class.java,
            ForwardGeocodingJavaExampleActivity::class.java,

            ForwardGeocodingBatchResolvingKotlinExampleActivity::class.java,
            ForwardGeocodingBatchResolvingJavaExampleActivity::class.java,

            ReverseGeocodingKotlinExampleActivity::class.java,
            ReverseGeocodingJavaExampleActivity::class.java,

            CategorySearchKotlinExampleActivity::class.java,
            CategorySearchJavaExampleActivity::class.java,

            OfflineSearchKotlinExampleActivity::class.java,
            OfflineSearchJavaExampleActivity::class.java,

            OfflineReverseGeocodingKotlinExampleActivity::class.java,
            OfflineReverseGeocodingJavaExampleActivity::class.java,

            HistoryDataProviderKotlinExample::class.java,
            HistoryDataProviderJavaExample::class.java,

            FavoritesDataProviderKotlinExample::class.java,
            FavoritesDataProviderJavaExample::class.java,
        )
    }
}
