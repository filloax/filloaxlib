package com.filloax.fxlib

import net.minecraft.resources.ResourceLocation

/**
 * For internal use in FXLib
 */
object FXLibUtils {
    fun resLoc(path: String): ResourceLocation {
        return ResourceLocation(FxLib.MOD_ID, path)
    }
}