package com.filloax.fxlib

import com.filloax.fxlib.codec.CodecCrossVer
import com.filloax.fxlib.platform.PlatformAbstractions
import com.filloax.fxlib.platform.ServiceUtil
import com.filloax.fxlib.platform.getPlatformAbstractions
import com.filloax.fxlib.structure.FixedStructureGeneration
import com.filloax.fxlib.structure.getFixedStructureGeneration
import com.filloax.fxlib.structure.tracking.CustomPlacedStructureTracker
import net.minecraft.server.level.ServerLevel

object FxLibServices {
    val fixedStructureGeneration: FixedStructureGeneration = getFixedStructureGeneration()
    val codecCross: CodecCrossVer = ServiceUtil.findService(CodecCrossVer::class.java)
    val platform: PlatformAbstractions = getPlatformAbstractions()
    fun customPlacedStructureTracker(level: ServerLevel) = CustomPlacedStructureTracker.get(level)
}