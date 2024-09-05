package com.mapbox.search.analytics

import android.annotation.SuppressLint
import android.graphics.Bitmap
import com.mapbox.geojson.Point
import com.mapbox.search.BuildConfig
import com.mapbox.search.RequestOptions
import com.mapbox.search.ViewportProvider
import com.mapbox.search.analytics.events.AppMetadata
import com.mapbox.search.analytics.events.SearchFeedbackEvent
import com.mapbox.search.analytics.events.SearchResultEntry
import com.mapbox.search.analytics.events.SearchResultsInfo
import com.mapbox.search.base.core.CoreSearchEngineInterface
import com.mapbox.search.base.location.calculateMapZoom
import com.mapbox.search.base.result.BaseRawResultType
import com.mapbox.search.base.result.BaseRawSearchResult
import com.mapbox.search.base.result.BaseSearchResponse
import com.mapbox.search.base.result.mapToCore
import com.mapbox.search.base.utils.FormattedTimeProvider
import com.mapbox.search.base.utils.UUIDProvider
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.mapToCore
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.SearchAddress.FormatStyle
import com.mapbox.search.result.mapToPlatform
import com.mapbox.search.utils.bitmap.BitmapEncodeOptions
import com.mapbox.search.utils.bitmap.encodeBase64
import java.util.TreeMap

