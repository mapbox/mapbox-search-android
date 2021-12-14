package com.mapbox.search.analytics

import android.annotation.SuppressLint
import android.graphics.Bitmap
import com.mapbox.geojson.Point
import com.mapbox.search.ApiType
import com.mapbox.search.BuildConfig
import com.mapbox.search.RequestOptions
import com.mapbox.search.ViewportProvider
import com.mapbox.search.analytics.events.AppMetadata
import com.mapbox.search.analytics.events.SearchFeedbackEvent
import com.mapbox.search.analytics.events.SearchResultEntry
import com.mapbox.search.analytics.events.SearchResultsInfo
import com.mapbox.search.common.throwDebug
import com.mapbox.search.core.CoreSearchEngineInterface
import com.mapbox.search.core.CoreSearchResponse
import com.mapbox.search.internal.bindgen.ResultType
import com.mapbox.search.location.calculateMapZoom
import com.mapbox.search.mapToCore
import com.mapbox.search.record.IndexableRecord
import com.mapbox.search.result.OriginalSearchResult
import com.mapbox.search.result.SearchAddress.FormatStyle
import com.mapbox.search.result.mapToCore
import com.mapbox.search.result.mapToPlatform
import com.mapbox.search.utils.FormattedTimeProvider
import com.mapbox.search.utils.UUIDProvider
import com.mapbox.search.utils.bitmap.BitmapEncodeOptions
import com.mapbox.search.utils.bitmap.encodeBase64
import java.util.TreeMap

