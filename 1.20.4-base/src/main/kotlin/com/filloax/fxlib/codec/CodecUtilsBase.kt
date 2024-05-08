package com.filloax.fxlib.codec

import com.filloax.fxlib.platform.ServiceUtil
import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.JsonOps
import com.mojang.serialization.MapCodec
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import java.util.*
import kotlin.jvm.optionals.getOrNull
import com.mojang.datafixers.util.Pair as MojPair


fun <A> Codec<A>.decodeRegistryJson(jsonElement: JsonElement, registryAccess: RegistryAccess): DataResult<com.mojang.datafixers.util.Pair<A, JsonElement>> {
    val registryOps: RegistryOps<JsonElement> = RegistryOps.create(JsonOps.INSTANCE, registryAccess)
    return decode(registryOps, jsonElement)
}

fun <A> Codec<A>.decodeNbt(tag: Tag): DataResult<MojPair<A, Tag>> {
    return decode(NbtOps.INSTANCE, tag)
}

fun <A> Codec<A>.encodeNbt(value: A): DataResult<Tag> {
    return encodeStart(NbtOps.INSTANCE, value)
}

// Cross ver stuff - things that would use functions changed from DFU 6.x to 7.x (why did they change it ugh)

fun <A> Codec<A>.decodeNbtNullable(tag: Tag): A? {
    return CodecCrossVer.inst.optionalFromDataResult(this.decodeNbt(tag)).map { it.first }.getOrNull()
}

fun <A> Codec<A>.decodeJsonNullable(jsonElement: JsonElement): A? {
    return CodecCrossVer.inst.optionalFromDataResult(this.decodeJson(jsonElement)).map { it.first }.getOrNull()
}

// Cross ver handling

interface CodecCrossVer {
    fun <T> optionalFromDataResult(dataResult: DataResult<T>): Optional<T>
    fun <T> validateCodec(codec: MapCodec<T>, checker: (T) -> DataResult<T>): MapCodec<T>

    companion object {
        val inst = ServiceUtil.findService(CodecCrossVer::class.java)
    }
}