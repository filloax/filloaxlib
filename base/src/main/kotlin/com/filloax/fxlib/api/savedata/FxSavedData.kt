package com.filloax.fxlib.api.savedata

import com.filloax.fxlib.SaveDataTypeException
import com.filloax.fxlib.api.codec.decodeNbtNullable
import com.filloax.fxlib.api.codec.encodeNbt
import com.filloax.fxlib.api.codec.throwableCodecErr
import com.mojang.serialization.Codec
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.saveddata.SavedData

/**
 * Utility to have a way to save data in levels/servers that
 * does not change with versions.
 *
 * Uses Codecs to standardize serialization. Must use itself (the extending class)
 * as a type argument.
 *
 * Example:
 * ```kt
 * class Save private constructor ([fields...]) : FxSavedData<Save>(CODEC) {
 *
 *     companion object {
 *         val CODEC: Codec<Save> = RecordCodecBuilder.create { builder ->
 *             builder.group(
 *                 //[codec fields...]
 *             ).apply(builder, ::Save)
 *         }
 *         val DEF = define("YourSaveId", ::Save, CODEC)
 *     }
 * }
 * // later...
 * level.loadData(Save.DEF)
 * ```
 */
abstract class FxSavedData<T : FxSavedData<T>>(
    private val codec: Codec<T>
) : SavedData() {
    init {
        assertType()
    }

    companion object {
        /**
         * FxLib - Load specified saved data from the level.
         */
        fun <T : FxSavedData<T>> ServerLevel.loadData(definition: Definition<T>): T {
            return dataStorage.computeIfAbsent(makeVanillaFactory(definition.codec, definition.provider), definition.id)
        }

        /**
         * FxLib - Load specified saved data from the server (using overworld).
         */
        fun <T : FxSavedData<T>> MinecraftServer.loadData(definition: Definition<T>): T {
            return overworld().loadData(definition)
        }

        fun <T : FxSavedData<T>> define(id: String, provider: () -> T, codec: Codec<T>) =
            Definition(id, provider, codec)


        private fun <T : FxSavedData<T>> makeVanillaFactory(codec: Codec<T>, provider: () -> T): Factory<T> {
            return Factory(provider, { compoundTag, _ ->
                codec.decodeNbtNullable(compoundTag) ?: provider()
            }, DataFixTypes.SAVED_DATA_COMMAND_STORAGE)
        }
    }

    @SuppressWarnings("unchecked")
    override fun save(compoundTag: CompoundTag, holderLookup: HolderLookup.Provider): CompoundTag {
        return codec.encodeNbt(this as T).getOrThrow(throwableCodecErr("fxSavedData")) as CompoundTag
    }

    @SuppressWarnings("unchecked")
    private fun assertType() {
        try {
            val tmp = this as T
        } catch (e: ClassCastException) {
            throw SaveDataTypeException(e)
        }
    }

    class Definition<T : FxSavedData<T>> constructor(
        val id: String,
        val provider: () -> T,
        val codec: Codec<T>,
    )
}