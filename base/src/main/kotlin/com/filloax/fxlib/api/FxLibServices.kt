package com.filloax.fxlib.api

import com.filloax.fxlib.api.codec.CodecCrossVer
import com.filloax.fxlib.api.networking.FxLibNetworking
import com.filloax.fxlib.api.platform.PlatformAbstractions
import com.filloax.fxlib.api.platform.getPlatformAbstractions
import com.filloax.fxlib.api.structure.FixedStructureGeneration
import com.filloax.fxlib.api.structure.getFixedStructureGeneration
import com.filloax.fxlib.api.structure.tracking.CustomPlacedStructureTracker
import net.minecraft.server.level.ServerLevel

object FxLibServices {
    val fixedStructureGeneration: FixedStructureGeneration = getFixedStructureGeneration()
    val codecCross: CodecCrossVer by lazy { CodecCrossVer.inst }
    val platform: PlatformAbstractions = getPlatformAbstractions()
    fun customPlacedStructureTracker(level: ServerLevel) = CustomPlacedStructureTracker.get(level)
    val networking by lazy { FxLibNetworking.inst }
}