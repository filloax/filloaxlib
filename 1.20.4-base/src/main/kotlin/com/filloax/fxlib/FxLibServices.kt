package com.filloax.fxlib

import com.filloax.fxlib.structure.FixedStructureGeneration
import com.filloax.fxlib.structure.getFixedStructureGeneration
import com.filloax.fxlib.structure.tracking.CustomPlacedStructureTracker
import net.minecraft.server.level.ServerLevel

object FxLibServices {
    val fixedStructureGeneration: FixedStructureGeneration = getFixedStructureGeneration()

    fun customPlacedStructureTracker(level: ServerLevel) = CustomPlacedStructureTracker.get(level)
}