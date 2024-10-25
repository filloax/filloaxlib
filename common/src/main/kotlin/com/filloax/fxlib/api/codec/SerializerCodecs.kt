package com.filloax.fxlib.api.codec

import com.filloax.fxlib.api.json.toGson
import com.filloax.fxlib.api.json.toKotlin
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec

private val JSON = Json

/**
 * Use Kotlin serializers to quickly generate codecs for simpler data classes that contain primitives
 * or other types with a serializer.
 * WIP (currently doesn't work with boolean fields when passing through NBT as it gets converted from Byte
 * to Number in json objects
 */
fun <T : Any> KSerializer<T>.codec(
    json: Json = JSON,
): Codec<T> {
    return Codec.of(K2DfuEncoder(this, json), K2DfuDecoder(this, json))
}

fun <T : Any> KSerializer<T>.streamCodec(json: Json = JSON): StreamCodec<RegistryFriendlyByteBuf, T> {
    return StreamCodec.of({ byteBuf, obj ->
        byteBuf.writeUtf(json.encodeToString(this, obj))
    }, { byteBuf ->
        json.decodeFromString(this, byteBuf.readUtf())
    })
}

private class K2DfuDecoder<A>(
    private val serializer: KSerializer<A>,
    private val json: Json = JSON,
) : Decoder<A> {
    @SuppressWarnings("unchecked")
    override fun <T> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<A, T>> {
        val jsonElement = if (input is Tag) convertNbt(input) else
            Dynamic.convert(ops, JsonOps.INSTANCE, input)
        return try {
            val value = json.decodeFromJsonElement(serializer, jsonElement.toKotlin())
            DataResult.success(Pair(value, input))
        } catch (e: SerializationException) {
            DataResult.error { "Failed to decode, couldn't deserialize: ${e.message}" }
        } catch (e: IllegalArgumentException) {
            DataResult.error { "Failed to decode, couldn't represent as type: ${e.message}" }
        }
    }

    // NbtOps save booleans as bytes, but DO NOT
    // read bytes back as booleans, so converting from NbtOps
    // to JsonOps as-is leads to json numbers being found where booleans
    // are expected
    private fun convertNbt(tag: Tag): JsonElement {
        return if (tag is CompoundTag) {
            JsonObject().apply {
                tag.allKeys.forEach { key ->
                    val child = tag.get(key)!!
                    add(key, convertNbt(child))
                }
            }
        } else if (tag is ByteTag) {
            JsonPrimitive(tag.asByte == 1.toByte())
        } else Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, tag)
    }

    override fun toString(): String {
        return "KDFUDecoder [ ${serializer.descriptor} ]"
    }
}

private class K2DfuEncoder<A>(
    val serializer: KSerializer<A>,
    val json: Json = JSON,
) : Encoder<A> {
    override fun <T> encode(input: A, ops: DynamicOps<T>, prefix: T): DataResult<T> {
        val jsonElement = try {
            json.encodeToJsonElement(serializer, input).toGson()
        } catch(e: SerializationException) {
            return DataResult.error { "Failed to encode, couldn't serialize: ${e.message}" }
        }
        val opsValue = Dynamic.convert(JsonOps.INSTANCE, ops, jsonElement)
        return DataResult.success(opsValue)
        // TODO: handle prefix, even if it seems to work even without?
    }

    override fun toString(): String {
        return "KDFUEncoder [ ${serializer.descriptor} ]"
    }
}