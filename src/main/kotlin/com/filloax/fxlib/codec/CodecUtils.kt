package com.filloax.fxlib.codec

import com.filloax.fxlib.FXLib
import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.JsonOps
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.MappingResolver
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import java.util.*
import kotlin.Pair
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.jvmErasure
import com.mojang.datafixers.util.Pair as MojPair


fun <A> Codec<A>.decodeJson(jsonElement: JsonElement): DataResult<MojPair<A, JsonElement>> {
    return decode(JsonOps.INSTANCE, jsonElement)
}

fun <A> Codec<A>.decodeJsonNullable(jsonElement: JsonElement): A? {
    return this.decodeJson(jsonElement).get().map({ it.first }, { null })
}


fun <A> Codec<A>.decodeRegistryJson(jsonElement: JsonElement, registryAccess: RegistryAccess): DataResult<com.mojang.datafixers.util.Pair<A, JsonElement>> {
    val registryOps: RegistryOps<JsonElement> = RegistryOps.create(JsonOps.INSTANCE, registryAccess)
    return decode(registryOps, jsonElement)
}

fun <A> Codec<A>.decodeNbt(tag: Tag): DataResult<MojPair<A, Tag>> {
    return decode(NbtOps.INSTANCE, tag)
}

fun <A> Codec<A>.decodeNbtNullable(tag: Tag): A? {
    return this.decodeNbt(tag).get().map({ it.first }, { null })
}

fun <A> Codec<A>.encodeJson(value: A): DataResult<JsonElement> {
    return encodeStart(JsonOps.INSTANCE, value)
}

fun <A> Codec<A>.encodeNbt(value: A): DataResult<Tag> {
    return encodeStart(NbtOps.INSTANCE, value)
}

fun <A, B> kPairCodec(first: Codec<A>, second: Codec<B>): Codec<Pair<A, B>> {
    return Codec.pair(
        first.fieldOf("first").codec(),
        second.fieldOf("second").codec(),
    ).xmap(
        { Pair(it.first, it.second) },
        { com.mojang.datafixers.util.Pair(it.first, it.second) },
    )
}

/**
 * Create a mutable map codec. Note that unbounded map codecs support only string values,
 * or codecs that decode to strings (like UUIDUtil.STRING_CODEC). This cannot be checked at runtime
 * (that I am aware of) so just avoid doing that.
 */
fun <K, V> mutableMapCodec(keyCodec: Codec<K>, valueCodec: Codec<V>): Codec<MutableMap<K, V>> {
    return Codec.unboundedMap(keyCodec, valueCodec).xmap({
        it.toMutableMap()
    }, { it })
}

fun <V> mutableListCodec(elementCodec: Codec<V>): Codec<MutableList<V>> {
    return Codec.list(elementCodec).xmap({
        it.toMutableList()
    }, { it })
}

// Kotlin version of CodecUtils.setOf
fun <V> mutableSetCodec(elementCodec: Codec<V>): Codec<MutableSet<V>> {
    return CodecUtils.setOf(elementCodec) // Already mutable
}

fun <T: Any, O> MapCodec<Optional<T>>.forNullableGetter(getter: (O) -> T?): RecordCodecBuilder<O, Optional<T>> {
//    return this.xmap<T?>({ opt -> opt.getOrNull() }, { Optional.ofNullable(it) }).forGetter(getter)
    return forGetter{ Optional.ofNullable(getter(it)) }
}


/** Easier way to call the JVM one, uses the primary constructor */
fun <T: Any> constructorWithOptionals(kClass: KClass<T>): CodecUtils.ConstructorProxy<T> {
    return CodecUtils.constructorWithOptionals(kClass.primaryConstructor?.javaConstructor
        ?: throw java.lang.IllegalArgumentException("No primary constructor in class! $kClass")
    )
}

/**
 * Convert a constructor with nullable arguments to a constructor with the nullable arguments
 * as an optional type, to be used with forNullableGetter above.
 * IMPORTANT: this being a vararg: Any foregoes any sort of type checking at compilation time,
 * so ONLY use in the context above to avoid mistakes
 */
@Deprecated("Issues with mappings of vanilla classes, kotlin reflect doesn't auto handle them")
fun <T: Any> constructorWithOptionals(constructor: KCallable<T> /*kClass: KClass<T>*/): CodecUtils.ConstructorProxy<T> {
    /* Debug stuff for when runtime was bugged
    FXLib.LOGGER.info("MAKING CONSTRUCTOR WITH OPTIONALS")
    FXLib.LOGGER.info("CLASS AVAILABLE METHODS:")
    FXLib.LOGGER.info("\tDECLARED FUNCTIONS:")
    FXLib.LOGGER.info("\t${kClass.declaredFunctions}")
    FXLib.LOGGER.info("\tINSTANCE FUNCTIONS:")
    FXLib.LOGGER.info("\t${kClass.memberFunctions.filter {it.javaClass != kClass.java}}")
    FXLib.LOGGER.info("\tCOMPANION OBJECT FUNCTIONS:")
    FXLib.LOGGER.info("\t${kClass.memberFunctions.filter {it.javaClass == kClass.java}}")
    FXLib.LOGGER.info("\tPRIMARY CONSTRUCTOR:")
    FXLib.LOGGER.info("\t${kClass.primaryConstructor}")
    FXLib.LOGGER.info("\tALL CONSTRUCTORS:")
    FXLib.LOGGER.info("\t${kClass.constructors}")
    FXLib.LOGGER.info("CONS:")
    FXLib.LOGGER.info("$constructor")
    */
    val requiredParameters = constructor.parameters.filter { !it.isOptional }
    val resolver: MappingResolver = FabricLoader.getInstance().mappingResolver

    return CodecUtils.ConstructorProxy<T> { args ->
        if (args.size < requiredParameters.size || args.size > constructor.parameters.size) {
            throw IllegalArgumentException("Incorrect number of arguments: is ${args.size}, should be ${requiredParameters.size} <= x <= ${constructor.parameters.size}")
        }

        val processedArgs = args.mapIndexed { i, arg ->
            if (arg is Optional<*>) {
                arg.getOrNull()
            } else {
                val param = constructor.parameters[i]
                val expectedType = param.type
                if (!arg::class.isSubclassOf(expectedType.jvmErasure)) {
                    throw IllegalArgumentException("Type for arg $i \"${param.name}\" is wrong: is ${arg::class}, should be $expectedType")
                }
                arg
            }
        }.toTypedArray()
        val argsMap = processedArgs.mapIndexed { i, arg ->
            constructor.parameters[i] to arg
        }.associate { it }

        constructor.callBy(argsMap)
    }
}

fun simpleCodecErr(name: String): (String) -> Unit = {
    FXLib.logger.error("Error in codec $name: $it")
}

fun simpleCodecWarn(name: String): (String) -> Unit = {
    FXLib.logger.warn("Error in codec $name: $it")
}