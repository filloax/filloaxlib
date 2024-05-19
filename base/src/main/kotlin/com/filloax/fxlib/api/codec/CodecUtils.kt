package com.filloax.fxlib.api.codec

import com.filloax.fxlib.FxLib
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.JsonOps
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor


fun <A> Codec<A>.decodeJson(jsonElement: JsonElement): DataResult<com.mojang.datafixers.util.Pair<A, JsonElement>> {
    return decode(JsonOps.INSTANCE, jsonElement)
}

private val gson = Gson()

fun <A> Codec<A>.decodeJson(jsonString: String): DataResult<com.mojang.datafixers.util.Pair<A, JsonElement>> {
    return decode(JsonOps.INSTANCE, gson.fromJson(jsonString, JsonElement::class.java))
}

fun <A> Codec<A>.encodeJson(value: A): DataResult<JsonElement> {
    return encodeStart(JsonOps.INSTANCE, value)
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

fun <A> Codec<A>.decodeRegistryJson(jsonElement: JsonElement, registryAccess: RegistryAccess): DataResult<com.mojang.datafixers.util.Pair<A, JsonElement>> {
    val registryOps: RegistryOps<JsonElement> = RegistryOps.create(JsonOps.INSTANCE, registryAccess)
    return decode(registryOps, jsonElement)
}

fun <A> Codec<A>.decodeNbt(tag: Tag): DataResult<com.mojang.datafixers.util.Pair<A, Tag>> {
    return decode(NbtOps.INSTANCE, tag)
}

fun <A> Codec<A>.encodeNbt(value: A): DataResult<Tag> {
    return encodeStart(NbtOps.INSTANCE, value)
}

fun <A> Codec<A>.decodeNbtNullable(tag: Tag): A? {
    return CodecCrossVer.inst.optionalFromDataResult(this.decodeNbt(tag)).map { it.first }.getOrNull()
}

fun <A> Codec<A>.decodeJsonNullable(jsonElement: JsonElement): A? {
    return CodecCrossVer.inst.optionalFromDataResult(this.decodeJson(jsonElement)).map { it.first }.getOrNull()
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

fun <K, V> Codec<K>.mapWithValueOf(valueCodec: Codec<V>) = mutableMapCodec(this, valueCodec)
fun <K, V> Codec<V>.mapWithKeyOf(keyCodec: Codec<K>) = mutableMapCodec(keyCodec, this)
fun <V> Codec<V>.mutableListOf() = mutableListCodec(this)
fun <V> Codec<V>.mutableSetOf() = mutableSetCodec(this)

fun <T: Any, O> MapCodec<Optional<T>>.forNullableGetter(getter: (O) -> T?): RecordCodecBuilder<O, Optional<T>> {
//    return this.xmap<T?>({ opt -> opt.getOrNull() }, { Optional.ofNullable(it) }).forGetter(getter)
    return forGetter{ Optional.ofNullable(getter(it)) }
}

private fun <T: Any> constructorWithOptionalsProxy(kClass: KClass<T>): CodecUtils.ConstructorProxy<T> {
    return CodecUtils.constructorWithOptionals(kClass.primaryConstructor?.javaConstructor
        ?: throw java.lang.IllegalArgumentException("No primary constructor in class! $kClass")
    )
}

/** Easier way to call the JVM one, uses the primary constructor */
fun <T: Any> KClass<T>.constructorWithOptionals() =
    constructorWithOptionalsProxy(this)

fun simpleCodecErr(name: String): (String) -> Unit = {
    FxLib.logger.error("Error in codec $name: $it")
}

fun simpleCodecWarn(name: String): (String) -> Unit = {
    FxLib.logger.warn("Error in codec $name: $it")
}

// For 7.x+

fun throwableCodecErr(name: String) = { err: String -> Exception("Error in codec $name: $err") }
