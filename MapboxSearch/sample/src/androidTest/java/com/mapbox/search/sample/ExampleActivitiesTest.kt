package com.mapbox.search.sample

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.rule.GrantPermissionRule
import com.adevinta.android.barista.rule.flaky.FlakyTestRule
import com.mapbox.search.sample.api.AddressAutofillKotlinExampleActivity
import com.mapbox.search.sample.api.CategorySearchJavaExampleActivity
import com.mapbox.search.sample.api.CategorySearchKotlinExampleActivity
import com.mapbox.search.sample.api.CustomIndexableDataProviderJavaExample
import com.mapbox.search.sample.api.CustomIndexableDataProviderKotlinExample
import com.mapbox.search.sample.api.DiscoverApiKotlinExampleActivity
import com.mapbox.search.sample.api.FavoritesDataProviderJavaExample
import com.mapbox.search.sample.api.FavoritesDataProviderKotlinExample
import com.mapbox.search.sample.api.ForwardGeocodingBatchResolvingJavaExampleActivity
import com.mapbox.search.sample.api.ForwardGeocodingBatchResolvingKotlinExampleActivity
import com.mapbox.search.sample.api.ForwardGeocodingJavaExampleActivity
import com.mapbox.search.sample.api.ForwardGeocodingKotlinExampleActivity
import com.mapbox.search.sample.api.HistoryDataProviderJavaExample
import com.mapbox.search.sample.api.HistoryDataProviderKotlinExample
import com.mapbox.search.sample.api.JapanSearchJavaExampleActivity
import com.mapbox.search.sample.api.JapanSearchKotlinExampleActivity
import com.mapbox.search.sample.api.OfflineReverseGeocodingJavaExampleActivity
import com.mapbox.search.sample.api.OfflineReverseGeocodingKotlinExampleActivity
import com.mapbox.search.sample.api.OfflineSearchJavaExampleActivity
import com.mapbox.search.sample.api.OfflineSearchKotlinExampleActivity
import com.mapbox.search.sample.api.ReverseGeocodingJavaExampleActivity
import com.mapbox.search.sample.api.ReverseGeocodingKotlinExampleActivity
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ExampleActivitiesTest(private val clazz: Class<out Activity>) {

    private val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private val flakyRule = FlakyTestRule().apply {
        allowFlakyAttemptsByDefault(3)
    }

    @Rule
    @JvmField
    var chain: RuleChain = RuleChain.outerRule(flakyRule)
        .around(permissionRule)

    @Test
    fun testActivityLaunchesWithoutCrash() {
        ActivityScenario.launch(clazz)
    }

    companion object {

        @Parameterized.Parameters
        @JvmStatic
        fun testParameters(): Collection<Class<out Activity>> = listOf(
            AddressAutofillKotlinExampleActivity::class.java,
            AddressAutofillUiActivity::class.java,

            DiscoverApiKotlinExampleActivity::class.java,
            DiscoverApiUiActivity::class.java,

            CustomThemeActivity::class.java,

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

            JapanSearchKotlinExampleActivity::class.java,
            JapanSearchJavaExampleActivity::class.java,

            HistoryDataProviderKotlinExample::class.java,
            HistoryDataProviderJavaExample::class.java,

            FavoritesDataProviderKotlinExample::class.java,
            FavoritesDataProviderJavaExample::class.java,
        )
    }
}
