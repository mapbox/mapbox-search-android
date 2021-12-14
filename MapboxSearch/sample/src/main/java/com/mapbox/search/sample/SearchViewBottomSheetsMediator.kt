package com.mapbox.search.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.ui.view.SearchBottomSheetView
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.category.SearchCategoriesBottomSheetView
import com.mapbox.search.ui.view.feedback.SearchFeedbackBottomSheetView
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import kotlinx.parcelize.Parcelize
import java.util.LinkedList
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Sample implementation of search cards navigation and coordination.
 */
class SearchViewBottomSheetsMediator(
    private val searchBottomSheetView: SearchBottomSheetView,
    private val placeBottomSheetView: SearchPlaceBottomSheetView,
    private val categoriesBottomSheetView: SearchCategoriesBottomSheetView,
    private val feedbackBottomSheetView: SearchFeedbackBottomSheetView,
) {

    private val serviceProvider = MapboxSearchSdk.serviceProvider
    private val context = searchBottomSheetView.context

    // Stack top points to currently open screen, if empty -> SearchBottomSheetView is open
    private val screensStack = LinkedList<Transaction>()

    private val eventsListeners = CopyOnWriteArrayList<SearchBottomSheetsEventsListener>()

    init {
        with(searchBottomSheetView) {
            addOnCategoryClickListener { openCategory(it) }
            addOnSearchResultClickListener { searchResult, responseInfo ->
                val coordinate = searchResult.coordinate
                if (coordinate != null) {
                    openPlaceCard(
                        SearchPlace.createFromSearchResult(
                            searchResult = searchResult,
                            responseInfo = responseInfo,
                            coordinate = coordinate,
                        )
                    )
                }
            }
            addOnFavoriteClickListener {
                openPlaceCard(SearchPlace.createFromIndexableRecord(it, it.coordinate, distanceMeters = null))
            }
            addOnHistoryClickListener { historyRecord ->
                val coordinate = historyRecord.coordinate
                if (coordinate != null) {
                    openPlaceCard(SearchPlace.createFromIndexableRecord(historyRecord, coordinate, distanceMeters = null))
                } else {
                    // TODO: For now we don't support handling HistoryRecord without coordinates,
                    // because SDK adds records only that have coordinates. However, customers still can
                    // add HistoryRecord w/o coordinates.
                }
            }
        }

        with(placeBottomSheetView) {
            addOnBottomSheetStateChangedListener { newState, fromUser ->
                if (newState == SearchPlaceBottomSheetView.HIDDEN) {
                    onSubCardHidden(fromUser)
                }
            }
            addOnCloseClickListener { resetToRoot() }
            addOnSearchPlaceAddedToFavoritesListener { searchPlace, favorite ->
                val lastTransaction = screensStack.pollFirst()
                if (lastTransaction != null && lastTransaction.screen == Screen.PLACE) {
                    screensStack.push(Transaction(Screen.PLACE, searchPlace.copy(record = favorite)))
                } else {
                    screensStack.push(lastTransaction)
                }
            }
            addOnFeedbackClickListener { _, feedback ->
                feedbackBottomSheetView.open(feedback)
            }
        }

        with(categoriesBottomSheetView) {
            addOnBottomSheetStateChangedListener { newState, fromUser ->
                if (newState == SearchCategoriesBottomSheetView.HIDDEN) {
                    onSubCardHidden(fromUser)
                }
            }

            addOnCloseClickListener { resetToRoot() }
            addOnSearchResultClickListener { searchResult, responseInfo ->
                val coordinate = searchResult.coordinate
                if (coordinate != null) {
                    openPlaceCard(
                        SearchPlace.createFromSearchResult(
                            searchResult = searchResult,
                            responseInfo = responseInfo,
                            coordinate = coordinate,
                        )
                    )
                }
            }
        }

        with(feedbackBottomSheetView) {
            addOnCloseClickListener { resetToRoot() }
        }
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val savedStack = savedInstanceState.getParcelableArrayList<Transaction>(KEY_STATE_EXTERNAL_BACK_STACK) ?: return
        screensStack.clear()
        screensStack.addAll(savedStack)
        applyTopState()
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(KEY_STATE_EXTERNAL_BACK_STACK, ArrayList(screensStack))
    }

    private fun onSubCardHidden(hiddenByUser: Boolean) {
        if (hiddenByUser) {
            resetToRoot()
        } else if (categoriesBottomSheetView.isHidden() && placeBottomSheetView.isHidden() && searchBottomSheetView.isHidden()) {
            searchBottomSheetView.restorePreviousNonHiddenState()
            eventsListeners.forEach { it.onBackToMainBottomSheet() }
        }
    }

    private fun openCategory(category: Category, fromBackStack: Boolean = false) {
        if (fromBackStack) {
            categoriesBottomSheetView.restorePreviousNonHiddenState(category)
        } else {
            screensStack.push(Transaction(Screen.CATEGORIES, category))
            categoriesBottomSheetView.open(category)
        }
        searchBottomSheetView.hide()
        placeBottomSheetView.hide()
        eventsListeners.forEach { it.onOpenCategoriesBottomSheet(category) }
    }

    private fun openPlaceCard(
        place: SearchPlace,
        fromBackStack: Boolean = false
    ) {
        if (!fromBackStack) {
            // Put place without distance into screen stack, so during
            // reconfiguration we will recalculate distance.
            screensStack.push(Transaction(Screen.PLACE, place.copy(distanceMeters = null)))
        }

        placeBottomSheetView.open(place)
        searchBottomSheetView.hide()
        categoriesBottomSheetView.hide()
        eventsListeners.forEach { it.onOpenPlaceBottomSheet(place) }

        if (place.distanceMeters == null) {
            userDistanceTo(place.coordinate) { distance ->
                distance?.let {
                    placeBottomSheetView.updateDistance(distance)
                }
            }
        }
    }

    private fun resetToRoot() {
        searchBottomSheetView.open()
        feedbackBottomSheetView.hide()
        placeBottomSheetView.hide()
        categoriesBottomSheetView.hideCardAndCancelLoading()
        screensStack.clear()
        eventsListeners.forEach { it.onBackToMainBottomSheet() }
    }

    private fun popBackStack(): Boolean {
        if (screensStack.isEmpty()) {
            return false
        }
        screensStack.pop()
        applyTopState()
        return true
    }

    private fun applyTopState() {
        if (screensStack.isEmpty()) {
            placeBottomSheetView.hide()
            categoriesBottomSheetView.hideCardAndCancelLoading()
        } else {
            val transaction = screensStack.peek()
            if (transaction == null) {
                fallback { "Transaction is null" }
            } else {
                transaction.execute()
            }
        }
    }

    private fun Transaction.execute() {
        when (screen) {
            Screen.CATEGORIES -> {
                val category = arg as? Category
                if (category == null) {
                    fallback { "Saved category is null" }
                } else {
                    openCategory(category, fromBackStack = true)
                }
            }
            Screen.PLACE -> {
                val place = arg as? SearchPlace
                if (place == null) {
                    fallback { "Saved place is null" }
                } else {
                    openPlaceCard(place, fromBackStack = true)
                }
            }
        }
    }

    fun handleOnBackPressed(): Boolean {
        return searchBottomSheetView.handleOnBackPressed() ||
                categoriesBottomSheetView.handleOnBackPressed() ||
                feedbackBottomSheetView.handleOnBackPressed() ||
                popBackStack()
    }

    private fun fallback(assertMessage: () -> String) {
        if (BuildConfig.DEBUG) {
            throw IllegalStateException(assertMessage())
        }
        resetToRoot()
    }

    @SuppressLint("MissingPermission")
    private fun lastKnownLocation(callback: (Point?) -> Unit) {
        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            callback(null)
        }

        serviceProvider.locationEngine().getLastLocation(object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult?) {
                val location = (result?.locations?.lastOrNull() ?: result?.lastLocation)?.let { location ->
                    Point.fromLngLat(location.longitude, location.latitude)
                }
                callback(location)
            }

            override fun onFailure(p0: Exception) {
                callback(null)
            }
        })
    }

    // Should work quit fast as we get last known location.
    // If default Android Location Manager is used, callback will be triggered immediately.
    private fun userDistanceTo(destination: Point, callback: (Double?) -> Unit) {
        lastKnownLocation { location ->
            if (location == null) {
                callback(null)
            } else {
                val distance = serviceProvider
                    .distanceCalculator(latitude = location.latitude())
                    .distance(location, destination)
                callback(distance)
            }
        }
    }

    fun addSearchBottomSheetsEventsListener(listener: SearchBottomSheetsEventsListener) {
        eventsListeners.add(listener)
    }

    fun removeSearchBottomSheetsEventsListener(listener: SearchBottomSheetsEventsListener) {
        eventsListeners.remove(listener)
    }

    interface SearchBottomSheetsEventsListener {
        fun onOpenPlaceBottomSheet(place: SearchPlace)
        fun onOpenCategoriesBottomSheet(category: Category)
        fun onBackToMainBottomSheet()
    }

    private enum class Screen {
        CATEGORIES,
        PLACE
    }

    @Parcelize
    private data class Transaction(val screen: Screen, val arg: Parcelable?) : Parcelable

    private companion object {

        const val KEY_STATE_EXTERNAL_BACK_STACK = "SearchViewBottomSheetsMediator.state.external.back_stack"

        fun SearchCategoriesBottomSheetView.hideCardAndCancelLoading() {
            hide()
            cancelCategoryLoading()
        }
    }
}
