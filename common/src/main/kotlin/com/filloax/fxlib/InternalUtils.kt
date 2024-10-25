package com.filloax.fxlib

import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.ApiStatus

/**
 * For internal use in FXLib
 */
@ApiStatus.Internal
object InternalUtils {
    fun resLoc(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(FxLib.MOD_ID, path)
    }

    var shiftDownClientHook: (() -> Boolean)? = null
        private set

    fun clientInitShiftDownHook(producer: () -> Boolean) {
        shiftDownClientHook = producer
    }
}