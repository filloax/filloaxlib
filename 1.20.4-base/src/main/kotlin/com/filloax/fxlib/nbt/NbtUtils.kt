package com.filloax.fxlib.nbt

import com.filloax.fxlib.FxLib
import com.mojang.serialization.Codec
import net.minecraft.nbt.*
import java.lang.ClassCastException
import kotlin.reflect.KMutableProperty0


fun CompoundTag.putIfAbsent(key: String, tag: Tag): Tag? {
    if (!this.contains(key)) {
        return this.put(key, tag)
    }
    return null
}

/**
 * Get compound tag at key, or null if there is no
 * tag there. Normal behavior creates a new empty tag without
 * putting it.
 */
fun CompoundTag.getCompoundOrNull(key: String): CompoundTag? {
    if (this.contains(key)) {
        return this.getCompound(key)
    }
    return null
}

/**
 * Get list tag at key, or null if there is no
 * tag there. Normal behavior creates a new empty tag without
 * putting it.
 */
fun CompoundTag.getListOrNull(key: String, type: Byte): ListTag? {
    if (this.contains(key)) {
        return this.getList(key, type.toInt())
    }
    return null
}

inline fun <reified T : Tag> CompoundTag.getOrPut(key: String, default: T): T {
    var value = this.get(key)
    if (value == null) {
        value = default.copy() as T
        this.put(key, value)
    } else if (value !is T) {
        throw ClassCastException("Existing tag is of wrong type! ${value.javaClass}")
    }
    return value
}

/**
 * Convenient way to load values from NBT using a codec.
 * Will not load if key is not present.
 */
fun <T> CompoundTag.loadField(name: String, codec: Codec<T>, setter: (T) -> Unit) {
    val value = loadField(name, codec)
    if (value != null) {
        setter(value)
    }
}

fun <T> CompoundTag.loadField(name: String, codec: Codec<T>, property: KMutableProperty0<T>) =
    loadField(name, codec, property.setter)

/**
 * Convenient way to load values from NBT using a codec.
 * Will return null if key is not present.
 */
fun <T> CompoundTag.loadField(name: String, codec: Codec<T>): T? {
    return this.get(name)?.let{ tag ->
        try {
            codec.decode(NbtOps.INSTANCE, tag).getOrThrow(false) {
                FxLib.logger.error("Error in decoding $name: $it")
            }.first
        } catch (e: Exception) {
            FxLib.logger.error("Exception while decoding $name: ${e.stackTraceToString()}")
            null
        }
    }
}

/**
 * Convenient way to save values to NBT using a codec.
 * Will silently avoid saving if value is null.
 */
fun <T> CompoundTag.saveField(name: String, codec: Codec<T>, getter: () -> T?) {
    val value = getter()
    if (value != null) {
        try {
            this.put(name, codec.encodeStart(NbtOps.INSTANCE, getter()).getOrThrow(true) {
                FxLib.logger.error("Error in encoding $name: $it")
            })
        } catch (e: Exception) {
            FxLib.logger.error("Exception while encoding $name: ${e.stackTraceToString()}")
        }
    }
}

fun <T : Tag> listTag(from: List<T>): ListTag {
    return ListTag().also { it.addAll(from) }
}

fun String.toTag(): StringTag {
    return StringTag.valueOf(this)
}

fun Int.toTag(): IntTag {
    return IntTag.valueOf(this)
}

fun Long.toTag(): LongTag {
    return LongTag.valueOf(this)
}