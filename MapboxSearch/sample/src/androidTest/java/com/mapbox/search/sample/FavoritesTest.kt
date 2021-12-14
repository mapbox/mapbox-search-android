package com.mapbox.search.sample

import com.mapbox.search.sample.Constants.Assets.CLEMENS_MOUNT_CLEMENS_RESULT_ASSET
import com.mapbox.search.sample.Constants.Assets.CLEMENS_SUGGESTIONS_ASSET
import com.mapbox.search.sample.Constants.Assets.RANELAGH_ROYAL_SPA_RESULT_ASSET
import com.mapbox.search.sample.Constants.Assets.RANELAGH_SUGGESTIONS_ASSET
import com.mapbox.search.sample.extensions.enqueue
import com.mapbox.search.sample.extensions.noInternetConnectionError
import com.mapbox.search.sample.robots.addressSearchView
import com.mapbox.search.sample.robots.editFavoriteView
import com.mapbox.search.sample.robots.favoriteActionsDialog
import com.mapbox.search.sample.robots.searchBottomSheet
import com.mapbox.search.sample.robots.searchPlaceBottomSheet
import com.mapbox.search.sample.tools.disableItemAnimatorOnViewPagerNestedRecyclers
import org.junit.Test

@Suppress("LargeClass")
class FavoritesTest : MockServerSearchActivityTest() {

    override fun beforeEachTest() {
        super.beforeEachTest()
        disableItemAnimatorOnViewPagerNestedRecyclers()
    }

    /**
     * Test 1.
     * Add favorite using "Add to favorites" button.
     */
    @Test
    fun testFavoriteAdd() {
        mockServer.enqueueSuccessfulResponses(
            RANELAGH_SUGGESTIONS_ASSET,
            RANELAGH_ROYAL_SPA_RESULT_ASSET
        )

        searchBottomSheet {
            addFavoriteFromFavoritesListAddButton(
                searchResultName = RANELAGH_TERRACE_62_NAME,
                searchAddressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART,
                favoriteName = FAVORITE_TEST_NAME
            )
        }
    }

    /**
     * Test 2.
     * Add favorite using "Add to favorites" button and delete this favorite.
     */
    @Test
    fun testFavoriteAddAndDelete() {
        mockServer.enqueueSuccessfulResponses(
            RANELAGH_SUGGESTIONS_ASSET,
            RANELAGH_ROYAL_SPA_RESULT_ASSET
        )

        searchBottomSheet {
            addFavoriteFromFavoritesListAddButton(
                searchResultName = RANELAGH_TERRACE_62_NAME,
                searchAddressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART
            )
            showFavoriteOptions(RANELAGH_TERRACE_62_NAME)
        }
        favoriteActionsDialog {
            delete()
        }
        searchBottomSheet {
            /**
             * FIXME: Verify there is no previously added item.
             */
            verifyFavoriteItemsCount(DEFAULT_FAVORITES_ITEMS_COUNT)
        }
    }

    /**
     * Test 3.
     * Add favorite using "Add to favorites" button and rename this favorite.
     */
    @Test
    fun testFavoriteAddAndRename() {
        mockServer.enqueueSuccessfulResponses(
            RANELAGH_SUGGESTIONS_ASSET,
            RANELAGH_ROYAL_SPA_RESULT_ASSET
        )

        searchBottomSheet {
            addFavoriteFromFavoritesListAddButton(
                searchResultName = RANELAGH_TERRACE_62_NAME,
                searchAddressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART
            )
            showFavoriteOptions(RANELAGH_TERRACE_62_NAME)
        }
        favoriteActionsDialog {
            rename()
        }
        editFavoriteView {
            clearNameInput()
            typeFavoriteName(FAVORITE_RENAMED_NAME)
            done()
        }
        searchBottomSheet {
            verifyFavoriteItemsCount(DEFAULT_FAVORITES_ITEMS_COUNT + 1)
            verifyFavoriteItem(
                favoriteName = FAVORITE_RENAMED_NAME,
                addressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART,
                itemPosition = DEFAULT_FAVORITES_ITEMS_COUNT - 1
            )
        }
    }

