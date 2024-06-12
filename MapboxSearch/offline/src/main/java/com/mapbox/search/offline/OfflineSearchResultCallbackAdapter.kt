package com.mapbox.search.offline

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.search.base.BaseResponseInfo
import com.mapbox.search.base.BaseSearchCallback
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.turf.TurfMeasurement

internal class OfflineSearchResultCallbackAdapter(private val feature: Feature, private val callback: OfflineSearchResultCallback) : BaseSearchCallback {

    override fun onResults(results: List<BaseSearchResult>, responseInfo: BaseResponseInfo) {
        val rawSearchResult: BaseRawSearchResult = bestMatch(results)?.rawSearchResult ?: feature.toBaseRawSearchResult()
        val offlineSearchResult = OfflineSearchResult(rawSearchResult)
        val offlineResponseInfo = OfflineResponseInfo(responseInfo.requestOptions.core.mapToOfflineSdkType())
        callback.onResult(offlineSearchResult, offlineResponseInfo)
    }

    override fun onError(e: Exception) {
        callback.onError(e)
    }

    private fun bestMatch(results: List<BaseSearchResult>): BaseSearchResult? {
        val featureName = REMOVE_PUNCTUATION_REGEX.replace(feature.getStringProperty("name").lowercase(), "")
        return results
            .filter { it.distanceMeters != null && it.distanceMeters!!.compareTo(MAX_DISTANCE_KM) < 0 }
            .find {
                val resultName = REMOVE_PUNCTUATION_REGEX.replace(it.name.lowercase(), "")
                (featureName == resultName) or resultName.startsWith(featureName) or featureName.startsWith(resultName)
            }
    }

    private fun Feature.toBaseRawSearchResult() = BaseRawSearchResult(
        "",
        null,
        listOf(BaseRawResultType.PLACE),
        listOf(getStringProperty("name")),
        listOf(),
        listOf(),
        null,
        null,
        null,
        null,
        (when (val geometry = geometry()) {
            is Point -> geometry
            else -> TurfMeasurement.center(this).geometry() as Point
        }),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        0,
        null,
        null,
        null
    )

    private companion object {
        private const val MAX_DISTANCE_KM = 200.0
        private val REMOVE_PUNCTUATION_REGEX = "[^\\w\\s]".toRegex()
    }
}
