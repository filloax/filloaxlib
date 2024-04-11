package com.filloax.fxlib.codec

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.JsonOps
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import com.mojang.datafixers.util.Pair as MojPair


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

fun <A> Codec<A>.encodeNbt(value: A): DataResult<Tag> {
    return encodeStart(NbtOps.INSTANCE, value)
}