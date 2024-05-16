package com.filloax.fxlib.api.codec

import com.filloax.fxlib.api.json.toGson
import com.filloax.fxlib.api.json.toKotlin
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private val JSON = Json

// Could be made more efficient with custom kotlin serializers/deserializers (as in the kserialization ones), but too complicated for now
fun <T> KSerializer<T>.codec(
    json: Json = JSON,
): Codec<T> {
    return Codec.of(K2DfuEncoder(this, json), K2DfuDecoder(this, json))
}

private class K2DfuDecoder<A>(
    val serializer: KSerializer<A>,
    val json: Json = JSON,
) : Decoder<A> {
    override fun <T> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<A, T>> {
        val jsonElement = Dynamic.convert(ops, JsonOps.INSTANCE, input)
        return try {
            val value = json.decodeFromJsonElement(serializer, jsonElement.toKotlin())
            DataResult.success(Pair(value, input))
        } catch (e: SerializationException) {
            DataResult.error { "Failed to decode, couldn't deserialize: ${e.message}" }
        } catch (e: IllegalArgumentException) {
            DataResult.error { "Failed to decode, couldn't represent as type: ${e.message}" }
        }
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