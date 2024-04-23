package com.filloax.fxlib.structure

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Rotation

interface FixablePosition {
    /**
     * Force the next placement of this structure to be at the specified position
     * (as structure generation is often done automatically). If useY is null (default),
     * will use the setting placed in the structure data json.
     */
    fun setNextPlacePosition(pos: BlockPos, useY: Boolean?)
    /**
     * Force the next placement of this structure to be at the specified position
     * (as structure generation is often done automatically). If useY is null (default),
     * will use the setting placed in the structure data json.
     */
    fun setNextPlacePosition(pos: BlockPos) { setNextPlacePosition(pos, null) }
    val nextPlaceUseY: Boolean
}

interface FixableRotation {
    fun setNextPlaceRotation(rotation: Rotation)
    val defaultRotation: Rotation?
}