    /**
     * Test 4.
     * Add favorite using "Add to favorites" button, rename this favorite and cancel renaming.
     */
    @Test
    fun testFavoriteAddRenameAndCancel() {
        mockServer.enqueueSuccessfulResponses(
            RANELAGH_SUGGESTIONS_ASSET,
            RANELAGH_ROYAL_SPA_RESULT_ASSET
        )

        searchBottomSheet {
            addFavoriteFromFavoritesListAddButton(
                searchResultName = RANELAGH_TERRACE_62_NAME,
                searchAddressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART
            )
            showFavoriteOptions(RANELAGH_TERRACE_62_NAME)
        }
        favoriteActionsDialog {
            rename()
        }
        editFavoriteView {
            clearNameInput()
            typeFavoriteName(FAVORITE_RENAMED_NAME)
            close()
        }
        searchBottomSheet {
            verifyFavoriteItemsCount(DEFAULT_FAVORITES_ITEMS_COUNT + 1)
            verifyFavoriteItem(
                favoriteName = RANELAGH_TERRACE_62_NAME,
                addressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART,
                itemPosition = DEFAULT_FAVORITES_ITEMS_COUNT - 1
            )
        }
    }

    /**
     * Test 5.
     * Add favorite using "Add to favorites" button and edit location of this favorite.
     */
    @Test
    fun testFavoriteAddAndEditLocation() {
        mockServer.enqueueSuccessfulResponses(
            RANELAGH_SUGGESTIONS_ASSET,
            RANELAGH_ROYAL_SPA_RESULT_ASSET,
            CLEMENS_SUGGESTIONS_ASSET,
            CLEMENS_MOUNT_CLEMENS_RESULT_ASSET
        )

        searchBottomSheet {
            addFavoriteFromFavoritesListAddButton(
                searchResultName = RANELAGH_TERRACE_62_NAME,
                searchAddressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART
            )
            showFavoriteOptions(RANELAGH_TERRACE_62_NAME)
        }
        favoriteActionsDialog {
            editLocation()
        }
        addressSearchView {
            typeQuery(CLEMENS_STREET_43_NAME)
            selectSearchResult(
                resultName = CLEMENS_STREET_43_NAME,
                addressSubstring = CLEMENS_STREET_43_ADDRESS_PART
            )
        }
        searchBottomSheet {
            verifyFavoriteItemsCount(DEFAULT_FAVORITES_ITEMS_COUNT + 1)
            verifyFavoriteItem(
                favoriteName = RANELAGH_TERRACE_62_NAME,
                addressSubstring = CLEMENS_STREET_43_ADDRESS_PART,
                itemPosition = DEFAULT_FAVORITES_ITEMS_COUNT - 1
            )
        }
    }

    /**
     * Test 6.
     * Add place to predefined Home favorite. Edit location for predefined favorite.
     */
    @Test
    fun testSpecifyHomeFavoriteAndEditLocation() {
        mockServer.enqueueSuccessfulResponses(
            RANELAGH_SUGGESTIONS_ASSET,
            RANELAGH_ROYAL_SPA_RESULT_ASSET,
            CLEMENS_SUGGESTIONS_ASSET,
            CLEMENS_MOUNT_CLEMENS_RESULT_ASSET
        )

        searchBottomSheet {
            addFavoriteFromPredefinedOption(
                predefinedOptionName = FAVORITE_HOME_NAME,
                predefinedOptionPosition = FAVORITE_HOME_ITEM_POSITION,
                searchResultName = RANELAGH_TERRACE_62_NAME,
                searchAddressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART
            )
            showFavoriteOptions(FAVORITE_HOME_NAME)
        }
        favoriteActionsDialog {
            editLocation()
        }
        addressSearchView {
            typeQuery(CLEMENS_STREET_43_NAME)
            selectSearchResult(
                resultName = CLEMENS_STREET_43_NAME,
                addressSubstring = CLEMENS_STREET_43_ADDRESS_PART
            )
        }
        searchBottomSheet {
            verifyFavoriteItemsCount(DEFAULT_FAVORITES_ITEMS_COUNT)
            verifyFavoriteItem(
                favoriteName = FAVORITE_HOME_NAME,
                addressSubstring = CLEMENS_STREET_43_ADDRESS_PART,
                itemPosition = FAVORITE_HOME_ITEM_POSITION
            )
        }
    }

