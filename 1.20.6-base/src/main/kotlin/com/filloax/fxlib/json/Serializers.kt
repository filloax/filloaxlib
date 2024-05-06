package com.filloax.fxlib.json

import com.filloax.fxlib.codec.decodeJson
import com.filloax.fxlib.codec.encodeJson
import com.filloax.fxlib.codec.simpleCodecErr
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Rotation
import java.lang.IllegalStateException

/**
 * Based on Kotlin Serialization API, check the base class KSerializer for doc on the methods
 * and LongAsStringSerializer to see what I took for reference
 */
class RotationSerializer : KSerializer<Rotation> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("fxlib.RotationSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Rotation {
        val string = decoder.decodeString()
        return Rotation.entries.find { it.serializedName == string } ?: throw IllegalStateException("Wrong rotation $string")
    }

    override fun serialize(encoder: Encoder, value: Rotation) {
        encoder.encodeString(value.serializedName)
    }
}

class SimpleComponentSerializer : KSerializer<Component> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("fxlib.SimpleComponentSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Component {
        val string = decoder.decodeString()
        return Component.translatable(string)
    }

    override fun serialize(encoder: Encoder, value: Component) {
        encoder.encodeString(value.string)
    }
}

class ItemByNameSerializer : KSerializer<Item> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("fxlib.ItemByNameSerializer", PrimitiveKind.STRING)


    override fun deserialize(decoder: Decoder): Item {
        val string = decoder.decodeString()
        return BuiltInRegistries.ITEM.byNameCodec()
            .decodeJson(com.google.gson.JsonPrimitive(string))
            .map { it.first }
            .getOrThrow(false, simpleCodecErr("ItemByNameSerializer.deserialize"))
    }

    override fun serialize(encoder: Encoder, value: Item) {
        val string = BuiltInRegistries.ITEM.byNameCodec()
            .encodeJson(value)
            .map { it.asString }
            .getOrThrow(false, simpleCodecErr("ItemByNameSerializer.serialize"))
        encoder.encodeString(string)
    }
}