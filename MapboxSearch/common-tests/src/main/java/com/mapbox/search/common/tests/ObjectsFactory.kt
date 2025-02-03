package com.mapbox.search.common.tests

import com.mapbox.search.common.tests.ObjectsFactory.ObjectMode
import java.lang.Exception
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.isAccessible

interface ObjectsFactory {
    var mode: ObjectMode

    fun <T : Any> create(clazz: KClass<T>): Any?
    fun createFromType(type: KType): Any?
    fun <T : Any> createWithArgs(clazz: KClass<T>, vararg args: Any?): Any?

    /**
     * We want to be able to create two different instances of any class, which are
     * guaranteed not to be equal to each other.
     *
     * To accomplish it we introduce two [ObjectMode] modes, in which [ObjectsFactory] can operate.
     * Objects, constructed in [ObjectMode.RED] mode, are guaranteed not to be equal to objects,
     * constructed in [ObjectMode.BLUE] mode. Also each inner/nested [ObjectMode.RED] object property
     * differs from corresponding [ObjectMode.BLUE] object property.
     *
     * This approach was inspired by EqualsVerifier library, in particular by
     * [nl.jqno.equalsverifier.internal.reflection.ClassAccessor] class (take a look
     * at getRedObject() and getBlueObject() methods).
     */
    enum class ObjectMode {
        RED, BLUE
    }
}

@Suppress("TooGenericExceptionThrown")
class ReflectionObjectsFactory(
    private val extraCreators: List<CustomTypeObjectCreator> = CommonSdkTypeObjectCreators.ALL_CREATORS,
    private val subclassProvider: (KClass<*>) -> KClass<*>? = { null },
) : ObjectsFactory {

    private val defaultCollectionSize
        get() = when (mode) {
            ObjectMode.RED -> 2
            ObjectMode.BLUE -> 3
        }

    override var mode: ObjectMode = ObjectMode.RED

    override fun <T : Any> create(clazz: KClass<T>): Any? {
        return create(clazz, clazz.starProjectedType)
    }

    override fun createFromType(type: KType): Any? {
        return when (val classifier = type.classifier) {
            is KClass<*> -> create(classifier, type)
            is KTypeParameter -> create(type.classifier as KClass<*>, type)
            else -> throw RuntimeException("Classifier \"${classifier}\" is not supported.")
        }
    }

    override fun <T : Any> createWithArgs(clazz: KClass<T>, vararg args: Any?): Any? {
        return createViaReflection(clazz, args.toList().toTypedArray())
    }

    private fun create(clazz: KClass<*>, type: KType): Any? {
        return createSimpleTypes(clazz, type)
            ?: extraCreators.firstOrNull { it.supports(clazz) }?.create(mode)
            ?: subclassProvider(clazz)?.let { create(it) }
            ?: createSpecialCases(clazz, type)
            ?: createViaReflection(clazz)
    }

    private fun <T : Any> createSimpleTypes(clazz: KClass<T>, type: KType): Any? = when (clazz) {
        Boolean::class -> listOf(true, false)[mode]
        Byte::class -> listOf<Byte>(5, 10)[mode]
        Int::class -> listOf(5, 10)[mode]
        Long::class -> listOf(5L, 10L)[mode]
        Double::class -> listOf(5.0, 10.0)[mode]
        Float::class -> listOf(5.0f, 10.0f)[mode]
        String::class -> listOf("test-string", "test-string-2")[mode]
        List::class -> {
            val listObjectType = type.arguments.first().type!!
            List(defaultCollectionSize) { createFromType(listObjectType) }
        }
        Map::class -> {
            val keyType = type.arguments[0].type!!
            val valueType = type.arguments[1].type!!
            List(defaultCollectionSize) {
                createFromType(keyType) to createFromType(valueType)
            }.toMap()
        }
        else -> null
    }

    private fun <T : Any> createSpecialCases(clazz: KClass<T>, type: KType): Any? = when {
        isEnum(clazz) -> {
            clazz.java.enumConstants!![mode]
        }
        isArray(clazz) -> {
            val arrayObjectType = type.arguments.first().type!!
            Array(defaultCollectionSize) { createFromType(arrayObjectType) }
        }
        clazz.isSealed -> {
            val sealedSubclass = clazz.sealedSubclasses[mode]
            create(sealedSubclass, sealedSubclass.starProjectedType)
        }
        else -> clazz.objectInstance
    }

    @Suppress("TooGenericExceptionCaught")
    private fun createViaReflection(clazz: KClass<*>, args: Array<Any?>? = null): Any {
        val constructor = clazz.constructors
            .filter { it.parameters.none(KParameter::isVararg) }
            .maxByOrNull { it.parameters.size }
            ?: throw RuntimeException("No appropriate constructor was found for ${clazz.simpleName}.")

        constructor.isAccessible = true
        val arguments = args ?: constructor.parameters
            .map { constructorParam -> createFromType(constructorParam.type) }
            .toTypedArray()

        return try {
            constructor.call(*arguments)
        } catch (e: Exception) {
            throw RuntimeException("Couldn't create ${clazz.java.simpleName} object!", e)
        }
    }
}

fun isEnum(type: KClass<out Any>): Boolean = type.isSubclassOf(Enum::class)

fun isArray(type: KClass<out Any>): Boolean = type.java.isArray

private operator fun <T> List<T>.get(mode: ObjectMode): T {
    return get(mode.ordinal.coerceAtMost(size - 1))
}

private operator fun <T> Array<T>.get(mode: ObjectMode): T {
    return get(mode.ordinal.coerceAtMost(size - 1))
}