    /**
     * Test 7.
     * Add place to predefined Work favorite. Edit location for predefined favorite.
     */
    @Test
    fun testSpecifyWorkFavoriteAndEditLocation() {
        mockServer.enqueueSuccessfulResponses(
            RANELAGH_SUGGESTIONS_ASSET,
            RANELAGH_ROYAL_SPA_RESULT_ASSET,
            CLEMENS_SUGGESTIONS_ASSET,
            CLEMENS_MOUNT_CLEMENS_RESULT_ASSET
        )

        searchBottomSheet {
            addFavoriteFromPredefinedOption(
                predefinedOptionName = FAVORITE_WORK_NAME,
                predefinedOptionPosition = FAVORITE_WORK_ITEM_POSITION,
                searchResultName = RANELAGH_TERRACE_62_NAME,
                searchAddressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART
            )
            showFavoriteOptions(FAVORITE_WORK_NAME)
        }
        favoriteActionsDialog {
            editLocation()
        }
        addressSearchView {
            typeQuery(CLEMENS_STREET_43_NAME)
            selectSearchResult(
                resultName = CLEMENS_STREET_43_NAME,
                addressSubstring = CLEMENS_STREET_43_ADDRESS_PART
            )
        }
        searchBottomSheet {
            verifyFavoriteItemsCount(DEFAULT_FAVORITES_ITEMS_COUNT)
            verifyFavoriteItem(
                favoriteName = FAVORITE_WORK_NAME,
                addressSubstring = CLEMENS_STREET_43_ADDRESS_PART,
                itemPosition = FAVORITE_WORK_ITEM_POSITION
            )
        }
    }

    /**
     * Test 8.
     * Add favorite from search result card and delete this favorite.
     */
    @Test
    fun testFavoriteAddFromSearchResultCardAndDelete() {
        mockServer.enqueueSuccessfulResponses(
            RANELAGH_SUGGESTIONS_ASSET,
            RANELAGH_ROYAL_SPA_RESULT_ASSET
        )

        searchBottomSheet {
            addFavoriteFromSearchCard(
                searchResultName = RANELAGH_TERRACE_62_NAME,
                searchAddressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART
            )
            showFavoriteOptions(RANELAGH_TERRACE_62_NAME)
        }
        favoriteActionsDialog {
            delete()
        }
        searchBottomSheet {
            /**
             * FIXME: Verify there is no previously added item.
             */
            verifyFavoriteItemsCount(DEFAULT_FAVORITES_ITEMS_COUNT)
        }
    }

    /**
     * Test 9.
     * Add favorite from search result card and rename this favorite.
     */
    @Test
    fun testFavoriteAddFromSearchResultCardAndRename() {
        mockServer.enqueueSuccessfulResponses(
            RANELAGH_SUGGESTIONS_ASSET,
            RANELAGH_ROYAL_SPA_RESULT_ASSET
        )

        searchBottomSheet {
            addFavoriteFromSearchCard(
                searchResultName = RANELAGH_TERRACE_62_NAME,
                searchAddressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART
            )
            showFavoriteOptions(RANELAGH_TERRACE_62_NAME)
        }
        favoriteActionsDialog {
            rename()
        }
        editFavoriteView {
            clearNameInput()
            typeFavoriteName(FAVORITE_RENAMED_NAME)
            done()
        }
        searchBottomSheet {
            verifyFavoriteItemsCount(DEFAULT_FAVORITES_ITEMS_COUNT + 1)
            verifyFavoriteItem(
                favoriteName = FAVORITE_RENAMED_NAME,
                addressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART,
                itemPosition = DEFAULT_FAVORITES_ITEMS_COUNT - 1
            )
        }
    }

    /**
     * Test 10.
     * Add favorite using "Add to favorites" button without internet connection, then with internet connection.
     */
    @Test
    fun testFavoriteAddWithNoInternetConnectionAndRetry() {
        mockServer.enqueue(
            createNoNetworkConnectionResponse(),
            createSuccessfulResponse(RANELAGH_SUGGESTIONS_ASSET),
            createSuccessfulResponse(RANELAGH_ROYAL_SPA_RESULT_ASSET)
        )

        searchBottomSheet {
            addFavoriteFromFavoritesListAddButton(
                searchResultName = RANELAGH_TERRACE_62_NAME,
                searchAddressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART,
                favoriteName = FAVORITE_TEST_NAME,
                retryOnErrorFirstTime = true
            )
        }
    }

