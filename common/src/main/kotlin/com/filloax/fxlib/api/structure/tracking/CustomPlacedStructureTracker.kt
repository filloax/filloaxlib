package com.filloax.fxlib.api.structure.tracking

import com.filloax.fxlib.FxLib
import com.filloax.fxlib.InternalUtils.resLoc
import com.filloax.fxlib.api.chunk.boundBoxChunkRange
import com.filloax.fxlib.api.nbt.getCompoundOrNull
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.longs.LongSet
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureStart
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import java.lang.IllegalStateException

// TODO: CLASS EDITED JUST FOR COMPILING, ORIGINAL CODE IS IN ////, MUST BE ACTUALLY FIXED BY USING SavedDataType correctly

/**
 * Allows manually tracking structures spawned outside of normal world gen,
 * and uses mixin to make them interact with /locate, achievements, etc.
 */
////class CustomPlacedStructureTracker private constructor(val level: ServerLevel) : SavedData() {

//justforcompile
class CustomPlacedStructureTracker() : SavedData() {
    var level: ServerLevel? = null

    constructor(a: Long) : this() {
        lastReference = a
    }
//endjustforcompile

    val structureData = mutableMapOf<Long, PlacedStructureData>()

    // chunk positions (long) of starting chunks of the structures that pass from each chunk key, as in base game chunk structureRefs
    val chunkStructureRefs: Map<Long, Map<Structure, LongSet>>
        get() = _chunkStructureRefs

    private val _chunkStructureRefs = mutableMapOf<Long, MutableMap<Structure, LongSet>>()
    private val structureDataByChunk = mutableMapOf<Long, MutableList<PlacedStructureData>>()
    private val structureDataByStartChunk = mutableMapOf<Long, MutableList<PlacedStructureData>>()
    private val byStructure = mutableMapOf<Structure, MutableList<PlacedStructureData>>()
    private val inverseMap = mutableMapOf<PlacedStructureData, Long>()
    // Unlike normal structure references, this doesn't refer to the starting chunk
    // but is just a unique id, allowing for repeats
    private var lastReference = 0L

    companion object {
////        private val factory = { level: ServerLevel ->
////            Factory(
////                { CustomPlacedStructureTracker(level) }, { tag, _ -> load(tag, level) },
////                DataFixTypes.STRUCTURE
////            )
////        }

//justforcompile
        val CODEC: Codec<CustomPlacedStructureTracker> = RecordCodecBuilder.create {
            instance -> instance.group(
                Codec.LONG.fieldOf("s").forGetter(CustomPlacedStructureTracker::lastReference)
            ).apply(instance, ::CustomPlacedStructureTracker)
        }

        val TYPE = SavedDataType<CustomPlacedStructureTracker>(
            resLoc("fxlib_structure_placement_tracking"),
            ::CustomPlacedStructureTracker,
            CODEC,
            DataFixTypes.STRUCTURE
        )
//endjustforcompile

        @JvmStatic
        fun get(level: ServerLevel): CustomPlacedStructureTracker {
            ///return level.dataStorage.computeIfAbsent(factory(level), "fxlib_structure_placement_tracking")
//justforcompile
            return level.dataStorage.computeIfAbsent(TYPE)
//endjustforcompile
        }

        private fun load(compoundTag: CompoundTag, level: ServerLevel): CustomPlacedStructureTracker {
            ////val out = CustomPlacedStructureTracker(level)
//justforcompile
            val out = CustomPlacedStructureTracker()
            out.level = level
//endjustforcompile
            val ctx = StructurePieceSerializationContext.fromLevel(level)
            compoundTag.getCompoundOrNull("spawnedStructureData")?.let { tag ->
                tag.keySet().forEach { idStr ->
                    val id = idStr.toLong()
                    val data = PlacedStructureData.load(tag.getCompound(idStr).get(), ctx, level)
                    out.structureData[id] = data

                    out.cacheData(data, id)
                }
            }
            out.lastReference = compoundTag.getLong("references").get()
            return out
        }
    }

    //// was override
    fun save(compoundTag: CompoundTag, holderLookup: HolderLookup.Provider): CompoundTag {
        val ctx = StructurePieceSerializationContext.fromLevel(level!!)
        compoundTag.put("spawnedStructureData", CompoundTag().also { tag ->
            structureData.forEach { (refId, data) ->
                tag.put(refId.toString(), data.save(ctx))
            }
        })
        compoundTag.putLong("references", lastReference)
        return compoundTag
    }

