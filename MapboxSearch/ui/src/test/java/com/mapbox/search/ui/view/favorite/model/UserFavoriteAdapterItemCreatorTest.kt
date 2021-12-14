package com.mapbox.search.ui.view.favorite.model

import com.mapbox.geojson.Point
import com.mapbox.search.record.FavoriteRecord
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.ui.R
import com.mapbox.search.ui.view.favorite.FavoriteTemplate
import com.mapbox.test.dsl.TestCase
import org.junit.jupiter.api.TestFactory

internal class UserFavoriteAdapterItemCreatorTest {

    @TestFactory
    fun `Test UI records creation`() = TestCase {
        Given("UserFavoriteAdapterItemCreator with mocked dependencies") {
            val itemsCreator = UserFavoriteAdapterItemsCreator()

            When("Merging no user favorites and Home/Work templates") {
                val userFavorites = emptyList<FavoriteRecord>()
                val userTemplates = listOf(HOME_TEMPLATE, WORK_TEMPLATE)
                val result = itemsCreator.createItems(userFavorites, userTemplates)

                Then(
                    "Results contain information from Home and Work templates",
                    listOf(
                        UserFavoriteAdapterItem.Favorite.Template(HOME_TEMPLATE),
                        UserFavoriteAdapterItem.Favorite.Template(WORK_TEMPLATE),
                        UserFavoriteAdapterItem.AddFavorite
                    ),
                    result
                )
            }

            When("Merging saved Home favorite and Home/Work templates") {
                val userFavorites = listOf(CREATED_HOME_FAVORITE_RECORD)
                val userTemplates = listOf(HOME_TEMPLATE, WORK_TEMPLATE)
                val result = itemsCreator.createItems(userFavorites, userTemplates)

                Then(
                    "Results contain information from saved Home favorite and Work template",
                    listOf(
                        UserFavoriteAdapterItem.Favorite.Created(
                            favorite = CREATED_HOME_FAVORITE_RECORD,
                            isCreatedFromTemplate = true,
                            drawableId = R.drawable.mapbox_search_sdk_ic_favorite_home,
                        ),
                        UserFavoriteAdapterItem.Favorite.Template(WORK_TEMPLATE),
                        UserFavoriteAdapterItem.AddFavorite
                    ),
                    result
                )
            }

            When("Merging saved custom favorite and Home/Work templates") {
                val userFavorites = listOf(CUSTOM_FAVORITE_RECORD)
                val userTemplates = listOf(HOME_TEMPLATE, WORK_TEMPLATE)
                val result = itemsCreator.createItems(userFavorites, userTemplates)

                Then(
                    "Results contain information from Home/Work templates and custom favorite",
                    listOf(
                        UserFavoriteAdapterItem.Favorite.Template(HOME_TEMPLATE),
                        UserFavoriteAdapterItem.Favorite.Template(WORK_TEMPLATE),
                        UserFavoriteAdapterItem.Favorite.Created(
                            favorite = CUSTOM_FAVORITE_RECORD,
                            isCreatedFromTemplate = false,
                            drawableId = R.drawable.mapbox_search_sdk_ic_favorite_uncategorized,
                        ),
                        UserFavoriteAdapterItem.AddFavorite
                    ),
                    result
                )
            }
        }
    }

    private companion object {

        const val FAVORITE_HOME_ID = "home_id"
        const val FAVORITE_WORK_ID = "work_id"

        val HOME_TEMPLATE = FavoriteTemplate(
            id = FAVORITE_HOME_ID,
            nameId = R.string.mapbox_search_sdk_favorite_template_home_name,
            resourceId = R.drawable.mapbox_search_sdk_ic_favorite_home
        )

        val WORK_TEMPLATE = FavoriteTemplate(
            id = FAVORITE_WORK_ID,
            nameId = R.string.mapbox_search_sdk_favorite_template_work_name,
            resourceId = R.drawable.mapbox_search_sdk_ic_favorite_work
        )

        val CREATED_HOME_FAVORITE_RECORD = FavoriteRecord(
            id = HOME_TEMPLATE.id,
            name = "Relax station",
            descriptionText = null,
            address = null,
            routablePoints = null,
            categories = emptyList(),
            makiIcon = null,
            coordinate = Point.fromLngLat(1.0, 2.0),
            type = SearchResultType.POI,
            metadata = null,
        )

        val CUSTOM_FAVORITE_RECORD = FavoriteRecord(
            id = "custom_id",
            name = "Restaurant",
            descriptionText = null,
            address = null,
            routablePoints = null,
            categories = emptyList(),
            makiIcon = null,
            coordinate = Point.fromLngLat(2.0, 3.0),
            type = SearchResultType.ADDRESS,
            metadata = null,
        )
    }
}