    /**
     * Test 11.
     * Add favorite using "Add to favorites" button and edit location without internet connection,
     * then with internet connection.
     */
    @Test
    fun testFavoriteAddAndEditLocationWithNoInternetAndRetry() {
        mockServer.enqueue(
            createSuccessfulResponse(RANELAGH_SUGGESTIONS_ASSET),
            createSuccessfulResponse(RANELAGH_ROYAL_SPA_RESULT_ASSET),
            createNoNetworkConnectionResponse(),
            createSuccessfulResponse(CLEMENS_SUGGESTIONS_ASSET),
            createSuccessfulResponse(CLEMENS_MOUNT_CLEMENS_RESULT_ASSET)
        )

        searchBottomSheet {
            addFavoriteFromFavoritesListAddButton(
                searchResultName = RANELAGH_TERRACE_62_NAME,
                searchAddressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART
            )
            showFavoriteOptions(RANELAGH_TERRACE_62_NAME)
        }
        favoriteActionsDialog {
            editLocation()
        }
        addressSearchView {
            typeQuery(CLEMENS_STREET_43_NAME)
            awaitResults()
            verifySearchResults {
                noInternetConnectionError()
            }
            retryFromError()
            selectSearchResult(
                resultName = CLEMENS_STREET_43_NAME,
                addressSubstring = CLEMENS_STREET_43_ADDRESS_PART
            )
        }
        searchBottomSheet {
            verifyFavoriteItemsCount(DEFAULT_FAVORITES_ITEMS_COUNT + 1)
            verifyFavoriteItem(
                favoriteName = RANELAGH_TERRACE_62_NAME,
                addressSubstring = CLEMENS_STREET_43_ADDRESS_PART,
                itemPosition = DEFAULT_FAVORITES_ITEMS_COUNT - 1
            )
        }
    }

    /**
     * Test 12.
     * Add place to predefined Home favorite. Remove created Home favorite.
     */
    @Test
    fun testSpecifyHomeFavoriteAndRemove() {
        mockServer.enqueueSuccessfulResponses(
            RANELAGH_SUGGESTIONS_ASSET,
            RANELAGH_ROYAL_SPA_RESULT_ASSET
        )

        searchBottomSheet {
            addFavoriteFromPredefinedOption(
                predefinedOptionName = FAVORITE_HOME_NAME,
                predefinedOptionPosition = FAVORITE_HOME_ITEM_POSITION,
                searchResultName = RANELAGH_TERRACE_62_NAME,
                searchAddressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART
            )
            showFavoriteOptions(FAVORITE_HOME_NAME)
        }
        favoriteActionsDialog {
            removeLocation()
        }
        searchBottomSheet {
            verifyFavoriteItemsCount(DEFAULT_FAVORITES_ITEMS_COUNT)
            verifyFavoriteTemplate(
                templateName = FAVORITE_HOME_NAME,
                itemPosition = FAVORITE_HOME_ITEM_POSITION
            )
        }
    }

    /**
     * Test 13.
     * Add place to predefined Work favorite. Remove created Home favorite.
     */
    @Test
    fun testSpecifyWorkFavoriteAndRemove() {
        mockServer.enqueueSuccessfulResponses(
            RANELAGH_SUGGESTIONS_ASSET,
            RANELAGH_ROYAL_SPA_RESULT_ASSET
        )

        searchBottomSheet {
            addFavoriteFromPredefinedOption(
                predefinedOptionName = FAVORITE_WORK_NAME,
                predefinedOptionPosition = FAVORITE_WORK_ITEM_POSITION,
                searchResultName = RANELAGH_TERRACE_62_NAME,
                searchAddressSubstring = RANELAGH_TERRACE_62_ADRRESS_PART
            )
            showFavoriteOptions(FAVORITE_WORK_NAME)
        }
        favoriteActionsDialog {
            removeLocation()
        }
        searchBottomSheet {
            verifyFavoriteItemsCount(DEFAULT_FAVORITES_ITEMS_COUNT)
            verifyFavoriteTemplate(
                templateName = FAVORITE_WORK_NAME,
                itemPosition = FAVORITE_WORK_ITEM_POSITION
            )
        }
    }

