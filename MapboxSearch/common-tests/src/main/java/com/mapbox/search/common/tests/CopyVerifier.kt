package com.mapbox.search.common.tests

import org.junit.Assert
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaField

@ExperimentalStdlibApi
@Suppress("SpreadOperator")
class CopyVerifier(
    private val clazz: KClass<*>,
    private val objectsFactory: ObjectsFactory = ReflectionObjectsFactory(),
) {

    private val classProperties = clazz.members
        .filter { it is KProperty && it.javaField != null }

    private val copyFun = clazz.functions
        .filter { it.name == "copy" }
        .maxByOrNull { it.parameters.size }!!

    private val instanceParam = copyFun.parameters.first()
    private val copyFunParameters = copyFun.parameters
        .filter { it.name != null }

    fun verify() {
        Assert.assertEquals(
            "All declared class properties and copy() function parameters should be equal!",
            classProperties.map { it.name }.toSortedSet(),
            copyFunParameters.map { it.name!! }.toSortedSet()
        )

        objectsFactory.mode = ObjectsFactory.ObjectMode.RED
        val copyFunParamInstances = copyFunParameters.map { param ->
            objectsFactory.createFromType(param.type)
        }
        val redInstance = objectsFactory.createWithArgs(clazz, *copyFunParamInstances.toTypedArray())

        objectsFactory.mode = ObjectsFactory.ObjectMode.BLUE
        val blueInstance = objectsFactory.create(clazz)
        val blueInstanceFromCopy = copyFun.callBy(mapOf(instanceParam to blueInstance))
        val redInstanceFromCopy = copyFun.call(blueInstance, *copyFunParamInstances.toTypedArray())

        Assert.assertTrue(
            "copy() function should not return same reference!",
            blueInstance !== blueInstanceFromCopy
        )
        Assert.assertEquals(
            "copy() function should return same instance!",
            blueInstance,
            blueInstanceFromCopy
        )
        Assert.assertEquals(
            "copy() function changes not all declared class properties!",
            redInstance,
            redInstanceFromCopy
        )

        objectsFactory.mode = ObjectsFactory.ObjectMode.BLUE
        copyFunParameters.forEachIndexed { index, param ->
            val newParams = copyFunParamInstances.toMutableList()
            val newParamsObject = objectsFactory.createFromType(param.type)
            newParams[index] = newParamsObject

            val newRedInstance = objectsFactory.createWithArgs(clazz, *newParams.toTypedArray())
            val redInstanceCopy = copyFun.callBy(mapOf(instanceParam to redInstance, param to newParamsObject))

            Assert.assertEquals(
                "copy() function don't work for \"${param.name}\" parameter.",
                newRedInstance,
                redInstanceCopy
            )
        }
    }
}
