package com.filloax.fxlib.structure

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Rotation

interface FixablePosition {
    fun setNextPlacePosition(pos: BlockPos)
    val nextPlaceUseY: Boolean
}

interface FixableRotation {
    fun setNextPlaceRotation(rotation: Rotation)
    val defaultRotation: Rotation?
}