    private fun addFavoriteFromPredefinedOption(
        predefinedOptionName: String,
        predefinedOptionPosition: Int,
        searchResultName: String,
        searchAddressSubstring: String,
        initialFavoritesItemCount: Int = DEFAULT_FAVORITES_ITEMS_COUNT,
        searchQuery: String = searchResultName
    ) {
        searchBottomSheet {
            expand()
            openFavoritesTab()
            verifyFavoriteItemsCount(initialFavoritesItemCount)
            verifyFavoriteTemplate(predefinedOptionName, predefinedOptionPosition)
            selectFavoriteWithName(predefinedOptionName)
        }
        addressSearchView {
            typeQuery(searchQuery)
            selectSearchResult(
                resultName = searchResultName,
                addressSubstring = searchAddressSubstring
            )
        }
        searchBottomSheet {
            verifyFavoriteItemsCount(initialFavoritesItemCount)
            verifyFavoriteItem(
                favoriteName = predefinedOptionName,
                addressSubstring = searchAddressSubstring,
                itemPosition = predefinedOptionPosition
            )
        }
    }

    private fun addFavoriteFromFavoritesListAddButton(
        searchResultName: String,
        searchAddressSubstring: String,
        initialFavoritesItemCount: Int = DEFAULT_FAVORITES_ITEMS_COUNT,
        searchQuery: String = searchResultName,
        favoriteName: String? = null,
        retryOnErrorFirstTime: Boolean = false
    ) {
        searchBottomSheet {
            expand()
            openFavoritesTab()
            verifyFavoriteItemsCount(initialFavoritesItemCount)
            addFavorite()
        }
        addressSearchView {
            typeQuery(searchQuery)

            if (retryOnErrorFirstTime) {
                awaitResults()
                verifySearchResults {
                    noInternetConnectionError()
                }
                retryFromError()
            }

            awaitResults()
            selectSearchResult(
                resultName = searchResultName,
                addressSubstring = searchAddressSubstring
            )
        }
        editFavoriteView {
            if (favoriteName != null) {
                clearNameInput()
                typeFavoriteName(favoriteName)
            }
            done()
        }
        searchBottomSheet {
            verifyFavoriteItemsCount(initialFavoritesItemCount + 1)
            verifyFavoriteItem(
                favoriteName = favoriteName ?: searchResultName,
                addressSubstring = searchAddressSubstring,
                itemPosition = initialFavoritesItemCount - 1
            )
        }
    }

    private fun addFavoriteFromSearchCard(
        searchResultName: String,
        searchAddressSubstring: String,
        initialFavoritesItemCount: Int = DEFAULT_FAVORITES_ITEMS_COUNT
    ) {
        searchBottomSheet {
            expand()
            typeQuery(searchResultName)
            selectSearchResult(
                resultName = searchResultName,
                addressSubstring = searchAddressSubstring
            )
        }
        searchPlaceBottomSheet {
            addToFavorite()
            verifyAddedToFavoritesButtonIsVisible()
            close()
        }
        searchBottomSheet {
            expand()
            openFavoritesTab()
            verifyFavoriteItemsCount(initialFavoritesItemCount + 1)
            verifyFavoriteItem(
                favoriteName = searchResultName,
                addressSubstring = searchAddressSubstring,
                itemPosition = initialFavoritesItemCount - 1
            )
        }
    }

    private companion object {
        const val RANELAGH_TERRACE_62_NAME = "62 Ranelagh Terrace"
        const val RANELAGH_TERRACE_62_ADRRESS_PART = "Royal Leamington Spa"
        const val CLEMENS_STREET_43_NAME = "43 Clemens Street"
        const val CLEMENS_STREET_43_ADDRESS_PART = "Mount Clemens"
        const val FAVORITE_TEST_NAME = "Test name"
        const val FAVORITE_RENAMED_NAME = "Renamed name"
        const val FAVORITE_HOME_NAME = "Home"
        const val FAVORITE_WORK_NAME = "Work"

        const val DEFAULT_FAVORITES_ITEMS_COUNT = 3
        const val FAVORITE_HOME_ITEM_POSITION = 0
        const val FAVORITE_WORK_ITEM_POSITION = 1
    }
}
