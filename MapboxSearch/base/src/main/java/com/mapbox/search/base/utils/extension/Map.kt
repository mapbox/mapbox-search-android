package com.mapbox.search.base.utils.extension

@JvmSynthetic
fun <K, V> MutableMap<K, MutableSet<V>>.addValue(
    key: K,
    value: V,
    setCreator: () -> MutableSet<V> = { mutableSetOf() },
) {
    var list = get(key)
    if (list == null) {
        list = setCreator()
        put(key, list)
    }
    list.add(value)
}
