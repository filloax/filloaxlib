package com.filloax.fxlib.structure.tracking

import com.mojang.serialization.codecs.RecordCodecBuilder
import com.filloax.fxlib.chunk.isBlockPosInChunk
import com.filloax.fxlib.structure.FXLibStructurePlacementTypes
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType
import java.util.*


/**
 * NOTE: this currently is a "fake" placement type,
 * as it doesn't ever actually get registered and is just mixed
 * in as part of the find structures functions
 */
class FixedStructurePlacement(locateOffset: Vec3i, val pos: BlockPos) : StructurePlacement(locateOffset, FrequencyReductionMethod.DEFAULT, 0f, 1, Optional.empty()) {
    companion object {
        // Ignore base structure placement codec as we don't need half of that
        val CODEC: MapCodec<FixedStructurePlacement> = RecordCodecBuilder.mapCodec { b -> b.group(
            Vec3i.CODEC.fieldOf("locateOffset").forGetter(FixedStructurePlacement::locateOffset),
            BlockPos.CODEC.fieldOf("pos").forGetter(FixedStructurePlacement::pos),
        ).apply(b, ::FixedStructurePlacement) }
    }

    constructor(pos: BlockPos): this(Vec3i.ZERO, pos)

    override fun isPlacementChunk(structureState: ChunkGeneratorStructureState, x: Int, z: Int): Boolean {
        return isBlockPosInChunk(ChunkPos(x, z), pos)
    }

    override fun type(): StructurePlacementType<*> = FXLibStructurePlacementTypes.FIXED
}