    fun getByChunkPos(chunkPos: ChunkPos, startChunkOnly:Boolean = false): List<PlacedStructureData> {
        return if (startChunkOnly) {
            structureDataByStartChunk[chunkPos.pack()]
        } else {
            structureDataByChunk[chunkPos.pack()]
        } ?: listOf()
    }

    /**
     * Get all PlacedStructureData at chunkPos AND for specific structure
     */
    fun getStructuresAtChunkPos(chunkPos: ChunkPos, structure: Structure, startChunkOnly:Boolean = false): List<PlacedStructureData> {
        return getByChunkPos(chunkPos, startChunkOnly).filter { it.structure == structure }
    }

    fun getByPos(blockPos: BlockPos): List<PlacedStructureData> {
        return getByChunkPos(ChunkPos(SectionPos.blockToSectionCoord(blockPos.x), SectionPos.blockToSectionCoord(blockPos.z))).filter { it.structureStart.boundingBox.isInside(blockPos) }
    }

    fun getByStructure(structure: Structure): List<PlacedStructureData> {
        return byStructure[structure] ?: listOf()
    }

    fun getByStructure(key: ResourceKey<Structure>): List<PlacedStructureData> {
        val structure = level!!.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValueOrThrow(key)
        return byStructure[structure] ?: listOf()
    }

    fun registerStructure(structureStart: StructureStart, pos: BlockPos): Long {
        val id = lastReference
        lastReference++

//        val structureManager = level.structureManager()
//        val chunkRef = structureStart.chunkPos.pack()

        val data = PlacedStructureData(structureStart, pos)

        structureData[id] = data
//        boundBoxChunkRange(structureStart.boundingBox).forEach {
//            // Vanilla (ChunkGenerator class) adds a reference in each chunk for all structures
//            // that cross into it, the reference being the longified ChunkPos of the starting chunks
//            // of the other structures; we do the inverse after the fact, so we add reference to our chunk
//            // to crossed chunks
//            // This is to allow compatible behavior with the mixins
//            val chunk = level.getChunk(it.x, it.z)
//            structureManager.addReferenceForStructure(SectionPos.of(pos), structureStart.structure, chunkRef, chunk)
//            chunk.isUnsaved = true
//        }

        cacheData(data, id)

        setDirty()

        return id
    }

    private fun cacheData(data: PlacedStructureData, id: Long) {
        val start = data.structureStart
        val chunkRef = data.chunkRef
        val structure = data.structure

        inverseMap[data] = id
        byStructure.computeIfAbsent(structure) { mutableListOf() }.add(data)
        boundBoxChunkRange(start.boundingBox).forEach {
            structureDataByChunk.computeIfAbsent(it.pack()) { mutableListOf() }
                .add(data)
            _chunkStructureRefs.computeIfAbsent(it.pack()) { mutableMapOf() }
                .computeIfAbsent(structure) { LongOpenHashSet() }
                .add(chunkRef)
        }
        structureDataByStartChunk.computeIfAbsent(chunkRef) { mutableListOf() }
            .add(data)
    }
}

data class PlacedStructureData(
    val structureStart: StructureStart,
    val pos: BlockPos,
) {
    val structure: Structure = structureStart.structure
    val placement = FixedStructurePlacement(pos)
    val chunkRef = structureStart.chunkPos.pack()

    fun save(ctx: StructurePieceSerializationContext): CompoundTag {
        return CompoundTag().also { tag ->
            tag.put("StructureStart", structureStart.createTag(ctx, structureStart.chunkPos))
            tag.put("BlockPos", BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).orThrow)
        }
    }

    companion object {
        fun load(tag: CompoundTag, ctx: StructurePieceSerializationContext, level: ServerLevel): PlacedStructureData {
            return PlacedStructureData(
                StructureStart.loadStaticStart(ctx, tag.getCompound("StructureStart").get(), level.seed)
                    ?: throw IllegalStateException("Couldn't load structure start from $tag"),
                BlockPos.CODEC.decode(NbtOps.INSTANCE, tag.get("BlockPos")).getOrThrow {
                    Exception("Error in decoding BlockPos: $it")
                }.first,
            )
        }
    }
}