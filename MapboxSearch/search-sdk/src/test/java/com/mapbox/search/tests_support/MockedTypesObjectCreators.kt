package com.mapbox.search.tests_support

import android.content.Context
import com.mapbox.common.TileStore
import com.mapbox.common.location.LocationProvider
import com.mapbox.search.ViewportProvider
import com.mapbox.search.common.tests.CustomTypeObjectCreator
import com.mapbox.search.common.tests.CustomTypeObjectCreatorImpl
import io.mockk.mockk

internal object MockedTypesObjectCreators {

    val CONTEXT_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(Context::class) { mode ->
        listOf(
            mockk<Context>(relaxed = true), mockk<Context>(relaxed = true),
        )[mode.ordinal]
    }

    val LOCATION_ENGINE_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(LocationProvider::class) { mode ->
        listOf(
            mockk<LocationProvider>(relaxed = true), mockk<LocationProvider>(relaxed = true),
        )[mode.ordinal]
    }

    val VIEW_PORT_PROVIDER_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(ViewportProvider::class) { mode ->
        listOf(
            mockk<ViewportProvider>(relaxed = true), mockk<ViewportProvider>(relaxed = true),
        )[mode.ordinal]
    }

    val TILE_STORE_OBJECT_CREATOR = CustomTypeObjectCreatorImpl(TileStore::class) { mode ->
        listOf(
            mockk<TileStore>(relaxed = true), mockk<TileStore>(relaxed = true),
        )[mode.ordinal]
    }

    val ALL_CREATORS = listOf<CustomTypeObjectCreator>(
        CONTEXT_OBJECT_CREATOR,
        LOCATION_ENGINE_OBJECT_CREATOR,
        VIEW_PORT_PROVIDER_OBJECT_CREATOR,
        TILE_STORE_OBJECT_CREATOR,
    )
}
