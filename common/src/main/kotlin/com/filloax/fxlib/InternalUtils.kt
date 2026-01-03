package com.filloax.fxlib

import net.minecraft.resources.Identifier
import org.jetbrains.annotations.ApiStatus

/**
 * For internal use in FXLib
 */
@ApiStatus.Internal
object InternalUtils {
    // todo: rename to match the change from ResourceLocation to Identifier in 1.21.11
    fun resLoc(path: String): Identifier {
        return Identifier.fromNamespaceAndPath(FxLib.MOD_ID, path)
    }

    var shiftDownClientHook: (() -> Boolean)? = null
        private set

    fun clientInitShiftDownHook(producer: () -> Boolean) {
        shiftDownClientHook = producer
    }
}