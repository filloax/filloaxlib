package com.filloax.fxlib.api.json

import com.filloax.fxlib.api.codec.decodeJson
import com.filloax.fxlib.api.codec.encodeJson
import com.filloax.fxlib.api.codec.throwableCodecErr
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.levelgen.structure.BoundingBox
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

class ComponentSerializer : KSerializer<Component> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("fxlib.Component", PrimitiveKind.STRING)
    val codec = ComponentSerialization.CODEC

    override fun serialize(encoder: Encoder, value: Component) {
        val json = codec.encodeJson(value)
            .getOrThrow { SerializationException("Couldn't serialize component: $it") }
            .toKotlin()
        encoder.encodeSerializableValue(JsonElement.serializer(), json)
    }

    override fun deserialize(decoder: Decoder): Component {
        val json = decoder.decodeSerializableValue(JsonElement.serializer())
        return codec.decodeJson(json.toGson()).result().map{it.first}.orElseThrow()
    }
}

class ItemByNameSerializer : KSerializer<Item> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("fxlib.ItemByNameSerializer", PrimitiveKind.STRING)


    override fun deserialize(decoder: Decoder): Item {
        val string = decoder.decodeString()
        return BuiltInRegistries.ITEM.byNameCodec()
            .decodeJson(com.google.gson.JsonPrimitive(string))
            .map { it.first }
            .getOrThrow(throwableCodecErr("ItemByNameSerializer.deserialize"))
    }

    override fun serialize(encoder: Encoder, value: Item) {
        val string = BuiltInRegistries.ITEM.byNameCodec()
            .encodeJson(value)
            .map { it.asString }
            .getOrThrow(throwableCodecErr("ItemByNameSerializer.serialize"))
        encoder.encodeString(string)
    }
}

class BlockPosSerializer : KSerializer<BlockPos> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BlockPos") {
        element("x", serialDescriptor<Int>())
        element("y", serialDescriptor<Int>())
        element("z", serialDescriptor<Int>())
    }

    override fun serialize(encoder: Encoder, value: BlockPos) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.x)
            encodeIntElement(descriptor, 1, value.y)
            encodeIntElement(descriptor, 2, value.z)
        }
    }

    override fun deserialize(decoder: Decoder): BlockPos {
        return decoder.decodeStructure(descriptor) {
            var x: Int? = null
            var y: Int? = null
            var z: Int? = null
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> x = decodeIntElement(descriptor, 0)
                    1 -> y = decodeIntElement(descriptor, 1)
                    2 -> z = decodeIntElement(descriptor, 2)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unknown index $index")
                }
            }
            BlockPos(x!!, y!!, z!!)
        }
    }
}

class BoundingBoxSerializer : KSerializer<BoundingBox> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BoundingBox") {
        element("x1", serialDescriptor<Int>())
        element("y1", serialDescriptor<Int>())
        element("z1", serialDescriptor<Int>())
        element("x2", serialDescriptor<Int>())
        element("y2", serialDescriptor<Int>())
        element("z2", serialDescriptor<Int>())
    }

    override fun serialize(encoder: Encoder, value: BoundingBox) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.minX())
            encodeIntElement(descriptor, 1, value.minY())
            encodeIntElement(descriptor, 2, value.minZ())
            encodeIntElement(descriptor, 3, value.maxX())
            encodeIntElement(descriptor, 4, value.maxY())
            encodeIntElement(descriptor, 5, value.maxZ())
        }
    }

    override fun deserialize(decoder: Decoder): BoundingBox {
        return decoder.decodeStructure(descriptor) {
            var x1: Int? = null
            var y1: Int? = null
            var z1: Int? = null
            var x2: Int? = null
            var y2: Int? = null
            var z2: Int? = null
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> x1 = decodeIntElement(descriptor, 0)
                    1 -> y1 = decodeIntElement(descriptor, 1)
                    2 -> z1 = decodeIntElement(descriptor, 2)
                    3 -> x2 = decodeIntElement(descriptor, 3)
                    4 -> y2 = decodeIntElement(descriptor, 4)
                    5 -> z2 = decodeIntElement(descriptor, 5)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unknown index $index")
                }
            }
            BoundingBox(x1!!, y1!!, z1!!, x2!!, y2!!, z2!!)
        }
    }
}