package com.filloax.fxlib.api.savedata

import com.filloax.fxlib.FxLib
import com.filloax.fxlib.SaveDataTypeException
import com.filloax.fxlib.api.codec.decodeNbt
import com.filloax.fxlib.api.codec.encodeNbt
import com.filloax.fxlib.api.codec.throwableCodecErr
import com.google.common.io.Files
import com.mojang.serialization.Codec
import net.minecraft.SharedConstants
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import net.minecraft.world.level.storage.DimensionDataStorage
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

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
 *
 * You can use optional arguments in define to configure on-load behavior:
 * - **beforeLoad**: code to run before loading the data
 * - **checkDeprecatedFilePaths**: list of file paths (relative to the level data folder) to check for the file existence.
 *   If a file is found at that location, and it matches the codec, and no file is present at the current location, it will
 *   be moved there before loading. To be used if you move a file between different versions of the mod.
 *
 * Example:
 * ```kt
 * val DEF = define("YourSaveId", ::Save, CODEC, beforeLoad={ serverLevel ->
 *   // code here
 * })
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
            val factory = makeSavedDataType(definition.id, definition.codec, definition.provider)
            return dataStorage.get(factory) ?: run {
                preLoad(definition, this, dataStorage, factory)
                dataStorage.computeIfAbsent(makeSavedDataType(definition.id, definition.codec, definition.provider))
            }
        }

        /**
         * FxLib - Load specified saved data from the server (using overworld).
         */
        fun <T : FxSavedData<T>> MinecraftServer.loadData(definition: Definition<T>): T {
            return overworld().loadData(definition)
        }

        /**
         * @param beforeLoad Optional code to run before loading the data from file
         * @param checkDeprecatedFilePaths List of file names (without .dat) relative to the dimension data folder to check, see [FxSavedData] javadoc
         */
        fun <T : FxSavedData<T>> define(
            id: String, provider: () -> T, codec: Codec<T>,
            beforeLoad: ((ServerLevel, DimensionDataStorage)->Unit)? = null,
            checkDeprecatedFilePaths: List<String> = listOf(),
        ) = Definition(id, provider, codec, beforeLoad, checkDeprecatedFilePaths)


        private fun <T : FxSavedData<T>> makeSavedDataType(id: String, codec: Codec<T>, provider: () -> T): SavedDataType<T> {
            return SavedDataType(id, provider, codec, DataFixTypes.SAVED_DATA_COMMAND_STORAGE)
        }

        private fun <T : FxSavedData<T>> preLoad(definition: Definition<T>, level: ServerLevel, dataStorage: DimensionDataStorage, savedDataType: SavedDataType<T>) {
            val filePath = dataStorage.getDataFile(definition.id)
            filePath.parent.createDirectory()
            definition.beforeLoad?.invoke(level, dataStorage)
            if (!filePath.exists()) {
                val foundFilePaths = definition.checkDeprecatedFilePaths.filter { checkFile ->
                    val tag = try {
                        dataStorage.readTagFromDisk(
                            checkFile,
                            savedDataType.dataFixType,
                            SharedConstants.getCurrentVersion().dataVersion().version
                        )
                    } catch (e: Exception) {
                        return@filter false
                    }
                    return@filter try {
                        definition.codec.decodeNbt(tag)
                        true
                    } catch (e: Exception) {
                        FxLib.logger.warn("In reading saved data ${definition.id}: found deprecated file $checkFile, but didn't match format")
                        false
                    }
                }
                if (foundFilePaths.size > 1) {
                    FxLib.logger.warn("In reading saved data ${definition.id}: found more than one deprecated file $foundFilePaths, will move first only")
                }
                if (foundFilePaths.isNotEmpty()) {
                    val oldFile = dataStorage.getDataFile(foundFilePaths.first())
                    Files.move(oldFile.toFile(), filePath.toFile())
                    FxLib.logger.warn("In reading saved data ${definition.id}: moved deprecated file $oldFile to $filePath")
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    fun save(compoundTag: CompoundTag, holderLookup: HolderLookup.Provider): CompoundTag {
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
        val beforeLoad: ((ServerLevel, DimensionDataStorage)->Unit)? = null,
        val checkDeprecatedFilePaths: List<String> = listOf(),
    )
}