public class SearchFeedbackEventsFactory(
    private val providedUserAgent: String,
    private val viewportProvider: ViewportProvider?,
    private val uuidProvider: UUIDProvider,
    private val coreSearchEngine: CoreSearchEngineInterface,
    private val eventJsonParser: AnalyticsEventJsonParser,
    private val formattedTimeProvider: FormattedTimeProvider,
    private val bitmapEncoder: (Bitmap) -> String = { it.encodeBase64(BITMAP_ENCODE_OPTIONS) }
) {

    @SuppressLint("WrongConstant")
    public fun createSearchFeedbackEvent(
        event: MissingResultFeedbackEvent,
        currentLocation: Point?,
        callback: CompletionCallback<SearchFeedbackEvent>
    ) {
        createSearchFeedbackEvent(
            baseRawSearchResult = null,
            requestOptions = event.responseInfo.requestOptions,
            searchResponse = event.responseInfo.coreSearchResponse,
            currentLocation = currentLocation,
            isReproducible = event.responseInfo.isReproducible,
            event = FeedbackEvent(
                reason = MISSING_RESULT_FEEDBACK_REASON,
                text = event.text,
                screenshot = event.screenshot,
                sessionId = event.sessionId,
                feedbackId = event.feedbackId,
            ),
            asTemplate = false,
            callback = callback
        )
    }

    public fun createSearchFeedbackEvent(
        record: IndexableRecord,
        event: FeedbackEvent,
        currentLocation: Point?,
    ): SearchFeedbackEvent {
        val formattedAddress = record.address?.formattedAddress(FormatStyle.Full)

        return SearchFeedbackEvent().apply {
            this.event = SearchFeedbackEvent.EVENT_NAME

            // Mandatory fields
            created = formattedTimeProvider.currentTimeIso8601Formatted()
            resultIndex = -1
            sessionIdentifier = INDEXABLE_RECORD_SESSION_IDENTIFIER
            selectedItemName = formattedAddress ?: INDEXABLE_RECORD_FALLBACK_FEEDBACK_NAME
            feedbackReason = event.reason
            queryString = ""

            // Optional fields
            fillCommonData(currentLocation, event.feedbackId ?: uuidProvider.generateUUID())
            cached = true // Agreed to mark local favorites as cached
            feedbackText = event.text
            screenshot = event.screenshot?.let(bitmapEncoder)
            if (event.sessionId != null) {
                appMetadata = AppMetadata(sessionId = event.sessionId)
            }
            resultCoordinates = record.coordinate.coordinates()
            schema = "${SearchFeedbackEvent.EVENT_NAME}-$SEARCH_FEEDBACK_SCHEMA_VERSION"
        }
    }

    public fun createSearchFeedbackEvent(
        baseRawSearchResult: BaseRawSearchResult?,
        requestOptions: RequestOptions,
        searchResponse: BaseSearchResponse?,
        currentLocation: Point?,
        isReproducible: Boolean? = null,
        event: FeedbackEvent? = null,
        isCached: Boolean? = null,
        asTemplate: Boolean = false,
        callback: CompletionCallback<SearchFeedbackEvent>
    ) {
        coreSearchEngine.makeFeedbackEvent(requestOptions.mapToCore(), baseRawSearchResult?.mapToCore()) {
            val baseEvent = eventJsonParser.parse(it) as? SearchFeedbackEvent
            if (baseEvent == null) {
                callback.onError(Exception("Unable to parse event: $it"))
                return@makeFeedbackEvent
            }

            baseEvent.apply {
                this.event = SearchFeedbackEvent.EVENT_NAME

                // Mandatory fields
                created = formattedTimeProvider.currentTimeIso8601Formatted()
                // IndexableRecordSearchResultImpl and IndexableRecordSearchSuggestion may have
                // serverIndex == null due to implementation details. Because "resultIndex" is
                // mandatory, fallback to -1 for null case.
                resultIndex = baseRawSearchResult?.serverIndex ?: -1
                sessionIdentifier = when (isCached) {
                    true -> INDEXABLE_RECORD_SESSION_IDENTIFIER
                    else -> requestOptions.sessionID
                }
                selectedItemName = baseRawSearchResult?.names?.firstOrNull() ?: ""
                feedbackReason = event?.reason
                queryString = requestOptions.query

                // Optional fields
                fillRequestOptionsData(requestOptions)
                fillSearchResultData(searchResponse, isReproducible)
                feedbackText = event?.text
                screenshot = event?.screenshot?.let(bitmapEncoder)
                if (event?.sessionId != null) {
                    appMetadata = AppMetadata(sessionId = event.sessionId)
                }
                resultId = when (isCached) {
                    true -> null // ID of IndexableRecord may potentially contain PII,
                    // so we don't specify it
                    else -> baseRawSearchResult?.id
                }
                resultCoordinates = baseRawSearchResult?.center?.coordinates()
                schema = "${SearchFeedbackEvent.EVENT_NAME}-$SEARCH_FEEDBACK_SCHEMA_VERSION"

                // For events, that will be used for template creation,
                // we don't want specify common data and cached flag, because it will
                // be overridden during sending anyway.
                if (!asTemplate) {
                    fillCommonData(currentLocation, event?.feedbackId ?: uuidProvider.generateUUID())
                    cached = isCached
                } else {
                    // This properties might be populated by Core SDK, so
                    // clean up is needed.
                    latitude = null
                    longitude = null
                    userAgent = null
                }
            }

            callback.onComplete(baseEvent)
        }
    }

    private fun SearchFeedbackEvent.fillRequestOptionsData(requestOptions: RequestOptions) {
        language = requestOptions.options.languages?.map { it.code }
        boundingBox = requestOptions.options.boundingBox?.run {
            listOf(west(), south(), east(), north())
        }
        country = requestOptions.options.countries?.map { it.code }
        types = requestOptions.options.types?.map { it.mapToCore().toString() }
        @Suppress("DEPRECATION")
        fuzzyMatch = requestOptions.options.fuzzyMatch
        limit = requestOptions.options.limit
        proximity = requestOptions.options.proximity?.coordinates()
        responseUuid = requestOptions.requestContext.responseUuid
        keyboardLocale = requestOptions.requestContext.keyboardLocale?.language
        orientation = requestOptions.requestContext.screenOrientation?.rawValue
    }

    private fun SearchFeedbackEvent.fillSearchResultData(searchResponse: BaseSearchResponse?, isReproducible: Boolean?) {
        val results = (searchResponse?.result as? BaseSearchResponse.Result.Success)?.result
        val resultEntries = results?.map { coreResult ->
            SearchResultEntry(
                name = when (BaseRawResultType.USER_RECORD) {
                    coreResult.types.firstOrNull() -> "<Local item>"
                    else -> coreResult.names.firstOrNull() ?: ""
                },
                address = coreResult.descriptionAddress ?: coreResult.addresses?.getOrNull(0)?.mapToPlatform()?.formattedAddress(FormatStyle.Full),
                coordinates = coreResult.center?.coordinates(),
                id = when (BaseRawResultType.USER_RECORD) {
                    coreResult.types.firstOrNull() -> "<Local id>"
                    else -> coreResult.id
                },
                language = coreResult.languages,
                types = coreResult.types.map { it.toString() },
                // Ensure same entries order during GSON serialization
                externalIDs = coreResult.externalIDs?.let { TreeMap(it) },
                category = coreResult.categories,
            )
        }

        searchResultsJson = if (resultEntries != null && isReproducible != null) {
            eventJsonParser.serializeAny(SearchResultsInfo(resultEntries, !isReproducible))
        } else {
            null
        }
    }

    private fun SearchFeedbackEvent.fillCommonData(currentLocation: Point?, feedbackId: String) {
        latitude = currentLocation?.latitude()
        longitude = currentLocation?.longitude()
        userAgent = providedUserAgent
        viewportProvider?.getViewport()?.apply {
            mapZoom = calculateMapZoom(this)
            mapCenterLatitude = (north() + south()) / 2
            mapCenterLongitude = (east() + west()) / 2
        }
        this.feedbackId = feedbackId
        // Absence of "isTest" field is treated as if "isTest = false".
        // So we set it only for `true` cases (same logic on iOS).
        if (BuildConfig.DEBUG) {
            isTest = true
        }
    }

    private companion object {
        const val SEARCH_FEEDBACK_SCHEMA_VERSION = "2.3"
        const val INDEXABLE_RECORD_SESSION_IDENTIFIER = "<Not available>"
        const val INDEXABLE_RECORD_FALLBACK_FEEDBACK_NAME = "<No address>"
        const val MISSING_RESULT_FEEDBACK_REASON = "cannot_find"
        val BITMAP_ENCODE_OPTIONS = BitmapEncodeOptions(minSideSize = 400, compressQuality = 90)
    }
}
