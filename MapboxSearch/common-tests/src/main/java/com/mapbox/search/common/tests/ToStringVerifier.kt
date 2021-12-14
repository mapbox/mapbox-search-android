package com.mapbox.search.common.tests

import org.junit.Assert
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

@ExperimentalStdlibApi
class ToStringVerifier(
    private val clazz: KClass<*>,
    private val objectsFactory: ObjectsFactory = ReflectionObjectsFactory(),
    private val ignoredProperties: List<String> = emptyList(),
    private val includeAllProperties: Boolean = true
) {

    fun verify() {
        val implInstance = objectsFactory.create(clazz)
        val string = implInstance.toString()
        val targetProperties = clazz.members
            .filter { it is KProperty && (includeAllProperties || it.javaField != null) }
            .filter { !ignoredProperties.contains(it.name) }

        targetProperties.forEach {
            /*
                FIXME
                This check doesn't work when an object contains nested object with the property of the same name, for example

                data class NestedClass(val address: String)

                class MyClass(val address: String, val nestedClass: NestedClass) {
                    override fun toString(): String {
                        // Does't include `address` property
                        return "MyClass(nestedClass='$nestedClass')"
                    }
                }
             */
            Assert.assertTrue(
                "Property ${it.name} is not included in toString() method.",
                string.contains(it.name)
            )
        }
    }
}
