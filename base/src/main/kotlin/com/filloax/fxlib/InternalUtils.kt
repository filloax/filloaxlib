package com.filloax.fxlib

import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.ApiStatus

/**
 * For internal use in FXLib
 */
@ApiStatus.Internal
object InternalUtils {
    fun resLoc(path: String): ResourceLocation {
        return ResourceLocation(FxLib.MOD_ID, path)
    }
}