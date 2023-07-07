package com.mapbox.search.offline.tests_support

import com.mapbox.bindgen.Expected
import com.mapbox.common.TileRegion
import com.mapbox.common.TileRegionCallback
import com.mapbox.common.TileRegionError
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileRegionsCallback
import com.mapbox.common.TileStore
import com.mapbox.search.common.tests.BaseBlockingCallback

internal fun TileStore.loadTileRegionBlocking(
    id: String,
    options: TileRegionLoadOptions
): Expected<TileRegionError, TileRegion> {
    val callback = BlockingTileRegionCallback()
    loadTileRegion(id, options, callback)
    return callback.getResultBlocking()
}

internal fun TileStore.removeTileRegionBlocking(id: String): Expected<TileRegionError, TileRegion> {
    val callback = BlockingTileRegionCallback()
    removeTileRegion(id, callback)
    return callback.getResultBlocking()
}

internal fun TileStore.getAllTileRegionsBlocking(): Expected<TileRegionError, MutableList<TileRegion>> {
    val callback = BlockingTileRegionsCallback()
    getAllTileRegions(callback)
    return callback.getResultBlocking()
}

internal class BlockingTileRegionsCallback :
    BaseBlockingCallback<Expected<TileRegionError, MutableList<TileRegion>>>(),
    TileRegionsCallback {
    override fun run(regions: Expected<TileRegionError, MutableList<TileRegion>>) {
        publishResult(regions)
    }
}

internal class BlockingTileRegionCallback :
    BaseBlockingCallback<Expected<TileRegionError, TileRegion>>(),
    TileRegionCallback {
    override fun run(region: Expected<TileRegionError, TileRegion>) {
        publishResult(region)
    }
}