internal class TelemetrySearchEventsFactory(
    private val providedUserAgent: String,
    private val viewportProvider: ViewportProvider?,
    private val uuidProvider: UUIDProvider,
    private val coreEngineProvider: (ApiType) -> CoreSearchEngineInterface,
    private val eventJsonParser: AnalyticsEventJsonParser,
    private val formattedTimeProvider: FormattedTimeProvider,
    private val jsonSerializer: (Any) -> String,
    private val bitmapEncoder: (Bitmap) -> String = { it.encodeBase64(BITMAP_ENCODE_OPTIONS) }
) {

    fun updateCachedSearchFeedbackEvent(cachedEvent: SearchFeedbackEvent, event: FeedbackEvent, currentLocation: Point?) {
        cachedEvent.apply {
            this.event = SearchFeedbackEvent.EVENT_NAME

            // Mandatory fields
            created = formattedTimeProvider.currentTimeIso8601Formatted()
            feedbackReason = event.reason

            // Optional fields
            fillCommonData(currentLocation)
            cached = true
            feedbackText = event.text ?: feedbackText
            screenshot = event.screenshot?.let(bitmapEncoder) ?: screenshot
            if (event.sessionId != null) {
                appMetadata = AppMetadata(sessionId = event.sessionId)
            }
        }
    }

    @SuppressLint("WrongConstant")
    fun createSearchFeedbackEvent(event: MissingResultFeedbackEvent, currentLocation: Point?): SearchFeedbackEvent {
        return createSearchFeedbackEvent(
            originalSearchResult = null,
            requestOptions = event.responseInfo.requestOptions,
            coreSearchResponse = event.responseInfo.coreSearchResponse,
            currentLocation = currentLocation,
            isReproducible = event.responseInfo.isReproducible,
            event = FeedbackEvent(
                reason = MISSING_RESULT_FEEDBACK_REASON,
                text = event.text,
                screenshot = event.screenshot,
                sessionId = event.sessionId,
            ),
            asTemplate = false
        )
    }

    fun createSearchFeedbackEvent(
        record: IndexableRecord,
        event: FeedbackEvent,
        currentLocation: Point?
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
            fillCommonData(currentLocation)
            cached = true // Agreed to mark local favorites as cached
            feedbackText = event.text
            screenshot = event.screenshot?.let(bitmapEncoder)
            if (event.sessionId != null) {
                appMetadata = AppMetadata(sessionId = event.sessionId)
            }
            resultCoordinates = record.coordinate?.coordinates()
            schema = "${SearchFeedbackEvent.EVENT_NAME}-$SEARCH_FEEDBACK_SCHEMA_VERSION"
        }
    }

    fun createSearchFeedbackEvent(
        originalSearchResult: OriginalSearchResult?,
        requestOptions: RequestOptions,
        coreSearchResponse: CoreSearchResponse?,
        currentLocation: Point?,
        isReproducible: Boolean? = null,
        event: FeedbackEvent? = null,
        isCached: Boolean? = null,
        asTemplate: Boolean = false
    ): SearchFeedbackEvent {
        // TODO remove coreEngineProvider for constructing SearchFeedbackEvent
        val baseEvent = try {
            requestOptions.requestContext.apiType.let(coreEngineProvider)
                .makeFeedbackEvent(requestOptions.mapToCore(), originalSearchResult?.mapToCore()).let {
                    eventJsonParser.parse(it) as? SearchFeedbackEvent
                } ?: throw IllegalStateException("Could not parse core event as SearchFeedbackEvent.")
        } catch (e: Exception) {
            throwDebug(e) { "Creating default SearchFeedbackEvent empty version." }
            SearchFeedbackEvent()
        }

        return baseEvent.apply {
            this.event = SearchFeedbackEvent.EVENT_NAME

            // Mandatory fields
            created = formattedTimeProvider.currentTimeIso8601Formatted()
            // IndexableRecordSearchResultImpl and IndexableRecordSearchSuggestion may have
            // serverIndex == null due to implementation details. Because "resultIndex" is
            // mandatory, fallback to -1 for null case.
            resultIndex = originalSearchResult?.serverIndex ?: -1
            sessionIdentifier = when (isCached) {
                true -> INDEXABLE_RECORD_SESSION_IDENTIFIER
                else -> requestOptions.sessionID
            }
            selectedItemName = originalSearchResult?.names?.firstOrNull() ?: ""
            feedbackReason = event?.reason
            queryString = requestOptions.query

            // Optional fields
            fillRequestOptionsData(requestOptions)
            fillSearchResultData(coreSearchResponse, isReproducible)
            feedbackText = event?.text
            screenshot = event?.screenshot?.let(bitmapEncoder)
            if (event?.sessionId != null) {
                appMetadata = AppMetadata(sessionId = event.sessionId)
            }
            resultId = when (isCached) {
                true -> null // ID of IndexableRecord may potentially contain PII,
                             // so we don't specify it
                else -> originalSearchResult?.id
            }
            resultCoordinates = originalSearchResult?.center?.coordinates()
            schema = "${SearchFeedbackEvent.EVENT_NAME}-$SEARCH_FEEDBACK_SCHEMA_VERSION"

            // For events, that will be used for template creation,
            // we don't want specify common data and cached flag, because it will
            // be overridden during sending anyway.
            if (!asTemplate) {
                fillCommonData(currentLocation)
                cached = isCached
            } else {
                // This properties might be populated by Core SDK, so
                // clean up is needed.
                latitude = null
                longitude = null
                userAgent = null
            }
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

    private fun SearchFeedbackEvent.fillSearchResultData(coreSearchResponse: CoreSearchResponse?, isReproducible: Boolean?) {
        val resultEntries = coreSearchResponse?.results?.map { coreResult ->
            SearchResultEntry(
                name = when {
                    coreResult.types.firstOrNull() == ResultType.USER_RECORD -> "<Local item>"
                    else -> coreResult.names.firstOrNull() ?: ""
                },
                address = coreResult.descrAddress ?: coreResult.addresses?.getOrNull(0)
                    ?.mapToPlatform()
                    ?.formattedAddress(FormatStyle.Full),
                coordinates = coreResult.center?.coordinates(),
                id = when {
                    coreResult.types.firstOrNull() == ResultType.USER_RECORD -> "<Local id>"
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
            jsonSerializer(SearchResultsInfo(resultEntries, !isReproducible))
        } else {
            null
        }
    }

    private fun SearchFeedbackEvent.fillCommonData(currentLocation: Point?) {
        latitude = currentLocation?.latitude()
        longitude = currentLocation?.longitude()
        userAgent = providedUserAgent
        viewportProvider?.getViewport()?.apply {
            mapZoom = calculateMapZoom(this)
            mapCenterLatitude = (north() + south()) / 2
            mapCenterLongitude = (east() + west()) / 2
        }
        feedbackId = uuidProvider.generateUUID()
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
