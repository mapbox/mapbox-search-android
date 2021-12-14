package com.mapbox.search.common.tests

import kotlin.reflect.KClass

interface CustomTypeObjectCreator {
    fun supports(clazz: KClass<*>): Boolean
    fun create(mode: ObjectsFactory.ObjectMode): Any?
}

class CustomTypeObjectCreatorImpl(
    private val clazz: KClass<*>,
    private val factory: (ObjectsFactory.ObjectMode) -> Any?
) : CustomTypeObjectCreator {

    override fun supports(clazz: KClass<*>) = clazz == this.clazz

    override fun create(mode: ObjectsFactory.ObjectMode): Any? = factory(mode)
}
